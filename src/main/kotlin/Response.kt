import model.Answer
import request.Header
import request.Question

data class Response(
	val header: Header,
	val questionList: List<Question>,
	val answerList: List<Answer>
) {
	fun toByteArray(): ByteArray =
		header.toByteArray() +
		questionList.map { it.toByteArray() }.reduce { a, b -> a + b } +
		answerList.map { it.toByteArray() }.reduce { a, b -> a + b }
}
