package com.mypills;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.EditText;
import android.widget.Button;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.os.Handler;
import androidx.core.app.NotificationCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends Activity {
    private TextView timeTextView;
    private Handler handler = new Handler();
    private Runnable timeUpdater;
    
    // Track pills dynamically
    private ArrayList<String> pillNames = new ArrayList<>();
    private ArrayList<String> pillDosages = new ArrayList<>();
    private ArrayList<String> pillTimes = new ArrayList<>();
    private ArrayList<Boolean> pillsTaken = new ArrayList<>();
    private ArrayList<LinearLayout> pillCards = new ArrayList<>();
    private ArrayList<Button> takeButtons = new ArrayList<>();
    private LinearLayout contentLayout;
    
    // Status tracking
    private TextView takenCountTextView;
    private TextView remainingCountTextView;
    
    // Storage
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "MyPillsPrefs";
    private static final String PILLS_KEY = "saved_pills";
    
    // Notifications
    private static final String CHANNEL_ID = "pill_reminders";
    private static final String CHANNEL_NAME = "Pill Reminders";
    private static final String CHANNEL_DESCRIPTION = "Notifications for pill reminders";
    private static final int NOTIFICATION_ID = 1;
    private static final int TEST_NOTIFICATION_ID = 999;
    
    // Alarm Manager
    private AlarmManager alarmManager;
    private static final String ACTION_PILL_REMINDER = "com.mypills.PILL_REMINDER";
    private static final String EXTRA_PILL_NAME = "pill_name";
    private static final String EXTRA_PILL_DOSAGE = "pill_dosage";
    private static final String EXTRA_PILL_INDEX = "pill_index";
    
    // Permissions
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1001;
    
    // Broadcast receiver for notification actions
    private BroadcastReceiver notificationActionReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize storage
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        
        // Request notification permissions
        requestNotificationPermissions();
        
        // Create notification channel
        createNotificationChannel();
        
        // Initialize alarm manager
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        
        // Setup broadcast receiver for notification actions
        setupNotificationActionReceiver();
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(Color.WHITE);
        layout.setGravity(Gravity.CENTER);
        
        // Header with modern styling inspired by React design
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setBackgroundColor(Color.WHITE);
        header.setGravity(Gravity.CENTER);
        header.setPadding(40, 60, 40, 40);
        
        // Large pill icon
        TextView pillIcon = new TextView(this);
        pillIcon.setText("💊");
        pillIcon.setTextSize(48);
        pillIcon.setGravity(Gravity.CENTER);
        pillIcon.setPadding(0, 0, 0, 10);
        
        // App title with better typography
        TextView title = new TextView(this);
        title.setText("My Pills");
        title.setTextSize(36);
        title.setTextColor(Color.parseColor("#1F2937")); // Dark gray
        title.setGravity(Gravity.CENTER);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setPadding(0, 0, 0, 15);
        
        // Date display
        TextView dateTextView = new TextView(this);
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMM d", Locale.getDefault());
        dateTextView.setText(dateFormat.format(new Date()));
        dateTextView.setTextSize(20);
        dateTextView.setTextColor(Color.parseColor("#6B7280")); // Medium gray
        dateTextView.setGravity(Gravity.CENTER);
        dateTextView.setPadding(0, 0, 0, 5);
        
        // Time display with modern styling
        timeTextView = new TextView(this);
        timeTextView.setTextSize(28);
        timeTextView.setTextColor(Color.parseColor("#2563EB")); // Blue
        timeTextView.setGravity(Gravity.CENTER);
        timeTextView.setTypeface(null, android.graphics.Typeface.BOLD);
        
        header.addView(pillIcon);
        header.addView(title);
        header.addView(dateTextView);
        header.addView(timeTextView);
        
        layout.addView(header);
        
        // Status cards section
        LinearLayout statusCardsLayout = new LinearLayout(this);
        statusCardsLayout.setOrientation(LinearLayout.HORIZONTAL);
        statusCardsLayout.setGravity(Gravity.CENTER);
        statusCardsLayout.setPadding(24, 20, 24, 20);
        statusCardsLayout.setBackgroundColor(Color.parseColor("#F9FAFB")); // Light gray background
        
        // Taken pills card
        LinearLayout takenCard = new LinearLayout(this);
        takenCard.setOrientation(LinearLayout.VERTICAL);
        takenCard.setGravity(Gravity.CENTER);
        takenCard.setPadding(40, 30, 40, 30);
        takenCard.setBackgroundColor(Color.parseColor("#DCFCE7")); // Light green
        takenCard.setElevation(4);
        
        takenCountTextView = new TextView(this);
        takenCountTextView.setText("0");
        takenCountTextView.setTextSize(48);
        takenCountTextView.setTextColor(Color.parseColor("#166534")); // Dark green
        takenCountTextView.setTypeface(null, android.graphics.Typeface.BOLD);
        takenCountTextView.setGravity(Gravity.CENTER);
        
        TextView takenLabel = new TextView(this);
        takenLabel.setText("Taken");
        takenLabel.setTextSize(20);
        takenLabel.setTextColor(Color.parseColor("#16A34A")); // Medium green
        takenLabel.setTypeface(null, android.graphics.Typeface.BOLD);
        takenLabel.setGravity(Gravity.CENTER);
        
        takenCard.addView(takenCountTextView);
        takenCard.addView(takenLabel);
        
        // Remaining pills card
        LinearLayout remainingCard = new LinearLayout(this);
        remainingCard.setOrientation(LinearLayout.VERTICAL);
        remainingCard.setGravity(Gravity.CENTER);
        remainingCard.setPadding(40, 30, 40, 30);
        remainingCard.setBackgroundColor(Color.parseColor("#FED7AA")); // Light orange
        remainingCard.setElevation(4);
        
        remainingCountTextView = new TextView(this);
        remainingCountTextView.setText("0");
        remainingCountTextView.setTextSize(48);
        remainingCountTextView.setTextColor(Color.parseColor("#C2410C")); // Dark orange
        remainingCountTextView.setTypeface(null, android.graphics.Typeface.BOLD);
        remainingCountTextView.setGravity(Gravity.CENTER);
        
        TextView remainingLabel = new TextView(this);
        remainingLabel.setText("Remaining");
        remainingLabel.setTextSize(20);
        remainingLabel.setTextColor(Color.parseColor("#EA580C")); // Medium orange
        remainingLabel.setTypeface(null, android.graphics.Typeface.BOLD);
        remainingLabel.setGravity(Gravity.CENTER);
        
        remainingCard.addView(remainingCountTextView);
        remainingCard.addView(remainingLabel);
        
        // Add cards to layout with spacing
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        cardParams.setMargins(8, 0, 8, 0);
        takenCard.setLayoutParams(cardParams);
        remainingCard.setLayoutParams(cardParams);
        
        statusCardsLayout.addView(takenCard);
        statusCardsLayout.addView(remainingCard);
        layout.addView(statusCardsLayout);
        
        // Add some padding to the main content area
        contentLayout = new LinearLayout(this);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setGravity(Gravity.CENTER);
        contentLayout.setPadding(24, 40, 24, 40);
        contentLayout.setBackgroundColor(Color.parseColor("#F8FAFC")); // Modern light gray
        
        // Load saved pills or add sample pills
        if (!loadPillsFromStorage()) {
            // If no saved pills, add sample pills
            addPill("Vitamin D", "1000 IU", "8:00 AM");
            addPill("Blood Pressure", "5mg", "2:00 PM");
            addPill("Multivitamin", "1 tablet", "6:00 PM");
        }
        
        // Schedule notifications for all pills
        scheduleAllPillNotifications();
        
        // Update status counts
        updateStatusCounts();
        
        // Add "Add Pill" button with modern styling
        Button addPillButton = new Button(this);
        addPillButton.setText("+ Add New Medication");
        addPillButton.setTextSize(22);
        addPillButton.setTextColor(Color.WHITE);
        addPillButton.setBackgroundColor(Color.parseColor("#2563EB")); // Blue
        addPillButton.setTypeface(null, android.graphics.Typeface.BOLD);
        
        // Set button size and modern styling
        LinearLayout.LayoutParams addButtonParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        addButtonParams.height = 88; // Slightly taller
        addButtonParams.setMargins(0, 32, 0, 0);
        addPillButton.setLayoutParams(addButtonParams);
        addPillButton.setElevation(8); // Modern shadow
        
        // Set button click listener
        addPillButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddPillDialog();
            }
        });
        
        contentLayout.addView(addPillButton);
        
        // Add "Test Notification" button
        Button testNotificationButton = new Button(this);
        testNotificationButton.setText("🔔 TEST NOTIFICATION");
        testNotificationButton.setTextSize(18);
        testNotificationButton.setTextColor(Color.WHITE);
        testNotificationButton.setBackgroundColor(Color.parseColor("#8B5CF6")); // Modern purple
        testNotificationButton.setTypeface(null, android.graphics.Typeface.BOLD);
        
        // Set button size and styling
        LinearLayout.LayoutParams testButtonParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        testButtonParams.height = 80;
        testButtonParams.setMargins(0, 16, 0, 0);
        testNotificationButton.setLayoutParams(testButtonParams);
        testNotificationButton.setElevation(6);
        
        // Set button click listener
        testNotificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTestNotification();
            }
        });
        
        contentLayout.addView(testNotificationButton);
        
        // Add "Reschedule Notifications" button
        Button rescheduleButton = new Button(this);
        rescheduleButton.setText("📅 RESCHEDULE NOTIFICATIONS");
        rescheduleButton.setTextSize(16);
        rescheduleButton.setTextColor(Color.WHITE);
        rescheduleButton.setBackgroundColor(Color.parseColor("#059669")); // Modern emerald
        rescheduleButton.setTypeface(null, android.graphics.Typeface.BOLD);
        
        // Set button size and styling
        LinearLayout.LayoutParams rescheduleButtonParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        rescheduleButtonParams.height = 70;
        rescheduleButtonParams.setMargins(0, 12, 0, 0);
        rescheduleButton.setLayoutParams(rescheduleButtonParams);
        rescheduleButton.setElevation(4);
        
        // Set button click listener
        rescheduleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scheduleAllPillNotifications();
            }
        });
        
        contentLayout.addView(rescheduleButton);
        
        // Add "Test Background Notification" button
        Button testBackgroundButton = new Button(this);
        testBackgroundButton.setText("🌙 TEST BACKGROUND NOTIFICATION");
        testBackgroundButton.setTextSize(16);
        testBackgroundButton.setTextColor(Color.WHITE);
        testBackgroundButton.setBackgroundColor(Color.parseColor("#DC2626")); // Modern red
        testBackgroundButton.setTypeface(null, android.graphics.Typeface.BOLD);
        
        // Set button size and styling
        LinearLayout.LayoutParams testBackgroundButtonParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        testBackgroundButtonParams.height = 70;
        testBackgroundButtonParams.setMargins(0, 12, 0, 0);
        testBackgroundButton.setLayoutParams(testBackgroundButtonParams);
        testBackgroundButton.setElevation(4);
        
        // Set button click listener
        testBackgroundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testBackgroundNotification();
            }
        });
        
        contentLayout.addView(testBackgroundButton);
        
        // Add "Test Notification Actions" button
        Button testActionsButton = new Button(this);
        testActionsButton.setText("🔔 TEST NOTIFICATION ACTIONS");
        testActionsButton.setTextSize(16);
        testActionsButton.setTextColor(Color.WHITE);
        testActionsButton.setBackgroundColor(Color.parseColor("#7C3AED")); // Modern purple
        testActionsButton.setTypeface(null, android.graphics.Typeface.BOLD);
        
        // Set button size and styling
        LinearLayout.LayoutParams testActionsButtonParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        testActionsButtonParams.height = 70;
        testActionsButtonParams.setMargins(0, 12, 0, 0);
        testActionsButton.setLayoutParams(testActionsButtonParams);
        testActionsButton.setElevation(4);
        
        // Set button click listener
        testActionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testNotificationActions();
            }
        });
        
        contentLayout.addView(testActionsButton);
        layout.addView(contentLayout);
        
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
    
    private void addPill(String name, String dosage, String time) {
        // Add to lists
        pillNames.add(name);
        pillDosages.add(dosage);
        pillTimes.add(time);
        pillsTaken.add(false);
        
        // Create the pill card
        addPillCard(name, dosage, time, pillNames.size() - 1);
        
        // Schedule notification for this pill
        int pillIndex = pillNames.size() - 1;
        schedulePillNotification(pillIndex, name, dosage, time);
        
        // Save to storage
        savePillsToStorage();
        
        // Update status counts
        updateStatusCounts();
    }
    
    private void addPillCard(String name, String dosage, String time, int index) {
        // Create main card container with modern styling
        LinearLayout mainCard = new LinearLayout(this);
        mainCard.setOrientation(LinearLayout.HORIZONTAL);
        mainCard.setGravity(Gravity.CENTER_VERTICAL);
        mainCard.setBackgroundColor(Color.WHITE);
        mainCard.setPadding(32, 24, 32, 24);
        mainCard.setElevation(12);
        
        // Create colored pill icon
        TextView pillIcon = new TextView(this);
        pillIcon.setText("💊");
        pillIcon.setTextSize(48);
        pillIcon.setPadding(0, 0, 20, 0);
        
        // Create left side with pill info
        LinearLayout pillInfo = new LinearLayout(this);
        pillInfo.setOrientation(LinearLayout.VERTICAL);
        pillInfo.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
        
        // Create pill name with better typography
        TextView pillName = new TextView(this);
        pillName.setText(name);
        pillName.setTextSize(28);
        pillName.setTextColor(Color.parseColor("#1F2937")); // Modern dark gray
        pillName.setGravity(Gravity.START);
        pillName.setTypeface(null, android.graphics.Typeface.BOLD);
        pillName.setPadding(0, 0, 0, 8);
        
        // Create dosage
        TextView pillDosage = new TextView(this);
        pillDosage.setText("📏 " + dosage);
        pillDosage.setTextSize(20);
        pillDosage.setTextColor(Color.parseColor("#6B7280")); // Modern medium gray
        pillDosage.setGravity(Gravity.START);
        pillDosage.setPadding(0, 0, 0, 4);
        
        // Create time
        TextView pillTime = new TextView(this);
        pillTime.setText("🕐 " + time);
        pillTime.setTextSize(20);
        pillTime.setTextColor(Color.parseColor("#2563EB")); // Blue
        pillTime.setGravity(Gravity.START);
        pillTime.setTypeface(null, android.graphics.Typeface.BOLD);
        
        // Add views to pill info
        pillInfo.addView(pillName);
        pillInfo.addView(pillDosage);
        pillInfo.addView(pillTime);
        
        // Create Take button with modern styling
        android.widget.Button takeButton = new android.widget.Button(this);
        takeButton.setText("TAKE");
        takeButton.setTextSize(18);
        takeButton.setTextColor(Color.WHITE);
        takeButton.setBackgroundColor(Color.parseColor("#2563EB")); // Blue
        takeButton.setTypeface(null, android.graphics.Typeface.BOLD);
        
        // Set button size (at least 60px tall)
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        buttonParams.width = 128; // Slightly wider
        buttonParams.height = 88; // Slightly taller
        buttonParams.setMargins(24, 0, 0, 0);
        takeButton.setLayoutParams(buttonParams);
        takeButton.setElevation(6); // Modern shadow
        
        // Set button click listener
        final int pillIndex = index;
        takeButton.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                takePill(pillIndex);
            }
        });
        
        // Store references
        pillCards.add(mainCard);
        takeButtons.add(takeButton);
        
        // Add views to main card
        mainCard.addView(pillIcon);
        mainCard.addView(pillInfo);
        mainCard.addView(takeButton);
        
        // Create layout params for spacing
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, 24); // More spacing between cards
        mainCard.setLayoutParams(cardParams);
        
        // Add card to content layout (before the Add Pill button)
        contentLayout.addView(mainCard, contentLayout.getChildCount() - 1);
    }
    
    private void takePill(int index) {
        pillsTaken.set(index, true);
        
        // Change card appearance to modern green
        pillCards.get(index).setBackgroundColor(Color.parseColor("#ECFDF5")); // Modern light green
        
        // Change button appearance
        takeButtons.get(index).setText("✓ TAKEN");
        takeButtons.get(index).setBackgroundColor(Color.parseColor("#10B981")); // Modern emerald
        takeButtons.get(index).setEnabled(false);
        
        // Save to storage
        savePillsToStorage();
        
        // Update status counts
        updateStatusCounts();
        
        // Optional: Show a brief confirmation
        android.widget.Toast.makeText(this, "✅ Pill taken!", android.widget.Toast.LENGTH_SHORT).show();
    }
    
    private void updateStatusCounts() {
        int takenCount = 0;
        int remainingCount = 0;
        
        for (Boolean taken : pillsTaken) {
            if (taken) {
                takenCount++;
            } else {
                remainingCount++;
            }
        }
        
        if (takenCountTextView != null) {
            takenCountTextView.setText(String.valueOf(takenCount));
        }
        if (remainingCountTextView != null) {
            remainingCountTextView.setText(String.valueOf(remainingCount));
        }
    }
    
    private void showAddPillDialog() {
        // Create dialog layout with modern styling
        LinearLayout dialogLayout = new LinearLayout(this);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setPadding(48, 40, 48, 40);
        dialogLayout.setBackgroundColor(Color.WHITE);
        
        // Title
        TextView titleText = new TextView(this);
        titleText.setText("Add Medication");
        titleText.setTextSize(28);
        titleText.setTextColor(Color.parseColor("#1F2937"));
        titleText.setTypeface(null, android.graphics.Typeface.BOLD);
        titleText.setGravity(Gravity.CENTER);
        titleText.setPadding(0, 0, 0, 32);
        
        // Create input fields with better styling
        EditText nameInput = new EditText(this);
        nameInput.setHint("Medication Name");
        nameInput.setTextSize(20);
        nameInput.setPadding(24, 24, 24, 24);
        nameInput.setBackgroundColor(Color.parseColor("#F9FAFB"));
        nameInput.setHintTextColor(Color.parseColor("#9CA3AF"));
        nameInput.setTextColor(Color.parseColor("#1F2937"));
        
        EditText dosageInput = new EditText(this);
        dosageInput.setHint("Dosage (e.g., 100mg)");
        dosageInput.setTextSize(20);
        dosageInput.setPadding(24, 24, 24, 24);
        dosageInput.setBackgroundColor(Color.parseColor("#F9FAFB"));
        dosageInput.setHintTextColor(Color.parseColor("#9CA3AF"));
        dosageInput.setTextColor(Color.parseColor("#1F2937"));
        
        EditText timeInput = new EditText(this);
        timeInput.setHint("Time (e.g., 8:00 AM)");
        timeInput.setTextSize(20);
        timeInput.setPadding(24, 24, 24, 24);
        timeInput.setBackgroundColor(Color.parseColor("#F9FAFB"));
        timeInput.setHintTextColor(Color.parseColor("#9CA3AF"));
        timeInput.setTextColor(Color.parseColor("#1F2937"));
        
        // Add spacing between fields
        LinearLayout.LayoutParams inputParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        inputParams.setMargins(0, 0, 0, 24); // More spacing
        nameInput.setLayoutParams(inputParams);
        dosageInput.setLayoutParams(inputParams);
        timeInput.setLayoutParams(inputParams);
        
        // Add fields to dialog
        dialogLayout.addView(titleText);
        dialogLayout.addView(nameInput);
        dialogLayout.addView(dosageInput);
        dialogLayout.addView(timeInput);
        
        // Create dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogLayout);
        
        builder.setPositiveButton("Add Pill", (dialog, which) -> {
            String name = nameInput.getText().toString().trim();
            String dosage = dosageInput.getText().toString().trim();
            String time = timeInput.getText().toString().trim();
            
            // Basic form validation
            if (name.isEmpty() || dosage.isEmpty() || time.isEmpty()) {
                android.widget.Toast.makeText(this, "Please fill in all fields!", android.widget.Toast.LENGTH_LONG).show();
                return;
            }
            
            // Add the new pill
            addPill(name, dosage, time);
            android.widget.Toast.makeText(this, "Pill added successfully!", android.widget.Toast.LENGTH_SHORT).show();
        });
        
        builder.setNegativeButton("CANCEL", (dialog, which) -> {
            dialog.dismiss();
        });
        
        AlertDialog dialog = builder.create();
        dialog.show();
        
        // Style the buttons
        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        
        if (positiveButton != null) {
            positiveButton.setTextSize(18);
            positiveButton.setTextColor(Color.WHITE);
            positiveButton.setBackgroundColor(Color.parseColor("#10B981")); // Modern emerald
            positiveButton.setPadding(32, 24, 32, 24);
            positiveButton.setElevation(6);
        }
        
        if (negativeButton != null) {
            negativeButton.setTextSize(18);
            negativeButton.setTextColor(Color.WHITE);
            negativeButton.setBackgroundColor(Color.parseColor("#EF4444")); // Modern red
            negativeButton.setPadding(32, 24, 32, 24);
            negativeButton.setElevation(6);
        }
    }
    
    // Storage methods
    private void savePillsToStorage() {
        try {
            JSONArray pillsArray = new JSONArray();
            
            for (int i = 0; i < pillNames.size(); i++) {
                JSONObject pillObject = new JSONObject();
                pillObject.put("name", pillNames.get(i));
                pillObject.put("dosage", pillDosages.get(i));
                pillObject.put("time", pillTimes.get(i));
                pillObject.put("taken", pillsTaken.get(i));
                pillsArray.put(pillObject);
            }
            
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(PILLS_KEY, pillsArray.toString());
            editor.apply();
            
        } catch (JSONException e) {
            android.widget.Toast.makeText(this, "Error saving pills: " + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            android.widget.Toast.makeText(this, "Storage error: " + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
        }
    }
    
    private boolean loadPillsFromStorage() {
        try {
            String pillsJson = sharedPreferences.getString(PILLS_KEY, null);
            if (pillsJson == null || pillsJson.isEmpty()) {
                return false; // No saved pills
            }
            
            JSONArray pillsArray = new JSONArray(pillsJson);
            
            // Clear existing pills
            pillNames.clear();
            pillDosages.clear();
            pillTimes.clear();
            pillsTaken.clear();
            
            // Load pills from storage
            for (int i = 0; i < pillsArray.length(); i++) {
                JSONObject pillObject = pillsArray.getJSONObject(i);
                String name = pillObject.getString("name");
                String dosage = pillObject.getString("dosage");
                String time = pillObject.getString("time");
                boolean taken = pillObject.getBoolean("taken");
                
                // Add to lists
                pillNames.add(name);
                pillDosages.add(dosage);
                pillTimes.add(time);
                pillsTaken.add(taken);
                
                // Create the pill card
                addPillCard(name, dosage, time, pillNames.size() - 1);
                
                // If pill was taken, update its appearance
                if (taken) {
                    int index = pillNames.size() - 1;
                    pillCards.get(index).setBackgroundColor(Color.parseColor("#ECFDF5"));
                    takeButtons.get(index).setText("✓ TAKEN");
                    takeButtons.get(index).setBackgroundColor(Color.parseColor("#10B981"));
                    takeButtons.get(index).setEnabled(false);
                }
            }
            
            return true; // Successfully loaded pills
            
        } catch (JSONException e) {
            android.widget.Toast.makeText(this, "Error loading pills: " + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
            return false;
        } catch (Exception e) {
            android.widget.Toast.makeText(this, "Storage error: " + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
            return false;
        }
    }
    
    // Permission and notification methods
    private void requestNotificationPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) 
                != PackageManager.PERMISSION_GRANTED) {
                
                // Show explanation dialog
                new AlertDialog.Builder(this)
                    .setTitle("🔔 Notification Permission Required")
                    .setMessage("My Pills needs notification permission to send you pill reminders even when the app is closed. This is essential for medication reminders!")
                    .setPositiveButton("Grant Permission", (dialog, which) -> {
                        ActivityCompat.requestPermissions(this, 
                            new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 
                            NOTIFICATION_PERMISSION_REQUEST_CODE);
                    })
                    .setNegativeButton("Later", (dialog, which) -> {
                        android.widget.Toast.makeText(this, "You can enable notifications in Settings later", android.widget.Toast.LENGTH_LONG).show();
                    })
                    .setCancelable(false)
                    .show();
            }
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                android.widget.Toast.makeText(this, "✅ Notification permission granted! Pill reminders will work in background.", android.widget.Toast.LENGTH_LONG).show();
            } else {
                android.widget.Toast.makeText(this, "⚠️ Notification permission denied. Reminders may not work when app is closed.", android.widget.Toast.LENGTH_LONG).show();
            }
        }
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESCRIPTION);
            channel.enableLights(true);
            channel.setLightColor(Color.parseColor("#6366F1"));
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 500, 200, 500});
            channel.setShowBadge(true);
            channel.setLockscreenVisibility(android.app.Notification.VISIBILITY_PUBLIC);
            channel.setBypassDnd(false);
            channel.setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI, null);
            
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    
    private void showTestNotification() {
        // Create intent for when notification is tapped
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 0, intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        // Create notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Use system icon
            .setContentTitle("💊 My Pills - Test Notification")
            .setContentText("This is a test notification from your pill reminder app!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setColor(Color.parseColor("#6366F1"))
            .setStyle(new NotificationCompat.BigTextStyle()
                .bigText("This is a test notification from your pill reminder app! " +
                        "If you can see this, notifications are working correctly. " +
                        "Future pill reminders will appear like this."));
        
        // Show notification
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
        
        // Show toast confirmation
        android.widget.Toast.makeText(this, "🔔 Test notification sent!", android.widget.Toast.LENGTH_SHORT).show();
    }
    
    private void testBackgroundNotification() {
        // Schedule a test notification for 10 seconds from now
        Calendar testTime = Calendar.getInstance();
        testTime.add(Calendar.SECOND, 10);
        
        Intent intent = new Intent(this, PillReminderReceiver.class);
        intent.setAction(ACTION_PILL_REMINDER);
        intent.putExtra(EXTRA_PILL_NAME, "Test Background Pill");
        intent.putExtra(EXTRA_PILL_DOSAGE, "1 tablet");
        intent.putExtra(EXTRA_PILL_INDEX, 999);
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            this, 
            999,
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                testTime.getTimeInMillis(),
                pendingIntent
            );
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                testTime.getTimeInMillis(),
                pendingIntent
            );
        }
        
        android.widget.Toast.makeText(this, "🌙 Background test scheduled! Close the app and wait 10 seconds.", android.widget.Toast.LENGTH_LONG).show();
    }
    
    private void testNotificationActions() {
        // Schedule a test notification with actions for 5 seconds from now
        Calendar testTime = Calendar.getInstance();
        testTime.add(Calendar.SECOND, 5);
        
        Intent intent = new Intent(this, PillReminderReceiver.class);
        intent.setAction(ACTION_PILL_REMINDER);
        intent.putExtra(EXTRA_PILL_NAME, "Test Action Pill");
        intent.putExtra(EXTRA_PILL_DOSAGE, "1 tablet");
        intent.putExtra(EXTRA_PILL_INDEX, 888); // Use unique index for test
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            this, 
            888,
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                testTime.getTimeInMillis(),
                pendingIntent
            );
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                testTime.getTimeInMillis(),
                pendingIntent
            );
        }
        
        android.widget.Toast.makeText(this, "🔔 Test notification with actions scheduled! Wait 5 seconds and try the Take/Snooze buttons.", android.widget.Toast.LENGTH_LONG).show();
    }
    
    // Pill notification scheduling methods
    private void schedulePillNotification(int pillIndex, String pillName, String pillDosage, String timeString) {
        try {
            // Parse time string (e.g., "8:00 AM", "2:00 PM")
            Calendar calendar = parseTimeString(timeString);
            if (calendar == null) {
                android.widget.Toast.makeText(this, "Invalid time format: " + timeString, android.widget.Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Create intent for the alarm
            Intent intent = new Intent(this, PillReminderReceiver.class);
            intent.setAction(ACTION_PILL_REMINDER);
            intent.putExtra(EXTRA_PILL_NAME, pillName);
            intent.putExtra(EXTRA_PILL_DOSAGE, pillDosage);
            intent.putExtra(EXTRA_PILL_INDEX, pillIndex);
            
            // Create pending intent with unique request code
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, 
                pillIndex, // Use pill index as unique request code
                intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            
            // Schedule daily repeating alarm
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
                );
                // Set repeating alarm for daily notifications
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
                );
            } else {
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
                );
            }
            
        } catch (Exception e) {
            android.widget.Toast.makeText(this, "Error scheduling notification: " + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
        }
    }
    
    private Calendar parseTimeString(String timeString) {
        try {
            // Remove extra spaces and convert to standard format
            timeString = timeString.trim().toUpperCase();
            
            // Parse formats like "8:00 AM", "2:00 PM", "14:00", etc.
            SimpleDateFormat format;
            if (timeString.contains("AM") || timeString.contains("PM")) {
                format = new SimpleDateFormat("h:mm a", Locale.US);
            } else {
                format = new SimpleDateFormat("H:mm", Locale.US);
            }
            
            Date time = format.parse(timeString);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(time);
            
            // Set to today's date
            Calendar today = Calendar.getInstance();
            calendar.set(Calendar.YEAR, today.get(Calendar.YEAR));
            calendar.set(Calendar.MONTH, today.get(Calendar.MONTH));
            calendar.set(Calendar.DAY_OF_MONTH, today.get(Calendar.DAY_OF_MONTH));
            
            // If the time has already passed today, set for tomorrow
            if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }
            
            return calendar;
            
        } catch (Exception e) {
            return null;
        }
    }
    
    private void scheduleAllPillNotifications() {
        // Cancel all existing alarms first
        cancelAllPillNotifications();
        
        // Schedule notifications for all pills
        for (int i = 0; i < pillNames.size(); i++) {
            schedulePillNotification(i, pillNames.get(i), pillDosages.get(i), pillTimes.get(i));
        }
        
        android.widget.Toast.makeText(this, "📅 Scheduled " + pillNames.size() + " pill reminders", android.widget.Toast.LENGTH_SHORT).show();
    }
    
    private void cancelAllPillNotifications() {
        for (int i = 0; i < pillNames.size(); i++) {
            cancelPillNotification(i);
        }
    }
    
    private void cancelPillNotification(int pillIndex) {
        Intent intent = new Intent(this, PillReminderReceiver.class);
        intent.setAction(ACTION_PILL_REMINDER);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            this, 
            pillIndex, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarmManager.cancel(pendingIntent);
    }
    
    private void setupNotificationActionReceiver() {
        notificationActionReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("com.mypills.PILL_TAKEN".equals(intent.getAction())) {
                    int pillIndex = intent.getIntExtra("pill_index", -1);
                    if (pillIndex >= 0 && pillIndex < pillsTaken.size()) {
                        // Update the pill status
                        pillsTaken.set(pillIndex, true);
                        
                        // Update the UI
                        updatePillCardAppearance(pillIndex);
                        
                        // Update status counts
                        updateStatusCounts();
                        
                        // Save to storage
                        savePillsToStorage();
                    }
                }
            }
        };
        
        // Register the receiver
        IntentFilter filter = new IntentFilter("com.mypills.PILL_TAKEN");
        registerReceiver(notificationActionReceiver, filter);
    }
    
    private void updatePillCardAppearance(int index) {
        if (index < pillCards.size() && index < takeButtons.size()) {
            // Update card background
            pillCards.get(index).setBackgroundColor(Color.parseColor("#ECFDF5")); // Light green
            
            // Update button appearance
            takeButtons.get(index).setText("✓ TAKEN");
            takeButtons.get(index).setBackgroundColor(Color.parseColor("#10B981")); // Green
            takeButtons.get(index).setEnabled(false);
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // Save pills when app goes to background
        savePillsToStorage();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Save pills when app is destroyed
        savePillsToStorage();
        if (timeUpdater != null) {
            handler.removeCallbacks(timeUpdater);
        }
        // Unregister broadcast receiver
        if (notificationActionReceiver != null) {
            unregisterReceiver(notificationActionReceiver);
        }
    }
}