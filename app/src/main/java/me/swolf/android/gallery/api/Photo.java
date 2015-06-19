package me.swolf.android.gallery.api;

import java.util.Date;

import android.content.Context;
import android.graphics.Bitmap;

public interface Photo extends PhotoItem
{
    /**
     * Returns the date &amp; time that the image was taken
     *
     * @return date &amp; time that the image was taken
     */
    Date getDateTaken();

    /**
     * Returns the orientation for the photo expressed as degrees.
     * Only degrees 0, 90, 180, 270 will work
     *
     * @return orientation
     */
    int getOrientation();

    /**
     * Returns the mime type of this photo
     *
     * @return mime type
     */
    String getMimeType();

    /**
     * Loads and returns the photo as a {@link Bitmap}
     *
     * @param context current context
     *
     * @return {@link Bitmap}
     */
    Bitmap loadBitmap(Context context);
}
