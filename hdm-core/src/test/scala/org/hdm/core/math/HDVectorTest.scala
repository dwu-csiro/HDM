package org.hdm.core.math

import org.hdm.core.executor.{AppContext, HDMContext}
import org.hdm.core.model.HDM

import org.hdm.core.math.HDMatrix._


import org.junit.{Before, Test}

/**
 * Created by tiantian on 30/11/16.
 */
class HDVectorTest extends HDMathTestSuite{




  @Test
  def testPrimitives(): Unit ={

    val vector = HDM.parallelize(elems = vecData, numOfPartitions = 4).zipWithIndex.cache()

    println("Sum of vector: " + vector.sum)
    //test add one double value
    printData (vector.mapElem(d => d.toFloat))

    //test add one double value
    printData (vector.add(1D))

    printData (vector.negate())

    printData (vector.slice(500, 999))

    Thread.sleep(5000)

  }

  @Test
  def testVectorAdd(): Unit ={
    val vector = HDM.parallelize(elems = vecData, numOfPartitions = 8).zipWithIndex.cache()
    val another = vector.add(5D)

    printData (vector.add(another))
  }

  @Test
  def testVectorTimes(): Unit ={
    val vector = HDM.parallelize(elems = vecData, numOfPartitions = 8).zipWithIndex.cache()
    val another = vector.add(5D)
    printData (vector.times(another))
  }

  @Test
  def testAddLocalVector(): Unit ={
    val vector = HDM.parallelize(elems = vecData, numOfPartitions = 8).zipWithIndex.cache()
    val another = vecData.toIterator
    printData(vector.add(another))
  }

  @Test
  def testTimesLocalVector(): Unit ={
    val vector = HDM.parallelize(elems = vecData, numOfPartitions = 8).zipWithIndex.cache()
    val another = vecData.toIterator
    printData(vector.times(another))
  }
}
