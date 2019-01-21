/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.components.picturecomponent;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

import de.vogler_engineering.smartdevicesapp.common.rx.SchedulersFacade;
import de.vogler_engineering.smartdevicesapp.viewelements.BuildConfig;
import io.reactivex.Observable;
import io.reactivex.Single;
import lombok.Data;
import timber.log.Timber;

import static de.vogler_engineering.smartdevicesapp.model.util.ImageUtils.crupAndScale;

@Data
public class PictureEntry {

    private static final String TAG = "PictureEntry";

    private final UUID id;

    private Bitmap image = null;
    private Bitmap thumbnail = null;
    private File file = null;

    private boolean loading = false;
    private boolean placeholder = true;

    private boolean isSelected = false;
    private Single<Bitmap> thumbnailAsync;


    public Bitmap getThumbnail(Context context, int scale) throws FileNotFoundException {
        if (thumbnail != null) {
            return thumbnail;
        }
        Uri uri = Uri.fromFile(file);
        Bitmap bitmap;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
            bitmap = crupAndScale(bitmap, scale); // if you mind scaling
            thumbnail = bitmap;
        } catch (IOException e) {
            Timber.tag(TAG).e(e, "Could't create thumbnail!");
        }
        return thumbnail;
    }

    public Uri getUri(Context context) {
        return FileProvider.getUriForFile(context,
                BuildConfig.APPLICATION_ID,
                file);
    }

    public Single<Bitmap> getThumbnailAsync() {
        return Single.fromCallable(() -> image);
    }

    public Observable<Drawable> getThumbnailDrawable(SchedulersFacade schedulersFacade, Resources resources){
        return Observable.fromCallable(() -> image)
                .observeOn(schedulersFacade.computation())
                .map(bitmap -> new BitmapDrawable(resources, bitmap));
    }
}
