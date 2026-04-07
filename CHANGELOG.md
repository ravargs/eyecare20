# Changelog / Releases

All notable changes to this project will be documented in this file.

## [1.1.0] - Upcoming Release

### Added
- **UI Enhancement**: Replaced the plain text timer with a smooth, circular timer animation using `CircularProgressIndicator`.
- **System Integration**: Added a 20-minute intelligence delay before resuming the screen-time counter after hanging up voice or video calls, preventing the overlay from abruptly appearing right after calls finish.
    - **Daily Statistics**: Implemented daily screen-time tracking that saves automatically and displays your total usage (e.g., "Today's Screen time: 4h 15m") neatly below the circular timer.

### Changed
- **Branding**: Updated the application name to "Eye Care 20" and changed the application launcher icon across all density sizes.
### Fixed
- **Overlay Crash**: Resolved an `IllegalArgumentException` crash on newer versions when drawing the timer overlay. Correctly wrapped the background Service context with the app's Material Theme using `ContextThemeWrapper`.

## [1.0.0] - Initial Release

### Added
- **Core Functionality**: Native Android application enforcing the standard 20/20/20 eye care rule to reduce eye strain.
- **Background Engine**: Integrated continuous background monitoring of screen-on time.
- **Enforcement Overlay**: Automatic customizable 20-second countdown full-screen overlay with notification sound alerts.
- **Seamless UX**: Designed the application to automatically restore the user's previous screen or task upon completion of the rest window.
