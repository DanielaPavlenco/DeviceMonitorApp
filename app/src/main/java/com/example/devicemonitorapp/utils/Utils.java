package com.example.devicemonitorapp.utils;

import android.app.Activity;
import android.app.AppOpsManager;
import android.app.Dialog;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.devicemonitorapp.utils.Logger;
import com.example.devicemonitorapp.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.topjohnwu.superuser.Shell;
import com.topjohnwu.superuser.ShellUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * General purpose utility class.
 * Most related to internal tasks/helpers
 */
public final class Utils {

    private Utils() {
        // No instances
    }

    public static boolean isInvalidContext(Context context) {
        if (context == null) return true;
        if (context instanceof Activity) {
            return ((Activity) context).isFinishing();
        }
        return false;
    }

    public static boolean hasUsageStatsPermission(Context context) {
        if (isInvalidContext(context)) return false;
        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    int mode = appOpsManager.checkOpNoThrow(
                            AppOpsManager.OPSTR_GET_USAGE_STATS,
                            applicationInfo.uid,
                            applicationInfo.packageName
                    );
                    return (mode == AppOpsManager.MODE_ALLOWED);
                } else return false;

            } else return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Coverts java.util.Date to a pattern format
     *
     * @param millis the Date reference in milliseconds
     * @param pattern the data patter
     * @return an empty String if {@param date} is illegal, the formatted String otherwise
     */
    public static String dateMillisToString(long millis, String pattern) {
        String s = "Unknown date";
        try  {
            DateFormat df = new SimpleDateFormat(pattern, Locale.getDefault());
            s = df.format(new Date(millis));
        } catch (Exception ignored) {}
        return s;
    }
    /**
     * Runs a command line command and waits for its output
     *
     * @param command the command to tun
     * @param defaultOutput value to return in case of error
     * @return the command output or defaultOutput
     */
    public static String runCommand(String command, String defaultOutput) {
        List<String> results = Shell.sh(command).exec().getOut();
        if (ShellUtils.isValidOutput(results)) {
            return results.get(results.size() - 1);
        }
        return defaultOutput;
    }
    public static String runRootCommand(String command, String defaultOutput) {
        List<String> results = Shell.su(command).exec().getOut();
        if (ShellUtils.isValidOutput(results)) {
            return results.get(results.size() - 1);
        }
        return defaultOutput;
    }

    public static boolean isDeviceRooted() {
        String[] paths = {
                "/system/app/Superuser.apk",
                "/sbin/su",
                "/system/bin/su",
                "/system/xbin/su",
                "/data/local/xbin/su",
                "/data/local/bin/su",
                "/system/sd/xbin/su",
                "/system/bin/failsafe/su",
                "/data/local/su"
        };
        for (String path : paths) {
            if (new File(path).exists()) {
                return true;
            }
        }
        return isRooted();
    }

    private static boolean isRooted() {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec("su");
            if (process.waitFor() == 0) {
                return true;
            }
        } catch (IOException | InterruptedException e) {
            return false;
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
        return false;
    }


    /**
     * Changes the interface text to English (US)
     * @param context is used to get resources
     */
    public static void toEnglish(Context context) {
        if (isInvalidContext(context)) return;

        UserPrefs userPrefs = new UserPrefs(context);
        boolean englishLanguage = (userPrefs.getBoolean("english_language", false));
        if (englishLanguage) {
            Locale locale = new Locale("en");
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
        }
    }


}
