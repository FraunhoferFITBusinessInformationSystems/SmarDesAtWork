/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.components.base;

import android.content.Intent;
import androidx.annotation.StringRes;

import java.util.UUID; /**
 * Created by vh on 27.03.2018.
 */

public interface ConfigurableViewModelFeatures {

    void sendJob();
    void sendJobReply(String value);
    void startJob(String jobKey);
    void openJob(String jobKey, UUID id);
    void removeJob();

    void startActivity(Intent intent, @StringRes int errorText);

    void openTodoListDetails(String listId, UUID instanceId, int stepNumber);
}
