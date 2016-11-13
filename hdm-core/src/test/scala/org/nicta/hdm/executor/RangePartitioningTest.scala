package org.nicta.hdm.executor

import org.junit.Test
import org.nicta.wdy.hdm.executor.RangePartitioner
import org.nicta.wdy.hdm.functions.RangePartitioning

import scala.util.Random

/**
 * Created by tiantian on 27/10/15.
 */
class RangePartitioningTest {

  val data = Seq.fill[Int](25000){
    Random.nextInt(10000)
  }

  @Test
  def testBoundsDetermine(): Unit ={
    RangePartitioning.decideBoundary(data, 64) foreach(println(_))
  }

  @Test
  def testRangePartitoning() {
    val bounds = RangePartitioning.decideBoundary(data, 64)
    val partitioner = new RangePartitioner(bounds)
    partitioner.split(data).toSeq.sortBy(_._1) foreach {kv =>
      println(s"${kv._1}, ${kv._2.size}")
    }
  }

}
