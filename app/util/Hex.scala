package util

object Hex {
  private val digits = "0123456789abcedf".toCharArray.map(_.toString)
  def byte2hex(byte: Byte): String = digits((byte >> 4) & 0x0f) + digits(byte & 0x0f)
  def toHex(bytes: Array[Byte]): String =  bytes.map(byte2hex).reduce(_ + _)

  implicit class ByteHex(byte: Byte) {
    def toHex: String = byte2hex(byte)
  }

  implicit class ArrayByteHex(bytes: Array[Byte]) {
    def toHex: String = Hex.toHex(bytes)
  }
}
