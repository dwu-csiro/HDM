package org.hdm.core.planing

import breeze.linalg.DenseVector
import org.hdm.core.examples.KVBasedPrimitiveBenchmark
import org.hdm.core.executor.{AppContext, HDMContext}
import org.hdm.core.io.Path
import org.hdm.core.model.HDM
import org.hdm.core.planing.StaticMultiClusterPlanner

import org.junit.Test
/**
 * Created by tiantian on 12/05/16.
 */
class MultiClusterPlanningTest {

  val hDMContext = HDMContext.defaultHDMContext

  val master1 = "akka.tcp://masterSys@127.0.1.1:8999/user/smsMaster"
  val master2 = "akka.tcp://masterSys@127.0.1.1:8998/user/smsMaster"

  val appContext1 = new AppContext(appName = "hdm-examples", version = "0.0.1", masterPath = master1)

  val appContext2 = new AppContext(appName = "hdm-examples", version = "0.0.1", masterPath = master2)
  val dataPath1:String = "hdfs://127.0.0.1:9001/user/spark/benchmark/1node/weather"
  val dataPath2:String = "hdfs://127.0.0.1:9001/user/spark/benchmark/1node/weather"

  @Test
  def testParallelExecution(): Unit ={

    import HDMContext._

    implicit val parallelism = 4
    hDMContext.init()
    Thread.sleep(200)
    val multiPlanner = new StaticMultiClusterPlanner(hDMContext.planer, hDMContext)

    val vecLen = 10
    val data1 = Path(dataPath1)
    val data2 = Path(dataPath2)
    val dataDP1 = HDM(data1, appContext1)
    val dataDP2 = HDM(data2, appContext2)

    val trainingDp1 = dataDP1.map(line => line.split("\\s+"))
      .map{ seq => seq.drop(3).dropRight(6)}
      .filter(seq => seq.forall(s => s.matches("\\d+(.\\d+)?")))
      .map{ seq => seq.take(vecLen).map(_.toDouble)}.map(arr => arr(0) -> arr)
//      .zipWithIndex.mapValues(d => DenseVector(d))

    val trainingDp2 = dataDP2.map(line => line.split("\\s+"))
      .map{ seq => seq.drop(3).dropRight(6)}
      .filter(seq => seq.forall(s => s.matches("\\d+(.\\d+)?")))
      .map{ seq => seq.take(vecLen).map(_.toDouble)}.map(arr => arr(0) -> arr)
//      .zipWithIndex.mapValues(d => DenseVector(d))

    val job = trainingDp1.joinByKey(trainingDp2)

    multiPlanner.planStages(job, parallelism).foreach { pl =>
      println("===========New Stage begins:")
      println(pl)
    }



  }

  @Test
  def testGroupBy(): Unit ={
    val len = 3
    implicit val parallelism = 4
    val context = "akka.tcp://masterSys@127.0.1.1:8999/user/smsMaster"
    val data = "hdfs://127.0.0.1:9001/user/spark/benchmark/partial/rankings"

    val multiPlanner = new StaticMultiClusterPlanner(hDMContext.planer, hDMContext)
    val benchmark = new KVBasedPrimitiveBenchmark(context = context, kIndex = 0, vIndex = 1)
    hDMContext.NETTY_BLOCK_SERVER_PORT = 9092
    hDMContext.init()
    Thread.sleep(200)
//    hDMContext.init(leader = "akka.tcp://masterSys@127.0.1.1:8999/user/smsMaster")
    val job = benchmark.testGroupBy(data, len, parallelism)
    multiPlanner.planStages(job, parallelism).foreach { pl =>
      println("===========New Stage begins:")
      println(pl)
    }
  }

}
