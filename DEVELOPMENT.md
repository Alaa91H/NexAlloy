# Morphe LSPosed Development Guide

This guide explains how to develop and verify **Morphe LSPosed**. The module applies local DexKit/Xposed hooks to applications selected in LSPosed; it does not produce modified target APKs or execute external patch archives.

## Project boundaries

| Area | Responsibility |
| --- | --- |
| `app/` | Android module, LSPosed entry point, hook definitions, UI, Runtime Store, and tests. |
| `morphe-patches/` | Bundled source dependency used by existing patch implementations. |
| `morphe-patches-library/` | Bundled shared extension dependency required by existing patch implementations. |
| `docs/` | Runtime-layer and release procedures. |
| `.github/workflows/` | Continuous integration and signed prerelease automation. |

The Android package is `app.morphe.lsposed`. Some inherited Kotlin namespaces remain in the source tree because bundled extension sources link to them. They are implementation details, not public product branding. Do not rename individual namespaces, the LSPosed entry point, or shrinker rules in isolation; a namespace migration must update the complete dependency graph and be validated on-device.

## Local setup

Use JDK 17 and Android SDK API 37. Initialize the repository with its submodules before building:

```bash
git submodule update --init --recursive
./gradlew --no-daemon :app:testDebugUnitTest :app:assembleDebug
```

The unit suite validates the Morphe catalog bridge, Runtime Store classification, trust policy, imported runtime specifications, and layer state. APK fingerprint fixtures are optional: when `app/binaries/` is absent, fixture-dependent tests are skipped while the normal unit suite remains reproducible.

## Runtime hook model

A hook must be narrow, deterministic, and disabled by default. It should use a reliable DexKit fingerprint and fail closed when the fingerprint is not unique. Avoid broad class-name matching, version-agnostic method hooks, arbitrary reflection, and any mechanism that executes external source code in a target process.

> A Runtime Store entry is an attribution and compatibility record. It is not authority to execute a community archive.

### Adding a built-in hook

1. Register the target host package in the application registry only when it has a reviewed patch set.
2. Add a Kotlin hook definition under the host application's patch area.
3. Use a fingerprint that resolves to exactly one class or method on the supported host version.
4. Make the action reversible through the module settings and keep it disabled until the user enables it.
5. Add a unit test for the fingerprint or runtime-layer definition.
6. Test the hook on a rooted device with LSPosed before documenting it as supported.

### Adding a Runtime Store adapter

Runtime Store adapters are compiled into the APK and registered by identifier. The currently supported operations are a Boolean return override and a matched `Void` method skip. Do not add a generic archive interpreter, bytecode loader, Smali evaluator, URL-based code fetcher, or arbitrary callback facility.

1. Verify that the catalog entry targets a registered application.
2. Record its source repository and source patch name for attribution.
3. Translate only the safe runtime behavior into a reviewed adapter definition.
4. Register the adapter and its target package in the runtime registry.
5. Add tests for the enabled state, target uniqueness, and failure behavior.
6. Update [Runtime Layers](docs/RUNTIME_LAYERS.md) and validate the adapter on-device.

## Imported runtime specifications

Imported specifications are deliberately constrained data. The importer accepts only registered target packages, bounded fingerprint strings, and the documented Boolean override operation. It rejects executable code, arbitrary class or method names, bytecode payloads, callbacks, URLs, and network-loaded content.

Any expansion of this format is a security-sensitive design change. Document it, test malformed input, preserve explicit user enablement, and confirm that an unmatched or ambiguous fingerprint cannot apply a hook.

## Testing

| Goal | Command |
| --- | --- |
| Unit tests | `./gradlew --no-daemon :app:testDebugUnitTest` |
| Debug APK | `./gradlew --no-daemon :app:assembleDebug` |
| Signed release APK | `./gradlew --no-daemon :app:testDebugUnitTest :app:assembleRelease` |

For a signed local release, create an untracked `signing.properties` file in the repository root:

```properties
KEYSTORE_FILE=/absolute/path/to/release.jks
KEYSTORE_PASSWORD=...
KEYSTORE_ALIAS=...
KEYSTORE_ALIAS_PASSWORD=...
```

Never commit the keystore, `signing.properties`, decoded signing material, or secret values. Confirm the resulting APK signature with Android build tools before distributing it.

## Release workflow

GitHub Actions restores signing material from encrypted repository secrets, runs the unit suite, builds the release APK, verifies signature and package metadata, creates a SHA-256 checksum, and publishes a prerelease for tags beginning with `v`.

Before creating a tag, ensure that the intended commit is on `main`, the version properties are incremented, and the tests pass. The full release procedure and required secret names are documented in [Release Guide](docs/RELEASING.md).

## Pull requests and issue reports

Keep changes narrow and explain the target application, tested version, fingerprint strategy, enabled-state behavior, and known limitations. For issues, include a redacted LSPosed log, target package/version, Morphe LSPosed version, enabled switches, and deterministic reproduction steps.
