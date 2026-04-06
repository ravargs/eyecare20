# Eye Care

Eye Care is an Android application designed to help protect your vision by enforcing the **20-20-20 rule**. 

The concept is simple: for every **20 minutes** you spend looking at a screen, you should look at something that is **20 feet** away for at least **20 seconds**. This app automates that reminder.

---

## Features

- **Continuous Screen Tracking:** Tracks the amount of time your device's screen remains on. The timer pauses automatically when you lock your device or the screen turns off.
- **Enforced Rest Periods:** After 20 minutes of continuous screen use, it displays an unskippable full-screen overlay for 20 seconds, allowing your eyes to rest. 
- **Smart Call Handling:** The app detects when you are on a voice or video call. It will suppress the rest overlay to avoid interrupting your conversations. Furthermore, when you finish a call, the app resets its timer, granting you a fresh 20-minute session before reminding you again.
- **Auto-Start on Boot:** Automatically restarts tracking in the background whenever you restart your device.
- **Foreground Service Notification:** Operates transparently with a silent, persistent notification to reassure you that screen monitoring is actively running.

## Usage Guide

### 1. Initial Setup
When you install and open the app for the first time, you may need to grant a few critical permissions:
- **Display over other apps (Draw Overlay):** Allows the 20-second rest screen to appear over whatever app you are currently using.
- **Post Notifications:** Ensures the background service can maintain continuous tracking.
- **Phone State Permission (optional):** Gives the app the context it needs to pause reminders while you are in a phone call.

### 2. General Operation
Once setup is complete, and the main tracking switch is enabled, simply use your phone as normal. The app will run quietly in the background. 
- Once you reach 20 minutes of screen-on usage, the rest screen will automatically pop up. 
- You will hear an alert sound (if enabled in settings/system) and must wait for the countdown to complete.
- After the countdown reaches zero, you will instantly be returned to exactly what you were doing.

### 3. Voice and Video Calls
If you receive or make a call, the app will recognize the audio/telephony state:
- If a 20-minute threshold is hit *during* a call, the app will quietly reset the timer in the background without interrupting your call.
- Once you hang up, a fresh 20-minute cycle automatically begins.

## Technical Requirements
- Minimum SDK: Varies based on build configuration, but recommended targeting for Android 8.0 (API level 26) and newer.
- For complete accuracy in smart call handling, `READ_PHONE_STATE` might be requested on certain firmware versions.

## Troubleshooting
- **Overlay Not Showing:** Ensure that "Display over other apps" is strictly toggled `ON` in the Android system settings. 
- **App Killed in Background:** Depending on your manufacturer (e.g., Xiaomi, Samsung), you might need to exclude the Eye Care app from aggressive battery optimizations.

## License

This project is licensed under the **GNU Affero General Public License v3.0** (AGPL-3.0). See the [LICENSE](LICENSE) file for details.
