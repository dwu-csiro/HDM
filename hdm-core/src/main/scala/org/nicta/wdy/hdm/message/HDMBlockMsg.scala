package org.nicta.wdy.hdm.message

import org.nicta.wdy.hdm.model.ParHDM
import org.nicta.wdy.hdm.storage.{BlockState, Block}

import scala.language.existentials

/**
 * Created by Tiantian on 2014/12/18.
 */
trait HDMBlockMsg extends Serializable

case class AddRefMsg(refs: Seq[ParHDM[_,_]], broadcast:Boolean = true) extends HDMBlockMsg

case class SyncRefMsg(refs: Seq[ParHDM[_,_]]) extends HDMBlockMsg

case class RemoveRefMsg(id: String) extends HDMBlockMsg

case class AddBlockMsg(bl: Block[_], declare:Boolean = false) extends HDMBlockMsg

case class RemoveBlockMsg(id :String) extends HDMBlockMsg

case class QueryBlockMsg (blockIds:Seq[String], location:String) extends HDMBlockMsg

case class BlockData(id:String, bl:Block[_]) extends HDMBlockMsg

case class CheckStateMsg (id: String) extends HDMBlockMsg

case class CheckAllStateMsg (id: Seq[String]) extends HDMBlockMsg

case class BlockStateMsg (id: String, state:BlockState) extends HDMBlockMsg

case class AllBlocksStateMsg (states: Seq[(String, BlockState)]) extends HDMBlockMsg


