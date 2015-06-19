package me.swolf.android.gallery;

import android.os.Bundle;
import android.app.Activity;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import me.swolf.android.gallery.api.Photo;
import me.swolf.android.gallery.api.PhotoAlbum;

public abstract class GalleryFragment extends Fragment
{
    private static final String SAVED_POSITION_STATE = "SAVED_POSITION_STATE";

    protected GalleryActivity activity;
    protected RecyclerView view;

    @Nullable
    @Override
    public RecyclerView onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState)
    {
        this.view = (RecyclerView)inflater.inflate(R.layout.activity_gallery_fragments, container, false);
        this.view.setItemAnimator(new DefaultItemAnimator());
        this.view.setAdapter(createAdapter());

        StaggeredGridLayoutManager manager = new StaggeredGridLayoutManager(this.getColumnCount(), StaggeredGridLayoutManager.VERTICAL);
        manager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        this.view.setLayoutManager(manager);

        if (savedInstanceState != null)
        {
            // TODO is there maybe a better solution to restoring the position? Just waiting 50 milliseconds doesn't seem to be the optimum for me
            this.view.postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    view.scrollToPosition(savedInstanceState.getInt(SAVED_POSITION_STATE, 0));
                }
            }, 50);
        }

        return this.view;
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);

        if (!(activity instanceof GalleryActivity))
        {
            throw new ClassCastException(activity.getClass().getName() + " must be or extend the PhotoPoolActivity");
        }

        this.activity = (GalleryActivity)activity;
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        this.activity = null;
    }

    /**
     * Method is invoked if the selection mode was finished
     */
    public void onLeaveSelectionMode()
    {
        // deactivates every view holder
        for (int i = 0; i < this.view.getChildCount(); i++)
        {
            PhotoViewHolder viewHolder = (PhotoViewHolder)this.view.getChildViewHolder(this.view.getChildAt(i));
            viewHolder.setActivated(false);
        }
    }

    /**
     * Method is invoked if a photo was selected
     *
     * @param album album of the selected photo
     * @param photo photo which was selected
     */
    protected abstract void onPhotoSelected(PhotoAlbum album, Photo photo);

    /**
     * Method is invoked if a photo was deselected
     *
     * @param album album of the deselected photo
     * @param photo photo which was deselected
     */
    protected abstract void onPhotoDeselected(PhotoAlbum album, Photo photo);

    /**
     * Returns the column count of the {@link RecyclerView} which is shown by this fragment
     *
     * @return column count
     */
    protected abstract int getColumnCount();

    /**
     * Creates an adapter for the {@link RecyclerView}
     *
     * @return {@link Adapter}
     */
    protected abstract Adapter createAdapter();
}
