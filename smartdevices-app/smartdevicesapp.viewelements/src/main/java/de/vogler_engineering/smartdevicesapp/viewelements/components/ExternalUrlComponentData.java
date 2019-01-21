/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.components;

import android.content.Intent;
import android.net.Uri;

import de.vogler_engineering.smartdevicesapp.model.entities.ui.UiComponent;
import de.vogler_engineering.smartdevicesapp.viewelements.R;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.ComponentData;
import de.vogler_engineering.smartdevicesapp.viewelements.components.base.ConfigurableViewModelFeatures;
import timber.log.Timber;

/**
 * Created by vh on 23.03.2018.
 */

public class ExternalUrlComponentData extends ComponentData<String> {

    private static final String TAG = "ExternalUrlComponentData";

    public ExternalUrlComponentData(UiComponent component, ConfigurableViewModelFeatures features) {
        super(component, features);
    }

    @Override
    public void setResourceValue(String s) {
        value.postValue(s);
    }

    @Override
    public String getResourceValue() {
        return value.getValue();
    }


    public void onClick() {
        String uriStr = value.getValue();
        if(uriStr == null) {
            uriStr = getFromAdditionalProperties("uri", String.class);
        }
        if(uriStr == null){
            uriStr = getFromAdditionalProperties("url", String.class);
        }
        if(uriStr == null){
            Timber.tag(TAG).e("No URI delivered");
            return;
        }

        Uri uri = null;
        try {
            uri = Uri.parse(uriStr);
        }catch (Exception ignored){ }

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, uri);
        features.startActivity(browserIntent, R.string.comp_external_url_link_error);
    }
}
