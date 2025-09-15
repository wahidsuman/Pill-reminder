package com.mypills;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.graphics.Color;
import android.view.Gravity;
import android.os.Handler;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {
    private TextView timeTextView;
    private Handler handler = new Handler();
    private Runnable timeUpdater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(Color.WHITE);
        layout.setGravity(Gravity.CENTER);
        
        // Header with blue background
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setBackgroundColor(Color.parseColor("#2196F3"));
        header.setGravity(Gravity.CENTER);
        header.setPadding(0, 60, 0, 60);
        
        TextView title = new TextView(this);
        title.setText("My Pills");
        title.setTextSize(36);
        title.setTextColor(Color.WHITE);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        
        timeTextView = new TextView(this);
        timeTextView.setTextSize(28);
        timeTextView.setTextColor(Color.WHITE);
        timeTextView.setGravity(Gravity.CENTER);
        timeTextView.setTypeface(null, android.graphics.Typeface.BOLD);
        
        header.addView(title);
        header.addView(timeTextView);
        
        layout.addView(header);
        
        // Add some padding to the main content area
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER);
        content.setPadding(0, 50, 0, 50);
        
        layout.addView(content);
        
        setContentView(layout);
        
        // Start time updater
        updateTime();
        startTimeUpdater();
    }
    
    private void updateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        String currentTime = sdf.format(new Date());
        timeTextView.setText(currentTime);
    }
    
    private void startTimeUpdater() {
        timeUpdater = new Runnable() {
            @Override
            public void run() {
                updateTime();
                handler.postDelayed(this, 1000); // Update every second
            }
        };
        handler.post(timeUpdater);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timeUpdater != null) {
            handler.removeCallbacks(timeUpdater);
        }
    }
}