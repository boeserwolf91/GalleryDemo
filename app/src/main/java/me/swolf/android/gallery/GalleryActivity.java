package me.swolf.android.gallery;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import android.os.Bundle;
import android.os.Environment;
import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.OnScanCompletedListener;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.view.ActionMode.Callback;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import me.swolf.android.gallery.api.Gallery;
import me.swolf.android.gallery.api.Photo;
import me.swolf.android.gallery.api.PhotoAlbum;
import me.swolf.android.gallery.api.PhotoApplication;
import me.swolf.android.gallery.api.Updatable.OnUpdateFinishedListener;


public class GalleryActivity extends AppCompatActivity implements OnUpdateFinishedListener
{
    private static final int REQUEST_IMAGE_CAPTURE = 0x93;
    private static final int REQUEST_TAKE_PHOTO = 0x40;

    private final static String SAVE_PHOTO_FILE = "FILE_OF_PHOTO_WHICH_IS_TAKEN";
    private final String SAVE_SELECTED_PHOTO_STATE = "SELECTED_PHOTO_STATE";
    private final String SAVE_SELECTION_MODE_ACTIVE_STATE = "SELECTION_MODE_ACTIVE_STATE";
    private final String SAVE_SHOW_PHOTOS_MODE = "SHOW_PHOTOS_MODE";

    private final String SELECTED_PHOTO_ALBUM_PHOTO_DIVIDER = "/";

    /**
     * This field contains the path to the photo which is be taken at the moment;
     * So if the user decides to take a picture, this value contains the path to the picture
     */
    private File photoFile;

    private Gallery gallery;
    private int columnCount;

    private ActionMode selectionMode;
    private Set<String> selectedPhotos;

    private PhotoBitmapLoaderTask photoBitmapLoaderTask;

    private GalleryAlbumFragment galleryAlbumFragment;
    private GalleryAlbumPhotoFragment galleryAlbumPhotoFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Application application = this.getApplication();
        if (!(application instanceof PhotoApplication))
        {
            throw new IllegalArgumentException("The application must implement the interface " + PhotoApplication.class.getName());
        }

        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_gallery);

        // initializes some variables
        this.selectedPhotos = new TreeSet<>();
        this.photoBitmapLoaderTask = new PhotoBitmapLoaderTask(this);
        this.columnCount = this.getResources().getInteger(R.integer.gallery_column_count);

        // initializes the gallery and updates it if necessary
        this.gallery = ((PhotoApplication)application).getGallery();
        this.gallery.setOnUpdateFinishedListener(this);
        if (this.gallery.isUpdating())
        {
            this.gallery.attachUpdateContext(this);
        }
        else if (this.gallery.needsToBeUpdated())
        {
            this.gallery.update(this);
        }

        // loads the photo container fragment and sets the weight of the layout (depending on the column count)
        FrameLayout frameLayout = (FrameLayout)this.findViewById(R.id.album_photo_container);
        frameLayout.setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, this.columnCount - 1));

        // loads the saved instance state or calls the onUpdateFinished method filling the layout with content
        if (savedInstanceState != null)
        {
            String photoFilePath = savedInstanceState.getString(SAVE_PHOTO_FILE);
            if (photoFilePath != null)
            {
                this.photoFile = new File(photoFilePath);
            }

            Collections.addAll(this.selectedPhotos, savedInstanceState.getStringArray(SAVE_SELECTED_PHOTO_STATE));
            if (savedInstanceState.getBoolean(SAVE_SELECTION_MODE_ACTIVE_STATE, false))
            {
                this.startSelectionMode();
            }
            if (savedInstanceState.getBoolean(SAVE_SHOW_PHOTOS_MODE, false))
            {
                frameLayout.setVisibility(View.VISIBLE);
            }
        }
        else
        {
            this.onUpdateFinished();
        }
    }

    @Override
    public void onUpdateFinished()
    {
        PhotoAlbum[] photoAlbums = gallery.getPhotoAlbums();

        if (photoAlbums != null && photoAlbums.length > 0)
        {
            // adds the GalleryAlbumFragment to the left fragment container
            this.getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            this.getSupportFragmentManager().beginTransaction().replace(R.id.album_container, new GalleryAlbumFragment()).commit();
        }
        else
        {
            // TODO show a message saying that photo albums are not available
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // creates the menu
        getMenuInflater().inflate(R.menu.menu_gallery, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        switch (id)
        {
            case R.id.action_take_photo:
                // opens the camera and takes a new photo
                this.takePhoto();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed()
    {
        // is called if the back button was pressed.
        // if the GalleryAlbumPhotoFragment is present it'll be closed
        // otherwise it'll finish the selection mode
        // if the selection mode isn't started too, the normal behaviour (closing the activity) will be executed
        if (this.galleryAlbumPhotoFragment != null)
        {
            this.closeAlbumPhotos();
        }
        else if (this.isSelectionModeRunning())
        {
            this.selectionMode.finish();
        }
        else
        {
            super.onBackPressed();
        }
    }

    protected void onSaveInstanceState(@NonNull Bundle outState)
    {
        super.onSaveInstanceState(outState);
        if (this.photoFile != null)
        {
            outState.putString(SAVE_PHOTO_FILE, this.photoFile.getAbsolutePath());
        }

        outState.putStringArray(SAVE_SELECTED_PHOTO_STATE, this.selectedPhotos.toArray(new String[this.selectedPhotos.size()]));
        outState.putBoolean(SAVE_SELECTION_MODE_ACTIVE_STATE, this.selectionMode != null);
        outState.putBoolean(SAVE_SHOW_PHOTOS_MODE, this.galleryAlbumPhotoFragment != null);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        // if the gallery update is running the context will be detached
        if (this.gallery.isUpdating())
        {
            this.gallery.detachUpdateContext();
        }
    }

    /**
     * Returns the {@link Gallery} storing the photos which shall be shown with this activity
     *
     * @return {@link Gallery}
     */
    public Gallery getGallery()
    {
        return this.gallery;
    }

    /**
     * Returns whether the {@link Gallery} is showing photos of a specific album.
     *
     * @return whether the activity is showing photos of a specific album
     */
    public boolean showsAlbumPhotos()
    {
        return this.galleryAlbumPhotoFragment != null;
    }

    /**
     * closes the photos of a specific album again and shows an overview about every photo album
     */
    public void closeAlbumPhotos()
    {
        FrameLayout layout = (FrameLayout)this.findViewById(R.id.album_photo_container);
        layout.setVisibility(View.GONE);

        this.getSupportFragmentManager().beginTransaction().remove(this.galleryAlbumPhotoFragment).commit();
        this.galleryAlbumPhotoFragment = null;

        LayoutManager layoutManager = galleryAlbumFragment.getRecyclerView().getLayoutManager();
        if (layoutManager instanceof StaggeredGridLayoutManager && ((StaggeredGridLayoutManager)layoutManager).getSpanCount() != this.columnCount)
        {
            ((StaggeredGridLayoutManager)layoutManager).setSpanCount(columnCount);
        }

        // TODO try it without
        this.galleryAlbumFragment.getRecyclerView().removeAllViews();

        this.setTitle(R.string.gallery_name);
    }

    /**
     * shows the album photos of the specified album
     *
     * @param album {@link PhotoAlbum}
     */
    public void showAlbumPhotos(PhotoAlbum album)
    {
        Bundle arguments = new Bundle(1);
        arguments.putInt(GalleryAlbumPhotoFragment.PHOTO_ALBUM_PARAM, album.getId());

        GalleryAlbumPhotoFragment fragment = new GalleryAlbumPhotoFragment();
        fragment.setArguments(arguments);

        FrameLayout layout = (FrameLayout)this.findViewById(R.id.album_photo_container);
        layout.setVisibility(View.VISIBLE);
        this.getSupportFragmentManager().beginTransaction().replace(R.id.album_photo_container, fragment).commit();

        LayoutManager layoutManager = galleryAlbumFragment.getRecyclerView().getLayoutManager();
        if (layoutManager instanceof StaggeredGridLayoutManager && ((StaggeredGridLayoutManager)layoutManager).getSpanCount() > 1)
        {
            ((StaggeredGridLayoutManager)layoutManager).setSpanCount(1);
        }
        this.setTitle(album.getName());
    }

    /**
     * Attaches the fragment showing an overview of the albums to this activity
     *
     * @param photoPoolAlbumFragment {@link GalleryAlbumFragment}
     */
    protected void attachAlbumFragment(GalleryAlbumFragment photoPoolAlbumFragment)
    {
        this.galleryAlbumFragment = photoPoolAlbumFragment;
    }

    /**
     * Attaches the fragment showing photos of a specific album to this activity
     *
     * @param fragment album photo fragment {@link GalleryAlbumPhotoFragment}
     */
    protected void attachAlbumPhotoFragment(GalleryAlbumPhotoFragment fragment)
    {
        this.galleryAlbumPhotoFragment = fragment;
    }

    /**
     * Returns the count of selected photos from a specific album
     *
     * @param album the photo album
     *
     * @return count of photos
     */
    public int getSelectionCount(PhotoAlbum album)
    {
        int count = 0;
        String namePrefix = album.getName() + SELECTED_PHOTO_ALBUM_PHOTO_DIVIDER;
        for (String photo : this.selectedPhotos)
        {
            if (photo.startsWith(namePrefix))
            {
                count++;
            }
        }
        return count;
    }

    /**
     * Starts the photo selection mode
     */
    public void startSelectionMode()
    {
        this.selectionMode = this.startSupportActionMode(new SelectionModeCallback());
    }

    /**
     * Returns whether the photo selection mode is running
     *
     * @return is photo selection mode running
     */
    public boolean isSelectionModeRunning()
    {
        return this.selectionMode != null;
    }

    /**
     * checks whether the specified photo is selected;
     * if the photo is null, the method checks whether at least one photo of the album is selected
     *
     * @param album album of the photo
     * @param photo photo
     *
     * @return whether photo is selected
     */
    public boolean isSelected(PhotoAlbum album, Photo photo)
    {
        if (photo == null)
        {
            for (String selected : this.selectedPhotos)
            {
                if (selected.startsWith(album.getName()))
                {
                    return true;
                }
            }
            return false;
        }
        return this.selectedPhotos.contains(this.createSelectionPhotoName(album, photo));
    }

    /**
     * Selects a photo; if the photo is null, every photo from the specified album will be selected
     *
     * @param album album of the photo
     * @param photo photo
     */
    public void selectPhoto(PhotoAlbum album, Photo photo)
    {
        if (photo == null)
        {
            // selects every photo of the album if the photo is null
            for (Photo p : album.getPhotos())
            {
                this.selectPhoto(album, p);
            }
            return;
        }
        this.selectedPhotos.add(this.createSelectionPhotoName(album, photo));

        // sets the number of selected photo to the selection mode callback
        int selectedPhotoSize = this.selectedPhotos.size();
        this.selectionMode.setSubtitle(this.getResources().getQuantityString(R.plurals.gallery_selection_mode_photos, selectedPhotoSize,
                                                                             selectedPhotoSize));

        // calls the onPhotoSelect method of the attached fragments
        if (this.galleryAlbumFragment != null)
        {
            this.galleryAlbumFragment.onPhotoSelected(album, photo);
        }
        if (this.galleryAlbumPhotoFragment != null)
        {
            this.galleryAlbumPhotoFragment.onPhotoSelected(album, photo);
        }
    }

    /**
     * Deselects a photo; if the photo is null, every photo from the specified album will be deselected
     *
     * @param album album of the photo
     * @param photo photo
     */
    public void deselectPhoto(PhotoAlbum album, Photo photo)
    {
        if (photo == null)
        {
            // deselects every photo of the album if the photo is null
            for (Photo p : album.getPhotos())
            {
                this.deselectPhoto(album, p);
            }
            return;
        }
        this.selectedPhotos.remove(this.createSelectionPhotoName(album, photo));

        // sets the number of selected photo to the selection mode callback
        int selectedPhotoSize = this.selectedPhotos.size();
        this.selectionMode.setSubtitle(this.getResources().getQuantityString(R.plurals.gallery_selection_mode_photos, selectedPhotoSize,
                                                                             selectedPhotoSize));

        // calls the onPhotoDeselect method of the attached fragments
        if (this.galleryAlbumFragment != null)
        {
            this.galleryAlbumFragment.onPhotoDeselected(album, photo);
        }
        if (this.galleryAlbumPhotoFragment != null)
        {
            this.galleryAlbumPhotoFragment.onPhotoDeselected(album, photo);
        }
    }

    /**
     * Creates the name of the photo for the selection mode
     *
     * @param album photo album
     * @param photo photo
     *
     * @return name for the selection mode
     */
    private String createSelectionPhotoName(PhotoAlbum album, Photo photo)
    {
        return album.getName() + SELECTED_PHOTO_ALBUM_PHOTO_DIVIDER + photo.getName();
    }

    /**
     * Returns the {@link PhotoBitmapLoaderTask} loading the photos asynchronously
     *
     * @return {@link PhotoBitmapLoaderTask}
     */
    public PhotoBitmapLoaderTask getPhotoBitmapLoaderTask()
    {
        return this.photoBitmapLoaderTask;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK)
        {
            // handling the image capture request. This is very useless at the moment.
            // maybe this will be used later
            // TODO think about and implement it
            Bundle extras = data.getExtras();
            if (extras == null)
            {
                return;
            }
            for (String key : extras.keySet())
            {
                System.out.println(key + ": " + extras.get(key).getClass());
            }
        }
        else if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK)
        {
            // a new photo was taken and saved
            // adds picture to the gallery
            MediaScannerConnection.scanFile(this, new String[]{this.photoFile.getAbsolutePath()}, null, new OnScanCompletedListener()
            {
                @SuppressLint("NewApi")
                @Override
                public void onScanCompleted(String path, Uri uri)
                {
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            // updates the gallery because the new picture should be available
                            Log.d(this.getClass().getSimpleName(), "updates the photo pool");
                            gallery.update(GalleryActivity.this);
                        }
                    });
                }
            });
        }
    }

    /**
     * opens the camera and let the user take a new photo
     */
    public void takePhoto()
    {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (cameraIntent.resolveActivity(getPackageManager()) != null)
        {
            // Create the File where the photo should go
            File photoFile = null;
            try
            {
                photoFile = createImageFile();
            }
            catch (IOException ex)
            {
                Log.e(this.getClass().getSimpleName(), "A photo can't be taken because the photo file couldn't be created", ex);
            }
            if (photoFile != null)
            {
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                this.startActivityForResult(cameraIntent, REQUEST_TAKE_PHOTO);
            }
            else
            {
                this.startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    /**
     * Creates an image file storing the new photo; This method is used within the {@link #takePhoto()} method
     *
     * @return file
     *
     * @throws IOException
     */
    private File createImageFile() throws IOException
    {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timeStamp;
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        storageDir = new File(storageDir, this.getString(R.string.taken_photo_directory_name));
        if (!storageDir.exists() && !storageDir.mkdirs())
        {
            Log.e(this.getClass().getSimpleName(), "the photo directory couldn't be created");
        }

        this.photoFile = File.createTempFile(imageFileName, ".jpg", storageDir);
        Log.d(this.getClass().getSimpleName(), "file:" + this.photoFile.getAbsolutePath());
        return this.photoFile;
    }

    /**
     * Callback for the selection mode
     */
    private class SelectionModeCallback implements Callback
    {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu)
        {
            mode.setTitle(getString(R.string.gallery_selection_mode));

            int selectedPhotoSize = selectedPhotos.size();
            mode.setSubtitle(getResources().getQuantityString(R.plurals.gallery_selection_mode_photos, selectedPhotoSize, selectedPhotoSize));

            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu)
        {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item)
        {
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode)
        {
            selectedPhotos.clear();
            selectionMode = null;
            galleryAlbumFragment.onLeaveSelectionMode();
            if (galleryAlbumPhotoFragment != null)
            {
                galleryAlbumPhotoFragment.onLeaveSelectionMode();
            }
        }
    }
}
