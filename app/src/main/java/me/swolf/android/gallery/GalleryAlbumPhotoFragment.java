package me.swolf.android.gallery;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import me.swolf.android.gallery.GalleryAlbumPhotoFragment.AlbumPhotoAdapter.ViewHolder;
import me.swolf.android.gallery.api.Photo;
import me.swolf.android.gallery.api.PhotoAlbum;

public class GalleryAlbumPhotoFragment extends GalleryFragment
{
    public static final String PHOTO_ALBUM_PARAM = "PHOTO_ALBUM_PARAM";

    private PhotoAlbum album;

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        this.activity.attachAlbumPhotoFragment(this);
    }

    @Nullable
    @Override
    public RecyclerView onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        Bundle arguments = this.getArguments();
        this.album = this.activity.getGallery().getPhotoAlbum(arguments.getInt(PHOTO_ALBUM_PARAM));

        this.activity.setTitle(this.album.getName());

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected int getColumnCount()
    {
        return this.getResources().getInteger(R.integer.gallery_column_count) - 1;
    }

    @Override
    protected Adapter createAdapter()
    {
        return new AlbumPhotoAdapter(this.activity, this.album);
    }

    @Override
    protected void onPhotoSelected(PhotoAlbum album, Photo photo)
    {
        this.onPhotoEvent(album, photo, true);
    }

    @Override
    protected void onPhotoDeselected(PhotoAlbum album, Photo photo)
    {
        this.onPhotoEvent(album, photo, false);
    }

    private void onPhotoEvent(PhotoAlbum album, Photo photo, boolean selected)
    {
        if (!album.equals(this.album))
        {
            return;
        }

        for (int i = 0; i < this.view.getChildCount(); i++)
        {
            ViewHolder holder = (ViewHolder)this.view.getChildViewHolder(this.view.getChildAt(i));

            if (photo.equals(holder.boundPhoto))
            {
                holder.setActivated(selected);
                break;
            }
        }
    }

    protected static class AlbumPhotoAdapter extends RecyclerView.Adapter<AlbumPhotoAdapter.ViewHolder>
    {
        private final GalleryActivity activity;
        private final PhotoAlbum album;
        private final Photo[] photos;

        private AlbumPhotoAdapter(GalleryActivity activity, PhotoAlbum album)
        {
            this.activity = activity;
            this.album = album;
            this.photos = album.getPhotos();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_gallery_photo_item, parent, false);
            return new ViewHolder(this.activity, v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position)
        {
            holder.bind(this.album, this.photos[position], position);
        }

        @Override
        public int getItemCount()
        {
            return this.photos.length;
        }

        public static class ViewHolder extends PhotoViewHolder
        {
            public ViewHolder(final GalleryActivity activity, final View itemView)
            {
                super(activity, itemView);

                this.itemView.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        if (activity.isSelectionModeRunning())
                        {
                            if (activity.isSelected(boundAlbum, boundPhoto))
                            {
                                activity.deselectPhoto(boundAlbum, boundPhoto);
                                ViewHolder.this.setActivated(false);
                            }
                            else
                            {
                                activity.selectPhoto(boundAlbum, boundPhoto);
                                ViewHolder.this.setActivated(true);
                            }
                        }
                        else
                        {
                            Context context = v.getContext();

                            Intent intent = new Intent(context, PhotoPreviewActivity.class);
                            intent.putExtra(PhotoPreviewActivity.PHOTO_ALBUM_ID, boundAlbum.getId());
                            intent.putExtra(PhotoPreviewActivity.PHOTO_ID, boundPhoto.getId());

                            context.startActivity(intent);
                        }
                    }
                });
            }

            @Override
            public void setActivated(boolean activated)
            {
                super.setActivated(activated);

                if (activated)
                {
                    this.thumbnailView.setColorFilter(this.activity.getResources().getColor(R.color.activity_gallery_item_tint_activated));
                }
                else
                {
                    this.thumbnailView.setColorFilter(Color.TRANSPARENT);
                }
            }
        }
    }
}
