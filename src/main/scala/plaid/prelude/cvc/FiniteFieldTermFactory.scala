package plaid.prelude.cvc

import io.github.cvc5.{Kind, Solver, Sort, Term}
import plaid.prelude.ast.{AtExpr, BVAddExpr, BVConcatExpr, BVMultExpr, Expr, MinusExpr, Num, PlusExpr, PublicExpr, TimesExpr, VectorExpr}

class FiniteFieldTermFactory(order: String) extends TermFactory {
  private val DEFAULT_SIZE = 10
  val sort: Sort = termManager.mkFiniteFieldSort(order, DEFAULT_SIZE)
  private val minusOne: Term = termManager.mkFiniteFieldElem("-1", sort, DEFAULT_SIZE)

  override def createSolver(): Solver = Solver(termManager)

  override def toTerm(expr: Expr, idx: Option[Int] = None): Term = expr match
    case x: VectorExpr => throw Exception("Finite field theory does not support vectors")
    case x: BVAddExpr => throw Exception("Finite field theory does not support vectors")
    case x: BVMultExpr => throw Exception("Finite field theory does not support vectors")
    case x: BVConcatExpr => throw Exception("Finite field theory does not support vectors")
    case x: PublicExpr => lookupOrCreate(x, idx)
    case x: AtExpr =>
      if (idx.nonEmpty) throw Exception(s"Party index $idx already active")
      toTerm(x.e1, Some(CvcUtils.toInt(x.e2)))
    case Num(n) => termManager.mkFiniteFieldElem(n.toString, sort, DEFAULT_SIZE)
    case PlusExpr(e1, e2) => termManager.mkTerm(Kind.FINITE_FIELD_ADD, toTerm(e1, idx), toTerm(e2, idx))
    case TimesExpr(e1, e2) => termManager.mkTerm(Kind.FINITE_FIELD_MULT, toTerm(e1, idx), toTerm(e2, idx))
    case MinusExpr(e) => termManager.mkTerm(Kind.FINITE_FIELD_MULT, toTerm(e, idx), minusOne)
    case x: Expr =>
      if (idx.isEmpty) throw Exception("A party index must be active")
      lookupOrCreate(x, idx)

  def valueOf(variable: Term): String =
    val mod = Integer.parseInt(sort.getFiniteFieldSize)
    val finiteFieldValue = Integer.parseInt(variable.getFiniteFieldValue)
    Math.floorMod(finiteFieldValue, mod).toString
}
