package me.swolf.android.gallery;

import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.ViewGroup.LayoutParams;

import me.swolf.android.gallery.api.Gallery;
import me.swolf.android.gallery.api.Photo;
import me.swolf.android.gallery.api.PhotoAlbum;
import me.swolf.android.gallery.api.PhotoApplication;
import me.swolf.android.gallery.util.TouchImageView;

public class PhotoPreviewActivity extends Activity
{
    public static String PHOTO_ALBUM_ID = "PHOTO_ALBUM_ID";
    public static String PHOTO_ID = "PHOTO_ID";

    private Photo photo;
    private TouchImageView touchImageView;

    private BitmapLoaderTask task;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        this.touchImageView = new TouchImageView(this);
        this.touchImageView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        this.touchImageView.setBackgroundColor(Color.WHITE);
        this.touchImageView.setMaxZoom(4f); // change the max level of zoom, default is 3f

        this.setContentView(this.touchImageView);

        Bundle extras = this.getIntent().getExtras();
        if (extras != null)
        {
            PhotoApplication photoApplication = (PhotoApplication)this.getApplication();
            Gallery gallery = photoApplication.getGallery();
            PhotoAlbum album = gallery.getPhotoAlbum(extras.getInt(PHOTO_ALBUM_ID));
            this.photo = album.getPhoto(extras.getInt(PHOTO_ID));

            this.touchImageView.setImageBitmap(this.photo.loadThumbnailBitmap(this));

            this.task = new BitmapLoaderTask();
            this.task.execute();
        }
    }

    public void recycleBitmap()
    {
        Drawable drawable = this.touchImageView.getDrawable();
        if (!(drawable instanceof BitmapDrawable))
        {
            return;
        }
        ((BitmapDrawable)drawable).getBitmap().recycle();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        if (!this.task.getStatus().equals(Status.FINISHED))
        {
            this.task.cancel(true);
        }
        this.recycleBitmap();
    }

    private class BitmapLoaderTask extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected Void doInBackground(Void... params)
        {
            final Bitmap bitmap = photo.loadBitmap(PhotoPreviewActivity.this);

            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    recycleBitmap();
                    touchImageView.setImageBitmap(bitmap);
                }
            });

            return null;
        }
    }
}
