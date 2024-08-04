package util

import java.nio.ByteBuffer

fun Byte.split(size: List<Int>): List<Int> {
	require(size.sum() == 8)
	val position = size.runningFold(0) { a, b -> a + b }
		.windowed(size = 2, step = 1)

	return position.map {
		(a, b) -> captureBit(this, a, b - a + 1)
	}
}

fun Int.toByteArray(): ByteArray =
	ByteBuffer.allocate(Int.SIZE_BYTES).putInt(this).array()

fun ByteArray.toIntMultiple(start: Int, end: Int): Int =
	(start..end).fold(0) {
		before, after -> (before shl 8) or this[after].toInt()
	}

fun mergeBits(numbers: List<Int>, size: List<Int>): Byte =
	numbers.zip(size).fold(0) {
		prev, (number, len) -> (prev shl len) or number
	}.toByte()

private fun captureBit(byte: Byte, startBit: Int, bitCount: Int): Int {
	val mask = ((1 shl bitCount) - 1) shl startBit
	return (byte.toInt() and mask) shr startBit
}
