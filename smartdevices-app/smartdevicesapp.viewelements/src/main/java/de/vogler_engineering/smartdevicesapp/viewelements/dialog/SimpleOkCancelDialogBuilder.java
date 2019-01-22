/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.KeyEvent;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import de.vogler_engineering.smartdevicesapp.viewelements.R;
import timber.log.Timber;

public class SimpleOkCancelDialogBuilder extends AlertDialog.Builder  {

    public static class ButtonChoice{
        public static String OK = "ok";
        public static String CANCEL = "cancel";
        public static String YES = "yes";
        public static String NO = "no";
        public static String DISMISS = "dismiss";
    }

    public SimpleOkCancelDialogBuilder(@NonNull Context context) {
        super(context);
    }

    public SimpleOkCancelDialogBuilder(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    public void setupDialog(DialogChoiceListener listener,
                            @DrawableRes int icon,
                            @StringRes int title,
                            @StringRes int text,
                            DialogButtonOptions buttonOptions){
        this.setTitle(title);
        this.setIcon(icon);
        this.setMessage(text);
        //Setup Button Options
        setupButtonOptions(listener, buttonOptions);

        this.setOnKeyListener((arg0, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                arg0.dismiss();
                listener.dialogChoice(ButtonChoice.DISMISS);
                return true;
            }
            return false;
        });
    }

    private void setupButtonOptions(DialogChoiceListener listener, DialogButtonOptions options){
        if(options == DialogButtonOptions.Ok || options == DialogButtonOptions.OkCancel){
            this.setPositiveButton(R.string.dialog_accept, (dialog, which) -> listener.dialogChoice(ButtonChoice.OK));
        }
        if(options == DialogButtonOptions.OkCancel || options == DialogButtonOptions.YesNoCancel){
            this.setNeutralButton(R.string.dialog_dismiss, (dialog, which) -> listener.dialogChoice(ButtonChoice.CANCEL));
        }
        if(options == DialogButtonOptions.YesNoCancel || options == DialogButtonOptions.YesNo){
            this.setPositiveButton(R.string.dialog_yes, (dialog, which) -> listener.dialogChoice(ButtonChoice.YES));
            this.setNegativeButton(R.string.dialog_no, (dialog, which) -> listener.dialogChoice(ButtonChoice.NO));
        }
    }

//    @NonNull
//    @Override
//    public AlertDialog create() {
//            mSelectedItems = new ArrayList();
//            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//
//            builder.setTitle("This is list choice dialog box");
//   .setMultiChoiceItems(R.array.toppings, null,
//                    new DialogInterface.OnMultiChoiceClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
//
//                            if (isChecked) {
//                                // If the user checked the item, add it to the selected items
//                                mSelectedItems.add(which);
//                            }
//
//                            else if (mSelectedItems.contains(which)) {
//                                // Else, if the item is already in the array, remove it
//                                mSelectedItems.remove(Integer.valueOf(which));
//                            }
//                        }
//                    })
//
//                    // Set the action buttons
//                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int id) {
//                            // User clicked OK, so save the mSelectedItems results somewhere
//                            // or return them to the component that opened the dialog
//                        }
//                    })
//
//                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int id) {
//                        }
//                    });
//            return builder.create();
//        }
//
//        return super.create();
//    }

    public interface DialogChoiceListener{
        void dialogChoice(String choice);
    }
}
