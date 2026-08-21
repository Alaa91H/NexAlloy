# Morphe LSPosed

**Morphe LSPosed** is an Android module for [LSPosed](https://github.com/LSPosed/LSPosed). It applies reviewed, local hooks to supported applications at runtime through DexKit and the Xposed API. The Android application ID is `app.morphe.lsposed`.

> **Independent project.** Morphe LSPosed is not an official Morphe product and is not affiliated with Morphe, ReVanced, or the developers of the applications listed below. Do not request support from those projects for this module.

## What it does

Morphe LSPosed runs inside the selected target application's process after LSPosed loads the module. Supported changes are implemented as local Kotlin hooks and can be enabled from the module settings. The module does **not** rebuild target APKs.

| Capability | Behavior |
| --- | --- |
| Built-in hooks | Applies reviewed DexKit/Xposed hooks bundled with the installed module. |
| Runtime Store | Reads Morphe community-catalog metadata and classifies entries by runtime compatibility. |
| Compiled runtime layers | Lets the user enable reviewed adapters that are already compiled into Morphe LSPosed. |
| Restricted import | Accepts only the documented data-only runtime specification for registered targets. |
| Safe failure mode | Does not apply a layer when its fingerprint resolves to zero or more than one target. |

The Runtime Store is **not** a `.mpp` interpreter. It never downloads, loads, or executes community patch archives, Smali, bytecode payloads, or arbitrary callbacks inside an application process. A catalog entry becomes usable only after it has been translated into a reviewed runtime adapter and included in a Morphe LSPosed release. See [Runtime Layers](docs/RUNTIME_LAYERS.md) for the complete trust model.

## Requirements

Morphe LSPosed requires a rooted Android device with a working LSPosed installation. Install the module APK, enable the module in LSPosed, select only the intended target applications, and restart each target application after changing its patch or runtime-layer settings.

Because Morphe LSPosed uses the distinct application ID `app.morphe.lsposed`, Android treats it as a separate installation from earlier modules. Reconfigure its LSPosed scope and settings after installation.

## Installation

1. Download the current APK from the [GitHub releases page](https://github.com/Alaa91H/Morphe-LSPosed/releases).
2. Install the APK on the rooted device.
3. Open LSPosed Manager, enable **Morphe LSPosed**, and select the target applications that you intend to modify.
4. Open Morphe LSPosed and enable only the desired built-in patches or Runtime Store layers.
5. Force-stop and reopen each affected target application.

> **Compatibility note:** Application updates can change DEX structure and invalidate a fingerprint. A patch that is listed for a host application is not a guarantee that every version of that host application is supported. If a fingerprint is ambiguous or missing, Morphe LSPosed fails closed instead of applying a broad hook.

## Supported host applications

The current application registry includes the following hosts. Available switches depend on the installed host version and the hook set included in the module build.

| Host application | Android package |
| --- | --- |
| YouTube | `com.google.android.youtube` |
| YouTube Music | `com.google.android.apps.youtube.music` |
| Reddit | `com.reddit.frontpage` |
| Google Photos | `com.google.android.apps.photos` |
| Photomath | `com.microblink.photomath` |
| Instagram | `com.instagram.android` |
| Threads | `com.instagram.barcelona` |
| Strava | `com.strava` |
| AllTrails | `com.alltrails.alltrails` |

## Built-in Runtime Store adapters

| Runtime layer | Host | Default state | Runtime behavior |
| --- | --- | --- | --- |
| `piko.instagram.disable-video-autoplay.runtime` | Instagram | Disabled | Overrides the uniquely matched Boolean video-autoplay setting when enabled. |
| `piko.instagram.disable-story-flipping.runtime` | Instagram | Disabled | Skips the uniquely matched story-navigation method when enabled. |

These layers are independent local hooks. They use public compatibility signals from the cited community source but do not execute the source archive.

## Building from source

Use Android SDK API 37 and JDK 17. The standard verification command is:

```bash
./gradlew --no-daemon :app:testDebugUnitTest :app:assembleDebug
```

A signed release build requires a local `signing.properties` file or the GitHub Actions signing secrets. The repository release workflow builds, verifies, checksums, and publishes tag-based prereleases. See [Development Guide](DEVELOPMENT.md) and [Release Guide](docs/RELEASING.md).

## Reporting issues

Report reproducible problems through the [issue tracker](https://github.com/Alaa91H/Morphe-LSPosed/issues/new/choose). Include the Morphe LSPosed version, Android version, LSPosed version, target application package and version, enabled layers, reproduction steps, and a redacted LSPosed log when available.

## Credits and source attribution

Morphe LSPosed uses [DexKit](https://luckypray.org/DexKit/en/) for DEX inspection and depends on community-maintained patch sources as compatibility and attribution references. Source attribution for Runtime Store adapters is kept in the adapter registry and in [Runtime Layers](docs/RUNTIME_LAYERS.md). Rights in third-party applications, names, and source projects remain with their respective owners.

## License

See the repository license and the license notices of bundled dependencies before redistributing modified builds.
