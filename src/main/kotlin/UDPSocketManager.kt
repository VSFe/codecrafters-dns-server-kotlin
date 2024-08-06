import model.Answer
import request.Header
import request.Question
import util.toIntMultiple
import java.net.DatagramPacket
import java.net.DatagramSocket

object UDPSocketManager {
	private val udpSocket = DatagramSocket(2053)

	fun process() {
		val buffer = ByteArray(512)
		val packet = DatagramPacket(buffer, buffer.size)
		udpSocket.receive(packet)

		val responseData = processInner(packet.data).toByteArray()
		val responsePacket = DatagramPacket(responseData, responseData.size, packet.address, packet.port)
		udpSocket.send(responsePacket)
	}

	private fun processInner(byteArray: ByteArray): Response {
		val headerBytes = byteArray.sliceArray(0..11)
		val header = Header.parseHeader(headerBytes)
		val question = parseQuestion(byteArray.drop(12))
		val responseHeader = Header(1234, 1, 0, 0, 0, 0, 0, 0, 0, header.qdcount, 1, 0, 0)
		val responseAnswer = getAnswer(question)

		return Response(responseHeader, listOf(question), listOf(responseAnswer))
	}

	private fun parseQuestion(bytes: List<Byte>): Question {
		var byteListHolder = bytes
		val result = mutableListOf<String>()
		while (true) {
			val size = byteListHolder[0].toInt()
			if (size == 0) break
			val content = byteListHolder.drop(1).take(size).toByteArray().toString(Charsets.UTF_8)
			result.add(content)

			byteListHolder = byteListHolder.drop(1 + size)
		}

		val name = result.joinToString(".")
		val type = byteListHolder.toByteArray().toIntMultiple(1, 2)
		val questionClass = byteListHolder.toByteArray().toIntMultiple(3, 4)

		return Question(name, type, questionClass)
	}

	private fun getAnswer(question: Question): Answer =
		Answer(question.name, 1, 1, 60, 4, byteArrayOf(8, 8, 8, 8))
}
