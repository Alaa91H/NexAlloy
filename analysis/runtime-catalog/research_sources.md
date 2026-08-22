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

### Messenger — Hide inbox subtabs

- **Source review:** inspected `HideInboxSubtabsPatch.kt` and its shared Inbox fingerprint in `rushiranpise/morphe-patches`. The source targets `InboxSubtabsItemSupplierImplementation$onSubscribe$1`, verified as `LX/2Je;->run(): void` for Messenger 573.0.0.44.88, and changes the supplier's readiness signal from true to false.
- **Runtime mapping:** local `MultiVoidMethodSkipLayerDefinition`, constrained to `com.facebook.orca`, class `LX/2Je;`, and method `run(): void`. Skipping the known one-shot Runnable leaves the supplier from signalling that its subtabs are ready.
- **Effect and failure mode:** Home and Channels inbox subtabs remain absent when the exact reviewed Runnable exists. If the obfuscated target changes or disappears, no hook is installed.
- **Scope:** this is a local interface-preference control only. It does not alter messages, accounts, authentication, subscriptions, payments, content-access controls, integrity, or safety protections.

### Messenger — Disable media transcoding

- **Source review:** inspected `DisableMediaTranscodingPatch.kt` and `Fingerprints.kt` in `rushiranpise/morphe-patches`. The source targets the public Messenger service operation that contains the three stable markers `transcode`, `should_transcode`, and `transcoded_video_larger`, then returns `OperationResult.A00` before the transcoding workflow begins.
- **Runtime mapping:** an `ExistingPatchRuntimeLayerDefinition` is constrained to `com.facebook.orca`, the exact `OperationResult` return type, one object parameter, public access, and all three reviewed strings. The local hook resolves the actual target method's return class and reads only its reviewed `A00` default-result field.
- **Effect and failure mode:** when both the exact method and its expected result field exist, the client-side media-transcoding operation is skipped and Messenger continues with its normal fallback upload path. If either lookup fails, the original operation runs unchanged. Server upload limits remain unchanged.
- **Scope:** this is a local media-quality preference; it does not alter authentication, accounts, subscriptions, payments, server limits, integrity, safety protections, or content-access controls.

### X — Hide badges from navigation bar icons

- **Source review:** inspected `HideNavBarBadges.kt` in `crimera/piko`. The source targets `setBadgeNumber` on the class whose descriptor ends in `BadgeableTabView` and rewrites its integer badge-count argument through a setting hook.
- **Runtime mapping:** local `ExistingPatchRuntimeLayerDefinition`, constrained to `com.twitter.android`, the reviewed class suffix, and the `setBadgeNumber` method name. The local pre-hook replaces exactly one integer argument with `0`; any other shape proceeds unchanged.
- **Effect and failure mode:** notification nudges and numeric badges are suppressed in X navigation tab views that expose the reviewed method. If the target does not resolve or its argument signature changes, no value is modified.
- **Scope:** this is a local interface-preference control only. It does not alter notifications at the service level, accounts, authentication, subscriptions, payments, integrity, or safety protections.

### TikTok — Sanitize sharing links

- **Source review:** inspected `SanitizeShareUrlsPatch.kt` and its `ShareUrlTrackerFingerprint` in `icysymmetra/tiktok-patches-for-morphe`. The source identifies a static string-returning share-URL tracker containing `utm_campaign` and `share_link_id`, with at least two string parameters, then returns a link with its query parameters removed.
- **Runtime mapping:** local `ExistingPatchRuntimeLayerDefinition`, constrained to `com.zhiliaoapp.musically`, static access, a String return type, and both reviewed tracking anchors. The pre-hook also verifies at least two String arguments and operates only on the first String URL argument.
- **Effect and failure mode:** valid reviewed share URLs are returned without query parameters. If matching is ambiguous, the method signature changes, parsing fails, or the expected String argument is unavailable, the original method executes unchanged.
- **Scope:** the adapter only removes client-side share-link query parameters. It does not alter authentication, accounts, subscriptions, payments, content-access controls, integrity, safety protections, or downloaded code.

### Truecaller — Hide Scams tab

- **Source review:** inspected `HideScamsTabPatch.kt` and `ScamFeedEnabledFingerprint` in `Paresh-Maheshwari/paresh-patches` on GitLab. The source identifies a no-argument Boolean scam-feed gate by the paired calls `hc2/a.a()` and `hc2/baz.a()`, then returns `false` so the tab is not added to the bottom navigation.
- **Runtime mapping:** local `ExistingPatchRuntimeLayerDefinition`, constrained to `com.truecaller`, a Boolean return type, no arguments, and both reviewed call filters. The hook returns `false` only when that complete fingerprint resolves.
- **Effect and failure mode:** the Scams tab is not added to Truecaller navigation when the reviewed gate is found. The adapter neither blocks scam detection services nor changes account, caller-ID, or protection behavior. If the fingerprint changes or is absent, no hook is installed.

### Proton VPN — Remove delay

- **Source review:** inspected `RemoveDelayPatch.kt` and its two source fingerprints in `hoo-dles/morphe-patches`. The source returns zero from `getChangeServerLongDelayInSeconds` and `getChangeServerShortDelayInSeconds`.
- **Runtime mapping:** local `ExistingPatchRuntimeLayerDefinition` is constrained to `ch.protonvpn.android` and those two reviewed accessor names. It acts only on no-argument methods whose reflected return type is a supported numeric primitive or wrapper, returning the correctly typed zero value.
- **Effect and failure mode:** the known local server-change delay accessors resolve to zero when found. If an accessor is absent, takes arguments, or returns a nonnumeric type, its original implementation runs unchanged. This adapter does not modify server eligibility, account state, subscription, authentication, or network-security behavior.

### Instagram — Disable video autoplay (alternate catalog source)

- **Source review:** inspected `DisableVideoAutoplayPatch.kt` in `brosssh/morphe-patches`. The source constrains its target to a Boolean-returning method carrying the stable `ig_disable_video_autoplay` string and returns `true` before the host implementation runs.
- **Runtime mapping:** a separately attributed, locally compiled `BooleanReturnOverrideLayerDefinition` targets only `com.instagram.android`, return type `Z`, and the exact reviewed anchor `ig_disable_video_autoplay`. It does not fetch, parse, or execute the upstream patch archive.
- **Effect and failure mode:** when the uniquely anchored Boolean feature gate resolves, it returns `true`, preserving the upstream patch’s video-autoplay disabling outcome. If the string is absent, the method’s return type changes, or the target does not resolve, no hook is installed and Instagram retains its original behavior.
- **Scope:** this bridges the catalog item from its alternate public source without widening hook behavior. It does not alter accounts, authentication, subscriptions, payments, eligibility, integrity, safety protections, or content-access controls.
