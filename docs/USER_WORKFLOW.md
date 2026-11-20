# Simple user workflow

## Add one service

1. Tap **Analyze a service**.
2. Paste its public website address and describe the task in everyday language.
3. The app checks for official developer documentation and supported authentication.
4. Read the result as a short summary first, with detailed evidence available below it.
5. If supported, choose the generated native screen flow and connect the account through the provider's approved sign-in.
6. Review the actions with a screen reader, save the adapter, and use the service from the app.

## If analysis finds a blocker

The report should state exactly what is missing, such as an API, OAuth client approval, permission, or a specific endpoint. It should produce a copyable request the user can send to the provider. If a technical source is needed, the app should ask for the documentation link or let the user attach the provider's integration guide; it should not require the user to inspect HTML or write JavaScript.

## Report contents

- Service identity and date checked.
- Requested task and expected inputs/outputs.
- Findings with official source URLs.
- Sign-in method and session lifetime behavior, if documented.
- Native screens and actions that appear feasible.
- Accessibility findings and unknowns.
- Provider permissions, API limits, and likely maintenance needs.
- Clear result: supported, partly supported, needs access, or not feasible with available evidence.
- Suggested next step in plain language.
