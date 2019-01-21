/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.layouts;

import de.vogler_engineering.smartdevicesapp.viewelements.components.base.ConfigurableDataHelper;

public interface LayoutViewModelReference extends ViewModelReference {
    ConfigurableDataHelper getDataHelper();
    String getContextDomain();
}
