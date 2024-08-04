package request

import util.mergeBits
import util.split
import util.toByteArray
import util.toIntMultiple

data class Header(
	val id: Int,
	val qr: Int,
	val opcode: Int,
	val aa: Int,
	val tc: Int,
	val rd: Int,
	val ra: Int,
	val z: Int,
	val rcode: Int,
	val qdcount: Int,
	val ancount: Int,
	val nscount: Int,
	val arcount: Int
) {
	fun toByteArray(): ByteArray {
		return id.toByteArray().sliceArray(2..3) + byteArrayOf(
			mergeBits(listOf(qr, opcode, aa, tc, rd), listOf(1, 4, 1, 1, 1)),
			mergeBits(listOf(ra, z, rcode), listOf(1, 3, 4)),
		) +
			qdcount.toByteArray().sliceArray(2..3) +
			ancount.toByteArray().sliceArray(2..3) +
			nscount.toByteArray().sliceArray(2..3) +
			arcount.toByteArray().sliceArray(2..3)
	}

	companion object {
		fun parseHeader(bytes: ByteArray): Header {
			require(bytes.size == 12)
			val id = bytes.toIntMultiple(0, 1)
			val (qr, opcode, aa, tc, rd) = bytes[2].split(listOf(1, 4, 1, 1, 1))
			val (ra, z, rcode) = bytes[3].split(listOf(1, 3, 4))
			val qdcount = bytes.toIntMultiple(3, 4)
			val ancount = bytes.toIntMultiple(5, 6)
			val nscount = bytes.toIntMultiple(7, 8)
			val arcount = bytes.toIntMultiple(9, 10)

			return Header(id, qr, opcode, aa, tc, rd, ra, z, rcode, qdcount, ancount, nscount, arcount)
		}
	}
}
