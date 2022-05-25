package types

trait Parameterized {
  class C1[A]

  class C2[A, B]

  class *[A, B]

  type T1 = C1[Int]

  type T2 = C2[Int, Long]

  type T3 = Int * Long

  type T4[A] = C1[A]

  type T5[A, B] = C2[A, B]
}