package util.extractors

object Base64 {
  def unapply(raw: String): Option[String] = {
    if (raw == null || raw.isEmpty) None else {
      try {
        val decodedBytes = java.util.Base64.getDecoder.decode(raw)
        Option(new String(decodedBytes))
      } catch {
        case _: IllegalArgumentException => None
      }
    }
  }
}
