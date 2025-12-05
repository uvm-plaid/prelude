package plaid.prelude.cvc

import io.github.cvc5.*
import plaid.prelude.ast.*

import scala.collection.mutable

abstract class TermFactory {
  val memories = mutable.HashSet[Memory]()
  val termManager = TermManager()
  val sort: Sort

  def lookupOrCreate(expr: Expr, idx: Option[Int]): Term =
    val name = CvcUtils.getCvcName(expr, idx)
    val memory = memories
      .find { _.name == name }
      .getOrElse { Memory(name, termManager.mkConst(sort, name), expr, idx) }
    memories.add(memory)
    memory.term

  def createSolver(): Solver
  def toTerm(expr: Expr, idx: Option[Int] = None): Term
  def valueOf(variable: Term): String

  def toTerm(constraint: Constraint): Term = constraint match
    case NotConstraint(e) => termManager.mkTerm(Kind.NOT, toTerm(e))
    case AndConstraint(e1, e2) => termManager.mkTerm(Kind.AND, toTerm(e1), toTerm(e2))
    case OrConstraint(e1, e2) => termManager.mkTerm(Kind.OR, toTerm(e1), toTerm(e2))
    case IffConstraint(e1, e2) => termManager.mkTerm(Kind.EQUAL, toTerm(e1), toTerm(e2))
    case ImpliesConstraint(e1, e2) => termManager.mkTerm(Kind.IMPLIES, toTerm(e1), toTerm(e2))
    case EqualConstraint(e1, e2) => termManager.mkTerm(Kind.EQUAL, toTerm(e1), toTerm(e2))
    case TrueConstraint() => termManager.mkTrue()
    case _ => throw Exception(s"Unsupported constraint $constraint")
}
