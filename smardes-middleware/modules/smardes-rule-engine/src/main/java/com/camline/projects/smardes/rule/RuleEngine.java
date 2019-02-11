/*******************************************************************************
 * Copyright (C) 2018-2019 camLine GmbH
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
package com.camline.projects.smardes.rule;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.jms.JMSContext;
import javax.xml.bind.JAXBException;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.camline.projects.smardes.common.AbstractSDService;
import com.camline.projects.smardes.common.collections.PropertyUtils;
import com.camline.projects.smardes.common.io.FullDirectoryMonitor;
import com.camline.projects.smardes.common.jaxb.JAXBUtils;
import com.camline.projects.smardes.jsonapi.SmarDesException;
import com.camline.projects.smartdev.ruledef.Rules;
import com.camline.projects.smartdev.ruledef.Rules.GlobalVariables;
import com.camline.projects.smartdev.ruledef.Rules.PatchMessageBodyPrototypes;
import com.camline.projects.smartdev.ruledef.Rules.RuleGroup;

public class RuleEngine extends AbstractSDService {
	private static final Logger logger = LoggerFactory.getLogger(RuleEngine.class);

	private static final int DIR_MONITOR_INTERVAL = 20;

	private final ConversationContextRepository conversationContextRepository;
	private final ScheduledExecutorService configurationMonitor;
	private final ScheduledExecutorService conversationMonitor;
	private final ExecutorService singletonWorker;

	public RuleEngine(final JMSContext baseJMSContext) {
		super(baseJMSContext, true, true);
		this.conversationContextRepository = new ConversationContextRepository();
		this.configurationMonitor = Executors.newSingleThreadScheduledExecutor(
				new BasicThreadFactory.Builder().namingPattern("rulesMonitor").build());
		this.conversationMonitor = Executors.newScheduledThreadPool(1,
				new BasicThreadFactory.Builder().namingPattern("conversationMonitor-%d").build());
		this.singletonWorker = Executors
				.newSingleThreadExecutor(new BasicThreadFactory.Builder().namingPattern("singletonWorker").build());
	}

	@Override
	public void startup() {
		final RulesDirectoryMonitor rulesConfigManager = new RulesDirectoryMonitor();
		rulesConfigManager.runImmediateThenScheduled(configurationMonitor, DIR_MONITOR_INTERVAL);
	}

	public void performConfigurationChange(final File file) throws IOException, JAXBException {
		logger.info("New version of rules.xml detected.");
		performConfigurationChange(JAXBUtils.unmarshal(Rules.class, file));
	}

	private void performConfigurationChange(final Rules rules) {
		if (rules.accept(new ConfigurationChecker()) != Boolean.TRUE) {
			throw new SmarDesException("Error in rule configuration");
		}

		final RuleEngineContext ruleEngineContext = new RuleEngineContext(conversationContextRepository,
				getServiceJMSContext(), getOutgoingJMSContext(), conversationMonitor, singletonWorker, rules);

		shutdownMessageHandlers();

		for (final RuleGroup ruleGroup : rules.getRuleGroup()) {
			final RuleGroupHandler ruleHandler = new RuleGroupHandler(ruleEngineContext, ruleGroup);
			ruleHandler.createConsumer();
			addMessageHandler(ruleHandler);
		}

		logger.info("Rule Engine with all background handlers started.");
	}

	@Override
	public void shutdown() {
		if (configurationMonitor != null) {
			configurationMonitor.shutdownNow();
		}
		if (conversationMonitor != null) {
			conversationMonitor.shutdownNow();
		}
		if (singletonWorker != null) {
			singletonWorker.shutdown();
			try {
				final boolean terminated = singletonWorker.awaitTermination(10, TimeUnit.SECONDS);
				if (!terminated) {
					logger.warn("singletonWorker.awaitTermination took longer than 10 seconds - some tasks might not been executed");
				}
			} catch (@SuppressWarnings("unused") final InterruptedException e) {
				logger.warn("singletonWorker.awaitTermination interrupted - this should not happen");
				Thread.currentThread().interrupt();
			}
		}

		super.shutdown();
		logger.info("Rule Engine with all background handlers stopped.");
	}

	private class RulesDirectoryMonitor extends FullDirectoryMonitor {
		public RulesDirectoryMonitor() {
			super(new File(PropertyUtils.CONFIG_DIR, "rules"));
		}

		@Override
		public boolean accept(final File dir, final String name) {
			return name.endsWith(".xml");
		}

		@Override
		protected void processDirectory() throws IOException {
			try {
				Rules rules = mergeRuleFiles();
				performConfigurationChange(rules);
			} catch (final JAXBException e) {
				throw new SmarDesException("Problems in parsing rules.xml", e);
			}
		}

		private Rules mergeRuleFiles() throws IOException, JAXBException {
			final Rules mergedRules = new Rules();
			mergedRules.setGlobalVariables(new GlobalVariables());
			mergedRules.setPatchMessageBodyPrototypes(new PatchMessageBodyPrototypes());

			Map<Triple<String, String, Boolean>, RuleGroup> ruleGroupsBySelector = new HashMap<>();

			for (File file : directory.listFiles(this)) {
				Rules rules = JAXBUtils.unmarshal(Rules.class, file);
				mergeGlobalVariables(mergedRules, rules);
				mergePatchMessagePrototypes(mergedRules, rules);
				mergeRuleGroups(mergedRules, rules, ruleGroupsBySelector);
			}


			return mergedRules;
		}

		private void mergeGlobalVariables(final Rules mergedRules, Rules rules) {
			if (rules.getGlobalVariables() == null) {
				return;
			}

			mergedRules.getGlobalVariables().getProperty().addAll(rules.getGlobalVariables().getProperty());
		}

		private void mergePatchMessagePrototypes(final Rules mergedRules, Rules rules) {
			if (rules.getPatchMessageBodyPrototypes() == null) {
				return;
			}

			mergedRules.getPatchMessageBodyPrototypes().getPatchMessageBodyPrototype()
					.addAll(rules.getPatchMessageBodyPrototypes().getPatchMessageBodyPrototype());
		}

		private void mergeRuleGroups(final Rules mergedRules, Rules rules,
				Map<Triple<String, String, Boolean>, RuleGroup> ruleGroupsBySelector) {
			for (RuleGroup ruleGroup : rules.getRuleGroup()) {
				Triple<String, String, Boolean> key = Triple.of(ruleGroup.getSelector().getAddress(),
						ruleGroup.getSelector().getCriteria(), Boolean.valueOf(ruleGroup.getSelector().isJsonBody()));

				RuleGroup existingRuleGroup = ruleGroupsBySelector.get(key);
				if (existingRuleGroup != null) {
					existingRuleGroup.getRuleOrRuleSet().addAll(ruleGroup.getRuleOrRuleSet());
				} else {
					ruleGroupsBySelector.put(key, ruleGroup);
					mergedRules.getRuleGroup().add(ruleGroup);
				}
			}
		}
	}
}
