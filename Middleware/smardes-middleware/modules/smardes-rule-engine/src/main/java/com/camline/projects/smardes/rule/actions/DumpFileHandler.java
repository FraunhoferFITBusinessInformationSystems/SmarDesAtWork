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
package com.camline.projects.smardes.rule.actions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.camline.projects.smardes.common.collections.PropertyUtils;
import com.camline.projects.smardes.rule.RuleGroupContext;
import com.camline.projects.smartdev.ruledef.NameValueExpressionType;
import com.camline.projects.smartdev.ruledef.RuleType.Actions.DumpFile;

public class DumpFileHandler extends RuleActionHandler {
	private static final Logger logger = LoggerFactory.getLogger(DumpFileHandler.class);
	private static final File TEMPLATES_DIR = new File(PropertyUtils.CONFIG_DIR, "templates");
	private static final String DUMP_DIR = "msgdump";

	public DumpFileHandler(final RuleGroupContext ruleGroupContext) {
		super(ruleGroupContext);
	}

	public void execute(final DumpFile dumpFile) {
		final VelocityContext velocityContext = new VelocityContext();
		for (NameValueExpressionType contextEntry : dumpFile.getContext().getEntry()) {
			Object value = expressionHandler.extractValue(contextEntry);
			velocityContext.put(contextEntry.getName(), value);
		}

		final File templateFile = new File(TEMPLATES_DIR, dumpFile.getTemplateName() + ".vm");
		final String destFile = expressionHandler.evaluateExpression(dumpFile.getDestinationFile(), String.class);

		logger.info("Dump message into file {} using velocity template {}", destFile, templateFile);

		ruleGroupContext.getRuleEngineContext().getSingletonWorker()
				.submit(() -> dumpFile(dumpFile, velocityContext, templateFile, destFile));
	}

	private static void dumpFile(final DumpFile dumpFile, final VelocityContext velocityContext, final File templateFile,
			final String destFile) {
		final File dumpDir = new File(DUMP_DIR);
		dumpDir.mkdirs();
		final File targetFile = new File(dumpDir, destFile);
		velocityContext.put("newFile", Boolean.valueOf(!targetFile.exists()));
		velocityContext.put("temporalTool", new TemporalTool());


		Charset charset;
		try {
			charset = Charset.forName(dumpFile.getEncoding());
		} catch (final IllegalCharsetNameException e) {
			logger.warn("Unknown dumpFile encoding " + dumpFile.getEncoding() + ". Fall back to UTF-8.", e);
			charset = StandardCharsets.UTF_8;
		}

		try (InputStream is = new FileInputStream(templateFile);
				InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
				FileOutputStream fos = new FileOutputStream(targetFile, dumpFile.isAppend());
				OutputStreamWriter osw = new OutputStreamWriter(fos, charset)) {
			final VelocityEngine ve = new VelocityEngine();
			ve.evaluate(velocityContext, osw, "velocity", reader);

			logger.info("Dumped message to file {}.", targetFile);
		} catch (final IOException e) {
			logger.error("Problems in dumping message to file", e);
		}
	}

	public static class TemporalTool {
		public String format(final Temporal temporal, final String pattern) {
			final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
			return formatter.format(temporal);
		}
	}
}
