
package org.nicta.wdy.hdm.server

import java.util.concurrent.ConcurrentHashMap

import org.nicta.wdy.hdm.model.{HDM, ParHDM}

import scala.collection.JavaConversions._
import scala.language.existentials

/**
 * Created by tiantian on 7/03/15.
 */
class AppManager {
  
  val appBuffer = new ConcurrentHashMap[String, Application]

  val referenceMap:ConcurrentHashMap[String, Int] = new ConcurrentHashMap[String, Int]
  
  def addApp(appId:String, hdm:HDM[_]): Unit = {
    appBuffer.put(appId, Application(id = hdm.id, namespace = appId, source = hdm))
  }
  
  def getApp(appId:String):Application = {
    appBuffer.get(appId)
  }
  
  def addPlan(appId:String, nPlan:Seq[HDM[_]] ) = {
    val app = appBuffer.get(appId)
    if(app != null) appBuffer.put(appId, app.copy(plan = nPlan))
  }

  def setReference(hdmId:String, amount:Int): Unit = {
    referenceMap.put(hdmId, amount)
  }

  def incReference(hdmId:String, amount:Int) {
    if(referenceMap.containsKey(hdmId)){
      val v = referenceMap.get(hdmId)
      referenceMap.put(hdmId, v + amount)
    } else referenceMap.put(hdmId, amount)
  }

  def decReference(hdmId:String, amount:Int): Unit ={
    if(referenceMap.containsKey(hdmId)){
      val v = referenceMap.get(hdmId)
      referenceMap.put(hdmId, v - amount)
    }
  }

  def addAllRef(refs: Map[String, Int]): Unit ={
    referenceMap.putAll(refs)
  }
  

}


case class Application(id: String,
                       namespace: String,
                       source: HDM[_],
                       plan: Seq[HDM[_]] = Seq.empty[ParHDM[_, _]],
                       planOpt: Seq[HDM[_]] = Seq.empty[ParHDM[_, _]],
                       state: String = "REGISTERED") {

}