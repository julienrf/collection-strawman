package strawman.collection

import java.util.concurrent.TimeUnit

import org.openjdk.jmh.annotations._
import org.openjdk.jmh.infra.Blackhole

import scala.{Any, AnyRef, Int, Long, Unit, math}
import scala.Predef.intWrapper

@BenchmarkMode(scala.Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(1)
@Warmup(iterations = 8)
@Measurement(iterations = 8)
@State(Scope.Benchmark)
class ListMacro {
  @Param(scala.Array("0", "1", "3", "8", "17", "282", "4096", "131070", "7312102"))
  var size: Int = _

  var xs: immutable.List[Long] = _
  def fresh(n: Int) = immutable.List((1 to n).map(_.toLong): _*)

  @Setup(Level.Trial)
  def initTrial(): Unit = {
    xs = fresh(size)
  }

  @Benchmark
  def sumOfSquaresEven(bh: Blackhole) = {
    val ret =
      xs.view
        .filter(x => x % 2L == 0L)
        .map(x => x * x)
        .sum
    bh.consume(ret)
  }

}


@BenchmarkMode(scala.Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(1)
@Warmup(iterations = 8)
@Measurement(iterations = 8)
@State(Scope.Benchmark)
class ScalaListMacro {
  @Param(scala.Array("0", "1", "3", "8", "17", "282", "4096", "131070", "7312102"))
  var size: Int = _

  var xs: scala.List[Long] = _
  def fresh(n: Int) = scala.List((1 to n).map(_.toLong): _*)

  @Setup(Level.Trial)
  def initTrial(): Unit = {
    xs = fresh(size)
  }

  @Benchmark
  def sumOfSquaresEven(bh: Blackhole) = {
    val ret =
      xs.view
        .filter(x => x % 2L == 0L)
        .map(x => x * x)
        .sum
    bh.consume(ret)
  }

}


@BenchmarkMode(scala.Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(1)
@Warmup(iterations = 8)
@Measurement(iterations = 8)
@State(Scope.Benchmark)
class VectorMacro {
  @Param(scala.Array("0", "1", "3", "8", "17", "282", "4096", "131070", "7312102"))
  var size: Int = _

  var xs: immutable.Vector[Long] = _
  def fresh(n: Int) = immutable.Vector((1 to n).map(_.toLong): _*)

  @Setup(Level.Trial)
  def initTrial(): Unit = {
    xs = fresh(size)
  }

  @Benchmark
  def sumOfSquaresEven(bh: Blackhole) = {
    val ret =
      xs.view
        .filter(x => x % 2L == 0L)
        .map(x => x * x)
        .sum
    bh.consume(ret)
  }

}

@BenchmarkMode(scala.Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(1)
@Warmup(iterations = 8)
@Measurement(iterations = 8)
@State(Scope.Benchmark)
class ScalaVectorMacro {
  @Param(scala.Array("0", "1", "3", "8", "17", "282", "4096", "131070", "7312102"))
  var size: Int = _

  var xs: scala.Vector[Long] = _
  def fresh(n: Int) = scala.Vector((1 to n).map(_.toLong): _*)

  @Setup(Level.Trial)
  def initTrial(): Unit = {
    xs = fresh(size)
  }

  @Benchmark
  def sumOfSquaresEven(bh: Blackhole) = {
    val ret =
      xs.view
        .filter(x => x % 2L == 0L)
        .map(x => x * x)
        .sum
    bh.consume(ret)
  }

}


@BenchmarkMode(scala.Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(1)
@Warmup(iterations = 8)
@Measurement(iterations = 8)
@State(Scope.Benchmark)
class ImmutableArrayMacro {
  @Param(scala.Array("0", "1", "3", "8", "17", "282", "4096", "131070", "7312102"))
  var size: Int = _

  var xs: immutable.ImmutableArray[Long] = _
  def fresh(n: Int) = immutable.ImmutableArray((1 to n).map(_.toLong): _*)

  @Setup(Level.Trial)
  def initTrial(): Unit = {
    xs = fresh(size)
  }

  @Benchmark
  def sumOfSquaresEven(bh: Blackhole) = {
    val ret =
      xs.view
        .filter(x => x % 2L == 0L)
        .map(x => x * x)
        .sum
    bh.consume(ret)
  }

}

@BenchmarkMode(scala.Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(1)
@Warmup(iterations = 8)
@Measurement(iterations = 8)
@State(Scope.Benchmark)
class ArrayMacro {
  @Param(scala.Array("0", "1", "3", "8", "17", "282", "4096", "131070", "7312102"))
  var size: Int = _

  var xs: scala.Array[Long] = _
  def fresh(n: Int) = scala.Array.tabulate(n)(i => (i + 1).toLong)

  @Setup(Level.Trial)
  def initTrial(): Unit = {
    xs = fresh(size)
  }

  @Benchmark
  def sumOfSquaresEven(bh: Blackhole) = {
    val ret =
      xs.view
        .filter(x => x % 2L == 0L)
        .map(x => x * x)
        .sum
    bh.consume(ret)
  }

}

@BenchmarkMode(scala.Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(1)
@Warmup(iterations = 8)
@Measurement(iterations = 8)
@State(Scope.Benchmark)
class ArrayBaselineMacro {
  @Param(scala.Array("0", "1", "3", "8", "17", "282", "4096", "131070", "7312102"))
  var size: Int = _

  var xs: scala.Array[Long] = _
  def fresh(n: Int) = scala.Array.tabulate(n)(i => (i + 1).toLong)

  @Setup(Level.Trial)
  def initTrial(): Unit = {
    xs = fresh(size)
  }

  @Benchmark
  def sumOfSquaresEven(bh: Blackhole) = {
    var i = 0
    var ret = 0L
    while (i < xs.length) {
      if (xs(i) % 2L == 0L)
        ret += xs(i) * xs(i)
      i += 1
    }
    bh.consume(ret)
  }

}