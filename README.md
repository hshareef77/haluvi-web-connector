# Haluvi Web Connector

An accessibility-first Android project for preparing native integrations with web services. The goal is to make a service-specific adapter workflow reusable, so Hussain does not need to hand-code every integration from scratch.

## Important scope

A website cannot be universally converted into a reliable native application without service-specific knowledge. The strongest path is an official API or Android SDK plus an approved native sign-in flow. A service that exposes only private web pages may block automation or make it fragile. The app must report these limits clearly and never promise that any website will work.

The first prototype is a report-first Android shell with one public API demonstration. It accepts a service URL and requested task, then explains what must be checked. The demo retrieves sample posts from JSONPlaceholder and displays them using native Android controls. It does not yet inspect arbitrary websites, sign in, store sessions, or generate adapters.

## Planned user workflow

1. Add a service address and describe the task in plain language.
2. Run an inspection that checks public documentation, API endpoints, OAuth options, accessibility semantics, and platform restrictions.
3. Get a readable technical report: supported features, blockers, permissions, required credentials, risks, and a proposed native screen flow.
4. Choose an integration template. The app generates an adapter package from service metadata and user-approved mappings.
5. Sign in through the service's official OAuth flow, when supported. Keep tokens separate from adapter files and encrypt them at rest.
6. Review native screens with TalkBack/Jieshuo+, test all actions, and install the adapter locally.

## Integration classes

- **Official API / SDK:** best option for native screens and stable behavior.
- **OAuth/OIDC:** native sign-in and token sessions when the provider approves the app and redirect flow.
- **Documented form/API endpoints:** adapter-specific, only when the provider permits this use.
- **Website-only workflow:** not guaranteed. An arbitrary site cannot be made a robust native app merely by scraping its buttons; a WebView or remote browser is a different product and does not meet the native-only requirement.
- **Protected, payment, CAPTCHA, device-bound, or MFA flows:** provider-controlled; may be unavailable for third-party clients.

## Accessibility requirements

- Compose-native controls with labels and meaningful headings.
- Logical TalkBack and Jieshuo+ focus order; no color-only state.
- Large touch targets, scalable text, clear selected/disabled states, and short status announcements.
- Reports are readable in linear order and contain no sensitive credentials.

## Security rules

- Do not collect or store website passwords.
- Keep API secrets out of adapter packages, reports, logs, and exports.
- Store provider tokens only after an approved sign-in; encrypt using an Android Keystore-backed key.
- Adapter packages declare exact hosts, methods, data fields, and requested permissions. No arbitrary code execution from imported packages.
- Provide per-service data deletion and sign-out.

## Repository layout

- `app/` Android Compose prototype.
- `docs/` product scope, workflow, adapter format, and feasibility boundaries.
- `samples/` non-secret sample service definition.
- `.github/workflows/build-apk.yml` debug APK workflow.
- `.github/workflows/build-release.yml` signed release workflow.

## Public API demonstration

On the home screen, choose **Try the public API demo** to fetch sample posts from [JSONPlaceholder](https://jsonplaceholder.typicode.com/). It uses HTTPS, requires no account, and only reads test data. This shows native rendering and API networking; it does not convert the JSONPlaceholder website.

## Build

Open in Android Studio with JDK 17 and Android SDK 36, or run the **Build Android APK** workflow in GitHub Actions. It uploads a debug APK as an artifact. The signed release workflow requires the repository's existing signing secrets and verifies the configured certificate before publishing.
