package me.swolf.android.gallery.internal;

import java.io.File;
import java.util.Date;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore.Images.Thumbnails;
import android.util.Log;

import me.swolf.android.gallery.api.Photo;

/**
 * Represents a photo which is stored at the device
 */
public class InternalPhoto implements Photo
{
    private final int id;
    private final String name;
    private final File file;

    private final String mimeType;
    private final Date dateTaken;
    private final int orientation;

    public InternalPhoto(int id, String name, File file, String mimeType, Date dateTaken, int orientation)
    {
        this.id = id;
        this.name = name;
        this.file = file;
        this.mimeType = mimeType;
        this.dateTaken = dateTaken;
        this.orientation = orientation;
    }

    @Override
    public Date getDateTaken()
    {
        return this.dateTaken;
    }

    @Override
    public int getOrientation()
    {
        return this.orientation;
    }

    @Override
    public String getMimeType()
    {
        return this.mimeType;
    }

    @Override
    public int getId()
    {
        return this.id;
    }

    @Override
    public String getName()
    {
        return this.name;
    }

    @Override
    public Bitmap loadBitmap(Context context)
    {
        return BitmapFactory.decodeFile(this.file.getAbsolutePath());
    }

    @Override
    public Bitmap loadThumbnailBitmap(Context context)
    {
        // 1. try to find thumbnail in the internal database
        Cursor cursor = context.getContentResolver().query(Thumbnails.EXTERNAL_CONTENT_URI, new String[]{Thumbnails.DATA},
                                                           Thumbnails.IMAGE_ID + "= ?", new String[]{String.valueOf(this.id)}, null);
        Bitmap bitmap = null;
        if (cursor.moveToFirst())
        {
            bitmap = BitmapFactory.decodeFile(cursor.getString(cursor.getColumnIndex(Thumbnails.DATA)));
            if (bitmap == null)
            {
                Log.e(this.getClass().getSimpleName(), "The thumbnail '" + cursor.getString(cursor.getColumnIndex(
                    Thumbnails.DATA)) + "' couldn't be decoded");
            }
        }
        else
        {
            Log.e(this.getClass().getSimpleName(), "the thumbnail of the photo '" + this.file.getAbsolutePath() + "'couldn't be found");
        }

        cursor.close();

        if (bitmap != null)
        {
            return bitmap;
        }

        // 2. load thumbnail with getThumbnail method
        return Thumbnails.getThumbnail(context.getContentResolver(), this.id, Thumbnails.MINI_KIND, null);
    }
}
