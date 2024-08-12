import request.Header
import request.Question

data class Request(
    val header: Header,
    val question: Question
) {
    fun toByteArray(): ByteArray = header.toByteArray() + question.toByteArray()
}
