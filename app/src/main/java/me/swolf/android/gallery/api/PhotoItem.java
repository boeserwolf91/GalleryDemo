package me.swolf.android.gallery.api;

import android.content.Context;
import android.graphics.Bitmap;

public interface PhotoItem
{
    /**
     * Returns the id of the photo item
     *
     * @return id
     */
    int getId();

    /**
     * Returns the name of the photo item
     *
     * @return name
     */
    String getName();

    /**
     * Loads and returns a thumbnail of the photo item as a {@link Bitmap}
     *
     * @param context current context
     *
     * @return {@link Bitmap}
     */
    Bitmap loadThumbnailBitmap(Context context);
}
