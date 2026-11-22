# Engada Pochu

Tamil-English local voice expense tracker.

## Planned v1

- Tamil, English, and Auto voice modes
- Mandatory spoken and visual confirmation before saving
- Sent/Received summaries and searchable transaction list
- Date/time editing, Today/Yesterday/custom date range filters
- Local-only storage, local backup/restore, and XLSX export
- TalkBack-friendly Android UI
- HTTPS web version with manual fallback
- GitHub Actions APK build

## Build

The Android APK is built by GitHub Actions. Open the **Actions** tab and run **Build Android APK**.

The web version is in `web/` and can be deployed through GitHub Pages.

See `docs/TERMUX-GUIDE.md` for phone-only setup.

## Privacy

Transactions are stored locally. Speech recognition depends on the recognition service available on the phone/browser; the app does not upload transaction records or require login.

## Current limitations

Tamil-English speech accuracy depends on the device language packs and recognition engine. Always verify the confirmation card before saving.
