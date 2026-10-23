# Super Duper Messaging Manager

> **Archived.** I created this SMS app and manager back around 2012–2014 Android project. I recently decided to publish it (2026) as part of my engineering history. It targets the Android SDK and Eclipse ADT toolchain from "back in the early days of Android" and will not build on a current toolchain without work. It is here to be read, not run. Issues and pull requests are not monitored.

An SMS/MMS replacement for Android (2012–2014) with a theming system and an SDK (`SDMM_SDK`) so other apps could use the messaging layer. Packages `com.sdmmllc.*` and `com.messagingappsllc.*` are my own companies from that era, SDMM LLC and MessagingApps LLC. `MessagingAppsLib` is derived from the AOSP Mms application (Apache-2.0; headers intact).

## What is mine and what is not

- Code under my own package names is mine (James Baker, Siris Development, Inc.).
- Code carrying Android Open Source Project or other Apache-2.0 headers is derived from AOSP and keeps its original license and notices.
- Third-party SDK binaries from the period (ad networks, analytics, Play licensing) were removed before publishing; the build files still reference them.
- Build output, IDE metadata and ad-network identifiers were removed. Nothing else was modernized or rewritten; this is the code as it shipped.

## License

My code: MIT (see `LICENSE`). AOSP-derived files: Apache-2.0, per their headers.
