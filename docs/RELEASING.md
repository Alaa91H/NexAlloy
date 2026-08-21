# Releasing Morphe LSPosed

This project publishes **signed Android prereleases** from Git tags beginning with `v`. The canonical Android package is `app.morphe.lsposed`.

## Versioning

Release identity is defined in `gradle.properties`.

| Property | Purpose | Initial alpha value |
| --- | --- | --- |
| `MORPHE_LSPOSED_VERSION_CODE` | Monotonically increasing Android installation version | `10001` |
| `MORPHE_LSPOSED_VERSION_NAME` | User-visible release version | `0.1.0-alpha.1` |

Update both properties before a subsequent release. Every published APK must use a higher version code than every prior APK using the same Android package.

## Signing

The repository stores only encrypted Actions secret values. The signing key and `signing.properties` must never be committed. The release workflows require the following repository secrets:

| Secret | Purpose |
| --- | --- |
| `KEYSTORE` | Base64-encoded PKCS12 keystore |
| `KEYSTORE_PASSWORD` | Keystore password |
| `KEYSTORE_ALIAS` | Release key alias |
| `KEYSTORE_ALIAS_PASSWORD` | Alias password |

Both CI and tag releases reconstruct the signing material in the runner temporary directory and remove it at the end of the job. The build verifies the APK signature and package metadata before publishing.

## Publishing an alpha

After changes are merged into the intended release commit, create a tag matching the visible version, for example `v0.1.0-alpha.1`, and push it. The `Publish Android Release` workflow runs tests, builds a signed release APK, validates it, uploads a checksum, and creates a GitHub prerelease.
