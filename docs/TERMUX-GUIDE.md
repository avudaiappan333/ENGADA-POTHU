# Phone-only Termux guide

## 1. Install Termux

Install Termux from F-Droid, then run:

```bash
pkg update -y
pkg install git unzip -y
git clone https://github.com/avudaiappan333/ENGADA-POTHU.git
cd ENGADA-POTHU
git checkout feature/engada-pochu-v1
```

## 2. Build through GitHub Actions

Open the repository on your phone, select **Actions**, choose **Build Android APK**, and tap **Run workflow**. After it finishes, download `engada-pochu-debug-apk` from the run’s Artifacts section.

## 3. GitHub Pages web app

Repository **Settings → Pages → Deploy from a branch**, select the feature branch and `/web` if supported by the Pages configuration. The web app needs HTTPS for microphone permission. Chrome speech recognition support varies by device, so manual entry remains available.

## 4. Verify on the phone

Test: fresh empty install, sent, received, balance, confirmation cancel/save, microphone denial, TalkBack, and web manual fallback. Tamil recognition and Tamil TTS depend on installed phone language services.

Only save after checking the confirmation card.
