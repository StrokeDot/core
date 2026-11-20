/*
 * ArchiveTune (2026)
 * © Rukamori — github.com/rukamori
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.rukamori.archivetune.innertube

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class YouTubeAccountChannelsTest {
    @Test
    fun retainsDelegationAndKeepsThePersonalChannelSeparate() {
        val response = Json.parseToJsonElement(
            """
            {
              "responseContext": {"mainAppWebResponseContext": {"dataSyncId": "200||100"}},
              "items": [
                {"accountItemRenderer": {
                  "accountName": {"simpleText": "Personal"},
                  "serviceEndpoint": {"selectActiveIdentityEndpoint": {"obfuscatedSelectedGaiaId": "100"}}
                }},
                {"accountItemRenderer": {
                  "accountName": {"simpleText": "Brand one"},
                  "serviceEndpoint": {"selectActiveIdentityEndpoint": {"dataSyncId": "200||100"}},
                  "isSelected": true
                }},
                {"accountItemRenderer": {
                  "accountName": {"simpleText": "Brand two"},
                  "serviceEndpoint": {"selectActiveIdentityEndpoint": {"obfuscatedSelectedGaiaId": "300"}}
                }}
              ]
            }
            """.trimIndent(),
        )

        val channels = YouTube.parseAccountChannels(response, null)
        assertEquals(
            listOf("Brand one" to "200||100", "Personal" to "100", "Brand two" to "300||100"),
            channels.map { it.name to it.dataSyncId },
        )
        assertEquals(listOf("Brand one"), channels.filter { it.isSelected }.map { it.name })
    }

    @Test
    fun usesPersistedSessionAndIgnoresOpaqueSelectors() {
        val response = Json.parseToJsonElement(
            """
            [
              {"accountItemRenderer": {
                "accountName": {"simpleText": "Known Brand"},
                "serviceEndpoint": {"selectActiveIdentityEndpoint": {"obfuscatedSelectedGaiaId": "200"}}
              }},
              {"accountItemRenderer": {
                "accountName": {"simpleText": "Unknown"},
                "serviceEndpoint": {"selectActiveIdentityEndpoint": {"selectedSerializedDelegationContext": "opaque"}}
              }}
            ]
            """.trimIndent(),
        )

        assertEquals(listOf("200||100"), YouTube.parseAccountChannels(response, "100").map { it.dataSyncId })
    }
}
