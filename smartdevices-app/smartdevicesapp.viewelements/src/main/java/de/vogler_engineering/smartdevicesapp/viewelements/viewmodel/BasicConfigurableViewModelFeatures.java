/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.viewmodel;

import android.content.Intent;

import java.util.UUID;

import de.vogler_engineering.smartdevicesapp.viewelements.components.base.ConfigurableViewModelFeatures;

public class BasicConfigurableViewModelFeatures implements ConfigurableViewModelFeatures {

    private static final String TAG = "BasicConfigurableViewModelFeatures";

    @Override
    public void sendJob() {
        throw new IllegalStateException("This method should not be called right now! Please override this method.");
    }

    @Override
    public void sendJobReply(String value) {
        throw new IllegalStateException("This method should not be called right now! Please override this method.");
    }

    @Override
    public void startJob(String jobKey) {
        throw new IllegalStateException("This method should not be called right now! Please override this method.");
    }

    @Override
    public void openJob(String jobKey, UUID id) {
        throw new IllegalStateException("This method should not be called right now! Please override this method.");
    }

    @Override
    public void removeJob() {
        throw new IllegalStateException("This method should not be called right now! Please override this method.");
    }

    @Override
    public void startActivity(Intent intent, int errorText) {
        throw new IllegalStateException("This method should not be called right now! Please override this method.");
    }

    @Override
    public void openTodoListDetails(String listId, UUID instanceId, int stepNumber) {
        throw new IllegalStateException("This method should not be called right now! Please override this method.");
    }
}
