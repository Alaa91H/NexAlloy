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
