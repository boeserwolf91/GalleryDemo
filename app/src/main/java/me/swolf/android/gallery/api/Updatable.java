package me.swolf.android.gallery.api;

import android.content.Context;

public interface Updatable
{
    /**
     * Updates the instance
     *
     * @param context current activity context
     */
    void update(Context context);

    /**
     * Returns the timestamp of the last update
     *
     * @return last update timestamp
     */
    long getLastUpdateTimestamp(); // TODO is it really needed?

    /**
     * Returns whether an update of the instance is recommended
     *
     * @return whether an update of the instance is recommended
     */
    boolean needsToBeUpdated();

    /**
     * Returns the information whether an update is running at the moment
     *
     * @return whether an update is running
     */
    boolean isUpdating();

    /**
     * Detaches the current activity context from the running process.
     * Should be called during the destruction of the activity if {@link #isUpdating()} returns true.
     */
    void detachUpdateContext();

    /**
     * Attaches an activity context to the running process.
     * Should be called when an activity changes or was recreated and {@link #isUpdating()} returns true.
     *
     * @param context current activity context
     */
    void attachUpdateContext(Context context);

    /**
     * Sets a {@link me.swolf.android.gallery.api.Updatable.OnUpdateFinishedListener} running some code when the update process was finished
     *
     * @param listener {@link me.swolf.android.gallery.api.Updatable.OnUpdateFinishedListener}
     */
    void setOnUpdateFinishedListener(OnUpdateFinishedListener listener);

    /**
     * Interface used to run some code when the update process was finished.
     */
    interface OnUpdateFinishedListener
    {
        /**
         * This method will be invoked when a the update process was finished.
         */
        void onUpdateFinished();
    }
}
