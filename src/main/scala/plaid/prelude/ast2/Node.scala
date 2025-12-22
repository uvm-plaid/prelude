package plaid.prelude.ast2

import plaid.prelude.ast2.Id.Id

type Cond = Node[CondKind]
type Expr = Node[ExprKind]
type Cmd = Node[CmdKind]
type Type = Node[TypeKind]
type Func = Node[FuncKind]
type Parm = Node[ParmKind]

sealed trait Kind
sealed trait CondKind extends Kind
sealed trait ExprKind extends Kind
sealed trait CmdKind extends Kind
sealed trait TypeKind extends Kind
sealed trait FuncKind extends Kind
sealed trait ParmKind extends Kind

enum Node[K <: Kind]:
  case AndCond(e1: Cond, e2: Cond) extends Cond
  case OrCond(e1: Cond, e2: Cond) extends Cond
  case IffCond(e1: Cond, e2: Cond) extends Cond
  case ImpliesCond(e1: Cond, e2: Cond) extends Cond
  case CallCond(id: Id, parms: List[Expr]) extends Cond
  case EqualCond(e1: Expr, e2: Expr) extends Cond
  case NotCond(e: Cond) extends Cond
  case TrueCond() extends Cond
  case AtExpr(e1: Expr, e2: Expr) extends Expr
  case ConcatExpr(e1: Expr, e2: Expr) extends Expr
  case FieldExpr(elements: Map[Id, Expr]) extends Expr
  case SelectExpr(e: Expr, id: Id) extends Expr
  case CallExpr(id: Id, parms: List[Expr]) extends Expr
  case LetExpr(id: Id, e1: Expr, e2: Expr) extends Expr
  case MessageExpr(e: Expr) extends Expr
  case RandomExpr(e: Expr) extends Expr
  case MinusExpr(e: Expr) extends Expr
  case NumExpr(num: Int) extends Expr
  case OutputExpr(e: Expr) extends Expr
  case PlusExpr(e1: Expr, e2: Expr) extends Expr
  case PublicExpr(e: Expr) extends Expr
  case SecretExpr(e: Expr) extends Expr
  case StrExpr(str: String) extends Expr
  case TimesExpr(e1: Expr, e2: Expr) extends Expr
  case IdExpr(id: Id) extends Expr
  case AssertCmd(e1: Expr, e2: Expr, e3: Expr) extends Cmd
  case AssignCmd(e1: Expr, e2: Expr) extends Cmd
  case CallCmd(id: Id, parms: List[Expr]) extends Cmd
  case LetCmd(id: Id, e: Expr, c: List[Cmd]) extends Cmd
  case PartyIndexType() extends Type
  case RecordType(elements: Map[Id, Type]) extends Type
  case StringType() extends Type
  case CondFunc(id: Id, parms: List[Id], body: Cond) extends Func
  case ExprFunc(id: Id, parms: List[Id], body: Expr) extends Func
  case CmdFunc(id: Id, parms: List[(Id, Type)], body: List[Cmd], pre: Option[Cond], post: Option[Cond]) extends Func
  case Program(fns: List[Func]) extends Node
