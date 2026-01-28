package plaid.prelude.logic

import org.junit.Assert.assertEquals
import org.junit.Test
import plaid.prelude.antlr.Loader
import plaid.prelude.ast.{AtExpr, EqualConstraint, Num, OutputExpr, Str, TrueConstraint}

class ContractTest {

  /** Assignment commands add equality constraints to the Hoare context. */
  @Test
  def assignmentAddsEqualityConstraint(): Unit =
    val cmd = Loader.command("out@1 := 4")
    assertEquals(
      PrePost(TrueConstraint(), EqualConstraint(AtExpr(OutputExpr(Str("~")), Num(1)), Num(4))),
      cmd.prePost(Nil))

  /** Assert commands add entailments to the Hoare context. */
  @Test
  def assertAddsEntailment(): Unit =
    val cmd = Loader.command("assert (4 = 4)@1")
    assertEquals(
      PrePost(EqualConstraint(Num(4), Num(4)), TrueConstraint()),
      cmd.prePost(Nil))

  /** The postcondition from a function call is bound to the actual parameters of the call. */
  @Test
  def postconditionBound(): Unit =
    val src = """ cmdfunctions: f(i:cid) { m["x"]@1 := i } g() { f(4) } """
    val List(f, g) = Loader.program(src).cmdFuncs.contracts(Nil, Nil)
    val post = Loader.constraint(""" T AND (T AND (m["x"]@1 == 4)) """)
    assertEquals(post, f.defaultPost)
}
