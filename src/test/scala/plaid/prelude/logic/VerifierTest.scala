package plaid.prelude.logic

import org.junit.Assert.{assertFalse, assertTrue}
import org.junit.Test
import plaid.prelude.antlr.Loader
import plaid.prelude.ast.Constraint
import plaid.prelude.cvc.{BitVectorTermFactory, FiniteFieldTermFactory}

class VerifierTest {

  private def satisfiableFF(src: String): Boolean =
    satisfiableFF(Loader.constraint(src))

  private def satisfiableFF(constraint: Constraint): Boolean =
    val factory = FiniteFieldTermFactory("7")
    val e = factory.toTerm(constraint)
    factory.satisfiable(e)

  private def satisfiableBV(src: String): Boolean =
    satisfiableBV(Loader.constraint(src))

  private def satisfiableBV(constraint: Constraint): Boolean =
    val factory = BitVectorTermFactory()
    val e = factory.toTerm(constraint)
    factory.satisfiable(e)

  /** Single equality constraints are always satisfiable. */
  @Test
  def singleEquality(): Unit =
    assertTrue(satisfiableFF("out@1 == 1"))

  /** Conjunction of non-contradictory constraints is satisfiable. */
  @Test
  def multipleNoncontradictory(): Unit =
    assertTrue(satisfiableFF("out@1 == 1 AND out@2 == 2"))

  /** Conjunction of contradictory constraints are not satisfiable. */
  @Test
  def contradictory(): Unit =
    assertFalse(satisfiableFF("out@1 == 1 AND out@1 == 2"))

  /** Everything entails a universally satisfied constraint. */
  @Test
  def everythingEntails(): Unit =
    val factory = FiniteFieldTermFactory("7")
    val t = factory.toTerm(Loader.constraint("T"))
    val f = factory.toTerm(Loader.constraint("NOT T"))
    assertTrue(factory.entails(t, t))
    assertTrue(factory.entails(f, t))

  /** Nothing entails an unsatisfiable constraint except another unsatisfiable constraint. */
  @Test
  def nothingEntails(): Unit =
    val factory = FiniteFieldTermFactory("7")
    val t = factory.toTerm(Loader.constraint("T"))
    val f = factory.toTerm(Loader.constraint("NOT T"))
    assertFalse(factory.entails(t, f))
    assertTrue(factory.entails(f, f))

  /** Bit vectors can participate in constraints. */
  @Test
  def bitVector(): Unit =
    assertTrue(satisfiableBV("|out@1, out@2| == |1, 0|"))
    assertFalse(satisfiableBV("|out@1, out@2| == |1, 0| AND out@1 == 0"))

  /** Bit vectors can be concatenated with BVConcat. */
  @Test
  def bitVectorConcat(): Unit =
    assertTrue(satisfiableBV("BVConcat(|out@1, out@2|, |out@3, out@4|) == |1, 0, 0, 1|"))
    assertFalse(satisfiableBV("BVConcat(|out@1, out@2|, |out@1, out@4|) == |1, 0, 0, 1|"))

  /** Bit vectors can be added with BVAdd. */
  @Test
  def bitVectorAdd(): Unit =
    assertTrue(satisfiableBV("BVAdd(|0, out@1|, |0, out@1|) == |1, 0|"))
    assertFalse(satisfiableBV("BVAdd(|0, out@1|, |0, out@1|) == |1, 1|"))

  /** What is the behavior of cvc5 bit vectors wrt addition overflow? */
  @Test
  def bitVectorOverflow(): Unit =
    // Sanity check: We can assert things without predicate variables
    assertTrue(satisfiableBV("BVAdd(|1, 0|, |0, 1|) == |1, 1|"))
    assertFalse(satisfiableBV("BVAdd(|1, 0|, |0, 1|) == |1, 0|"))
    // Go over by 1
    assertTrue(satisfiableBV("BVAdd(|1, 1|, |0, 1|) == |0, 0|"))
    // Go over by 2
    assertTrue(satisfiableBV("BVAdd(|1, 1|, |1, 0|) == |0, 1|"))
    // Go over by 3
    assertTrue(satisfiableBV("BVAdd(|1, 1|, |1, 1|) == |1, 0|"))
}
