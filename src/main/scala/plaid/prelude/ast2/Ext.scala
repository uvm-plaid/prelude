package plaid.prelude.ast2

import plaid.prelude.ast2.Node._

extension [K <: Kind] (trg: Node[K])

  /**
   * Apply a transformation f to this node. If f does not return a node,
   * transform applies f to the children of this node, recursively. If there
   * are no children, the node is returned as-is.
   */
  def transform(f: Node[K] => Option[Node[K]]): Node[K] = f(trg).getOrElse(trg match
    case AndCond(e1, e2) => AndCond(e1.transform(f), e2.transform(f))
    case OrCond(e1, e2) => OrCond(e1.transform(f), e2.transform(f))
    case IffCond(e1, e2) => IffCond(e1.transform(f), e2.transform(f))
    case ImpliesCond(e1, e2) => ImpliesCond(e1.transform(f), e2.transform(f))
    case NotCond(e) => NotCond(e.transform(f))
    case AtExpr(e1, e2) => AtExpr(e1.transform(f), e2.transform(f))
    case ConcatExpr(e1, e2) => ConcatExpr(e1.transform(f), e2.transform(f))
    case FieldExpr(elements) => FieldExpr(elements.view.mapValues(_.transform(f)).toMap)
    case SelectExpr(e, l) => SelectExpr(e.transform(f), l)
    case CallExpr(fn, parms) => CallExpr(fn, parms.map(_.transform(f)))
    case LetExpr(y, e1, e2) => LetExpr(y, e1.transform(f), e2.transform(f))
    case MessageExpr(e) => MessageExpr(e.transform(f))
    case RandomExpr(e) => RandomExpr(e.transform(f))
    case MinusExpr(e) => MinusExpr(e.transform(f))
    case PlusExpr(e1, e2) => PlusExpr(e1.transform(f), e2.transform(f))
    case PublicExpr(e) => PublicExpr(e.transform(f))
    case SecretExpr(e) => SecretExpr(e.transform(f))
    case TimesExpr(e1, e2) => TimesExpr(e1.transform(f), e2.transform(f))
    case _ => trg)

  /**
   * Collect all the children of this node.
   */
  def children: List[Node[? <: Kind]] = trg match
    case AtExpr(e1, e2) => List(e1, e2)
    case ConcatExpr(e1, e2) => List(e1, e2)
    case FieldExpr(elements) => elements.values.toList
    case SelectExpr(e, id) => List(e)
    case CallExpr(fn, parms) => parms
    case LetExpr(id, e1, e2) => List(e1, e2)
    case MessageExpr(e) => List(e)
    case RandomExpr(e) => List(e)
    case MinusExpr(e) => List(e)
    case PlusExpr(e1, e2) => List(e1, e2)
    case PublicExpr(e) => List(e)
    case SecretExpr(e) => List(e)
    case TimesExpr(e1, e2) => List(e1, e2)
    case AndCond(e1, e2) => List(e1, e2)
    case OrCond(e1, e2) => List(e1, e2)
    case IffCond(e1, e2) => List(e1, e2)
    case ImpliesCond(e1, e2) => List(e1, e2)
    case NotCond(e) => List(e)
    case CallCond(id, parms) => parms
    case EqualCond(e1, e2) => List(e1, e2)
    case AssertCmd(e1, e2, e3) => List(e1, e2, e3)
    case AssignCmd(e1, e2) => List(e1, e2)
    case CallCmd(id, parms) => parms
    case LetCmd(id, e, c) => e :: c
    case ExprFunc(id, parms, body) => List(body)
    case CondFunc(id, parms, body) => List(body)
    case CmdFunc(id, parms, body, pre, post) => body ++ pre.toList ++ post.toList
    case _ => Nil

  /**
   * Provides a human-readable string representation of an AST node and its decedents.
   */
  def toCode: String = trg match
    case AssertCmd(e1, e2, i) => s"assert (${e1.toCode} = ${e2.toCode})@${i.toCode}"
    case AssignCmd(e1, e2) => s"${e1.toCode} := ${e2.toCode}"
    case AtExpr(e, i) => s"${e.toCode}@${i.toCode}"
    case IdExpr(id) => id.value
    case MessageExpr(e) => s"m[${e.toCode}]"
    case MinusExpr(e) => s"-${e.toCode}"
    case NumExpr(n) => s"$n"
    case OutputExpr(StrExpr("~")) => "out"
    case OutputExpr(e) => s"out[${e.toCode}]"
    case PlusExpr(e1, e2) => s"(${e1.toCode} + ${e2.toCode})"
    case PublicExpr(e) => s"p[${e.toCode}]"
    case RandomExpr(e) => s"r[${e.toCode}]"
    case SecretExpr(e) => s"s[${e.toCode}]"
    case StrExpr(x) => s"\"$x\""
    case ConcatExpr(e1, e2) => s"${e1.toCode} ++ ${e2.toCode}"
    case TimesExpr(e1, e2) => s"(${e1.toCode} * ${e2.toCode})"
    case AndCond(e1, e2) => s"${e1.toCode} AND ${e2.toCode}"
    case OrCond(e1, e2) => s"${e1.toCode} OR ${e2.toCode}"
    case IffCond(e1, e2) => s"${e1.toCode} IFF ${e2.toCode}"
    case ImpliesCond(e1, e2) => s"${e1.toCode} IMPLIES ${e2.toCode}"
    case NotCond(e) => s"NOT ${e.toCode}"
    case EqualCond(e1, e2) => s"${e1.toCode} == ${e2.toCode}"
    case TrueCond() => "T"
    case CallExpr(id, parms) => s"${id.value}(${parms.map(_.toCode).mkString(", ")})"
    case CallCond(id, parms) => s"${id.value}(${parms.map(_.toCode).mkString(", ")})"
    case CallCmd(id, parms) => s"${id.value}(${parms.map(_.toCode).mkString(", ")})"
    case _ => throw Exception(s"Unhandled $trg")

  /**
   * Collect all the descendants of this node.
   */
  def descendants: Iterable[Node[? <: Kind]] = trg.children ++ trg.children.flatMap(_.descendants)
