package benchmarks.stddev

import java.util.concurrent.TimeUnit

import org.openjdk.jmh.annotations._
import org.openjdk.jmh.infra.Blackhole
import strawman.collection.immutable


@BenchmarkMode(scala.Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(1)
@Warmup(iterations = 8)
@Measurement(iterations = 8)
@State(Scope.Benchmark)
class List {
  @Param(scala.Array("1", "3", "8", "17", "282", "4096", "131070", "7312102"))
  var size: Int = _

  var xs: immutable.List[Long] = _
  def fresh(n: Int) = immutable.List((1 to n).map(_.toLong): _*)

  @Setup(Level.Trial)
  def initTrial(): Unit = {
    xs = fresh(size)
  }

  @Benchmark
  def stddev(bh: Blackhole) = {
    val mean = xs.sum / xs.size
    val squared =
      xs.map(x => (x - mean) * (x - mean)).sum
    bh.consume(squared)
  }

}

@BenchmarkMode(scala.Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(1)
@Warmup(iterations = 8)
@Measurement(iterations = 8)
@State(Scope.Benchmark)
class ScalaList {
  @Param(scala.Array("1", "3", "8", "17", "282", "4096", "131070", "7312102"))
  var size: Int = _

  var xs: scala.List[Long] = _
  def fresh(n: Int) = scala.List((1 to n).map(_.toLong): _*)

  @Setup(Level.Trial)
  def initTrial(): Unit = {
    xs = fresh(size)
  }

  @Benchmark
  def stddev(bh: Blackhole) = {
    val mean = xs.sum / xs.size
    val squared =
      xs.map(x => (x - mean) * (x - mean)).sum
    bh.consume(squared)
  }

}

@BenchmarkMode(scala.Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(1)
@Warmup(iterations = 8)
@Measurement(iterations = 8)
@State(Scope.Benchmark)
class Vector {
  @Param(scala.Array("1", "3", "8", "17", "282", "4096", "131070", "7312102"))
  var size: Int = _

  var xs: immutable.Vector[Long] = _
  def fresh(n: Int) = immutable.Vector((1 to n).map(_.toLong): _*)

  @Setup(Level.Trial)
  def initTrial(): Unit = {
    xs = fresh(size)
  }

  @Benchmark
  def stddev(bh: Blackhole) = {
    val mean = xs.sum / xs.size
    val squared =
      xs.map(x => (x - mean) * (x - mean)).sum
    bh.consume(squared)
  }

}

@BenchmarkMode(scala.Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(1)
@Warmup(iterations = 8)
@Measurement(iterations = 8)
@State(Scope.Benchmark)
class ScalaVector {
  @Param(scala.Array("1", "3", "8", "17", "282", "4096", "131070", "7312102"))
  var size: Int = _

  var xs: scala.Vector[Long] = _
  def fresh(n: Int) = scala.Vector((1 to n).map(_.toLong): _*)

  @Setup(Level.Trial)
  def initTrial(): Unit = {
    xs = fresh(size)
  }

  @Benchmark
  def stddev(bh: Blackhole) = {
    val mean = xs.sum / xs.size
    val squared =
      xs.map(x => (x - mean) * (x - mean)).sum
    bh.consume(squared)
  }

}

@BenchmarkMode(scala.Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(1)
@Warmup(iterations = 8)
@Measurement(iterations = 8)
@State(Scope.Benchmark)
class ImmutableArray {
  @Param(scala.Array("1", "3", "8", "17", "282", "4096", "131070", "7312102"))
  var size: Int = _

  var xs: immutable.ImmutableArray[Long] = _
  def fresh(n: Int) = immutable.ImmutableArray((1 to n).map(_.toLong): _*)

  @Setup(Level.Trial)
  def initTrial(): Unit = {
    xs = fresh(size)
  }

  @Benchmark
  def stddev(bh: Blackhole) = {
    val mean = xs.sum / xs.size
    val squared =
      xs.map(x => (x - mean) * (x - mean)).sum
    bh.consume(squared)
  }

}
