package me.swolf.android.gallery.util;

import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

public final class Misc
{
    private Misc()
    {
    }

    /**
     * Returns the screen size of the device
     *
     * @param windowManager {@link WindowManager}
     *
     * @return screen size in pixel
     */
    @SuppressLint("NewApi")
    public static Point getScreenSize(WindowManager windowManager)
    {
        Point size = new Point();
        Display display = windowManager.getDefaultDisplay();
        if (VERSION.SDK_INT < VERSION_CODES.HONEYCOMB_MR2)
        {
            //noinspection deprecation
            size.set(display.getWidth(), display.getHeight());
        }
        else
        {
            display.getSize(size);
        }
        return size;
    }

    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp      A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     *
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public static float convertDpToPixel(float dp, Context context)
    {
        // medium density (mdpi) screens have the android default value; It can be used to measure the other dips
        final float BASELINE_DENSITY = 160f;
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return dp * (metrics.densityDpi / BASELINE_DENSITY);
    }

    /**
     * This method converts device specific pixels to density independent pixels.
     *
     * @param px      A value in px (pixels) unit. Which we need to convert into db
     * @param context Context to get resources and device specific display metrics
     *
     * @return A float value to represent dp equivalent to px value
     */
    public static float convertPixelsToDp(float px, Context context)
    {
        // medium density (mdpi) screens have the android default value; It can be used to measure the other dips
        final float BASELINE_DENSITY = 160f;
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return px / (metrics.densityDpi / BASELINE_DENSITY);
    }
}
