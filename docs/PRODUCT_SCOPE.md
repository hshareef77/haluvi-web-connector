# Product scope and feasibility

## Product goal

Haluvi Web Connector helps a non-programmer turn a requested service workflow into accessible Android-native screens using repeatable templates and a service-specific adapter. It should minimize the user's effort, but it cannot remove provider-specific setup, app registration, or API restrictions.

## What “native” means here

The app draws its own Android controls and calls an authorized service API or SDK. It does not display the target website in a WebView. Authentication may briefly use the system browser for OAuth; that is a standards-based sign-in handoff, not a website embedded in the app.

## Feasibility classes

| Class | Native UI | Session reuse | Main condition |
|---|---|---|---|
| Public API, no sign-in | Yes | Not needed | API terms permit use |
| Official API + OAuth/OIDC | Yes | Token-based, if provider supports refresh | App registration, scopes, redirect URI, review/test-user rules |
| Official Android SDK | Yes | SDK/provider-managed | SDK license and Android requirements |
| Documented API key | Usually | Key is protected locally | Provider permits client-side use; a public app may need a backend |
| Forms handled by official endpoints | Sometimes | Provider-specific | Clear documentation and permission |
| Website HTML only | Not reliably | Cookies are fragile and provider-specific | Site-specific engineering and provider permission; may be impossible |
| CAPTCHA, MFA, payments, device-bound login | Provider-dependent | Cannot be guaranteed | Official integration support |

## Product promises

The app may say “supported”, “partly supported”, “needs provider access”, or “not currently feasible” only after recording evidence. It must not imply a website has been analyzed if it only received a URL. Every report must distinguish verified facts from assumptions and indicate the date and sources inspected.

## Scope for later releases

1. Service intake and report export.
2. Public documentation and API discovery, with source citations.
3. Declarative adapter generator and local adapter catalog.
4. Native OAuth/OIDC for selected providers.
5. Encrypted token storage and sign-out/data deletion.
6. Native screen renderer for a restricted, validated adapter schema.
7. Accessible interaction review and regression checks.

## Explicit exclusions

- Generic website scraping presented as a dependable conversion method.
- Storing website passwords or bypassing CAPTCHA/MFA/access controls.
- Arbitrary executable code inside imported adapters.
- Promising that browser cookies can be reused in native HTTP requests.
