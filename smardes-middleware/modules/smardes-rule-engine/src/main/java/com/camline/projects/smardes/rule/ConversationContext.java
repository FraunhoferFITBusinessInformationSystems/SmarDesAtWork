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

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.json.bind.annotation.JsonbTransient;

import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.camline.projects.smardes.jsonapi.el.ELMessage;
import com.camline.projects.smardes.rule.ConversationManager.State;
import com.camline.projects.smartdev.ruledef.RuleType.Actions.StartConversation;

public class ConversationContext {
	private static final Logger logger = LoggerFactory.getLogger(ConversationContext.class);

	private final UUID uuid;
	private final ELMessage triggerMessage;
	@JsonbTransient
	private final StartConversation startConversation;
	private final NamedConditions namedConditions;
	private final Map<String, Object> ctx;
	private final ConversationManager conversationManager;

	public ConversationContext(final ELMessage triggerMessage, final StartConversation startConversation,
			final NamedConditions namedConditions) {
		this.uuid = UUID.randomUUID();
		this.triggerMessage = triggerMessage;
		this.startConversation = startConversation;
		this.namedConditions = namedConditions;
		this.ctx = new HashMap<>();
		this.conversationManager = new ConversationManager(startConversation.getName(),
				startConversation.getRequestMessage() != null && startConversation.getRequestMessage().isParallel());
	}

	public String getId() {
		return uuid.toString();
	}

	public String getName() {
		return startConversation.getName();
	}

	public UUID getUuid() {
		return uuid;
	}

	public ELMessage getTriggerMessage() {
		return triggerMessage;
	}

	public StartConversation getStartConversation() {
		return startConversation;
	}

	public NamedConditions getNamedConditions() {
		return namedConditions;
	}

	public Map<String, Object> getCtx() {
		return ctx;
	}

	/*
	 * Keep these for EL convenience
	 */
	public State getState() {
		return conversationManager.getState();
	}

	public String getCurrentAssignee() {
		return conversationManager.getCurrentAssignee();
	}

	public List<String> getUsers() {
		return conversationManager.getUsers();
	}

	public ConversationManager getConversationManager() {
		return conversationManager;
	}

	public List<Triple<State, OffsetDateTime, String>> getProtocol() {
		return conversationManager.getProtocol();
	}

	public static class ContextVars extends HashMap<String, Object> {
		private static final long serialVersionUID = 1L;

		@SuppressWarnings("unlikely-arg-type")
		@Override
		public Object get(final Object key) {
			final Object value = super.get(key);
			if (value == null && !containsKey(key)) {
				logger.warn("Conversation context variable {} does not exist.", key);
				return null;
			}
			return value;
		}
	}
}
