Square Root Stream
==================

This project provides a Java implementation of the Scala Stream based converging sequence example in my Scala project [stream-primes](https://github.com/george-hawkins/stream-primes) (see there the file [`ConvergingSequences.scala`](https://github.com/george-hawkins/stream-primes/blob/master/src/main/scala/week2/ConvergingSequences.scala)).

Here you can see the explicit use of `Supplier` to model call-by-name and memoization to achieve `lazy val` like behavior.
