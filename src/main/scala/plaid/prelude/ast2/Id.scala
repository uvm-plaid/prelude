package plaid.prelude.ast2

object Id:
  opaque type Id = String

  def from(s: String): Id =
    if s.matches("[a-zA-Z0-9_-]+") then s else throw Exception(s"Invalid id: $s")

  extension (id: Id)
    def value: String = id
