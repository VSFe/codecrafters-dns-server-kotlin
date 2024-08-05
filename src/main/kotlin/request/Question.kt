package request

import util.toByteArray

data class Question(
	val name: String,
	val type: Int,
	val questionClass: Int
) {
	fun toByteArray(): ByteArray =
		nameToByteArray() +
		type.toByteArray().sliceArray(2 .. 3) +
		questionClass.toByteArray().sliceArray(2 .. 3)

	private fun nameToByteArray(): ByteArray =
		name.split('.')
			.map { byteArrayOf(it.length.toByte()) + it.toByteArray() }
			.reduce { a, b -> a + b } + byteArrayOf(0)
}
