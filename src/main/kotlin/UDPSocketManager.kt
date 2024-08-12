import model.Answer
import request.Header
import request.Question
import util.captureBit
import util.toIntMultiple
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

object UDPSocketManager {
	private val udpSocket = DatagramSocket(2053)

	fun process(resolver: String?) {
		val buffer = ByteArray(512)
		val packet = DatagramPacket(buffer, buffer.size)
		udpSocket.receive(packet)

		val responseData = processInner(packet.data, resolver).toByteArray()
		val responsePacket = DatagramPacket(responseData, responseData.size, packet.address, packet.port)
		udpSocket.send(responsePacket)
	}

	private fun processInner(byteArray: ByteArray, resolver: String?): Response {
		val headerBytes = byteArray.sliceArray(0..11)
		val header = Header.parseHeader(headerBytes)
		var remainByteArray = byteArray.drop(12)
		val questions = mutableListOf<Question>()
		val answers = mutableListOf<ByteArray>()

		(0 until header.qdcount)
			.map {
				val result = parseQuestion(remainByteArray)
				val question = if(questions.isEmpty()) result.first!! else questions.last()
				remainByteArray = result.second
				val responseAnswer = getAnswer(question, resolver)

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
			val size = byteListHolder[0].toInt() and 0xFF
			if (size == 0) break
			val content = byteListHolder.drop(1).take(size).toByteArray().toString(Charsets.UTF_8)
			result.add(content)

			byteListHolder = byteListHolder.drop(1 + size)
		}

		val name = result.joinToString(".")
		val type = byteListHolder.toByteArray().toIntMultiple(1, 2)
		val questionClass = byteListHolder.toByteArray().toIntMultiple(3, 4)

		return Pair(Question(name, type, questionClass), byteListHolder.drop(5))
	}

	private fun getAnswer(question: Question, resolver: String?): ByteArray =
		if (resolver != null) getAnswerFromResolver(question, resolver) else
			Answer(question.name, 1, 1, 60, 4, byteArrayOf(8, 8, 8, 8)).toByteArray()

	private fun checkCompressed(byteList: List<Byte>): Boolean {
		return captureBit(byteList[0], 0, 2) == 3
	}

	private fun getAnswerFromResolver(question: Question, resolver: String): ByteArray {
		val (address, port) = getResolverInfo(resolver)
		val request = Request(Header(1234, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0), question).toByteArray()
		val socket = DatagramSocket()
		val sendPacket = DatagramPacket(request, request.size, InetAddress.getByName(address), port)
		val responseBuffer = ByteArray(512)
		val responsePacket = DatagramPacket(responseBuffer, 512)
		socket.send(sendPacket)
		socket.receive(responsePacket)

		val header = Header.parseHeader(responseBuffer.sliceArray(0..11))
		val (question, remainBuffer) = parseQuestion(responseBuffer.drop(12))
		val answer = parseAnswer(remainBuffer).copy(name = question!!.name)
		return answer.toByteArray()
	}

	private fun parseAnswer(bytes: List<Byte>): Answer {
		var byteListHolder = bytes
		val result = mutableListOf<String>()

		while (true) {
			val size = byteListHolder[0].toInt() and 0xFF
			if (size == 0) break
			val content = byteListHolder.drop(1).take(size).toByteArray().toString(Charsets.UTF_8)
			result.add(content)

			byteListHolder = byteListHolder.drop(1 + size)
		}

		val name = result.joinToString(".")
		val type = byteListHolder.toByteArray().toIntMultiple(1, 2)
		val answerClass = byteListHolder.toByteArray().toIntMultiple(3, 4)
		val ttl = byteListHolder.toByteArray().toIntMultiple(5, 8)
		val rdLength = byteListHolder.toByteArray().toIntMultiple(9, 10)
		val rdata = byteListHolder.drop(11).take(rdLength).toByteArray()

		return Answer(name, type, answerClass, ttl, rdLength, rdata)
	}

	private fun getResolverInfo(resolver: String): Pair<String, Int> {
		val pair = resolver.split(":")
		return Pair(pair[0], pair[1].toInt())
	}
}