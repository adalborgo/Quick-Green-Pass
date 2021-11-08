package it.dibis.quickgreenpass;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class MyUtils {
    /**
     * Revision control id
     */
    public static final String cvsId = "$Id: MyUtils.java,v 0.1 21/10/2021 23:59:59 adalborgo $";

    public static Point getScreenDimension(Activity activity) {
        // initialize the DisplayMetrics object
        DisplayMetrics deviceDisplayMetrics = new DisplayMetrics();

        // Populate the DisplayMetrics object with the display characteristics
        activity.getWindowManager().getDefaultDisplay().getMetrics(deviceDisplayMetrics);

        // get the width and height
        int width = deviceDisplayMetrics.widthPixels;
        int height = deviceDisplayMetrics.heightPixels;
        return new Point(width, height);
    }

    /**
     * Chech if the pathname exists
     *
     * @param pathname
     * @return boolean true: found; false: not found
     */
    public static boolean checkPathname(String pathname) {
        try {
            return (new File(pathname).exists());
        } catch (Exception e) {
            return false;
        }
    }

    public static int getToolBar(Context context) {
        TypedValue tv = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true);
        int toolbarHeight = context.getResources().getDimensionPixelSize(tv.resourceId);
        // System.out.println("ActionBarSize: " + toolbarHeight);
        return toolbarHeight;
    }

    /**
     * Conversion of a positive literal to int
     *
     * @param s
     * @return int (on error, return Integer.MIN_VALUE)
     */
    public static int stringToIntPositive(String s) {
        try {
            return Integer.valueOf(s);
        } catch (NumberFormatException e) {
            return Integer.MIN_VALUE;
        }
    }

    public static float stringToFloat(String s) {
        try {
            return Float.valueOf(s);
        } catch (NumberFormatException e) {
            return (float) Integer.MIN_VALUE;
        }
    }


}
