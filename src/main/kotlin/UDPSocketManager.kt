import model.Answer
import request.Header
import request.Question
import util.captureBit
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
		var remainByteArray = byteArray.drop(12)
		val questions = mutableListOf<Question>()
		val answers = mutableListOf<Answer>()

		(0 until header.qdcount)
			.map {
				val result = parseQuestion(remainByteArray)
				val question = if(questions.isEmpty()) result.first!! else questions.last()
				remainByteArray = result.second
				val responseAnswer = getAnswer(question)

				questions.add(question)
				answers.add(responseAnswer)
			}

		val responseHeader = Header(header.id, 1, header.opcode, 0, 0, header.rd, 0, 0, if (header.opcode == 0) 0 else 4, header.qdcount, header.qdcount, 0, 0)

		return Response(responseHeader, questions, answers)
	}

	private fun parseQuestion(bytes: List<Byte>): Pair<Question?, List<Byte>> =
		if (!checkCompressed(bytes)) parseUncompressedQuestion(bytes) else Pair(null, bytes.drop(2))

	private fun parseUncompressedQuestion(bytes: List<Byte>): Pair<Question, List<Byte>> {
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

		return Pair(Question(name, type, questionClass), byteListHolder.drop(9))
	}

	private fun getAnswer(question: Question): Answer =
		Answer(question.name, 1, 1, 60, 4, byteArrayOf(8, 8, 8, 8))

	private fun checkCompressed(byteList: List<Byte>): Boolean {
		println(byteList.first())
		println(captureBit(byteList[0], 0, 2))
		return captureBit(byteList[0], 0, 2) == 3
	}
}