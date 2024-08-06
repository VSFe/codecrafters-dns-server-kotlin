package model

import util.toByteArray

data class Answer(
	val name: String,
	val type: Int,
	val answerClass: Int,
	val ttl: Int,
	val rdlength: Int,
	val rdata: ByteArray
) {
	fun toByteArray(): ByteArray =
		nameToByteArray() +
		type.toByteArray().sliceArray(2..3) +
		answerClass.toByteArray().sliceArray(2..3) +
		ttl.toByteArray() +
		rdlength.toByteArray().sliceArray(2..3) +
		rdata

	private fun nameToByteArray(): ByteArray =
		name.split('.')
			.map { byteArrayOf(it.length.toByte()) + it.toByteArray() }
			.reduce { a, b -> a + b } + byteArrayOf(0)
}
