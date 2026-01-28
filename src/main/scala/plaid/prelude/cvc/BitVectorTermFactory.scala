package plaid.prelude.cvc

import io.github.cvc5.{Kind, Solver, Sort, Term}
import plaid.prelude.ast.{AtExpr, Expr, MinusExpr, Num, PlusExpr, PublicExpr, TimesExpr, VectorExpr}

class BitVectorTermFactory extends TermFactory {

  val sort: Sort = termManager.mkBitVectorSort(1)

  override def createSolver(): Solver =
    val result = Solver(termManager)
    result.setLogic("QF_BV")
    result.setOption("bitblast", "eager")
    result.setOption("bv-sat-solver", "cadical")
    result

  override def toTerm(expr: Expr, idx: Option[Int] = None): Term = expr match
    case VectorExpr(es) => termManager.mkTerm(Kind.BITVECTOR_CONCAT, es.map(toTerm(_)).toArray)
    case x: PublicExpr => lookupOrCreate(x, idx)
    case x: AtExpr =>
      if (idx.nonEmpty) throw Exception(s"Party index $idx already active")
      toTerm(x.e1, Some(CvcUtils.toInt(x.e2)))
    case Num(n) => termManager.mkBitVector(1, n)
    case PlusExpr(e1, e2) => termManager.mkTerm(Kind.BITVECTOR_XOR, toTerm(e1, idx), toTerm(e2, idx))
    case TimesExpr(e1, e2) => termManager.mkTerm(Kind.BITVECTOR_AND, toTerm(e1, idx), toTerm(e2, idx))
    case MinusExpr(e) => toTerm(e, idx)
    case x: Expr =>
      if (idx.isEmpty) throw Exception("A party index must be active")
      lookupOrCreate(x, idx)

  def valueOf(variable: Term): String = variable.getBitVectorValue
}
