# Morphe LSPosed Runtime Layers

Morphe LSPosed Runtime Layers are **compiled DexKit/Xposed adapters** that run locally in the selected target application's process through the module patch executor. They use the same per-application preference model as other built-in hooks and are disabled until the user enables them.

A Runtime Layer is not a Morphe `.mpp` interpreter. Community bundles can contain patcher-time bytecode transformations, extension assumptions, and instructions that only make sense while rebuilding an APK. Executing those archives inside an LSPosed target process would be technically incompatible with the hook model and would permit unreviewed third-party code to run in that process.

## Trust model

| Boundary | Morphe LSPosed behavior |
| --- | --- |
| Community catalog | Used for discovery, attribution, source patch names, and compatibility review. |
| Community archives | Never downloaded for execution, interpreted, loaded, or injected at runtime. |
| Built-in adapter | Written and reviewed as Kotlin, compiled into the Morphe LSPosed APK, and registered for a known target package. |
| Activation | Explicit user action is required. Built-in and imported layers start disabled. |
| Match failure | A layer fails closed if its fingerprint resolves to zero or multiple methods. |

## Adapter workflow

1. A maintainer reviews a community catalog record as a discovery and attribution source.
2. The intended behavior is translated into a small local Kotlin adapter using the module patch executor, DexKit fingerprints, and Xposed hooks.
3. The adapter is registered with its source repository, source patch name, target package, and compatibility signals.
4. The adapter is compiled into the APK and appears in Morphe LSPosed settings or the Runtime Store.
5. The user chooses whether to enable it and restarts the target application after the setting change.

## Built-in adapters

| Runtime layer | Host application | Community attribution | Runtime behavior |
| --- | --- | --- | --- |
| `piko.instagram.disable-video-autoplay.runtime` | Instagram | [`crimera/piko`](https://github.com/crimera/piko), “Disable video autoplay” | Hooks the uniquely matched Boolean setting method and returns `true` while the layer is enabled. |
| `piko.instagram.disable-story-flipping.runtime` | Instagram | [`crimera/piko`](https://github.com/crimera/piko), “Disable story flipping” | Skips the uniquely matched `ReelViewerFragment` `Void` method while the layer is enabled, preventing automatic move-to-next-story behavior. |

These are independent local hooks. Public fingerprint strings are used as compatibility signals; no Piko `.mpp` archive is copied, loaded, or executed.

## Compatibility policy

A Runtime Layer must remain disabled by default until its fingerprint has been validated against a supported host version. If DexKit cannot identify exactly one matching method, Morphe LSPosed records the failure and applies no layer. Host updates can therefore require an adapter update; they cannot silently broaden a hook.

The Runtime Store can show a catalog record without making it executable. "Ready" means a compiled adapter is included in the installed module; it does not mean an arbitrary community patch has been converted automatically.

## Imported runtime specifications

The Runtime Store can import a constrained, data-only specification for a Boolean return override. Imported layers use the same local executor path as compiled layers, remain disabled until explicitly enabled, and apply only to registered target packages.

```json
{
  "schemaVersion": 1,
  "id": "community.instagram.sample-boolean-override",
  "sourceRepository": "owner/repository",
  "sourcePatchName": "Source patch name",
  "packageName": "com.instagram.android",
  "patchName": "Runtime · Sample Boolean override",
  "description": "A reviewed runtime override",
  "fingerprintStrings": ["unique_feature_flag"],
  "replacementValue": true,
  "enabled": false
}
```

| Accepted | Rejected |
| --- | --- |
| Registered target packages | Unregistered targets |
| Bounded string lists | Arbitrary class or method names |
| Boolean return override data | Code, Smali, bytecode payloads, callbacks, or URLs |
| Explicit persisted enabled state | Self-enabling imports or remote execution instructions |

After enabling or disabling an imported layer, force-stop and reopen the target application. If the fingerprint does not resolve to exactly one method, the layer fails closed and the module records the unsuccessful patch instead of applying a broad hook.

## Runtime Store status

| Store state | Meaning |
| --- | --- |
| **Ready for LSPosed runtime** | The installed module includes a compiled adapter for the exact source patch and target package. |
| **Needs a runtime adapter** | The target application is registered, but the catalog record has not been translated into a reviewed runtime operation. |
| **Other catalog patches** | The catalog record targets an application that is not registered in Morphe LSPosed and cannot be imported into the Runtime Store. |

This classification makes the supported subset explicit. It does not claim that every community patch can run through runtime hooking.

## Verification and fixtures

```bash
./gradlew --no-daemon :app:testDebugUnitTest :app:assembleDebug
```

This command validates the Runtime Store and produces a debug APK. When APK fixtures are supplied under `binaries/`, the fingerprint suite also checks their DexKit matches dynamically. When that directory is absent or empty, fixture-dependent tests produce no dynamic cases, keeping the ordinary unit suite reproducible in a clean checkout. Runtime behavior against a particular host release still requires on-device validation with LSPosed.
