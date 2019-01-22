/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.integration;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import android.util.Log;

import net.lachlanmckee.timberjunit.TimberTestRule;

import org.junit.Rule;
import org.junit.rules.TestRule;

import java.util.concurrent.atomic.AtomicBoolean;

import de.vogler_engineering.smartdevicesapp.model.mock.TimberAssertingOnErrorTestRule;

public abstract class AbstractIntegrationTest {

    @Rule
    public TimberTestRule timberTestRule = TimberTestRule.builder()
            .showThread(true)
            .showTimestamp(true)
            .onlyLogWhenTestFails(false)
            .build();

    @Rule
    public TestRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Rule
    public TimberAssertingOnErrorTestRule assertingOnErrorTestRule = TimberAssertingOnErrorTestRule
            .builder()
            .minAssertPriority(Log.ERROR)
            .build();


    protected boolean WaitForAsyncToFinish(int timeoutMs, AtomicBoolean finished) {
        long start = System.currentTimeMillis();
        boolean timeout = false;
        while(finished.get() && !timeout){

            long elapsed = System.currentTimeMillis() - start;
            if((elapsed/1000) > timeoutMs){
                timeout = true;
            }
        }
        return !timeout;
    }
}
