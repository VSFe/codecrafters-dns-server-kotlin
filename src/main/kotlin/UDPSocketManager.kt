import request.Header
import java.net.DatagramPacket
import java.net.DatagramSocket

object UDPSocketManager {
	private val udpSocket = DatagramSocket(2053)

	fun process() {
		val buffer = ByteArray(512)
		val packet = DatagramPacket(buffer, buffer.size)
		udpSocket.receive(packet)

		val responseData = processInner(packet.data)
		val responsePacket = DatagramPacket(responseData, responseData.size, packet.address, packet.port)
		udpSocket.send(responsePacket)
	}

	private fun processInner(byteArray: ByteArray): ByteArray {
		val headerBytes = byteArray.sliceArray(0..11)
		val header = Header.parseHeader(headerBytes)

		return Header(1234, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0).toByteArray()
	}
}
