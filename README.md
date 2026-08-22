# CricZen 🏏
> Find your focus. Follow your game.

![CricZen Banner](assets/feature_graphic.png)

CricZen is a minimalist, hyper-personalized cricket companion designed to cut through the noise of generic sports apps. Built specifically for Indian students and fans, it prioritizes **extreme battery efficiency, ultra-low data usage, and ad-free focus**.

Instead of overwhelming you with endless tabs and betting ads, CricZen focuses strictly on what matters: **Your Teams. Your Players. Your Scores.**

## Why CricZen? ✨
- 🧘 **Zero Clutter**: No bloated menus, no irrelevant news. Just a clean, zen-like interface.
- 🔋 **Ultra-Low Data & Battery Mode**: Features a custom "Sniper Fetch" engine and "Memory Lock" anti-spam protocol. In Data Saver mode, the app downloads micro-scorecards (<1KB) and completely hibernates when there is no new action, saving 100% of background data and battery.
- 🎯 **Hyper-Personalized "My Team" Focus**: Pick your teams and let the app filter out the noise.
- 🤖 **Zenny's Memory Bank**: A bundled offline cricket historian! When no matches are live, or during rain delays, Zenny shares "On This Day" milestones and specific trivia about your favorite players using zero internet data.
- 🖼️ **Idol Wallpaper & Fan Mode**: Set your favorite player as your app background.
- 📱 **Home Screen Widget**: Track live scores directly from your home screen with a beautiful Glance widget.
- 🪟 **Picture-in-Picture (PiP)**: Keep a floating mini-scorecard active while you chat or browse.
- 📊 **Glassmorphism Target Track**: Watch run chases on a premium visual progress bar that glows when the chase gets tense and bursts in gold when a team crushes the target!

## Features at a Glance 🚀
* **Modern UI:** Built fully in Jetpack Compose with Material 3.
* **Offline Resilience:** Room database caching keeps your scores accessible in poor networks.
* **Smart Parsing:** Efficiently parses live XML/HTML to extract only the most relevant match data.

## Screenshots 📸
| Dashboard | Fan Mode & News | Home Widget & PiP |
|:---:|:---:|:---:|
| <img src="assets/screenshot_standard.png" width="250"> | <img src="assets/screenshot_fanmode.png" width="250"> | <img src="assets/screenshot_dark.png" width="250"> |

## For Students & Developers 👨‍💻
CricZen is fully open-source and built as a pristine example of modern Android development. 
* **UI:** Jetpack Compose, Glance (App Widgets)
* **Architecture:** MVVM, Clean Architecture, Repository Pattern
* **Local Storage:** Room Database, Preferences DataStore
* **Networking:** Retrofit, OkHttp, Custom Sniper HTML/XML Parsing
* **Background Tasks:** WorkManager for Widget Updates

### Build Instructions
1. Clone the repository: `git clone https://github.com/your-username/criczen.git`
2. Open in Android Studio.
3. Sync Gradle and hit Run!

## License 📜
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
