# Morphe LSPosed localization

The user interface follows the Android system locale automatically. No in-app language override is used, so the system language remains the default selection.

| Resource directory | Purpose |
|---|---|
| `app/src/main/res/values/xp_strings.xml` | Default English fallback used when no matching translation exists. |
| `app/src/main/res/values-en/xp_strings.xml` | Explicit English resource set. |
| `app/src/main/res/values-ar/xp_strings.xml` | Arabic resource set with right-to-left support provided by Android. |

To add another language, create `app/src/main/res/values-<language>/xp_strings.xml`, where `<language>` is an Android language qualifier such as `fr`, `de`, or `es`. Copy all string keys from the default `values/xp_strings.xml`, translate only the text values, and preserve positional format markers such as `%1$s` and `%2$d` exactly.

Before submitting a translation, verify that the locale file has the same string keys as the default resource, then run `./gradlew --no-daemon :app:mergeDebugResources`.
