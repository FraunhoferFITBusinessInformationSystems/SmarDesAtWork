/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.ui.layouts;

import android.content.Context;
import android.widget.Checkable;
import android.widget.FrameLayout;

import de.vogler_engineering.smartdevicesapp.viewelements.R;

/**
 * Created by vh on 12.03.2018.
 */

public class CheckableLayout extends FrameLayout implements Checkable {
    private boolean mChecked;

    public CheckableLayout(Context context) {
        super(context);
    }

    public void setChecked(boolean checked) {
        mChecked = checked;
        setBackground(checked ?
                getResources().getDrawable(R.drawable.bg_blue, null)
                : null);
    }

    public boolean isChecked() {
        return mChecked;
    }

    public void toggle() {
        setChecked(!mChecked);
    }

}