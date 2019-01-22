/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.testsuites;

import org.junit.experimental.categories.Categories;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import de.vogler_engineering.smartdevicesapp.common.test.categories.SlowTests;
import de.vogler_engineering.smartdevicesapp.model.unittests.TabRepositoryUnitTest;
import de.vogler_engineering.smartdevicesapp.model.unittests.TestEnvironmentUnitTest;


@RunWith(Categories.class)
@Categories.ExcludeCategory(SlowTests.class)
@Suite.SuiteClasses( {
        TestEnvironmentUnitTest.class,
        TabRepositoryUnitTest.class })
public class UnitTestSuite {}