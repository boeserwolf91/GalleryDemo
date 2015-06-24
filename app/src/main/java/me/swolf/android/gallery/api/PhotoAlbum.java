package me.swolf.android.gallery.api;

public interface PhotoAlbum extends PhotoItem
{
    /**
     * Returns the photo with the specified id
     *
     * @param id id of the photo
     *
     * @return {@link Photo}
     */
    Photo getPhoto(int id);

    /**
     * Returns the photos which are stored in this album
     *
     * @return photos
     */
    Photo[] getPhotos();
}
