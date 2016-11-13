package org.nicta.wdy.hdm.functions

import org.nicta.wdy.hdm.executor.Partitioner
import org.nicta.wdy.hdm.model.DataDependency

/**
 * Created by tiantian on 8/01/15.
 */
trait HDMFunctions {

  val denpendency: DataDependency

  val func: ParallelFunction[_,_]

  val partitioner: Partitioner[_]


}
