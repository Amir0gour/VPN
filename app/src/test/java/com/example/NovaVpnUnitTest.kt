package com.example

import com.example.vpn.crypto.WireGuardCrypto
import com.example.vpn.model.DnsProvider
import com.example.vpn.model.VpnProtocol
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NovaVpnUnitTest {

    @Test
    fun testWireGuardKeyPairGeneration() {
        val keyPair = WireGuardCrypto.generateKeyPair()
        assertNotNull(keyPair.privateKeyBase64)
        assertNotNull(keyPair.publicKeyBase64)
        assertTrue(keyPair.privateKeyBase64.isNotEmpty())
        assertTrue(keyPair.publicKeyBase64.isNotEmpty())
    }

    @Test
    fun testWireGuardHandshakePacketStructure() {
        val packet = WireGuardCrypto.createHandshakeInitiationPacket(senderIndex = 1)
        // WireGuard initiation packet must be exactly 148 bytes
        assertEquals(148, packet.size)
        // First byte must be message type 1 (Initiation)
        assertEquals(1.toByte(), packet[0])
    }

    @Test
    fun testWireGuardConfigGenerationAndParsing() {
        val testPriv = "aGVsbG93b3JsZHRlc3RrZXkxMjM0NTY3ODkwMTI="
        val testPub = "c2VydmVycHVibGlja2V5MTIzNDU2Nzg5MDEyMzQ="
        val testEndpoint = "192.0.2.1:51820"

        val conf = WireGuardCrypto.generateConfigFile(
            clientPrivateKey = testPriv,
            clientAddress = "10.66.66.2/32",
            dns = "1.1.1.1",
            mtu = 1420,
            serverPublicKey = testPub,
            serverEndpoint = testEndpoint
        )

        assertTrue(conf.contains("[Interface]"))
        assertTrue(conf.contains("[Peer]"))
        assertTrue(conf.contains(testEndpoint))

        val parsed = WireGuardCrypto.parseConfigFile(conf)
        assertNotNull(parsed)
        assertEquals(testPriv, parsed?.privateKey)
        assertEquals(testPub, parsed?.publicKey)
        assertEquals(testEndpoint, parsed?.endpoint)
        assertEquals(1420, parsed?.mtu)
    }

    @Test
    fun testDnsProviderConfigurations() {
        assertEquals("1.1.1.1", DnsProvider.CLOUDFLARE.primaryIp)
        assertEquals("9.9.9.9", DnsProvider.QUAD9.primaryIp)
        assertEquals(51820, VpnProtocol.WIREGUARD.port)
    }

    @Test
    fun testWireGuardHandshakeResponsePacketStructure() {
        val response = WireGuardCrypto.createHandshakeResponsePacket(senderIndex = 2, receiverIndex = 1)
        // WireGuard Type 2 Handshake Response must be exactly 92 bytes
        assertEquals(92, response.size)
        // First byte must be message type 2 (Response)
        assertEquals(2.toByte(), response[0])
    }
}
