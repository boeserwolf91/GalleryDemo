package me.swolf.android.gallery.util;

import java.lang.IllegalArgumentException;
import java.lang.Override;
import java.lang.String;

import android.os.AsyncTask;
import android.os.Build.VERSION_CODES;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * This class represents an {@link AsyncTask} which is handled with a {@link ProgressDialog}.
 *
 * @param <Params>   param types for the execution
 * @param <Progress> param types for the progress states
 * @param <Result>   param type of the result
 */
public abstract class ProgressDrivenAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result>
{
    private final Context applicationContext;
    private Context context;
    private boolean running;

    private final int progressStyle;
    private final boolean cancelableProgress;
    private ProgressDialog progressDialog;

    private int maxProgressValue;
    private int progressValue;

    private int progressTitleId;
    private String progressMessage;

    protected ProgressDrivenAsyncTask(Context context, int progressStyle, boolean cancelableProgress, int progressTitleId)
    {
        this.progressStyle = progressStyle;
        this.cancelableProgress = cancelableProgress;
        this.setProgressTitleId(progressTitleId);

        this.setMaxProgressValue(0);
        this.setProgressValue(0);

        this.applicationContext = context.getApplicationContext();
        this.attachContext(context);
    }

    @Override
    protected void onPreExecute()
    {
        super.onPreExecute();

        this.createProgressDialog();
        this.getProgressDialog().show();
        this.running = true;
    }

    @Nullable
    public Context getContext()
    {
        return this.context;
    }

    public Context getApplicationContext()
    {
        return this.applicationContext;
    }

    /**
     * This must be called if a new activity context gets created.
     * It means that the old one was destroyed already and that the progress must be shown in the new activity
     *
     * @param context activity context
     */
    public void attachContext(Context context)
    {
        this.context = context;

        if (this.getProgressDialog() == null || this.getStatus().equals(Status.FINISHED))
        {
            return;
        }

        this.createProgressDialog();
        this.getProgressDialog().show();
    }

    /**
     * This must be called if the current activity context gets destroyed;
     * for example due to an orientation change of the activity.
     */
    public void detachContext()
    {
        this.dismissDialog();

        this.context = null;
    }

    /**
     * Helper method dismissing the dialog
     */
    protected void dismissDialog()
    {
        if (this.getProgressDialog() == null || !this.getProgressDialog().isShowing())
        {
            return;
        }

        try
        {
            this.progressDialog.dismiss();
        }
        catch (IllegalArgumentException e)
        {
            /*
             * TODO find a better solution for the problem
             * "View=com.android.internal.policy.impl.PhoneWindow$DecorView{1723805a V.E..... R.....ID 0,0-1272,538} not attached to window manager"
             */
            Log.e(this.getClass().getSimpleName(), String.format("The dialog couldn't be dismissed. current context: %s",
                                                                 this.context.getClass().getName()), e);
        }
    }

    @TargetApi(VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCancelled(Result result)
    {
        super.onCancelled(result);

        this.dismissDialog();
        this.running = false;
    }

    @Override
    protected void onCancelled()
    {
        super.onCancelled();

        this.dismissDialog();
        this.running = false;
    }

    @Override
    protected void onPostExecute(Result result)
    {
        super.onPostExecute(result);

        this.dismissDialog();
        this.running = false;
    }

    /**
     * Returns whether the task is running
     *
     * @return whether the task is running
     */
    public boolean isRunning()
    {
        return this.getStatus().equals(Status.RUNNING) && this.running;
    }

    /**
     * Returns the {@link ProgressDialog} of this task
     *
     * @return {@link ProgressDialog}
     */
    public ProgressDialog getProgressDialog()
    {
        return progressDialog;
    }

    /**
     * Returns the maximum progress value; this specifies the highest value for the progress
     *
     * @return max progress value
     */
    public int getMaxProgressValue()
    {
        return maxProgressValue;
    }

    /**
     * Sets the maximum progress value
     *
     * @param maxProgressValue max progress value
     *
     * @see #getMaxProgressValue()
     */
    protected void setMaxProgressValue(int maxProgressValue)
    {
        this.maxProgressValue = maxProgressValue;
        if (this.progressDialog != null)
        {
            this.progressDialog.setMax(this.maxProgressValue);
        }
    }

    /**
     * Returns the current progress value
     *
     * @return current progress
     */
    public int getProgressValue()
    {
        return progressValue;
    }

    /**
     * Sets the current progress value
     *
     * @param progressValue current progress value
     */
    protected void setProgressValue(int progressValue)
    {
        this.progressValue = progressValue;
        if (this.progressDialog != null)
        {
            this.progressDialog.setProgress(this.progressValue);
        }
    }

    /**
     * Returns the progress message
     *
     * @return progress message
     */
    public String getProgressMessage()
    {
        return progressMessage;
    }

    /**
     * Sets the progress message
     *
     * @param progressMessage progress message
     */
    protected void setProgressMessage(String progressMessage)
    {
        this.progressMessage = progressMessage;
        if (this.progressDialog != null)
        {
            this.progressDialog.setMessage(this.progressMessage);
        }
    }

    /**
     * Returns the resource id of the progress title
     *
     * @return resource id of the progress title
     */
    public int getProgressTitleId()
    {
        return progressTitleId;
    }

    /**
     * Sets the resource id of the progress title
     *
     * @param progressTitleId resource id of the progress title
     */
    protected void setProgressTitleId(int progressTitleId)
    {
        this.progressTitleId = progressTitleId;
        if (this.progressDialog != null)
        {
            this.progressDialog.setTitle(this.progressTitleId);
        }
    }

    /**
     * Returns whether this progress is cancelable
     *
     * @return whether this progress is cancelable
     */
    public boolean isCancelableProgress()
    {
        return cancelableProgress;
    }

    /**
     * Creates the progress dialog handling the progress
     */
    private void createProgressDialog()
    {
        if (this.getContext() == null)
        {
            return;
        }
        this.progressDialog = new ProgressDialog(this.getContext());
        this.progressDialog.setProgressStyle(this.progressStyle);
        this.progressDialog.setCancelable(this.isCancelableProgress());

        this.progressDialog.setMax(this.getMaxProgressValue());
        this.progressDialog.setProgress(this.getProgressValue());

        if (this.getProgressTitleId() != 0)
        {
            this.progressDialog.setTitle(this.getProgressTitleId());
        }
        this.progressDialog.setMessage(this.getProgressMessage());
    }
}
