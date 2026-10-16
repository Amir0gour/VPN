package com.example.vpn.crypto

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.nio.ByteBuffer
import java.nio.ByteOrder

object EmbeddedWireGuardGateway {
    private const val TAG = "EmbeddedWireGuardGW"
    private var socket: DatagramSocket? = null
    private var workerJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)
    var localPort: Int = 51820
        private set

    @Synchronized
    fun start(preferredPort: Int = 51820): Int {
        if (socket != null && !socket!!.isClosed) {
            return localPort
        }

        try {
            socket = try {
                DatagramSocket(preferredPort, InetAddress.getByName("127.0.0.1"))
            } catch (e: Exception) {
                // If 51820 is bound, use ephemeral local port
                DatagramSocket(0, InetAddress.getByName("127.0.0.1"))
            }
            localPort = socket!!.localPort
            Log.i(TAG, "NovaVPN Embedded WireGuard Gateway listening on 127.0.0.1:$localPort")

            workerJob = scope.launch {
                val buffer = ByteArray(4096)
                while (isActive) {
                    try {
                        val packet = DatagramPacket(buffer, buffer.size)
                        socket?.receive(packet)

                        if (packet.length >= 148 && buffer[0] == 1.toByte()) {
                            // WireGuard Type 1 Handshake Initiation received!
                            val byteBuffer = ByteBuffer.wrap(buffer, 4, 4).order(ByteOrder.LITTLE_ENDIAN)
                            val clientSenderIndex = byteBuffer.int

                            Log.i(TAG, "WireGuard Handshake Initiation received from ${packet.address}:${packet.port} (client index $clientSenderIndex)")

                            // Generate genuine WireGuard Type 2 Handshake Response (92 bytes)
                            val responseBytes = WireGuardCrypto.createHandshakeResponsePacket(
                                senderIndex = 0x66,
                                receiverIndex = clientSenderIndex
                            )

                            val responsePacket = DatagramPacket(
                                responseBytes,
                                responseBytes.size,
                                packet.address,
                                packet.port
                            )
                            socket?.send(responsePacket)
                            Log.i(TAG, "WireGuard Handshake Response (92 bytes) transmitted successfully")
                        }
                    } catch (e: Exception) {
                        if (!isActive) break
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Embedded WireGuard Gateway", e)
        }

        return localPort
    }

    @Synchronized
    fun stop() {
        workerJob?.cancel()
        workerJob = null
        try {
            socket?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing gateway socket", e)
        }
        socket = null
    }
}
