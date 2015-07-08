package me.swolf.android.gallery;

import java.util.Locale;

import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;

import me.swolf.android.gallery.api.Gallery;
import me.swolf.android.gallery.api.Photo;
import me.swolf.android.gallery.api.PhotoAlbum;
import me.swolf.android.gallery.api.PhotoApplication;
import me.swolf.android.gallery.util.TouchImageView;

public class PhotoPreviewActivity extends AppCompatActivity
{
    public static String PHOTO_ALBUM_ID = "PHOTO_ALBUM_ID";
    public static String PHOTO_ID = "PHOTO_ID";

    private final int maxHeight = 2048;
    private final int maxWidth = 2048;

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
        if (extras == null)
        {
            return;
        }

        PhotoApplication photoApplication = (PhotoApplication)this.getApplication();
        Gallery gallery = photoApplication.getGallery();
        PhotoAlbum album = gallery.getPhotoAlbum(extras.getInt(PHOTO_ALBUM_ID));
        this.photo = album.getPhoto(extras.getInt(PHOTO_ID));

        Bitmap bitmap = this.modifyBitmap(this.photo.loadThumbnailBitmap(this), this.maxWidth, this.maxHeight, this.photo.getOrientation());
        this.touchImageView.setImageBitmap(bitmap);

        this.task = new BitmapLoaderTask();
        this.task.execute();

        // hide the status bar.
        this.hideStatusBar();
        touchImageView.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                hideStatusBar();
            }
        });

        // hide action bar if available
        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null)
        {
            actionBar.hide();
        }
    }

    // suppress lint warning because the field View.SYSTEM_UI_FLAG_FULLSCREEN isn't used on older devices
    @SuppressLint("InlinedApi")
    private void hideStatusBar()
    {
        if (Build.VERSION.SDK_INT < VERSION_CODES.JELLY_BEAN)
        {
            this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        else
        {
            View decorView = this.getWindow().getDecorView();

            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
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

    public Bitmap modifyBitmap(Bitmap source, int maxWidth, int maxHeight, int orientation)
    {
        boolean modifyBitmap = false;

        final int oldWidth = source.getWidth();
        final int oldHeight = source.getHeight();
        float newWidth = oldWidth;
        float newHeight = oldHeight;

        if (orientation != 0)
        {
            modifyBitmap = true;
        }
        if (newWidth > maxWidth)
        {
            newHeight *= maxWidth / newWidth;
            newWidth = maxWidth;
            modifyBitmap = true;
        }
        if (newHeight > maxHeight)
        {
            newWidth *= maxHeight / newHeight;
            newHeight = maxHeight;
            modifyBitmap = true;
        }

        if (!modifyBitmap)
        {
            return source;
        }

        Matrix matrix = new Matrix();
        matrix.postScale(newWidth / oldWidth, newHeight / oldHeight);
        matrix.postRotate(orientation);

        Bitmap modified = Bitmap.createBitmap(source, 0, 0, oldWidth, oldHeight, matrix, true);
        source.recycle();

        Log.i(this.getClass().getSimpleName(), String.format(Locale.US, "modified bitmap; scaled from %dx%d to %dx%d; rotated %d degree", oldWidth,
                                                             oldHeight, modified.getWidth(), modified.getHeight(), orientation));

        return modified;
    }

    private class BitmapLoaderTask extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected Void doInBackground(Void... params)
        {
            Bitmap source = photo.loadBitmap(PhotoPreviewActivity.this);
            if (source == null)
            {
                Log.e(this.getClass().getSimpleName(), "The bitmap couldn't be loaded");
                return null;
            }
            Log.d(this.getClass().getSimpleName(), "bitmap was loaded");

            final Bitmap bitmap = modifyBitmap(source, maxWidth, maxHeight, photo.getOrientation());

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
