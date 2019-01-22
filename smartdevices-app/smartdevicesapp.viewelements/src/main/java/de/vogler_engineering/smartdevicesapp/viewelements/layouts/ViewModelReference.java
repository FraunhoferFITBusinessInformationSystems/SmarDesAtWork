/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.layouts;

import de.vogler_engineering.smartdevicesapp.viewelements.ui.manager.ActivityLoadingState;
import io.reactivex.disposables.Disposable;

public interface ViewModelReference {
    void postLoadingState(ActivityLoadingState state);
    ActivityLoadingState getLoadingState();
    void finishActivity();
    void addDisposable(Disposable disposable);
    void updateData();
}