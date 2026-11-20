# Haluvi Web Connector — technical feasibility report

**Prepared:** 20 November 2026  
**Status:** Product and architecture assessment; no target service has been analyzed yet.  
**Current prototype:** Android-native report intake plus a read-only public API demonstration. It does not inspect arbitrary websites, authenticate, save sessions, or execute adapters.

## Recommendation

Build this as a native integration workbench with a reusable, restricted adapter format. For each service, discover its official API or Android SDK, authentication method, permissions, and terms. Then generate accessible Android screens that call only those documented capabilities. This can reduce repeated custom work, but it cannot make every arbitrary website convertible.

The first implementation should not send user logins or service data to an AI provider. An optional AI-assisted analysis can be considered later, using public documentation only and a report that separates cited findings from model inferences. The app must identify that service, model, date, sources, and any user-provided material before such analysis is run.

## Native sign-in and saved sessions

- For OAuth on a native app, the recommended pattern is an external browser or browser tab, with an app redirect back to the client. Native public clients should use PKCE. This lets the provider keep the password and MFA interaction in its own sign-in page; Haluvi receives an authorization result rather than the password.
- Save provider-issued access/refresh tokens only when the provider permits this client. Keep token values encrypted locally, with encryption keys protected by Android Keystore. Support sign-out, revocation where available, and per-service deletion.
- Do not design around copying browser cookies into the native app. Cookies may be scoped to a browser context, short-lived, bound to CSRF/device signals, or unavailable to a native client. Cookie reuse is not a general-purpose sign-in session strategy.
- A provider can require its own app registration, review, approved redirect URI, test users, or scopes. If it does not support third-party/native access, the integration may stop at a report and an access request.

## Service capability matrix

| Service capability | Haluvi can build | Session expectation | Result |
|---|---|---|---|
| Official API or Android SDK | Native lists, forms, actions, and status screens | Provider-issued token or SDK session | Best-supported path |
| OAuth/OIDC documented for mobile apps | Native service screens after browser sign-in | Tokens saved securely, subject to provider policy | Supported after client setup |
| Documented API key | Native API calls if key use from a mobile app is permitted | Key stored encrypted; public apps may need a backend | Provider-specific |
| Public website pages only | Possibly read public content through an API or feed | Usually none | Depends on documented interface |
| Private site with HTML only | No reliable general conversion | Website cookies are not guaranteed reusable | Custom investigation; may be infeasible |
| CAPTCHA, MFA, payment, device binding, anti-bot checks | Only an official provider integration can support these reliably | Provider-controlled | No bypassing; can be unsupported |

## Proposed end-to-end workflow

1. **Describe:** user enters a public service URL and the action they want in everyday language.
2. **Inspect:** check official developer docs, API/SDK availability, OAuth support, scopes, rate limits, terms, and accessibility of the requested task. Record the URLs and date checked.
3. **Report:** state what is confirmed, what is inferred, what access is missing, and whether the result is supported, partial, needs provider access, or infeasible. Offer a provider request message when access is missing.
4. **Design:** map each task to native screens, controls, API fields, error states, and confirmation steps. Make destructive or financial actions require an explicit confirmation.
5. **Build adapter:** produce a validated declarative package with allowed hosts, endpoints, fields, UI controls, and evidence. Never execute imported JavaScript/Kotlin.
6. **Connect:** use an approved provider sign-in. Store only tokens, encrypted at rest; never store account passwords.
7. **Review:** navigate every screen with TalkBack and Jieshuo+, verify headings, labels, selected/disabled states, focus order, and status announcements.
8. **Test and update:** exercise success, network loss, expired session, permission denial, empty results, provider errors, and sign-out. Re-check official docs when the adapter version changes.

## Risks and limits

- **No universal converter:** each website has different APIs, access policies, and business rules. A URL alone does not reveal reliable native behavior.
- **Provider policy:** API terms can prohibit specific uses or require approval. Haluvi must check and disclose this rather than bypass controls.
- **Site changes:** undocumented endpoints and HTML-based automation can break without notice. The adapter should record evidence and display when its documentation was last checked.
- **Secrets:** no passwords, tokens, API keys, or private account data in reports, logs, adapter exports, or source control.
- **Backend requirements:** some providers do not allow confidential client secrets in a mobile app. Those integrations may require a trusted server, which creates hosting, privacy, and cost decisions. Do not add a backend without the user's approval of that service and its costs.
- **AI:** optional only. Public documentation can be summarized with citations, but model output can be wrong. Do not upload credentials, cookies, account pages, or private content. The first prototype uses no AI service.

## First build and known gaps

The first prototype provides a screen-reader-friendly intake flow, saves reports on-device, and shares report text through Android's system share sheet. It also fetches public sample posts from JSONPlaceholder and presents them using native Android controls. The report explicitly says it is not a live inspection, avoiding false confidence. The repository also defines adapter safety rules, a sample package, build workflows, and the product roadmap.

Not implemented yet: live documentation discovery, file-based report export, adapter generation/import, OAuth, encrypted token storage, generalized native API execution, and site-specific integrations. The demo is a fixed read-only test endpoint, not yet the adapter engine. These should be delivered in stages, with each integration grounded in a real service the user wants to support.

## Technical choices

- Kotlin and Jetpack Compose for Android-native UI.
- Minimum Android API 26; compile/target SDK 36.
- JDK 17, Android Gradle Plugin 9.1.1, Gradle 9.3.1, Android's built-in Kotlin 2.2.10 support, and Compose BOM 2026.09.00. Compose 1.12 requires compile SDK 37 and AGP 9 or newer. The build selects API 37.0 explicitly because the SDK package uses that minor-version name.
- Provider OAuth through an external user agent with PKCE; tokens protected with Android Keystore.
- Adapter files are data-only JSON; a strict parser validates schema and host/method allowlists.
- No WebView or embedded website is part of the app's integration path.

## Primary references

- IETF, [RFC 8252: OAuth 2.0 for Native Apps](https://datatracker.ietf.org/doc/html/rfc8252).
- Android Developers, [Android Keystore system](https://developer.android.com/privacy-and-security/keystore).
- Android Developers, [Security tips](https://developer.android.com/privacy-and-security/security-tips).
- Android Developers, [Compose BOM](https://developer.android.com/develop/ui/compose/bom).
- Android Developers, [Android Gradle Plugin 9.1 release notes](https://developer.android.com/build/releases/agp-9-1-0-release-notes).
- Android Developers, [Migrate to built-in Kotlin](https://developer.android.com/build/migrate-to-built-in-kotlin).
