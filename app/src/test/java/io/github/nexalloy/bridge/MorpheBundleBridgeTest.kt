package io.github.nexalloy.bridge

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MorpheBundleBridgeTest {
    @Test
    fun `parser reads compatible packages options and catalog hash`() {
        val catalog = MorpheCatalogParser().parse(SAMPLE_CATALOG, fetchedAtMillis = 1L)

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
    fun `all valid github sources require review rather than auto trust`() {
        val catalog = MorpheCatalogParser().parse(
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
        val catalog = MorpheCatalogParser().parse(SAMPLE_CATALOG)
        val profile = profile(catalog, patchId = "disable-video-autoplay")

        val validation = MorpheProfileValidator.validate(profile, catalog) { it == "crimera/piko" }

        assertEquals(ProfileValidationState.CURRENT, validation.state)
        assertTrue(validation.canHandoff)
    }

    @Test
    fun `changed catalog requires profile review before handoff`() {
        val catalog = MorpheCatalogParser().parse(SAMPLE_CATALOG)
        val profile = profile(catalog, catalogHash = "previous-catalog")

        val validation = MorpheProfileValidator.validate(profile, catalog) { true }

        assertEquals(ProfileValidationState.CATALOG_UPDATED, validation.state)
        assertTrue(!validation.canHandoff)
    }

    @Test
    fun `unsupported target app blocks profile handoff`() {
        val catalog = MorpheCatalogParser().parse(SAMPLE_CATALOG)
        val profile = profile(catalog).copy(packageName = "com.reddit.frontpage")

        val validation = MorpheProfileValidator.validate(profile, catalog) { true }

        assertEquals(ProfileValidationState.TARGET_APP_NOT_AVAILABLE, validation.state)
        assertTrue(!validation.canHandoff)
    }

    @Test
    fun `missing enabled patch blocks profile handoff`() {
        val catalog = MorpheCatalogParser().parse(SAMPLE_CATALOG)
        val profile = profile(catalog, patchId = "retired-patch")

        val validation = MorpheProfileValidator.validate(profile, catalog) { true }

        assertEquals(ProfileValidationState.PATCHES_NOT_AVAILABLE, validation.state)
        assertEquals(setOf("retired-patch"), validation.missingPatchIds)
    }

    @Test
    fun `catalog parser drops incomplete bundle records`() {
        val catalog = MorpheCatalogParser().parse(
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
