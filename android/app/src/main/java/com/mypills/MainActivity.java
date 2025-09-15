package com.mypills;

import android.app.Activity;
import android.app.AlertDialog;
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.ArrayList;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(Color.WHITE);
        layout.setGravity(Gravity.CENTER);
        
        // Header with modern gradient-like background
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setBackgroundColor(Color.parseColor("#6366F1")); // Modern indigo
        header.setGravity(Gravity.CENTER);
        header.setPadding(0, 80, 0, 80);
        
        TextView title = new TextView(this);
        title.setText("My Pills");
        title.setTextSize(42);
        title.setTextColor(Color.WHITE);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setPadding(0, 0, 0, 15);
        title.setShadowLayer(4, 0, 2, Color.parseColor("#1E1B4B")); // Subtle shadow
        
        timeTextView = new TextView(this);
        timeTextView.setTextSize(32);
        timeTextView.setTextColor(Color.parseColor("#E0E7FF")); // Light indigo
        timeTextView.setGravity(Gravity.CENTER);
        timeTextView.setTypeface(null, android.graphics.Typeface.BOLD);
        timeTextView.setShadowLayer(2, 0, 1, Color.parseColor("#1E1B4B"));
        
        header.addView(title);
        header.addView(timeTextView);
        
        layout.addView(header);
        
        // Add some padding to the main content area
        contentLayout = new LinearLayout(this);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setGravity(Gravity.CENTER);
        contentLayout.setPadding(24, 40, 24, 40);
        contentLayout.setBackgroundColor(Color.parseColor("#F8FAFC")); // Modern light gray
        
        // Add sample pills
        addPill("Vitamin D", "1000 IU", "8:00 AM");
        addPill("Blood Pressure", "5mg", "2:00 PM");
        addPill("Multivitamin", "1 tablet", "6:00 PM");
        
        // Add "Add Pill" button
        Button addPillButton = new Button(this);
        addPillButton.setText("+ ADD NEW PILL");
        addPillButton.setTextSize(20);
        addPillButton.setTextColor(Color.WHITE);
        addPillButton.setBackgroundColor(Color.parseColor("#10B981")); // Modern emerald
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
    }
    
    private void addPillCard(String name, String dosage, String time, int index) {
        // Create main card container
        LinearLayout mainCard = new LinearLayout(this);
        mainCard.setOrientation(LinearLayout.HORIZONTAL);
        mainCard.setBackgroundColor(Color.WHITE);
        mainCard.setPadding(24, 24, 24, 24);
        mainCard.setElevation(12); // More prominent shadow
        
        // Create left side with pill info
        LinearLayout pillInfo = new LinearLayout(this);
        pillInfo.setOrientation(LinearLayout.VERTICAL);
        pillInfo.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
        
        // Create pill name
        TextView pillName = new TextView(this);
        pillName.setText(name);
        pillName.setTextSize(30);
        pillName.setTextColor(Color.parseColor("#1F2937")); // Modern dark gray
        pillName.setGravity(Gravity.CENTER);
        pillName.setTypeface(null, android.graphics.Typeface.BOLD);
        pillName.setPadding(0, 0, 0, 12);
        
        // Create dosage
        TextView pillDosage = new TextView(this);
        pillDosage.setText("💊 " + dosage);
        pillDosage.setTextSize(22);
        pillDosage.setTextColor(Color.parseColor("#6B7280")); // Modern medium gray
        pillDosage.setGravity(Gravity.CENTER);
        pillDosage.setPadding(0, 0, 0, 8);
        
        // Create time
        TextView pillTime = new TextView(this);
        pillTime.setText("🕐 " + time);
        pillTime.setTextSize(22);
        pillTime.setTextColor(Color.parseColor("#6B7280")); // Modern medium gray
        pillTime.setGravity(Gravity.CENTER);
        
        // Add views to pill info
        pillInfo.addView(pillName);
        pillInfo.addView(pillDosage);
        pillInfo.addView(pillTime);
        
        // Create Take button
        android.widget.Button takeButton = new android.widget.Button(this);
        takeButton.setText("TAKE");
        takeButton.setTextSize(18);
        takeButton.setTextColor(Color.WHITE);
        takeButton.setBackgroundColor(Color.parseColor("#6366F1")); // Modern indigo
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
        
        // Optional: Show a brief confirmation
        android.widget.Toast.makeText(this, "✅ Pill taken!", android.widget.Toast.LENGTH_SHORT).show();
    }
    
    private void showAddPillDialog() {
        // Create dialog layout
        LinearLayout dialogLayout = new LinearLayout(this);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setPadding(48, 40, 48, 40);
        dialogLayout.setBackgroundColor(Color.parseColor("#F8FAFC")); // Modern light background
        
        // Create input fields
        EditText nameInput = new EditText(this);
        nameInput.setHint("💊 Medication Name (e.g., Aspirin)");
        nameInput.setTextSize(18);
        nameInput.setPadding(24, 24, 24, 24);
        nameInput.setBackgroundColor(Color.WHITE);
        nameInput.setElevation(4);
        
        EditText dosageInput = new EditText(this);
        dosageInput.setHint("📏 Dosage (e.g., 100mg)");
        dosageInput.setTextSize(18);
        dosageInput.setPadding(24, 24, 24, 24);
        dosageInput.setBackgroundColor(Color.WHITE);
        dosageInput.setElevation(4);
        
        EditText timeInput = new EditText(this);
        timeInput.setHint("🕐 Time (e.g., 8:00 AM)");
        timeInput.setTextSize(18);
        timeInput.setPadding(24, 24, 24, 24);
        timeInput.setBackgroundColor(Color.WHITE);
        timeInput.setElevation(4);
        
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
        dialogLayout.addView(nameInput);
        dialogLayout.addView(dosageInput);
        dialogLayout.addView(timeInput);
        
        // Create dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Pill");
        builder.setView(dialogLayout);
        
        builder.setPositiveButton("ADD PILL", (dialog, which) -> {
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
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timeUpdater != null) {
            handler.removeCallbacks(timeUpdater);
        }
    }
}