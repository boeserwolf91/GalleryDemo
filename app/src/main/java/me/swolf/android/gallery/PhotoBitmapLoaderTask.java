package me.swolf.android.gallery;

import java.util.LinkedList;
import java.util.Queue;

import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import me.swolf.android.gallery.api.Photo;

public class PhotoBitmapLoaderTask
{
    private final Context context;

    private final Queue<QueueEntry> entries;

    private RealLoadingTask task;

    public PhotoBitmapLoaderTask(Context context)
    {
        this.context = context;
        this.entries = new LinkedList<>();
    }

    public void loadBitmapAsync(PhotoViewHolder viewHolder, Photo photo)
    {
        this.entries.offer(new QueueEntry(viewHolder, photo));

        if (this.task == null || this.task.getStatus() == Status.FINISHED)
        {
            this.task = new RealLoadingTask();
            this.task.execute();
        }
    }

    private class RealLoadingTask extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected Void doInBackground(Void... params)
        {
            int loadedBitmapCount = 0;
            while (!entries.isEmpty())
            {
                QueueEntry entry = entries.poll();

                if (!entry.viewHolder.getThumbnailPhoto(entry.viewHolder.boundAlbum, entry.viewHolder.boundPhoto).equals(entry.photo))
                {
                    continue;
                }
                loadedBitmapCount++;

                Bitmap bitmap = entry.photo.loadThumbnailBitmap(context);

                if (entry.viewHolder.getThumbnailPhoto(entry.viewHolder.boundAlbum, entry.viewHolder.boundPhoto).equals(entry.photo))
                {
                    entry.viewHolder.setPhotoBitmap(entry.photo, bitmap);
                }
            }
            if (BuildConfig.DEBUG)
            {
                Log.d(this.getClass().getSimpleName(), loadedBitmapCount + " photos were loaded with this task");
            }
            return null;
        }
    }

    private static class QueueEntry
    {
        private final PhotoViewHolder viewHolder;
        private final Photo photo;

        private QueueEntry(PhotoViewHolder viewHolder, Photo photo)
        {
            this.viewHolder = viewHolder;
            this.photo = photo;
        }
    }
}
