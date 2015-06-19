package me.swolf.android.gallery.api;

/**
 * An application showing the {@link me.swolf.android.gallery.GalleryActivity} must implement this interface
 */
public interface PhotoApplication
{
    /**
     * Returns the {@link Gallery} which is storing the images
     *
     * @return {@link Gallery}
     */
    Gallery getGallery();
}
