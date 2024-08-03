import java.net.DatagramPacket
import java.net.DatagramSocket

object UDPSocketManager {
	private val udpSocket = DatagramSocket(2053)

	fun process() {
		val buffer = ByteArray(512)
		val packet = DatagramPacket(buffer, buffer.size)
		udpSocket.receive(packet)
		println("Received data")

		val responseData = "hello world".toByteArray() // Dummy response, replace when implementing later stages
		val responsePacket = DatagramPacket(responseData, responseData.size, packet.address, packet.port)
		udpSocket.send(responsePacket)
	}
}
