package me.swolf.android.gallery.api;

public interface PhotoAlbum extends PhotoItem
{
    /**
     * Returns the photos which are stored in this album
     *
     * @return photos
     */
    Photo[] getPhotos();
}
