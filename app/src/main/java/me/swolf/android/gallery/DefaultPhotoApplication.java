package me.swolf.android.gallery;

import android.app.Application;

import me.swolf.android.gallery.api.Gallery;
import me.swolf.android.gallery.api.PhotoApplication;

public class DefaultPhotoApplication extends Application implements PhotoApplication
{
    private Gallery gallery;

    @Override
    public void onCreate()
    {
        super.onCreate();

        this.gallery = new DefaultGallery();
    }

    @Override
    public Gallery getGallery()
    {
        return this.gallery;
    }
}
