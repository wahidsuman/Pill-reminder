# My Pills - React Native App

A simple React Native app designed for elderly users with a clean interface showing "My Pills" header and current time.

## Features

- Large, bold text suitable for elderly users
- Clean white background with blue header
- Real-time clock display
- Simple, accessible design

## Prerequisites

Before running the app, make sure you have:

- Node.js (version 16 or higher)
- React Native CLI
- Android Studio (for Android development)
- Java Development Kit (JDK)

## Installation

1. Install dependencies:
```bash
npm install
```

2. For Android development, make sure you have:
   - Android Studio installed
   - Android SDK configured
   - An Android device connected or emulator running

## Running the App

### Android

1. Start the Metro bundler:
```bash
npm start
```

2. In a new terminal, run the Android app:
```bash
npm run android
```

### iOS (if on macOS)

1. Install iOS dependencies:
```bash
cd ios && pod install && cd ..
```

2. Run the iOS app:
```bash
npm run ios
```

## Project Structure

- `App.tsx` - Main application component
- `android/` - Android-specific configuration and native code
- `package.json` - Dependencies and scripts

## Design Features

- **Accessibility**: Large text (36px for title, 28px for time) with high contrast
- **Color Scheme**: Blue header (#2196F3) on white background
- **Typography**: Bold fonts for better readability
- **Real-time Updates**: Clock updates every second

## Future Enhancements

This is a basic version. Future features could include:
- Pill reminder functionality
- Medication tracking
- Dosage schedules
- Visual pill identification