package org.nicta.hdm.io.netty

import java.util.concurrent._
import java.util.concurrent.atomic.{AtomicBoolean, AtomicInteger}

import org.junit.Test
import org.nicta.wdy.hdm.Buf
import org.nicta.wdy.hdm.executor.HDMContext
import org.nicta.wdy.hdm.io.Path
import org.nicta.wdy.hdm.message.FetchSuccessResponse
import org.nicta.wdy.hdm.storage.{Block, HDMBlockManager}

/**
 * Created by tiantian on 4/12/15.
 */
class NettyConcurrencyTest {

  val numOfClients = 160
  val numOfParititonsPerRequest = 8
  val blockServerAddr = "netty://tiantian-HP-EliteBook-Folio-9470m:9091"
  val executor = Executors.newFixedThreadPool(32)
  val blks = for (i <- 0 until numOfClients) yield {
    for (j <- 0 until numOfParititonsPerRequest) yield {
      s"blk-00${i * numOfParititonsPerRequest + j}"
    }
  }
  val watcher = new CountDownLatch(numOfClients)


  @Test
  def testCurrentLoad(): Unit ={
    for (i <- 0 until numOfClients){
      val task = new LoadingTask(blockServerAddr, blks(i))
      val res = executor.submit(task)

    }
    watcher.await()
  }



  class LoadingTask(val serverAddr:String , val blks:Seq[String]) extends Callable[Buf[_]] {
    var res:Buf[Any] = Buf.empty[Any]
    val blockCounter = new AtomicInteger(0)
    val countDownWatch = new AtomicInteger(0)
    val fetchingCompleted = new AtomicBoolean(false)
    val inputQueue = new LinkedBlockingDeque[AnyRef]

    val blockHandler = (blk:Block[_]) => {
      if (blockCounter.incrementAndGet() >= blks.length) {
        fetchingCompleted.set(true)
      }
      inputQueue.offer(blk)
      println(s"Fetched block:${blk.id}, progress: (${blockCounter.get}/${blks.length}).")
    }

    val fetchHandler = (resp:FetchSuccessResponse) => {
      if (blockCounter.incrementAndGet() >= blks.length) {
        fetchingCompleted.set(true)
      }
      inputQueue.offer(resp)
      println(s"Received fetch response:${resp.id} with size ${resp.length}, progress: (${blockCounter.get}/${blks.length}).")
    }

    override def call(): Buf[_] = {
     HDMBlockManager.loadBlockAsync(Path(serverAddr), blks, blockHandler, fetchHandler)
      while (!fetchingCompleted.get()) {
        val received = inputQueue.poll(60, TimeUnit.SECONDS)
        res += received.asInstanceOf[Any]
      }
      watcher.countDown()
      println("Current task length:" + watcher.getCount)
      res
    }

  }

}
