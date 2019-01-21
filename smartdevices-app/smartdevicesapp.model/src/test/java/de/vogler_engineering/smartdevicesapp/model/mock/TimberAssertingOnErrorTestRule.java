/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.mock;

import android.util.Log;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import timber.log.Timber;

import static junit.framework.Assert.assertTrue;

public class TimberAssertingOnErrorTestRule implements TestRule {
    private final TestRuleParams params;

    public TimberAssertingOnErrorTestRule(TestRuleParams testRuleParams) {
        this.params = testRuleParams;
    }

    public static final class TestRuleParams{
        private int minAssertPriority;

        public TestRuleParams() {
            this.minAssertPriority = Log.ERROR;
        }

        public TestRuleParams minAssertPriority(int minAssertPriority) {
            this.minAssertPriority = minAssertPriority;
            return this;
        }

        public TimberAssertingOnErrorTestRule build() {
            return new TimberAssertingOnErrorTestRule(this);
        }
    }

    public static TestRuleParams builder() {
        return new TestRuleParams();
    }

    @Override
    public Statement apply(Statement base, Description description) {
        return new TimberStatement(base, params);
    }

    private static class TimberStatement extends Statement {
        private final Statement mNext;
        private final AssertOnErrorTree mTree;

        TimberStatement(Statement base, TestRuleParams params) {
            mNext = base;
            mTree = new AssertOnErrorTree(params);
        }

        @Override
        public void evaluate() throws Throwable {
            Timber.plant(mTree);
            try {
                mNext.evaluate();
            } catch (Throwable t) {
                throw t;
            } finally {
                // Ensure the tree is removed to avoid duplicate logging.
                Timber.uproot(mTree);
            }
        }
    }

    private static class AssertOnErrorTree extends Timber.DebugTree {

        private TestRuleParams params;

        public AssertOnErrorTree(TestRuleParams params) {
            this.params = params;
        }

        @Override
        protected void log(int priority, String tag, String message, Throwable t) {
            assertTrue(message, priority < params.minAssertPriority);
        }
    }

}
