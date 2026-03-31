package com.example.smartfit;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartfit.device.DeviceDashboardActivity;
import com.google.android.material.button.MaterialButton;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_activity);

        TextView tvVersion = findViewById(R.id.tv_version);
        MaterialButton btnSettings = findViewById(R.id.btn_about_settings);
        MaterialButton btnDevice = findViewById(R.id.btn_about_device);
        MaterialButton btnShare = findViewById(R.id.btn_about_share);
        MaterialButton btnBack = findViewById(R.id.btn_about_back);

        String appVersion = BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")";
        tvVersion.setText(getString(R.string.about_version_template, appVersion));

        btnSettings.setOnClickListener(v -> startActivity(new Intent(AboutActivity.this, SettingsActivity.class)));

        btnDevice.setOnClickListener(v -> startActivity(new Intent(AboutActivity.this, DeviceDashboardActivity.class)));

        btnShare.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
            shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.about_share_message));
            startActivity(Intent.createChooser(shareIntent, getString(R.string.about_share_chooser)));
        });

        btnBack.setOnClickListener(v -> finish());
    }
}
