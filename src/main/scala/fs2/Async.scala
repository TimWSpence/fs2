package fs2

trait Async[F[_]] extends Monad[F] {
  type Ref[A]

  /** Create a asynchronous, concurrent mutable reference. */
  def ref[A]: F[Ref[A]]

  /**
   * After the returned `F[Unit]` is bound, the task is
   * running in the background. Multiple tasks may be added to a
   * `Ref[A]`.
   *
   * Satisfies: `set(v)(t) flatMap { _ => get(v) } == t`.
   */
  def set[A](q: Ref[A])(a: F[A]): F[Unit]
  def setFree[A](q: Ref[A])(a: Free[F,A]): F[Unit]

  /**
   * Obtain the value of the `Ref`, or wait until it has been `set`.
   */
  def get[A](q: Ref[A]): F[A]

  /**
   * Chooses nondeterministically between `a map (Left(_))` and
   * `a2 map (Right(_))`. Result must be equivalent to one of
   * these two expressions. */
  def race[A,B](a: F[A], a2: F[B]): F[Either[A,B]] =
    bind(ref[Either[A,B]]) { ref =>
    bind(set(ref)(map(a)(Left(_)))) { _ =>
    bind(set(ref)(map(a2)(Right(_)))) { _ =>
    get(ref)
    }}}
}

object Async {

  implicit class Syntax[F[_],A](a: F[A]) {
    def race[B](b: F[B])(implicit F: Async[F]): F[Either[A,B]] = F.race(a,b)
  }
}

object Affine {

}