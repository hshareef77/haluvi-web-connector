# Adapter package design (proposal)

Adapters should be declarative JSON, versioned, signed or verified, and restricted to a host allowlist. The Android app interprets a small approved schema; imported packages must never contain executable JavaScript or Kotlin.

## Proposed top-level fields

- `schemaVersion`: adapter schema version.
- `service`: stable id, display name, base URL, allowed hosts.
- `authentication`: `none`, `oauth2`, `oidc`, or `api_key` with references to secure values (never the values themselves).
- `screens`: native screen definitions made from approved controls.
- `actions`: named HTTP requests with allowed methods, paths, declared fields, and response mappings.
- `permissions`: minimal Android permissions and why they are needed.
- `evidence`: official documentation URLs and checked date.

## Safety validation

- HTTPS only, host allowlist enforced after redirects.
- No arbitrary URLs from action input.
- No secrets in logs, reports, exports, or crash messages.
- Strict method/path/data-size limits and request timeouts.
- Validate response types before rendering; render untrusted text as text.
- Clear user confirmation before destructive actions, purchases, or sending data.
- Adapter schema upgrades are explicit and backward-compatible where possible.

See `../samples/example-service.json` for a small metadata-only example.
