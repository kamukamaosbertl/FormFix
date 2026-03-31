package com.example.smartfit.device.collectors;

import android.content.Context;
import android.os.Build;

import com.example.smartfit.BuildConfig;
import com.example.smartfit.R;
import com.example.smartfit.device.model.InfoCategory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StaticDeviceInfoCollector {

    public static List<InfoCategory> collect(Context context) {
        List<InfoCategory> categories = new ArrayList<>();

        // Device category
        InfoCategory device = new InfoCategory("Device", R.color.cat_purple_50)
                .add("Manufacturer", safe(Build.MANUFACTURER))
                .add("Brand", safe(Build.BRAND))
                .add("Model", safe(Build.MODEL))
                .add("Device", safe(Build.DEVICE))
                .add("Product", safe(Build.PRODUCT))
                .add("Hardware", safe(Build.HARDWARE));
        categories.add(device);

        // OS category
        InfoCategory os = new InfoCategory("Operating System", R.color.cat_purple_100)
                .add("Android", safe(Build.VERSION.RELEASE))
                .add("SDK", String.valueOf(Build.VERSION.SDK_INT))
                .add("Security patch", securityPatch())
                .add("Fingerprint", safe(Build.FINGERPRINT));
        categories.add(os);

        // Locale category
        Locale loc = Locale.getDefault();
        InfoCategory locale = new InfoCategory("Locale", R.color.cat_purple_200)
                .add("Language", safe(loc.getLanguage()))
                .add("Country", safe(loc.getCountry()))
                .add("Locale", safe(loc.toString()));
        categories.add(locale);

        // App build category
        InfoCategory app = new InfoCategory("App Build", R.color.cat_purple_300)
                .add("ApplicationId", BuildConfig.APPLICATION_ID)
                .add("Version", BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")")
                .add("Build type", safe(BuildConfig.BUILD_TYPE))
                .add("Debug", String.valueOf(BuildConfig.DEBUG));
        categories.add(app);

        return categories;
    }

    private static String securityPatch(){
        String patch = Build.VERSION.SECURITY_PATCH;
        return (patch == null || patch.isEmpty()) ? "Unknown" : patch;
    }

    private static String safe(String s){
        return (s == null || s.trim().isEmpty()) ? "Unknown" : s.trim();
    }
}
