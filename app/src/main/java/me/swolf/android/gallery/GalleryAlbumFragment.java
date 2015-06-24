package me.swolf.android.gallery;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import me.swolf.android.gallery.GalleryAlbumFragment.AlbumAdapter.ViewHolder;
import me.swolf.android.gallery.api.Photo;
import me.swolf.android.gallery.api.PhotoAlbum;

public class GalleryAlbumFragment extends GalleryFragment
{
    @Override
    protected int getColumnCount()
    {
        return activity.showsAlbumPhotos() ? 1 : this.getResources().getInteger(R.integer.gallery_column_count);
    }

    @Override
    protected Adapter createAdapter()
    {
        return new AlbumAdapter(this.activity);
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        this.activity.attachAlbumFragment(this);
    }

    @Override
    protected void onPhotoSelected(PhotoAlbum album, Photo photo)
    {
        this.setSelectedPhotoCount(album);
    }

    @Override
    protected void onPhotoDeselected(PhotoAlbum album, Photo photo)
    {
        this.setSelectedPhotoCount(album);
    }

    public void setSelectedPhotoCount(PhotoAlbum album)
    {
        for (int i = 0; i < this.view.getChildCount(); i++)
        {
            ViewHolder holder = (ViewHolder)this.view.getChildViewHolder(this.view.getChildAt(i));

            if (album.equals(holder.boundAlbum))
            {
                int selectionCount = this.activity.getSelectionCount(holder.boundAlbum);
                holder.setActivated(selectionCount > 0);
                holder.countView.setText(String.valueOf(selectionCount));
                break;
            }
        }
    }

    public RecyclerView getRecyclerView()
    {
        return view;
    }

    public static class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.ViewHolder>
    {
        private final GalleryActivity activity;
        private final PhotoAlbum[] albums;

        public AlbumAdapter(GalleryActivity activity)
        {
            this.activity = activity;
            this.albums = activity.getGallery().getPhotoAlbums();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_gallery_photo_album_item, parent, false);
            return new ViewHolder(this.activity, v);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position)
        {
            holder.bind(this.albums[position], null, position);
        }

        @Override
        public int getItemCount()
        {
            return this.albums.length;
        }

        public static class ViewHolder extends PhotoViewHolder
        {
            private final TextView nameView;
            private final TextView countView;

            public ViewHolder(final GalleryActivity activity, final View itemView)
            {
                super(activity, itemView);

                this.nameView = (TextView)itemView.findViewById(R.id.item_name);
                this.countView = (TextView)itemView.findViewById(R.id.item_selected_count);

                this.itemView.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        activity.showAlbumPhotos(boundAlbum);
                    }
                });
            }

            @Override
            public void bind(PhotoAlbum album, Photo photo, int position)
            {
                super.bind(album, photo, position);

                String text = String.format("%s (%d)", album.getName(), album.getPhotos().length);

// TODO just show the album name in one line and at the end the number of photos in the album; The following code doesn't work correctly unfortunately
//                float width = this.nameView.getWidth();
//                if (width == 0)
//                {
//                    int columnCount = this.activity.getResources().getInteger(R.integer.gallery_column_count);
//
//                    Point displaySize = Misc.getScreenSize(this.activity.getWindowManager());
//                    width = displaySize.x / (float)columnCount - columnCount * Misc.convertDpToPixel(5, this.activity);
//                }
//                float textWidth = this.nameView.getPaint().measureText(text);
//                if (textWidth > width)
//                {
//                    float ratio = width / textWidth;
//                    int numberOfCharacters = (int)(ratio * text.length()) - 6 - String.valueOf(album.getPhotos().length).length();
//
//                    String name = album.getName();
//                    if (numberOfCharacters > 0 && name.length() > numberOfCharacters)
//                    {
//                        name = name.substring(0, numberOfCharacters);
//                    }
//                    text = String.format("%s... (%d)", name, album.getPhotos().length);
//                }
                this.nameView.setText(text);

                this.countView.setText(String.valueOf(this.activity.getSelectionCount(album)));
            }

            @Override
            protected Photo getThumbnailPhoto(PhotoAlbum album, Photo photo)
            {
                return album.getPhotos()[0];
            }

            @Override
            public void setActivated(boolean activated)
            {
                super.setActivated(activated);
                if (activated)
                {
                    this.countView.setVisibility(View.VISIBLE);
                }
                else
                {
                    this.countView.setVisibility(View.GONE);
                }
            }
        }
    }
}
