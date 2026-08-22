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

### X — Remove search suggestions

- **Source review:** inspected `RemoveSearchSuggestionsPatch.kt` in `crimera/piko`. The source identifies a Collection-returning search-provider method by its `/search/provider/` class-path constraint and the paired `type` and `query_id` anchors, then returns `null` before the original provider body runs.
- **Runtime mapping:** the local `ExistingPatchRuntimeLayerDefinition` is constrained to `com.twitter.android`, the same partial provider class path, the exact `java.util.Collection` return contract, and both reviewed strings. The hook returns `null` only after that complete target resolves; it does not load the source archive or its settings extension.
- **Effect and failure mode:** matching X search-suggestion provider calls return no collection. If the class path, anchors, return type, or target resolution changes, no hook is installed and X retains its original search behavior.
- **Scope:** this is a local search-interface preference only. It does not alter results access, accounts, authentication, subscriptions, payments, integrity, protection, safety controls, or downloaded code.

### Messenger — Disable typing indicator (De-Vanced catalog source)

- **Source review:** inspected `DisableTypingIndicatorPatch.kt` and `Fingerprints.kt` in `RookieEnough/De-Vanced`. The source resolves the composer `run(): void` Runnable through a retained Redex original-name field whose value is `ConversationTypingContext$sendActiveStateRunnable$1`, then replaces its first instruction with `return-void`.
- **Runtime mapping:** the separately attributed local `MultiVoidMethodSkipLayerDefinition` remains constrained to the already reviewed `com.facebook.orca` target `LX/Ay7;->run(): void`. It does not import the source archive, interpret a retained-name field at runtime, or perform a broad `run()` scan.
- **Effect and failure mode:** the known composer Runnable is skipped before sending a typing-state event. If that concrete target is absent or changes in a Messenger build, no hook is installed. The source attribution does not widen the pre-existing method contract.
- **Scope:** this is a local messaging-privacy control only. It does not alter accounts, authentication, subscriptions, payments, moderation, integrity, safety protections, or content-access controls.

### Messenger — Hide inbox ads

- **Source review:** inspected `HideInboxAdsPatch.kt` and `LoadInboxAdsFingerprint` in `RookieEnough/De-Vanced`. The source optionally resolves a void method on `InboxAdsItemSupplierImplementation` with both `ads_load_begin` and `inbox_ads_fetch_start` markers, then returns before the load body executes. Newer Messenger versions may omit the supplier entirely.
- **Runtime mapping:** the local `ExistingPatchRuntimeLayerDefinition` is constrained to `com.facebook.orca`, the exact reviewed `InboxAdsItemSupplierImplementation` descriptor, a void return type, and both source markers. It returns before the body only when that complete target resolves; no downloaded code, source extension, or broad inbox scan is used.
- **Effect and failure mode:** a resolved source-equivalent loader is skipped, preventing that local inbox-ad load path. If the supplier was removed, its descriptor or markers change, or the target fails to resolve, no hook is installed and Messenger continues unchanged.
- **Scope:** this is a narrow local interface control. It does not alter messages, accounts, authentication, subscriptions, payments, eligibility, integrity, safety protections, or content-access controls.

### Messenger — Hide inbox stories and notes tray (De-Vanced catalog source)

- **Source review:** inspected `HideInboxStoriesNotesTrayPatch.kt` and `FriendsInboxTrayFingerprint` in `RookieEnough/De-Vanced`. The source targets a Boolean-returning gate anchored by `com.facebook.messaging.friendsinboxunit.plugins.inboxunit.FriendsInboxUnitKillSwitch` and returns `false` before the original body runs.
- **Runtime mapping:** the separately attributed local `BooleanReturnOverrideLayerDefinition` remains constrained to `com.facebook.orca`, Boolean return type, and that exact reviewed marker. It does not import the source archive or add a broad Messenger UI hook.
- **Effect and failure mode:** the exact gate returns `false`, so the reviewed inbox stories-and-notes tray is not displayed. If the marker, return type, or target changes, no hook is installed and Messenger keeps its original interface behavior.
- **Scope:** this is a local interface-preference control only. It does not alter messages, accounts, authentication, subscriptions, payments, eligibility, integrity, safety protections, or content-access controls.

### Messenger — Open links externally (De-Vanced catalog source)

- **Source review:** inspected `OpenLinksExternallyPatch.kt` and `ShouldOpenInAppBrowserFingerprint` in `RookieEnough/De-Vanced`. The source identifies the Boolean in-app-browser decision through the exact `Uri` and `FbUserSession` parameters plus `iab_skipped_reason` and `user_prefers_external`, then returns `false` to select the normal external-browser route.
- **Runtime mapping:** the separately attributed local `BooleanReturnOverrideLayerDefinition` retains exactly those package, Boolean-return, parameter, and marker constraints. It is locally compiled and does not import an upstream archive or invoke an unreviewed external component.
- **Effect and failure mode:** only the resolved in-app-browser decision returns `false`, handing an eligible link to Messenger's existing external-browser path. If the signature, anchors, or target resolution changes, no hook is installed and Messenger keeps its original behavior.
- **Scope:** this is a local link-handling preference only. It does not alter accounts, authentication, subscriptions, payments, eligibility, integrity, safety protections, or content-access controls.

### Messenger — Hide inbox subtabs (De-Vanced catalog source)

- **Source review:** inspected `HideInboxSubtabsPatch.kt` and the shared `CreateInboxSubTabsFingerprint` in `RookieEnough/De-Vanced`. The source resolves the inbox-subtabs supplier through the retained Redex original name `InboxSubtabsItemSupplierImplementation$onSubscribe$1`, then changes the supplier's readiness result to false.
- **Runtime mapping:** the separately attributed local `MultiVoidMethodSkipLayerDefinition` remains constrained to the already reviewed `com.facebook.orca` target `LX/2Je;->run(): void`. It does not enumerate arbitrary `run()` methods or interpret upstream metadata dynamically.
- **Effect and failure mode:** the known supplier Runnable is skipped before it signals that subtabs are ready. If its concrete target is absent or changes after an app update, no hook is installed and Messenger continues unchanged.
- **Scope:** this is a local inbox-interface control only. It does not alter messages, accounts, authentication, subscriptions, payments, eligibility, integrity, safety protections, or content-access controls.

### CamScanner — Disable telemetry

- **Source review:** inspected `DisableTelemetryPatch.kt` in `rushiranpise/morphe-patches`. The patch identifies CamScanner's non-obfuscated `Lcom/intsig/logagent/LogAgent;` class and its three static `Init` overloads, all returning `int`: Application plus three strings and an integer; Application plus `SocketInterface`; and Application plus four strings. The source returns `0`, the documented benign initialized/success status, before the agent is created.
- **Runtime mapping:** the local adapter enumerates only those three exact `Init` signatures on the exact LogAgent descriptor, and returns integer `0` before each resolved body. It does not hook arbitrary methods, scan unrelated classes, load upstream code, or block CamScanner's scanning, document, account, subscription, or payment logic.
- **Effect and failure mode:** resolved LogAgent initializers do not create the custom telemetry, event-tracking, or remote-logging agent. If any descriptor, method name, return contract, or parameter signature changes, that overload is left unhooked and CamScanner retains its original behavior.
- **Scope:** this is a narrow telemetry control only. It does not alter authentication, license or subscription state, paid features, integrity, safety protections, content access, or account behavior.

### ES File Explorer — Disable Tracking

- **Source review:** inspected `DisableTrackingPatch.kt` and `AnalyticsInitFingerprint` in `rushiranpise/morphe-patches`. The source targets the non-obfuscated `Lcom/estrongs/android/pop/FexApplication;->M(): void` method that contains the `China` UMeng analytics-channel anchor and initializes UMConfigure plus UMCrash.
- **Runtime mapping:** the local adapter requires the exact FexApplication descriptor, method name `M`, void return type, no parameters, and `China` marker before skipping the body. It does not hook broader application initialization or unrelated analytics SDK methods.
- **Effect and failure mode:** a resolved target skips the reviewed UMeng analytics and crash-report setup path. If the class, method name, marker, return contract, or signature changes, no hook is installed and ES File Explorer retains its original behavior.
- **Scope:** this is a local telemetry and crash-reporting control only. It does not alter authentication, licensing or subscription state, paid features, integrity, safety protections, content access, or account behavior.

### Amazon Shopping / Amazon India — Disable search suggestions tracking

- **Source review:** inspected `DisableSearchSuggestionsTrackingPatch.kt` and `SearchSuggestionsSetEventFingerprint` in `rushiranpise/morphe-patches`. The source targets exactly `SearchSuggestionsV2Request$Builder.setEvent(Event)`, returning the builder unchanged before a keypress or focus event is stored for a suggestions request.
- **Runtime mapping:** the local adapter requires the exact nested Builder descriptor, `setEvent` method name, Builder return contract, and Event parameter, then returns the original builder object. The catalog mapping is limited to `com.amazon.mShop.android.shopping` and `in.amazon.mShop.android.shopping`.
- **Effect and failure mode:** matching search-suggestion requests proceed without the reviewed event field, while the original builder and regular request flow remain intact. If the class, method, return type, or parameter changes, no hook is installed and the app keeps its original behavior.
- **Scope:** this is a local search-event telemetry control only. It does not buy, sell, order, pay, alter product eligibility, modify accounts, authentication, subscriptions, integrity, safety protections, or content access.

### Telegram Plus — Disable analytics

- **Source review:** inspected `TelegramPlusDisableAnalyticsPatch.kt` and `TelegramPlusFingerprints.kt` in `rushiranpise/morphe-patches`, verified for Telegram Plus `org.telegram.plus`. The source targets exactly `AnalyticsHelper.enableAnalytics(Application)`, `trackEvent(String)`, and `trackEvent(String, HashMap)`, each returning void.
- **Runtime mapping:** the local adapter lists only those three exact AnalyticsHelper signatures, then skips each resolved body. It does not touch Firebase app initialization, push notifications, Telegram messaging, account state, or arbitrary analytics classes.
- **Effect and failure mode:** matching initialization and event-dispatch methods are no-oped. If any class descriptor, method name, return contract, or parameter signature changes, that target is not hooked and Telegram Plus keeps its original behavior.
- **Scope:** this is a local Firebase analytics and event-tracking control only. It does not alter authentication, subscriptions, licensing, premium features, integrity, safety protections, content access, message delivery, or account behavior.

### Follow-up screening — 2026-08-22

- **Duolingo — Disable dynamic app icon** (`hoo-dles/morphe-patches`): deferred. The reviewed source depends on a login-integrity patch and edits the target application's `AndroidManifest.xml` by removing disabled `activity-alias` entries. This is a resource-time transformation, not a narrow LSPosed Runtime hook.
- **Truecaller — Hide Assistant tab** (`Paresh-Maheshwari/paresh-patches`): already represented locally as `paresh.truecaller.hide-assistant-tab.runtime`. The source fingerprint is a no-argument Boolean decision with the exact `featureCallAssistant` string anchor; no duplicate adapter is added.
- **Instagram — Disable story auto flipping** (`brosssh/morphe-patches`): already represented locally as `piko.instagram.disable-story-flipping.runtime`, using the reviewed `ReelViewerFragment`, `userSession`, one-object-argument, void target. No duplicate catalog bridge is added.
- **Facebook — Hide Sponsored Stories** (`RookieEnough/De-Vanced`): deferred. The reviewed source synthesizes a private static helper, extracts runtime literals from another method, and injects a conditional GraphQL-story branch into a target method. This cannot be faithfully represented by a narrow fixed LSPosed hook without broad bytecode construction.
- **TikTok — Disable long-press quick share** (`icysymmetra/tiktok-patches-for-morphe`): deferred. It depends on the TikTok shared extension, inserts a settings-status call, and injects a feature-controls override at a calculated return instruction; no extension code or dynamic patch instructions are accepted by this module.
- **Gboard — Disable Diagnostics** (`kveld9/kveld-morphe-patches`): already represented locally as `kveld.gboard.disable-diagnostics.runtime`, which mirrors the reviewed AppDoctor DEX-`p0` object return and exact receiver void skip. No duplicate adapter is added.
- **Brave — Block Telemetry** (`kveld9/kveld-morphe-patches`): deferred. The source requires target-resource default rewrites and direct version-offset mutation of `libchrome.so`, in addition to bytecode changes. It is outside the local LSPosed Runtime contract.
- **TeraBox — Unlock VIP** (`rushiranpise/morphe-patches`): rejected. The only catalog entry is a subscription/Premium entitlement unlock and remains permanently out of scope.

All reviewed sources in this pass were read as text only. No archive, extension, native payload, or external executable code was loaded, interpreted, or run.

### Telegram Plus — Remove ads

The public `TelegramPlusRemoveAdsPatch.kt` and its fingerprint files in `rushiranpise/morphe-patches` were reviewed as text only. The local adapter is limited to the exact Plus controller and instance targets `AdsController.adsDisabled(): boolean`, `AdsInstance.loadAds(): void`, and `AdsInstance.loadNativeAd(Context, boolean, AdsInstanceInterface)`, as well as the exact inherited Telegram sponsored-state and private video-ad loader targets named by the source.

`loadNativeAd` is intentionally fail-closed at runtime. The hook returns `false` only if the resolved target returns Boolean and skips it only if it returns void, which are the two source-reviewed contracts. Any other return type is left to the original implementation. All other targets require their exact class descriptor, name, return contract, and where applicable parameter types and private access flag. A missing, ambiguous, or changed target therefore receives no hook.

The adapter affects only local banner, native, sponsored-message, and video-ad loading decisions for `org.telegram.plus`. It does not modify message delivery, accounts, authentication, contacts, subscriptions, Premium or licensing state, payments, integrity or protection checks, channel/content restrictions, or any remote patch payload. No community archive, extension, or executable external code is loaded or interpreted.

### Telegram Plus — Hide typing indicator

The public `TelegramPlusHideTypingPatch.kt` and its `PlusSendTypingFingerprint` in `rushiranpise/morphe-patches` were reviewed as text only. The local adapter matches the exact `MessagesController.sendTyping(long, long, int, int): boolean` dispatch method and returns `false`, then enumerates only no-argument void methods named `needSendTyping` in the Telegram Plus process, matching the source's explicit all-match UI-delegate behavior.

The Boolean dispatch target is constrained by its exact class descriptor, name, return contract, and four primitive parameter types. The UI target list requires the exact method name, void return type, and zero parameters; an empty or changed match list simply installs no hooks. This adapter only suppresses the client-side typing-state send path. It does not alter messages, account state, authentication, subscriptions, payments, Premium or licensing, integrity, safety controls, content restrictions, or remote patch payloads.

### Gboard — Force Incognito Mode

The public `GboardForceIncognitoPatch.kt` in `kveld9/kveld-morphe-patches` was reviewed as text only. The local adapter enumerates only two exact Boolean targets: `Lsew;.H(EditorInfo): boolean` and `Lfoh;.F(): boolean`. Each resolved target returns `true`, mirroring the reviewed source's local incognito-state decision without any extension, resource transformation, or downloaded code.

A target with a changed class descriptor, method name, return contract, or parameter list does not resolve and receives no hook. The adapter only controls these client-side incognito checks for `com.google.android.inputmethod.latin`; it does not unlock features, bypass authentication or integrity, change payments or subscriptions, access text content, or load external payloads.

### Truecaller — Disable analytics

The public `DisableAnalyticsPatch.kt` and `Fingerprints.kt` in `bufferk/morphe-patches` were reviewed as text only. The local adapter lists only the two reviewed public-final void methods on `Lkr0/k;`: `push(String)` and `push(String, Map)`. Each resolved target is skipped before it dispatches a CleverTap behavioural event.

The adapter does not hook profile updates, push-notification registration, Firebase diagnostics, Adjust attribution, or arbitrary analytics classes. If the exact class descriptor, method name, void contract, parameter list, or public-final access flags change, no hook is installed. It only suppresses those local behavioural-event dispatches for `com.truecaller`; accounts, caller-ID service, authentication, payments, subscriptions, licensing, integrity, protection, and content access remain unchanged.

### Truecaller — Hide ads

The public `HideAdsPatch.kt` and `Fingerprints.kt` in `bufferk/morphe-patches` were reviewed as text only. The local adapter skips only the two exact void after-call ad-update targets reviewed by the source: `Lcom/truecaller/acs/ui/baz;.Rh(boolean)` and `Ltw1/f;.Th(boolean)`.

Both targets require the exact class descriptor, method name, void return contract, and Boolean parameter. If either path changes or is absent in a Truecaller release, no hook is installed for it. The adapter affects only the local after-call and Neo after-call advertising update paths; it does not modify caller-ID logic, spam detection, accounts, authentication, payments, subscriptions, licensing, integrity or protection checks, or content access.

### Avito — Disable telemetry

The public `DisableTelemetryPatch.kt` and `Fingerprints.kt` in `xob0t/morphe-patches` were reviewed as text only. The local adapter considers only six reviewed void telemetry paths: the primary clickstream tracker (or legacy clickstream enqueue path when independently resolved), Adjust initialization, Adjust event tracking, Adjust user-ID partner-parameter handling, and Adjust push-token handling. Each candidate is constrained by the source's Avito package-prefix, parameter, string, and/or invoked-method anchors before a no-op hook is installed.

Unlike the bytecode source patch, a missing or changed target simply receives no hook; the module does not guess classes, scan arbitrary analytics methods, or load source extensions. The adapter does not modify Avito listings, messages, accounts, authentication, payments, subscriptions, licensing, integrity, or content access. It only suppresses the reviewed local clickstream and Adjust dispatch paths for `com.avito.android`.

### MyTelenor — Block trackers

The public `BlockTrackersPatch.kt` and `Fingerprints.kt` in `totsiaw/proxma-patches` were reviewed as text only. The local adapter targets only three reviewed void initialization chokepoints: the Insider launcher anchored by the paired Kotlin parameter strings `partnerName` and `notificationCallback`; the TikTok Business worker anchored by its `TikTokBusinessSdk.initializeSdk` call; and the Mixpanel Session Replay launcher anchored by `Failed to initialize Mixpanel SDK`.

Each resolved target is skipped before the associated tracking SDK starts. If any class, signature, access flags, invoked method, or string anchor changes, that target installs no hook. AWS Amplify, Firebase core, RemoteConfig, FCM, Google Mobile Ads, app data, push, accounts, authentication, payments, subscriptions, licensing, integrity, protection, and content access remain unchanged.

### ZEE5 Android TV — Disable analytics

The public `DisableAnalyticsPatch.kt` and `Fingerprints.kt` in `WZSE/aapam-patches` were reviewed as text only. The local adapter hooks only `Lcom/zee5/android/analytics/data/DefaultAnalytics;->trackEvent(Lcom/zee5/android/analytics/data/trackers/mixpanel/data/models/AnalyticEvent;)V`, the source's central analytics event dispatcher for the supported `com.graymatrix.did` target.

The resolved void method is skipped before analytics dispatch. If its class, name, return type, or event parameter changes, no hook is installed. Playback, server-side ad insertion, accounts, authentication, payments, subscriptions, licensing, integrity, protection, and content access remain unchanged.

### ZEE5 Android TV — Disable CleverTap

The public `DisableCleverTapPatch.kt` and its `CleverTapInitFingerprint` in `WZSE/aapam-patches` were reviewed as text only. The local adapter hooks only `Lcom/zee5/android/analytics/data/trackers/clevertap/DefaultCleverTapAnalytics;->initCleverTap(): void`, the source-reviewed CleverTap initialization chokepoint for the supported `com.graymatrix.did` target.

The resolved void method is skipped before the CleverTap SDK initializes. If the class, method name, public access contract, return type, or no-argument signature changes, no hook is installed. The adapter does not download, interpret, or execute any upstream archive, extension, or payload. Playback, server-side ad insertion, accounts, authentication, payments, subscriptions, licensing, integrity, protection, and content access remain unchanged.

### Instagram — Disable story auto flipping

The public `DisableStoryAutoFlippingPatch.kt` in `brosssh/morphe-patches` was reviewed as text only. The source targets the story-timeout action with the `userSession` marker, a void return type, one object parameter, and a class descriptor ending in `ReelViewerFragment`. The local catalog-attribution adapter uses the exact reviewed `Linstagram/features/stories/fragment/ReelViewerFragment;` descriptor together with the same marker, return contract, and parameter constraint.

When that exact timeout action resolves, it is skipped before the original body and the active story is not advanced by this client-side timeout path. A changed class, marker, return type, or parameter signature produces no hook. The adapter does not alter stories themselves, networking, accounts, authentication, subscriptions, payments, integrity, safety controls, content access, or any external archive, extension, or payload.

### Plus Messenger — Disable analytics

The public `DisableAnalyticsPatch.kt` and its Plus Messenger compatibility constant in `Paresh-Maheshwari/paresh-patches` were reviewed as text only. The local adapter considers only `Lorg/telegram/plus/helpers/AnalyticsHelper;->start(): void`, `trackEvent(String): void`, and `trackEvent(String, HashMap): void` for the reviewed `org.telegram.plus` package.

Each resolved void target is skipped before analytics startup or event dispatch. A changed class descriptor, method name, return type, or parameter signature produces no hook for that method. The adapter does not load, interpret, or execute an upstream archive, extension, or payload, and it does not modify messages, accounts, authentication, contacts, subscriptions, licensing, payments, integrity, safety controls, content restrictions, or content access.

### Strava — Block Snowplow tracking

The public `BlockSnowplowTrackingPatch.kt` and `InsertEventFingerprint` in `RookieEnough/De-Vanced` were reviewed as text only. The local adapter is limited to the unique void method anchored by `Added event to database: %s`, the source-reviewed Snowplow event-store insertion path for `com.strava`.

When the exact method resolves, it is skipped before the event is persisted for later Snowplow upload. A changed anchor, return type, or target ambiguity yields no hook. Activity recording, routes, social data, accounts, authentication, subscriptions, payments, integrity, safety controls, and content access remain unchanged. No external archive, extension, or payload is downloaded, interpreted, or executed.

### Strava — subscription bypass cleanup

The previously registered local `Unlock subscription features` hook has been removed, along with its implementation files. Subscription and entitlement paths are outside this module's Runtime scope and are not converted or exposed.

### Photomath — entitlement cleanup

The active local `Unlock plus` path was audited and removed, including its application registration, package visibility and LSPosed scope entries, Runtime target registration, and implementation files. It overrode a Plus-unlocked decision and therefore falls outside the local privacy and interface Runtime scope. Photomath is no longer displayed or classified as a supported local Runtime target.

### SoundCloud — Disable telemetry

The public `DisableTelemetryPatch.kt`, `HandleMessageFingerprint`, and compatibility constant in `hoo-dles/morphe-patches` were reviewed as text only. The local adapter considers only `TrackingHandler.handleMessage(): void` for `com.soundcloud.android`, as constrained by the source-defined `TrackingHandler` class suffix and method name.

The resolved void method is skipped before the reviewed telemetry message is handled. A changed class suffix, method name, return type, or absent method produces no hook. Playback, accounts, authentication, subscriptions, licensing, purchases, integrity, content access, and application UI outside that telemetry handler remain unchanged. No external archive, extension, or payload is downloaded, interpreted, or executed.

### NoMone — Disable telemetry (deferred)

The candidate source was reviewed as text only and rejected. Although its final target is a telemetry event method, it explicitly depends on `disableAntiTamperPatch`. Protection and anti-tamper bypasses are outside the permitted local Runtime scope, so no adapter, package registration, or catalog entry was added.

### SuperChinese — Disable telemetry (deferred)

The candidate source was reviewed as text only and rejected. It explicitly depends on `spoofSignaturePatch` and replaces a constructor instruction. Signature spoofing and protection bypasses are outside the permitted local Runtime scope, so no adapter, package registration, or catalog entry was added.

### Strava — Disable Quick Edit

The public `DisableQuickEditPatch.kt` and `GetHasAccessToQuickEditFingerprint` in `RookieEnough/De-Vanced` were reviewed as text only. The local adapter targets only the boolean `getHasAccessToQuickEdit` method and returns `false`, matching the reviewed prompt-suppression behavior.

This is limited to suppressing the Quick Edit prompt gate. It does not enable Quick Edit or other paid functionality, alter subscriptions or entitlement state, modify purchases, accounts, authentication, integrity, activity recording, routes, or social data. A changed method name or return type produces no hook, and no external archive, extension, or payload is downloaded, interpreted, or executed.

### CamScanner — Disable telemetry (Hoo-dles candidate, deferred)

The candidate source was reviewed as text only and rejected. It explicitly depends on `spoofSignaturePatch` and a package-installer change. Signature spoofing and installer manipulation are outside the permitted local Runtime scope, so this alternate catalog source is not adapted or displayed. The separate reviewed CamScanner telemetry adapter already present remains limited to its own safe fingerprints.

### Cloudflare WARP — Disable Analytics / Telemetry (deferred)

The candidate source was reviewed as text only and deferred. It couples a direct void hook with dynamic discovery of compiler-numbered Kotlin lambda classes, instruction inspection, and Kotlin `Unit` object-return rewrites. Translating only its first layer would make the catalog claim incomplete, while translating the dynamic scan would broaden the Runtime surface beyond a narrow, stable adapter. No adapter or catalog entry was added.

### Instagram — Hide Notes Tray (deferred)

The candidate source was reviewed as text only and deferred. It depends on resource mapping and performs register-aware instruction injection around an obfuscated view-construction path. That is not equivalent to a narrow LSPosed/DexKit method hook, so no partial or broader Runtime adapter was added.

### Instagram — Disable Reels Scrolling (deferred)

The candidate source was reviewed as text only and deferred. It locates an obfuscated `ViewPager2` field and injects instructions to change user input, plus modifies a touch-interception method. These field and instruction rewrites exceed the narrow LSPosed/DexKit Runtime adapter model, so no partial adapter or catalog entry was added.

### TikTok — Disable Long Press Quick Share (deferred)

The candidate source was reviewed as text only and rejected. It explicitly depends on a TikTok shared extension and invokes extension-owned settings and feature-control methods. The application does not download, interpret, or execute extensions, so no adapter or catalog entry was added.

### Messenger — Disable typing indicator

The public `DisableTypingIndicatorPatch.kt` and `SendTypingIndicatorFingerprint` in `RookieEnough/De-Vanced` were reviewed as text only. The local adapter requires a zero-argument `run(): void` method in a class containing the Redex original-name field value `ConversationTypingContext$sendActiveStateRunnable$1`.

Only that resolved active-typing dispatch runnable is skipped. A changed field value, method name, signature, or class shape produces no hook. Message content, delivery, read receipts, accounts, authentication, attachments, calls, subscriptions, payments, integrity controls, screenshot controls, and content access remain unchanged. No external archive, extension, or payload is downloaded, interpreted, or executed.

### Facebook — Hide story ads (deferred)

The candidate source was reviewed as text only and deferred. Its two `run(): void` targets are identified solely through Redex original-name field values. The local Runtime fingerprint bridge supports method constraints and class-level matchers, but not a field-value class constraint; a method-name-only translation would be broad and unsafe. No adapter or catalog entry was added.

### Pixiv — Hide ads

The public `HideAdsPatch.kt`, `ShouldShowAdsFingerprint`, and Pixiv compatibility record in `RookieEnough/De-Vanced` were reviewed as text only. The local adapter matches only a public final boolean `shouldShowAds` method inside a class whose name ends with `.AdUtils`, then returns `false`.

A changed class suffix, method name, access flags, or return type produces no hook. The adapter does not alter Pixiv accounts, authentication, subscriptions, purchases, licensing, content access, image downloads, privacy settings, or integrity controls. No external archive, extension, or payload is downloaded, interpreted, or executed.

### Amazon Music — Prevent log upload

The public `PreventUploadLogsPatch.kt` in `RookieEnough/De-Vanced` was reviewed as text only. The source defines two exact, optional zero-argument void methods on `Lcom/amazon/mp3/det/PendingCrashLogs;`: `uploadLogAfterCrash()` and `uploadPendingCrashLogsIfRequired()`.

The local adapter is constrained to `com.amazon.mp3`, that exact class descriptor, both exact method names, void return type, and an empty parameter list. Each resolved method is skipped before the original upload body. A changed descriptor, name, return type, parameter list, or absent target installs no hook. The adapter does not alter Amazon Music playback, catalogue access, downloads, accounts, authentication, subscriptions, purchases, licensing, integrity, crash capture, or ordinary application logging; it only suppresses the two reviewed client-side crash-log upload dispatches. No external archive, extension, or payload is downloaded, interpreted, or executed.

### Follow-up screening after Alpha 52

- **Letterboxd — Hide ads** (`RookieEnough/De-Vanced`): deferred. Its source combines a setter-argument rewrite with several app-version-dependent `showAds` methods whose complete parameter contracts are not constrained by the supplied fingerprints. A partial `shouldShowAds` override would not faithfully represent the source, so no adapter or catalog entry was added.
- **IMDb and 9GAG — Remove ads** (`jkennethcarino/adobo`): deferred. Both sources require the shared host-blocker dependency, and 9GAG also requires a hide-containers dependency. The local module does not install host lists or execute dependency patches.
- **Reddit — Hide Ask button** (`jkennethcarino/adobo`): deferred. The source depends on certificate-hash spoofing, which is outside the local Runtime safety boundary.
- **Opera News — Remove ads** (`rushiranpise/morphe-patches`): deferred. The source scans broad classes and rewrites method bodies and instructions across multiple advertising SDK integrations; it cannot be represented as a narrow fixed Runtime hook.
- **TikTok — Remember clear display** (`icysymmetra/tiktok-patches-for-morphe`): deferred. The source injects instructions and calls extension-owned code, neither of which is loaded or executed by this module.
- **Messenger — Hide Facebook buttons** (`rushiranpise/morphe-patches`): deferred. The source combines signature handling with class-wide instruction inspection to discover plugin constructors. No broad scan, signature bypass, or partial UI hook was added.

All candidates in this follow-up pass were read as text only; no archive, extension, native payload, executable code, or external patch artifact was downloaded, interpreted, or run.

### Sofascore — Disable ads

The public `DisableAdsPatch.kt` and `Fingerprints.kt` in `hoo-dles/morphe-patches` were reviewed as text only. The source declares compatibility with `com.sofascore.results` version `25.12.17` and changes exactly three `UserAccount` advertising decisions: `getForceAds` to `false`, `getForceHideAds` to `true`, and `getHasServerAds` to `false`.

The local adapter narrows each method further to a Boolean return contract, no parameters, and a class descriptor ending in `UserAccount;`, then applies only the source-reviewed Boolean value. A changed class suffix, method name, return type, parameter list, or absent target installs no hook. This is a local advertising-visibility control only; it does not enable paid features, alter subscriptions, purchases, accounts, authentication, results data, live-score access, integrity, or protection behavior. No external archive, extension, or payload is downloaded, interpreted, or executed.

### SonyLIV — Remove video ads

The public `RemoveVideoAdsPatch.kt` and `Fingerprints.kt` in `durgesh0505/chiggi_morphe_patches` were reviewed as text only. The source identifies exactly two public static Boolean gates on `Lcom/sonyliv/mediaplayer/util/PlayerUtil;`: `isAdEnable()` and `isAdEnable(Lcom/sonyliv/network/model/core/AssetMetadata;)`. It returns `false` from both gates so the client player does not initiate the reviewed Google IMA ad setup path.

The local adapter applies the same `false` result only when the exact class descriptor, method name, public-static access, Boolean return type, and the respective zero-argument or AssetMetadata parameter contract resolve in `com.sonyliv`. A changed class, method, access flags, return type, parameter list, or absent target installs no hook. This layer only controls client-side ad setup; it does not alter accounts, authentication, subscriptions, purchases, licensing, content eligibility, DRM, server-side access controls, downloads, integrity, or geolocation checks. Server-side stitched advertising may remain unaffected. No external archive, extension, or payload is downloaded, interpreted, or executed.

### Vi Movies and TV — Remove ads

The public `RemoveAdsPatch.kt` and `Fingerprints.kt` in `durgesh0505/chiggi_morphe_patches` were reviewed as text only. The source identifies two client-side ad inputs in `com.vimtv`: `Lcom/yupptv/ottsdk/model/ads/AdUrlResponse;->getAdUrlTypes(): java.util.List` and `Lcom/yupptv/ottsdk/model/user/TorcAiAdConfig;->isAdEnabled(): boolean`. The upstream behavior returns `null` from the exact ad-URL list getter and `false` from the exact Boolean ad-enable gate, so the reviewed client IMA/SSAI ad-tag path is not initiated.

The local adapter applies those results only after the exact class descriptors, method names, and return types resolve inside `com.vimtv`; absent or changed targets install no hook. This layer does not alter accounts, authentication, subscriptions, purchases, licensing, content eligibility, DRM, downloads, integrity, regional availability, or other server-side access controls. No external archive, extension, or payload is downloaded, interpreted, or executed.
