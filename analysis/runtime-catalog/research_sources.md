# Runtime adapter research sources

## Catalog snapshot

- URL: `https://morphe-patches.software/data/bundles.json`
- Access note: the service returned JSON only when requested with browser-style `Accept`, `Referer`, and Android Chrome `User-Agent` headers; the plain client request redirected to the homepage HTML.
- Snapshot file: `bundles.json` (downloaded 2026-08-21).
- Priority analysis output: `priority_catalog_analysis.md` and `safe_priority_candidates.md`.

## Reviewed upstream sources

| Source | URL | Reviewed candidate | Adapter decision |
|---|---|---|---|
| Piko | https://github.com/crimera/piko | Instagram Disable Reels scrolling | Narrow local touch-interception adapter used; no remote code loaded. |
| Adobo | https://github.com/jkennethcarino/adobo | Reddit Sanitize share links | Existing local patch mapped to catalog item. |
| Morphe community patches | https://github.com/rushiranpise/morphe-patches | Reddit Hide Ads; Messenger/Telegram references | Reddit existing local patch mapped; Telegram Remove ads is mapped through constrained local Boolean targets; Messenger Open links externally is mapped through a local, parameter- and string-anchored Boolean decision override. |
| Kveld Gboard patches | https://github.com/kveld9/kveld-morphe-patches | Gboard Block Telemetry | Reviewed exact Void targets; local multi-void Runtime adapter added for known telemetry dispatch paths. |
| Paresh patches | https://gitlab.com/Paresh-Maheshwari/paresh-patches | Truecaller and Proton VPN Disable telemetry | Reviewed exact Truecaller target; Proton VPN maps only the reviewed telemetry-worker, observability-send, and event-recording paths through local Void/Kotlin-Unit overrides. |
| LSPosed documentation | https://github.com/LSPosed/LSPosed/wiki/New-XSharedPreferences | Shared module preferences | Module uses `xposedminversion=93` and `xposedsharedprefs=true` for world-readable runtime-state preferences. |

## Proton VPN — Disable telemetry

- **Source review:** inspected `patches/src/main/kotlin/app/paresh/patches/protonvpn/misc/DisableTelemetryPatch.kt` in the public Paresh repository.
- **Runtime mapping:** local targets only: `TelemetryWorkerManagerImpl.enqueueOrKeep-HG0u8IE(): V`, `SendObservabilityEventsImpl.invoke(List, Continuation): Object`, and the uniquely matched private/final VPN telemetry bridge with parameters `TelemetryEvent`, `Z`, and `Continuation`.
- **Effect:** skips telemetry-upload scheduling and returns the host process's `kotlin.Unit.INSTANCE` to the two reviewed Kotlin suspend bridges before their bodies execute.
- **Scope:** no connection, server-selection, account, entitlement, upgrade, NetShield, or Premium interface behavior is changed. The adjacent upstream `Proton VPN Premium` patch remains excluded.
- **Compatibility:** target matches are deliberately exact and fail closed when the expected app version no longer provides a unique target.

## Messenger — Open links externally

- **Source review:** inspected the retained source patch and fingerprint definition under `analysis/morphe-patches-source/patches/src/main/kotlin/app/template/patches/messenger/linkhandling/`.
- **Runtime mapping:** `BooleanReturnOverrideLayerDefinition`, constrained to `com.facebook.orca`, return type `Z`, parameter types `android.net.Uri` and `com.facebook.auth.usersession.FbUserSession`, plus anchors `iab_skipped_reason` and `user_prefers_external`.
- **Effect:** overrides only the in-app-browser decision to `false`, handing matching links to the default system browser.
- **Compatibility:** intentionally version-dependent; anchors and parameter constraints narrow matching against obfuscated releases. No account, entitlement, premium, safety, payment, or content-access behavior is modified.

## Exclusions

Subscription, Premium, Pro, VIP, license bypass, and unlock patches are not candidates for Runtime conversion. Patches requiring unreviewed downloaded code, integrity bypassing, content-access restriction bypass, or broad version-dependent bytecode rewrites remain excluded until an independently reviewed local implementation exists.

## Batch review — 2026-08-22

### Gboard — kveld9/kveld-morphe-patches

- `Disable Remote Configuration`: reviewed source uses five exact `void` targets only: the Phenotype update and account-removed receivers plus `Lwhh;` methods `dB`, `e`, and `g`. It is eligible for a complete local `MultiVoidMethodSkipLayerDefinition`.
- `Disable Superpacks Eager Sync`: reviewed source uses two exact `void` targets (`Lgvk;.n` and `Lgrp;.n`). It is eligible for a complete local `MultiVoidMethodSkipLayerDefinition`.
- `Disable Tenor Share Tracking`: reviewed source uses one exact `void` target (`Limg;.K(Lafsc;Lidb;)V`). It is eligible for a complete local `MultiVoidMethodSkipLayerDefinition`.
- `Disable In-App Training` and `Disable MDD Background Sync` include worker return values with application-specific future/result constructors. They are not translated as source-patch equivalents in this batch because constrained local Runtime types do not yet model those return values safely.

### Truecaller — Paresh-Maheshwari/paresh-patches

- GitLab source tree confirms `DisableUpdateCheckPatch.kt` is in the non-premium `truecaller/misc` directory, alongside telemetry and third-party SDK patches. The source body must be reviewed before adding any local adapter.

All upstream files were read as text only; no patch archive or external code was downloaded or executed.

### Truecaller — Disable update check

The reviewed non-premium Paresh source replaces the Boolean `ShouldTriggerUpdate` decision with `false`. Its class is constrained by the strings `playAppUpdateManager` and `configsInventory`; the target returns `Z` and accepts `Lcom/truecaller/inappupdate/UpdateTrigger;`. This is eligible for the existing constrained Boolean Runtime adapter, anchored to both strings and the exact parameter type. No login, entitlement, Premium, or account path is involved.

### Deferred candidates in this review

The reviewed Truecaller `Neutralize third-party SDKs` source combines many SDK-specific initialization overrides, including null and empty-list object returns, interface checks, and multiple ad/analytics providers. It is intentionally deferred rather than implemented partially.

The reviewed Gboard `Disable WorkManager` source combines `p0` object returns, future-like `Lagjs;` values, a required `JobService` super-call, Boolean job returns, and Void receiver hooks. It is also deferred: the local Runtime layer set cannot faithfully reproduce all required return contracts without an explicitly reviewed type-safe adapter.

### Truecaller — Hide Assistant tab

The reviewed non-premium layout patch suppresses one Boolean assistant-feature decision so the bottom navigation tab is not added. Its fingerprint definition remains to be reviewed before a local adapter is created. No account, entitlement, or Premium behavior is changed.

### Messenger — Disable typing indicator

- **Source review:** inspected the public `DisableTypingIndicatorPatch.kt` source in `rushiranpise/morphe-patches`. The upstream patch replaces the first instruction of the Composer typing-state `Runnable` with `return-void`.
- **Runtime mapping:** local `MultiVoidMethodSkipLayerDefinition`, constrained to `com.facebook.orca`, class `LX/Ay7;`, and method `run(): V`. The known source comment identifies this as `ConversationTypingContext$sendActiveStateRunnable$1` for Messenger 573.0.0.44.88.
- **Effect and failure mode:** the selected Runnable is skipped before its body and therefore does not transmit the active typing-state event. If the narrow target is absent after an app update, no hook is installed.
- **Scope:** this is a messaging-privacy control only. It does not alter authentication, accounts, subscription, payment, moderation, security, or content-access controls.

### X — Clear tracking params

- **Source review:** inspected `ClearTrackingParamsPatch.kt` in `crimera/piko`. The source intercepts its uniquely fingerprinted share-parameter method and returns DEX register `p0` before a session token is appended.
- **Runtime mapping:** local `StaticFirstStringArgumentReturnLayerDefinition`, constrained to `com.twitter.android`, return type `String`, exactly three parameters (`String`, object, `String`), and the anchors `<this>`, `shareParam`, and `sessionToken`.
- **Effect and failure mode:** only a static target whose first argument is a String receives that string as its result. A target absent after an X update, a non-static match, or any mismatch in the first argument proceeds unchanged.
- **Scope:** this is a share-link privacy control. It does not alter accounts, login, content visibility, subscriptions, payments, update enforcement, integrity, or safety protections.

### Telegram — Hide typing indicator

- **Source review:** inspected `TelegramHideTypingPatch.kt` in `rushiranpise/morphe-patches`. The source enumerates every no-argument `needSendTyping(): void` implementation and inserts an immediate `return-void`.
- **Runtime mapping:** local `AllMatchingVoidMethodSkipLayerDefinition`, constrained to `org.telegram.messenger`, method name `needSendTyping`, void return type, and no parameters. Its multi-match semantics intentionally mirror the reviewed upstream source rather than choosing an obfuscated class name.
- **Effect and failure mode:** every exact implementation found in the target process is skipped before it can dispatch a typing-state request. If a release has no such method, the matched set is empty and no hook is installed.
- **Scope:** this is a user-controlled messaging privacy setting only. It does not change authentication, account behavior, subscriptions, payments, server-side content controls, integrity checks, or safety protections.

### Messenger — Remove Meta AI

- **Source review:** inspected `RemoveMetaAiPatch.kt` and its fingerprints in `rushiranpise/morphe-patches`. The source suppresses the Meta AI floating compose button, the AI Creation drawer item, the AI Home drawer item, and AI search suggestions; each target is optional to accommodate app-version changes.
- **Runtime mapping:** a locally compiled `ExistingPatchRuntimeLayerDefinition` targets `com.facebook.orca` using the same reviewed anchors. The FAB renderer is matched by `fab_expanded` and `AiFabComponent` and returns null; the three visibility gates are constrained by their reviewed method calls, stable strings, return type, and access flags, then return false.
- **Effect and failure mode:** any surface whose exact target is found is hidden. A missing, reorganized, or non-unique target produces no hook for that surface; no downloaded code or broad class-name scan is used.
- **Scope:** the adapter only hides local Meta AI interface surfaces. It does not alter authentication, accounts, payments, subscriptions, model safety behavior, moderation, integrity, or content-access controls.
