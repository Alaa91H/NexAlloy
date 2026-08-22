package io.github.nexalloy.bridge

import io.github.nexalloy.morphe.FingerprintDsl
import io.github.nexalloy.runtime.ImportedBooleanRuntimeLayerSpec
import io.github.nexalloy.runtime.RuntimeLayerImportException
import io.github.nexalloy.runtime.RuntimeLayerRegistry
import io.github.nexalloy.runtime.RuntimeLayerSpecCodec
import io.github.nexalloy.runtime.RuntimeStoreAvailability
import io.github.nexalloy.runtime.RuntimeStoreClassifier
import io.github.nexalloy.runtime.RuntimeStoreRecipeRegistry
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RuntimeCatalogBridgeTest {
    @Test
    fun `parser reads compatible packages options and catalog hash`() {
        val catalog = RuntimeCatalogParser().parse(SAMPLE_CATALOG, fetchedAtMillis = 1L)

        assertEquals(1L, catalog.fetchedAtMillis)
        assertEquals(64, catalog.sha256.length)
        assertEquals(1, catalog.bundles.size)
        assertEquals(1, catalog.patchCount)

        val bundle = catalog.bundles.single()
        assertEquals("crimera/piko", bundle.repository)
        assertEquals(SourceTrust.REVIEW_REQUIRED, bundle.trust)
        assertEquals(setOf("com.instagram.android"), bundle.packageNames)

        val patch = bundle.patches.single()
        assertEquals("Disable video autoplay", patch.name)
        assertEquals(setOf("com.instagram.android"), patch.compatiblePackages)
        assertEquals(1, patch.options.size)
        assertEquals("enabled", patch.options.single().key)
    }

    @Test
    fun `runtime layer registry exposes reviewed compiled layers`() {
        val layer = RuntimeLayerRegistry.find(
            sourceRepository = "crimera/piko",
            sourcePatchName = "Disable video autoplay",
            packageName = "com.instagram.android",
        )

        assertEquals("piko.instagram.disable-video-autoplay.runtime", layer?.id)
        val storyLayer = RuntimeLayerRegistry.find(
            sourceRepository = "crimera/piko",
            sourcePatchName = "Disable story flipping",
            packageName = "com.instagram.android",
        )
        assertEquals("piko.instagram.disable-story-flipping.runtime", storyLayer?.id)
        assertEquals(3, RuntimeLayerRegistry.layersFor("com.instagram.android").size)
        assertEquals(2, RuntimeLayerRegistry.layersFor("com.reddit.frontpage").size)
        assertEquals(
            "morphe.reddit.hide-ads.runtime",
            RuntimeLayerRegistry.find(
                sourceRepository = "rushiranpise/morphe-patches",
                sourcePatchName = "Hide Ads",
                packageName = "com.reddit.frontpage",
            )?.id,
        )
        assertEquals(
            "kveld.gboard.block-telemetry.runtime",
            RuntimeLayerRegistry.find(
                sourceRepository = "kveld9/kveld-morphe-patches",
                sourcePatchName = "Block Telemetry",
                packageName = "com.google.android.inputmethod.latin",
            )?.id,
        )
        assertEquals(
            "kveld.gboard.disable-diagnostics.runtime",
            RuntimeLayerRegistry.find(
                sourceRepository = "kveld9/kveld-morphe-patches",
                sourcePatchName = "Disable Diagnostics",
                packageName = "com.google.android.inputmethod.latin",
            )?.id,
        )
        assertEquals(
            "kveld.gboard.disable-remote-configuration.runtime",
            RuntimeLayerRegistry.find(
                sourceRepository = "kveld9/kveld-morphe-patches",
                sourcePatchName = "Disable Remote Configuration",
                packageName = "com.google.android.inputmethod.latin",
            )?.id,
        )
        assertEquals(
            "kveld.gboard.disable-superpacks-eager-sync.runtime",
            RuntimeLayerRegistry.find(
                sourceRepository = "kveld9/kveld-morphe-patches",
                sourcePatchName = "Disable Superpacks Eager Sync",
                packageName = "com.google.android.inputmethod.latin",
            )?.id,
        )
        assertEquals(
            "kveld.gboard.disable-tenor-share-tracking.runtime",
            RuntimeLayerRegistry.find(
                sourceRepository = "kveld9/kveld-morphe-patches",
                sourcePatchName = "Disable Tenor Share Tracking",
                packageName = "com.google.android.inputmethod.latin",
            )?.id,
        )
        assertEquals(
            "paresh.truecaller.hide-assistant-tab.runtime",
            RuntimeLayerRegistry.find(
                sourceRepository = "Paresh-Maheshwari/paresh-patches",
                sourcePatchName = "Hide Assistant tab",
                packageName = "com.truecaller",
            )?.id,
        )
        assertEquals(
            "paresh.truecaller.disable-update-check.runtime",
            RuntimeLayerRegistry.find(
                sourceRepository = "Paresh-Maheshwari/paresh-patches",
                sourcePatchName = "Disable update check",
                packageName = "com.truecaller",
            )?.id,
        )
        assertEquals(
            "paresh.truecaller.disable-telemetry.runtime",
            RuntimeLayerRegistry.find(
                sourceRepository = "Paresh-Maheshwari/paresh-patches",
                sourcePatchName = "Disable telemetry",
                packageName = "com.truecaller",
            )?.id,
        )
        assertEquals(
            "morphe.telegram.remove-sponsored-messages.runtime",
            RuntimeLayerRegistry.find(
                sourceRepository = "rushiranpise/morphe-patches",
                sourcePatchName = "Remove ads",
                packageName = "org.telegram.messenger",
            )?.id,
        )
        assertEquals(
            "morphe.telegram.disable-auto-update.runtime",
            RuntimeLayerRegistry.find(
                sourceRepository = "rushiranpise/morphe-patches",
                sourcePatchName = "Disable auto-update",
                packageName = "org.telegram.messenger",
            )?.id,
        )
        assertEquals(
            "morphe.telegram.hide-typing-indicator.runtime",
            RuntimeLayerRegistry.find(
                sourceRepository = "rushiranpise/morphe-patches",
                sourcePatchName = "Hide typing indicator",
                packageName = "org.telegram.messenger",
            )?.id,
        )
        assertEquals(
            "morphe.telegram.disable-channel-switching.runtime",
            RuntimeLayerRegistry.find(
                sourceRepository = "rushiranpise/morphe-patches",
                sourcePatchName = "Disable channel switching",
                packageName = "org.telegram.messenger",
            )?.id,
        )
        assertEquals(
            "morphe.messenger.remove-meta-ai.runtime",
            RuntimeLayerRegistry.find(
                sourceRepository = "rushiranpise/morphe-patches",
                sourcePatchName = "Remove Meta AI",
                packageName = "com.facebook.orca",
            )?.id,
        )
        assertEquals(
            "morphe.messenger.disable-media-transcoding.runtime",
            RuntimeLayerRegistry.find(
                sourceRepository = "rushiranpise/morphe-patches",
                sourcePatchName = "Disable media transcoding",
                packageName = "com.facebook.orca",
            )?.id,
        )
        assertEquals(
            "morphe.messenger.open-links-externally.runtime",
            RuntimeLayerRegistry.find(
                sourceRepository = "rushiranpise/morphe-patches",
                sourcePatchName = "Open links externally",
                packageName = "com.facebook.orca",
            )?.id,
        )
        assertEquals(
            "morphe.messenger.hide-inbox-subtabs.runtime",
            RuntimeLayerRegistry.find(
                sourceRepository = "rushiranpise/morphe-patches",
                sourcePatchName = "Hide inbox subtabs",
                packageName = "com.facebook.orca",
            )?.id,
        )
        assertEquals(
            "morphe.messenger.disable-typing-indicator.runtime",
            RuntimeLayerRegistry.find(
                sourceRepository = "rushiranpise/morphe-patches",
                sourcePatchName = "Disable typing indicator",
                packageName = "com.facebook.orca",
            )?.id,
        )
        assertEquals(
            "morphe.messenger.hide-inbox-stories-notes-tray.runtime",
            RuntimeLayerRegistry.find(
                sourceRepository = "rushiranpise/morphe-patches",
                sourcePatchName = "Hide inbox stories and notes tray",
                packageName = "com.facebook.orca",
            )?.id,
        )
        assertEquals(
            "morphe.x.hide-navigation-badges.runtime",
            RuntimeLayerRegistry.find(
                sourceRepository = "crimera/piko",
                sourcePatchName = "Hide badges from navigation bar icons",
                packageName = "com.twitter.android",
            )?.id,
        )
        assertEquals(
            "piko.x.clear-tracking-params.runtime",
            RuntimeLayerRegistry.find(
                sourceRepository = "crimera/piko",
                sourcePatchName = "Clear tracking params",
                packageName = "com.twitter.android",
            )?.id,
        )
        assertEquals(
            "paresh.proton-vpn.disable-telemetry.runtime",
            RuntimeLayerRegistry.find(
                sourceRepository = "Paresh-Maheshwari/paresh-patches",
                sourcePatchName = "Disable telemetry",
                packageName = "ch.protonvpn.android",
            )?.id,
        )
    }

    @Test
    fun `runtime fingerprints use distinct cache keys for distinct targets`() {
        val first = FingerprintDsl {
            definingClass("Lexample/First;")
            name("first")
            returns("V")
        }.build()
        val second = FingerprintDsl {
            definingClass("Lexample/Second;")
            name("second")
            returns("V")
        }.build()

        assertTrue(first.runtimeCacheKey!!.isNotBlank())
        assertTrue(first.runtimeCacheKey != second.runtimeCacheKey)
    }

    @Test
    fun `runtime specification codec accepts constrained Boolean override`() {
        val spec = ImportedBooleanRuntimeLayerSpec(
            id = "community.instagram.boolean-override",
            sourceRepository = "community/example",
            sourcePatchName = "Disable sample feature",
            packageName = "com.instagram.android",
            patchName = "Runtime · Disable sample feature",
            description = "Safe Boolean override",
            fingerprintStrings = listOf("sample_feature_flag"),
            replacementValue = true,
        )

        val parsed = RuntimeLayerSpecCodec.parse(RuntimeLayerSpecCodec.encode(spec))

        assertEquals(spec.id, parsed.id)
        assertEquals(false, parsed.enabled)
    }

    @Test
    fun `runtime specification codec rejects unregistered target`() {
        val spec = ImportedBooleanRuntimeLayerSpec(
            id = "community.unknown.boolean-override",
            sourceRepository = "community/example",
            sourcePatchName = "Unknown target",
            packageName = "com.example.unregistered",
            patchName = "Runtime · Unknown target",
            description = "Safe Boolean override",
            fingerprintStrings = listOf("sample_feature_flag"),
            replacementValue = false,
        )

        val error = runCatching { RuntimeLayerSpecCodec.encode(spec) }.exceptionOrNull()

        assertTrue(error is RuntimeLayerImportException)
    }

    @Test
    fun `runtime store classifies compiled layer as ready`() {
        val catalog = RuntimeCatalogParser().parse(SAMPLE_CATALOG)

        val item = RuntimeStoreClassifier().classify(catalog).single()

        assertEquals(RuntimeStoreAvailability.READY, item.availability)
        assertEquals("piko.instagram.disable-video-autoplay.runtime", item.runtimeLayer?.id)
        assertEquals(null, item.recipe)
    }

    @Test
    fun `runtime store recipe encodes as a constrained specification`() {
        val recipe = RuntimeStoreRecipeRegistry.all().single()

        val parsed = RuntimeLayerSpecCodec.parse(RuntimeLayerSpecCodec.encode(recipe.spec))

        assertEquals(recipe.spec.id, parsed.id)
        assertEquals(false, parsed.enabled)
    }

    @Test
    fun `runtime store classifies story flipping layer as ready`() {
        val catalog = RuntimeCatalogParser().parse(
            SAMPLE_CATALOG.replace("Disable video autoplay", "Disable story flipping")
        )

        val item = RuntimeStoreClassifier().classify(catalog).single()

        assertEquals(RuntimeStoreAvailability.READY, item.availability)
        assertEquals("piko.instagram.disable-story-flipping.runtime", item.runtimeLayer?.id)
    }

    @Test
    fun `runtime store marks matching host patch without adapter`() {
        val catalog = RuntimeCatalogParser().parse(
            SAMPLE_CATALOG.replace("Disable video autoplay", "Disable story autoplay")
        )

        val item = RuntimeStoreClassifier().classify(catalog).single()

        assertEquals(RuntimeStoreAvailability.NEEDS_RUNTIME_ADAPTER, item.availability)
    }

    @Test
    fun `runtime store marks unknown host package unsupported`() {
        val catalog = RuntimeCatalogParser().parse(
            SAMPLE_CATALOG.replace("com.instagram.android", "com.example.notregistered")
        )

        val item = RuntimeStoreClassifier().classify(catalog).single()

        assertEquals(RuntimeStoreAvailability.UNSUPPORTED_TARGET, item.availability)
    }

    @Test
    fun `all valid github sources require review rather than auto trust`() {
        val catalog = RuntimeCatalogParser().parse(
            SAMPLE_CATALOG.replace("crimera/piko", "community/unknown-bundle")
        )

        assertEquals(SourceTrust.REVIEW_REQUIRED, catalog.bundles.single().trust)
    }

    @Test
    fun `non github and malformed repositories are blocked`() {
        assertEquals(SourceTrust.BLOCKED, MorpheSourceTrustPolicy.evaluate("gitlab", "author/bundle"))
        assertEquals(SourceTrust.BLOCKED, MorpheSourceTrustPolicy.evaluate("github", "not-a-repository"))
    }

    @Test
    fun `current approved profile is eligible for Morphe handoff`() {
        val catalog = RuntimeCatalogParser().parse(SAMPLE_CATALOG)
        val profile = profile(catalog, patchId = "disable-video-autoplay")

        val validation = MorpheProfileValidator.validate(profile, catalog) { it == "crimera/piko" }

        assertEquals(ProfileValidationState.CURRENT, validation.state)
        assertTrue(validation.canHandoff)
    }

    @Test
    fun `changed catalog requires profile review before handoff`() {
        val catalog = RuntimeCatalogParser().parse(SAMPLE_CATALOG)
        val profile = profile(catalog, catalogHash = "previous-catalog")

        val validation = MorpheProfileValidator.validate(profile, catalog) { true }

        assertEquals(ProfileValidationState.CATALOG_UPDATED, validation.state)
        assertTrue(!validation.canHandoff)
    }

    @Test
    fun `unsupported target app blocks profile handoff`() {
        val catalog = RuntimeCatalogParser().parse(SAMPLE_CATALOG)
        val profile = profile(catalog).copy(packageName = "com.reddit.frontpage")

        val validation = MorpheProfileValidator.validate(profile, catalog) { true }

        assertEquals(ProfileValidationState.TARGET_APP_NOT_AVAILABLE, validation.state)
        assertTrue(!validation.canHandoff)
    }

    @Test
    fun `missing enabled patch blocks profile handoff`() {
        val catalog = RuntimeCatalogParser().parse(SAMPLE_CATALOG)
        val profile = profile(catalog, patchId = "retired-patch")

        val validation = MorpheProfileValidator.validate(profile, catalog) { true }

        assertEquals(ProfileValidationState.PATCHES_NOT_AVAILABLE, validation.state)
        assertEquals(setOf("retired-patch"), validation.missingPatchIds)
    }

    @Test
    fun `catalog parser drops incomplete bundle records`() {
        val catalog = RuntimeCatalogParser().parse(
            """
            {
              "compatibilities": [],
              "bundles": [
                {"source":"github", "repo":"crimera/piko", "patches": []},
                {"source":"github", "patches": [{"name":"missing repository"}]}
              ]
            }
            """.trimIndent()
        )

        assertTrue(catalog.bundles.isEmpty())
    }

    private fun profile(
        catalog: CommunityCatalog,
        patchId: String = "disable-video-autoplay",
        catalogHash: String = catalog.sha256,
    ) = PatchProfile(
        id = "test-profile",
        name = "Piko profile",
        repository = "crimera/piko",
        packageName = "com.instagram.android",
        catalogSha256 = catalogHash,
        selections = listOf(PatchSelection(patchId, enabled = true)),
    )

    private companion object {
        val SAMPLE_CATALOG =
            """
            {
              "compatibilities": [
                [{"packageName":"com.instagram.android", "versions":["400.0.0"]}]
              ],
              "bundles": [
                {
                  "source":"github",
                  "repo":"crimera/piko",
                  "name":"Piko",
                  "author":"crimera",
                  "patches": [
                    {
                      "name":"Disable video autoplay",
                      "description":"Stops automatic video playback.",
                      "compatiblePackagesKey":0,
                      "default":false,
                      "options":[
                        {"key":"enabled", "title":"Enabled", "default":"false"}
                      ]
                    }
                  ]
                }
              ]
            }
            """.trimIndent()
    }
}
