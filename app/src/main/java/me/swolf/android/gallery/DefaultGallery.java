package me.swolf.android.gallery;

import java.io.File;
import java.util.Comparator;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.ImageColumns;
import android.util.Log;
import android.util.SparseArray;

import me.swolf.android.gallery.api.Gallery;
import me.swolf.android.gallery.api.PhotoAlbum;
import me.swolf.android.gallery.internal.InternalPhoto;
import me.swolf.android.gallery.internal.SimplePhotoAlbum;
import me.swolf.android.gallery.util.ProgressDrivenAsyncTask;

public class DefaultGallery implements Gallery
{
    private long updateTimestamp = 0;
    private SparseArray<SimplePhotoAlbum> photoAlbumSparseArray;

    private LoadInternalPhotosTask runningTask;

    private OnUpdateFinishedListener listener;

    public DefaultGallery()
    {
        this.photoAlbumSparseArray = new SparseArray<>();
    }

    @Override
    public PhotoAlbum[] getPhotoAlbums()
    {
        Set<PhotoAlbum> photoAlbums = new TreeSet<>(new Comparator<PhotoAlbum>()
        {
            @Override
            public int compare(PhotoAlbum lhs, PhotoAlbum rhs)
            {
                return lhs.getName().compareTo(rhs.getName());
            }
        });

        for (int i = 0; i < this.photoAlbumSparseArray.size(); i++)
        {
            photoAlbums.add(this.photoAlbumSparseArray.valueAt(i));
        }

        return photoAlbums.toArray(new PhotoAlbum[photoAlbums.size()]);
    }

    @Override
    public PhotoAlbum getPhotoAlbum(int id)
    {
        for (int i = 0; i < this.photoAlbumSparseArray.size(); i++)
        {
            PhotoAlbum album = this.photoAlbumSparseArray.valueAt(i);
            if (album.getId() == id)
            {
                return album;
            }
        }
        return null;
    }

    @Override
    public void update(Context context)
    {
        this.runningTask = new LoadInternalPhotosTask(context);
        this.runningTask.execute();
    }

    @Override
    public long getLastUpdateTimestamp()
    {
        return this.updateTimestamp;
    }

    @Override
    public boolean needsToBeUpdated()
    {
        return this.updateTimestamp == 0;
    }

    @Override
    public boolean isUpdating()
    {
        return this.runningTask != null && this.runningTask.isRunning();
    }

    @Override
    public void detachUpdateContext()
    {
        this.runningTask.detachContext();
    }

    @Override
    public void attachUpdateContext(Context context)
    {
        this.runningTask.attachContext(context);
    }

    @Override
    public void setOnUpdateFinishedListener(OnUpdateFinishedListener listener)
    {
        this.listener = listener;
    }

    private class LoadInternalPhotosTask extends ProgressDrivenAsyncTask<Void, Void, Void>
    {
        protected LoadInternalPhotosTask(Context context)
        {
            super(context, ProgressDialog.STYLE_SPINNER, false, 0);
        }

        @Override
        protected Void doInBackground(Void... params)
        {
            // TODO add loaders to the gallery and use the loaders here; so factor it out into an InternalPhotoLoader
            final String[] columns = {
                ImageColumns._ID,
                ImageColumns.TITLE,
                ImageColumns.DATA,
                ImageColumns.SIZE,
                ImageColumns.MIME_TYPE,
                ImageColumns.DATE_TAKEN,
                ImageColumns.ORIENTATION,
                ImageColumns.BUCKET_ID,
                ImageColumns.BUCKET_DISPLAY_NAME
            };

            Cursor imageCursor = MediaStore.Images.Media.query(this.getApplicationContext().getContentResolver(),
                                                               MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null, ImageColumns._ID);

            // To load the video or audio cursor use this snippet:
            // CursorLoader loader = new CursorLoader(getCore().getContext(), MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
            // Cursor videoCursor = loader.loadInBackground();

            int imageCount = 0;
            if (imageCursor != null && imageCursor.getCount() > 0)
            {
                while (imageCursor.moveToNext())
                {
                    int bucketId = imageCursor.getInt(imageCursor.getColumnIndex(ImageColumns.BUCKET_ID));
                    String bucketName = imageCursor.getString(imageCursor.getColumnIndex(ImageColumns.BUCKET_DISPLAY_NAME));

                    SimplePhotoAlbum photoAlbum = photoAlbumSparseArray.get(bucketId);
                    if (photoAlbum == null)
                    {
                        photoAlbum = new SimplePhotoAlbum(bucketId, bucketName);
                        photoAlbumSparseArray.put(bucketId, photoAlbum);
                    }

                    int id = imageCursor.getInt(imageCursor.getColumnIndex(ImageColumns._ID));

                    if (photoAlbum.containsPhoto(id))
                    {
                        continue;
                    }

                    String name = imageCursor.getString(imageCursor.getColumnIndex(ImageColumns.TITLE));
                    File file = new File(imageCursor.getString(imageCursor.getColumnIndex(ImageColumns.DATA)));
                    String mimeType = imageCursor.getString(imageCursor.getColumnIndex(ImageColumns.MIME_TYPE));
                    Date dateTaken = new Date(imageCursor.getInt(imageCursor.getColumnIndex(ImageColumns.DATE_TAKEN)));
                    int orientation = imageCursor.getInt(imageCursor.getColumnIndex(ImageColumns.ORIENTATION));

                    InternalPhoto photo = new InternalPhoto(id, name, file, mimeType, dateTaken, orientation);
                    photoAlbum.addPhoto(photo);
                    imageCount++;
                }

                updateTimestamp = new Date().getTime();
            }

            Log.d(this.getClass().getSimpleName(), photoAlbumSparseArray.size() + " albums with " + imageCount + " photos were added to the gallery");
            if (listener != null)
            {
                listener.onUpdateFinished();
            }

            return null;
        }
    }
}
