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
        content.setPadding(20, 30, 20, 30);
        
        // Add sample pills
        addPillCard(content, "Vitamin D", "1000 IU", "8:00 AM");
        addPillCard(content, "Blood Pressure", "5mg", "2:00 PM");
        addPillCard(content, "Multivitamin", "1 tablet", "6:00 PM");
        
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
    
    private void addPillCard(LinearLayout parent, String name, String dosage, String time) {
        // Create card container
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundColor(Color.WHITE);
        card.setPadding(30, 25, 30, 25);
        card.setGravity(Gravity.CENTER);
        
        // Add shadow effect (simple border)
        card.setElevation(8);
        
        // Create pill name
        TextView pillName = new TextView(this);
        pillName.setText(name);
        pillName.setTextSize(28);
        pillName.setTextColor(Color.parseColor("#333333"));
        pillName.setGravity(Gravity.CENTER);
        pillName.setTypeface(null, android.graphics.Typeface.BOLD);
        pillName.setPadding(0, 0, 0, 10);
        
        // Create dosage
        TextView pillDosage = new TextView(this);
        pillDosage.setText("Dosage: " + dosage);
        pillDosage.setTextSize(24);
        pillDosage.setTextColor(Color.parseColor("#666666"));
        pillDosage.setGravity(Gravity.CENTER);
        pillDosage.setPadding(0, 0, 0, 8);
        
        // Create time
        TextView pillTime = new TextView(this);
        pillTime.setText("Time: " + time);
        pillTime.setTextSize(24);
        pillTime.setTextColor(Color.parseColor("#666666"));
        pillTime.setGravity(Gravity.CENTER);
        
        // Add views to card
        card.addView(pillName);
        card.addView(pillDosage);
        card.addView(pillTime);
        
        // Create layout params for spacing
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, 20);
        card.setLayoutParams(cardParams);
        
        // Add card to parent
        parent.addView(card);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timeUpdater != null) {
            handler.removeCallbacks(timeUpdater);
        }
    }
}