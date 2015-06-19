package me.swolf.android.gallery.internal;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;

import me.swolf.android.gallery.api.Photo;
import me.swolf.android.gallery.api.PhotoAlbum;

/**
 * Represents a simple photo album
 */
public class SimplePhotoAlbum implements PhotoAlbum
{
    private final int id;
    private final String name;

    private final List<Photo> photos;

    public SimplePhotoAlbum(int id, String name)
    {
        this.id = id;
        this.name = name;

        this.photos = new ArrayList<>();
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
    public Photo[] getPhotos()
    {
        return this.photos.toArray(new Photo[this.photos.size()]);
    }

    @Override
    public Bitmap loadThumbnailBitmap(Context context)
    {
        // just use the first picture and load its thumbnail
        if (this.photos.isEmpty())
        {
            return null;
        }
        return this.photos.get(0).loadThumbnailBitmap(context);
    }

    /**
     * Returns whether the photo with the specified id exists
     *
     * @param id id of the photo
     *
     * @return whether the photo exists
     */
    public boolean containsPhoto(int id)
    {
        for (Photo photo : this.photos)
        {
            if (photo.getId() == id)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds a {@link Photo} to the album
     *
     * @param photo {@link Photo}
     */
    public void addPhoto(Photo photo)
    {
        // add photo as the first entry of the list
        this.photos.add(0, photo);
    }
}
