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
package com.camline.projects.smardes.common.concurrent;

import java.lang.Thread.UncaughtExceptionHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This Runnable decorator catches all exceptions. It can be used for
 * ScheduledExecutors that simply stop re-executing their runnables after they
 * raised an exeption.
 *
 * @author matze
 *
 */
public class CatchingRunnable implements Runnable {
	private final Runnable realRunnable;
    private final UncaughtExceptionHandler exceptionHandler;

    public CatchingRunnable(Runnable realRunnable) {
    	this(realRunnable, new UncaughtExceptionLogger());
    }

    public CatchingRunnable(Runnable realRunnable, UncaughtExceptionHandler exceptionHandler) {
    	this.realRunnable = realRunnable;
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    public void run() {
        try {
            realRunnable.run();
        } catch (RuntimeException e) {
            exceptionHandler.uncaughtException(Thread.currentThread(), e);
        }
    }

    public static class UncaughtExceptionLogger implements UncaughtExceptionHandler {
    	private static final Logger logger = LoggerFactory.getLogger(UncaughtExceptionLogger.class);

		@Override
		public void uncaughtException(Thread t, Throwable e) {
			logger.error("Runnable stopped with uncaught exception", e);
		}
    }
}
