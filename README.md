# 🛡️ BelieveMe Parental Control App

[![License: Proprietary](https://img.shields.io/badge/License-Proprietary-red.svg)](LICENSE)
[![Maintenance](https://img.shields.io/badge/Maintained%3F-yes-green.svg)](https://github.com/your-github-username/BelieveMe/graphs/commit-activity)
[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen.svg)](https://www.android.com/)
[![Telegram Bot](https://img.shields.io/badge/Control_via-Telegram_Bot-blue.svg)](https://telegram.org/)

**BelieveMe** is a powerful parental control application designed to help parents monitor and manage their children's device usage effectively. This application utilizes a **Telegram Bot** to receive commands, allowing for remote interaction and oversight. **Please note that this application is not open source.**

**Control your child's device remotely through a Telegram Bot!**

## ✨ Available Commands (via Telegram Bot) ✨

BelieveMe utilizes a command-based system accessible through your configured Telegram Bot. Simply send the following commands to the bot to trigger the corresponding actions on the child's device.

### 🎭 Stealth Mode

Control the visibility of the BelieveMe application on the child's device.

* `/hide` — Hide the application icon from the app drawer. This makes the app discreet and less likely to be tampered with.
* `/unhide` — Restore the application icon to the app drawer, making it visible again.

### ⚡️ Fun Actions

Engage with the child's device in playful ways (use responsibly!).

* `/torchon` — Turn ON the device's flashlight.
* `/torchoff` — Turn OFF the device's flashlight.
* `/vibrate` — Make the device vibrate.
* `/ring_phone` — Make the device ring loudly for 10 seconds. Useful for locating a misplaced device.
* `/stop_ring` — Stop the device from ringing if it was initiated by `/ring_phone`.
* `/set_wall_1` — Apply a predefined Wallpaper 1 to the device's home screen.
* `/set_wall_2` — Apply a different predefined Wallpaper 2 to the device's home screen.
* `/set_wall_0` — Restore the device's original wallpaper.

### 🕵️ Monitor & Info

Gain insights into the device's status and usage.

* `/apps` — List all the applications currently installed on the device.
* `/showsms` — Display the last 10 incoming and outgoing SMS messages.
* `/contacts <name/letter>` — Search for a specific contact by name or starting letter. For example, `/contacts John` or `/contacts J`.
* `/phone_status` — Report the current screen status (ON or OFF).
* `/check_batt` — Report the current battery level of the device (in percentage).
* `/clipboard` — Retrieve the current text content copied to the device's clipboard.
* `/uptime` — Display the total time the device has been continuously running since the last boot.
* `/free_storage` — Show the amount of free internal storage space available on the device.
* `/check_exp` — Check the validity status and expiry date (if applicable) of the application's subscription or access plan.
* `/usagetime <package>` — Report the total usage time of the specified application (identified by its package name). You can usually find the package name in the device's app settings or through the `/apps` command. Example: `/usagetime com.whatsapp`.
* `/sendimage <number>` — Fetch a specific image from the device's gallery. Replace `<number>` with the index or a unique identifier of the image (implementation dependent, refer to detailed documentation).
* `/voice_rec <secs>` — Initiate a voice recording for the specified number of seconds (`<secs>`) and send the recording to the parent's Telegram chat.

### 🔥️ Destroy The App

A critical command for removing access to the application.

* `/self_destruct` — Completely remove the BelieveMe application and all associated data from the device. This action is irreversible.

### ♻️ Help

Get assistance with the available commands.

* `/help` — Display this comprehensive list of available commands and their descriptions within the Telegram Bot.

## 🛠️ Installation Guide

To start using BelieveMe, please follow these steps:

1.  **Create a Telegram Bot:**
    * Open the Telegram application on your device.
    * Search for the **BotFather** bot. You should see a verified bot with a blue checkmark.
    * Start a chat with BotFather by tapping **Start**.
    * Send the command `/newbot` to BotFather.
    * BotFather will ask you to choose a name for your bot. Enter a name (e.g., "BelieveMe Control").
    * Next, BotFather will ask you to choose a username for your bot. This username must end with `bot` (e.g., "BelieveMeControlBot").
    * Once you've chosen a valid username, BotFather will congratulate you and provide you with your bot's **HTTP API token**. **Copy this token carefully.**

2.  **Install BelieveMe on the Child's Device:**
    * Install the BelieveMe application on the device you wish to monitor. (Provide instructions on how to obtain and install the app here, e.g., from a website or app store).

3.  **Enter Bot Token and Access Key in the App:**
    * Open the BelieveMe application on the child's device.
    * You will be prompted to enter the **Telegram Bot Token** that you copied from BotFather. Paste the token into the designated field.
    * You will also be prompted to enter an **Access Key** provided by the developer. Enter the correct access key in the designated field.

4.  **The App is Ready:**
    * Once you have entered the correct Bot Token and Access Key, the BelieveMe application will be ready to receive commands from your Telegram Bot.

5.  **Start Controlling:**
    * Open your Telegram application and search for the username of the bot you created (e.g., `@BelieveMeControlBot`).
    * Start a chat with your bot.
    * You can now send the available commands (listed above) to the bot to perform actions and retrieve information from the child's device.

## ⚠️ Important Notes

* This application is intended for legitimate parental control purposes only. Misuse is strictly prohibited.
* Ensure you comply with all applicable laws and regulations regarding monitoring and privacy.
* The functionality and availability of certain commands may vary depending on the device's operating system version and permissions granted during installation.
* The `sendimage` command's implementation for identifying images by number will be detailed in the full documentation.
* **Parents are responsible for creating and managing their own Telegram Bot.**
* **Keep your Telegram Bot Token and the application's Access Key confidential.**

## 📄 License

This project is licensed under a **Proprietary License**.

## 📞 Support

For any questions, issues, or support inquiries, please contact [your support email address or link to support page].

---

**Thank you for using BelieveMe!**
