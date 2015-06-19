package me.swolf.android.gallery;

import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import me.swolf.android.gallery.api.Photo;
import me.swolf.android.gallery.api.PhotoAlbum;

abstract class PhotoViewHolder extends ViewHolder
{
    protected final GalleryActivity activity;

    protected final ImageView thumbnailView;
    private final PhotoBitmapLoaderTask task;

    protected PhotoAlbum boundAlbum;
    protected Photo boundPhoto;
    protected int boundPosition;

    public PhotoViewHolder(final GalleryActivity activity, View itemView)
    {
        super(itemView);
        this.activity = activity;
        this.task = activity.getPhotoBitmapLoaderTask();

        this.thumbnailView = (ImageView)itemView.findViewById(R.id.thumbnail);
        this.thumbnailView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener()
        {
            @SuppressLint("NewApi")
            @Override
            public void onGlobalLayout()
            {
                if (VERSION.SDK_INT < VERSION_CODES.JELLY_BEAN)
                {
                    thumbnailView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
                else
                {
                    thumbnailView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }

                thumbnailView.setLayoutParams(new RelativeLayout.LayoutParams(thumbnailView.getWidth(), thumbnailView.getWidth()));
            }
        });

        this.itemView.setOnLongClickListener(new OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View v)
            {
                if (!activity.isSelectionModeRunning())
                {
                    activity.startSelectionMode();
                }

                if (activity.isSelected(boundAlbum, boundPhoto))
                {
                    activity.deselectPhoto(boundAlbum, boundPhoto);
                    PhotoViewHolder.this.setActivated(false);
                }
                else
                {
                    activity.selectPhoto(boundAlbum, boundPhoto);
                    PhotoViewHolder.this.setActivated(true);
                }
                return true;
            }
        });
    }

    public void bind(PhotoAlbum album, Photo photo, int position)
    {
        this.boundAlbum = album;
        this.boundPhoto = photo;
        this.boundPosition = position;

        this.thumbnailView.setImageDrawable(null);

        if (this.activity.isSelected(album, photo))
        {
            this.setActivated(true);
        }
        else
        {
            this.setActivated(false);
        }

        this.task.loadBitmapAsync(this, this.getThumbnailPhoto(album, photo));
    }

    protected Photo getThumbnailPhoto(PhotoAlbum album, Photo photo)
    {
        return photo;
    }

    public void setActivated(boolean activated)
    {
        if (VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB)
        {
            if (activated)
            {
                this.itemView.setActivated(true);
            }
            else
            {
                this.itemView.setActivated(false);
            }
        }
    }

    public void setPhotoBitmap(Photo photo, final Bitmap bitmap)
    {
        this.activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                thumbnailView.setImageBitmap(bitmap);
            }
        });
    }
}
