package me.swolf.android.gallery;

import android.os.Bundle;
import android.app.Activity;
import android.graphics.Point;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import me.swolf.android.gallery.api.Photo;
import me.swolf.android.gallery.api.PhotoAlbum;
import me.swolf.android.gallery.util.Misc;

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

        // calculates how many view holders should be created maximum
        Point screenSize = Misc.getScreenSize(this.activity.getWindowManager()); // loads the screen size
        int columnCount = this.getResources().getInteger(R.integer.gallery_column_count);
        int width = screenSize.x / columnCount + 1; // calculates the maximum width of a single view holder
        int rowCount = screenSize.y / width + 1; // calculates how many view holders can be displayed in one row; assumes height and width are equal
        int maxViewHoldersCount = (rowCount + 2) * columnCount; // calculates the maximum view holders of the screen and one pre-loading row

        if (BuildConfig.DEBUG)
        {
            Log.d(this.getClass().getSimpleName(), "sets maximum view holders size to " + maxViewHoldersCount);
        }
        this.view.getRecycledViewPool().setMaxRecycledViews(this.view.getAdapter().getItemViewType(0), maxViewHoldersCount);

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
