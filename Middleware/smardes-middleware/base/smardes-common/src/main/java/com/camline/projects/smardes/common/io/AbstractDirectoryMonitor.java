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
package com.camline.projects.smardes.common.io;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Collection;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.camline.projects.smardes.common.concurrent.CatchingRunnable;

public abstract class AbstractDirectoryMonitor implements Runnable, FilenameFilter {
	protected final File directory;

	public AbstractDirectoryMonitor(File directory) {
		this.directory = directory;
	}

	public void runImmediateThenScheduled(final ScheduledExecutorService executorService, final long delay) {
		CatchingRunnable catchingRunnable = new CatchingRunnable(this);
		catchingRunnable.run();
		executorService.scheduleWithFixedDelay(new CatchingRunnable(this), delay, delay, TimeUnit.SECONDS);
	}

	public static void runImmediateThenScheduled(final ScheduledExecutorService executorService, final long delay,
			final Collection<Runnable> runnables) {
		final Runnable composite = () -> runnables.forEach(Runnable::run);
		final CatchingRunnable catchingRunnable = new CatchingRunnable(composite);
		catchingRunnable.run();
		executorService.scheduleWithFixedDelay(catchingRunnable, delay, delay, TimeUnit.SECONDS);
	}
}
