# Runtime Layers

NexAlloy runtime layers are **compiled Xposed/DexKit adapters** that execute in the target process through the existing `PatchExecutor`. They use the same preference model as built-in NexAlloy patches and appear in the ordinary per-app patch settings screen.

A runtime layer is not a Morphe `.mpp` interpreter. Community bundles contain patcher-time bytecode transformations and can depend on patcher APIs, instruction insertion, and patched-APK extension code. Loading these archives at runtime would be incompatible with the Xposed hook model and would allow unreviewed third-party code to execute in a host process.

## Adapter workflow

1. The community catalogue is used as a discovery and attribution source.
2. A maintainer translates a selected patch into a small Kotlin adapter using NexAlloy's existing `PatchExecutor`, DexKit fingerprints, and Xposed hooks.
3. The adapter is registered in `RuntimeLayerRegistry` with its source repository, source patch name, and compatible package names.
4. The compiled adapter is included in the ordinary app patch array and can be enabled from the NexAlloy settings screen.

## First adapter

| Runtime layer | Host app | Community source | Runtime behavior |
|---|---|---|---|
| `piko.instagram.disable-video-autoplay.runtime` | Instagram | [`crimera/piko`](https://github.com/crimera/piko), “Disable video autoplay” | Hooks the matched boolean setting method and returns `true` when the NexAlloy layer is enabled. |

The first adapter uses Piko's public fingerprint strings as compatibility signals. It is a new NexAlloy runtime hook and does not load, copy, or execute a Piko `.mpp` archive.

## Compatibility policy

A runtime adapter must stay disabled by default unless its fingerprint has been validated against a supported host version. If DexKit does not find exactly one matching method, NexAlloy records the patch failure and does not apply the layer. A host application update can therefore require an adapter update; it cannot silently execute a mismatched transformation.

## Imported runtime specifications

The Runtime store can import a small, data-only specification that describes a Boolean return override. Imported specifications run through the same `PatchExecutor` path as compiled layers, but are disabled until the user explicitly enables them and restarts the target application.

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

The importer only accepts registered NexAlloy target packages, bounded string lists, and the Boolean override operation. It rejects code, Smali, class or method names, URLs, arbitrary hook callbacks, and bytecode payloads. This restriction is intentional: a generic interpreter would not safely reproduce patcher-time `.mpp` transformations in an LSPosed process.

After enabling or disabling an imported layer, force-stop and reopen the target application. If the fingerprint does not match exactly one method in that application version, the layer fails closed and `PatchExecutor` reports the failed patch instead of applying a broad hook.

## Runtime store status

When a catalog is refreshed, the store separates records into three states:

| Store state | Meaning |
|---|---|
| **Ready for LSPosed runtime** | NexAlloy has a compiled adapter for the exact source patch and target package. |
| **Needs a runtime adapter** | The patch targets an application registered by NexAlloy, but its build-time behavior has not yet been translated into a reviewed runtime operation. |
| **Other catalog patches** | The target application is not registered by NexAlloy and cannot be imported into the LSPosed Runtime store. |

This classification makes the supported subset explicit rather than pretending every community patch is automatically compatible with runtime hooking.
