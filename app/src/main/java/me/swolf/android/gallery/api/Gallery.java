package me.swolf.android.gallery.api;

public interface Gallery extends Updatable
{
    /**
     * Returns the {@link PhotoAlbum}s which are stored by this gallery
     *
     * @return array containing {@link PhotoAlbum} instances
     */
    PhotoAlbum[] getPhotoAlbums();

    /**
     * Returns the {@link PhotoAlbum} having the specified id
     *
     * @param id id of the {@link PhotoAlbum}
     *
     * @return {@link PhotoAlbum}
     */
    PhotoAlbum getPhotoAlbum(int id);
}
