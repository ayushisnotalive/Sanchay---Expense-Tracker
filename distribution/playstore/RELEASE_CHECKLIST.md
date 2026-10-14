# Google Play Store Launch Readiness Checklist (Release Tomorrow)

## 1. Technical & Compliance Verification
- [x] **Target SDK 35 (Android 15)**: Meets Google Play minimum API level requirements.
- [x] **64-bit Architecture**: Validated with standard ARM64/x86_64 ABI support.
- [x] **ProGuard/R8 Optimization**: Code shrinking, resource shrinking, and obfuscation enabled with zero crashes.
- [x] **Offline Stability**: Fully functional without network connectivity; zero unhandled network exceptions.
- [x] **API Key Security**: Zero plaintext AI/service keys bundled into release APK; backend proxy integration verified.
- [x] **Vibecoding / AI Marker Exclusion**: Strictly excluded from git tracking via `.gitignore`.

## 2. Google Play Console Form Requirements
- [x] **App Content Declarations**:
  - Financial Services: Declared as Personal Finance Management tool (does not provide loans or banking services).
  - Target Audience: 18+ (Fintech / Expense Management).
  - Data Safety: On-device storage by default; optional cloud sync; encrypted in transit; user-requested deletion supported.
  - Permissions Declaration: Camera (optional, receipt scanning), Notifications (optional, reminders).
- [x] **Store Listing**:
  - App Name: `Sanchay: Wealth & Expense Manager`
  - Short Description: Under 80 chars.
  - Full Description: Under 4000 chars, structured with feature bullets.
  - Privacy Policy URL configured.

## 3. Rollout Strategy
1. **Internal Testing Track**: Upload release `.aab` or `.apk` to internal testing track today.
2. **Smoke Test**: Verify login, add expense, wallet switch, AI advisor prompt, and cloud sync trigger on a physical Android device or emulator.
3. **Production Track Promotion**: Promote internal test build to Production with staged rollout (20% -> 50% -> 100%) tomorrow.
