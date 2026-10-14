# Sanchay — Privacy Policy

**Effective Date:** October 6, 2026  
**Last Updated:** October 6, 2026

## 1. Overview
At Sanchay ("we", "our", or "us"), we are deeply committed to protecting your personal and financial privacy. This Privacy Policy details how Sanchay collects, manages, and safeguards your information when you use our mobile application and associated services.

## 2. On-Device First Data Architecture
- **Local Storage**: By default, all financial records (expenses, categories, wallets, debts, subscriptions, and investment assets) are stored directly on your device in a private, encrypted Room SQLite database.
- **No Mandatory Cloud Account**: You can use 100% of Sanchay's core features entirely offline without ever creating a cloud account or transmitting your data over the internet.

## 3. Optional Cloud Synchronization
- If you explicitly choose to configure Cloud Sync, your financial data is synced securely with your designated Sanchay backend server using TLS 1.3 encryption and JSON Web Tokens (JWT).
- We do not share, sell, or monetize your financial sync payloads with third-party advertisers, data aggregators, or credit bureaus.

## 4. Permissions & Usage
- **Camera (`CAMERA`)**: Used solely for optical character recognition (OCR) via Google ML Kit to parse receipt totals and transaction dates. No photos or camera feeds are ever uploaded to external servers without your explicit knowledge.
- **Notifications (`POST_NOTIFICATIONS`)**: Used to display timely alerts for recurring subscription renewals, debt payment milestones, and scheduled synchronization updates.
- **Location (`ACCESS_COARSE_LOCATION` / `ACCESS_FINE_LOCATION`)**: Optional permission used exclusively to tag transaction locations when you record an expense. You may decline this permission with zero impact on other app features.

## 5. AI Financial Insights
- AI queries sent through Sanchay are anonymized and summarized to minimize personal identifiers.
- API keys and private model integrations are securely managed server-side, preventing client-side credentials exposure.

## 6. Data Retention and Erasure
- You have complete control over your data.
- You may export your financial history or permanently delete all local database records at any time directly through the Settings screen.

## 7. Contact Us
If you have any questions or concerns regarding this Privacy Policy, please reach out to:  
**Email:** privacy@camuel.com  
**Website:** https://camuel.com/sanchay
