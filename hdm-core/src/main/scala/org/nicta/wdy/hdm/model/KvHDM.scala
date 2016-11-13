package org.nicta.wdy.hdm.model

import org.nicta.wdy.hdm.executor.{KeepPartitioner, HashPartitioner}
import org.nicta.wdy.hdm.functions._
import org.nicta.wdy.hdm.executor.HDMContext._

import scala.reflect.ClassTag

/**
 * Created by Tiantian on 2014/12/11.
 */
class KvHDM[K:ClassTag,V:ClassTag](self:HDM[(K,V)]) extends Serializable {

  def mapValues[R: ClassTag](f: V => R): HDM[(K, R)] = {
    //    self.map(t => (t._1, f(t._2)))
    new DFM[(K, V), (K, R)](children = Seq(self), dependency = OneToOne, func = new MapValues[V, K, R](f), distribution = self.distribution, location = self.location, keepPartition = true, partitioner = new KeepPartitioner[(K, R)](1), appContext = self.appContext)

  }

  def mapKey[NK: ClassTag](f: K => NK): HDM[(NK, V)] = {
    //    self.map(t => (f(t._1), t._2))
    new DFM[(K, V), (NK, V)](children = Seq(self), dependency = OneToOne, func = new MapKeys[V, K, NK](f), distribution = self.distribution, location = self.location, keepPartition = true, partitioner = new KeepPartitioner[(NK, V)](1), appContext = self.appContext)

  }

  def reduceByKey(f: (V, V) => V): HDM[(K, V)] = {
    val pFunc = (t: (K, V)) => t._1.hashCode()
    val mapAll = (elems: Seq[(K, V)]) => {
      elems.groupBy(_._1).mapValues(_.map(_._2).reduce(f)).toSeq
    }
    val parallel = new DFM[(K, V), (K, V)](children = Seq(self), dependency = OneToN, func = new ReduceByKey(f), distribution = self.distribution, location = self.location, keepPartition = false, partitioner = new HashPartitioner(4, pFunc), appContext = self.appContext)
    //    val aggregate = (elems:Seq[(K,V)]) => elems.groupBy(e => e._1).mapValues(_.map(_._2).reduce(f)).toSeq
    new DFM[(K, V), (K, V)](children = Seq(parallel), dependency = NToOne, func = new ReduceByKey(f), distribution = self.distribution, location = self.location, keepPartition = true, partitioner = new KeepPartitioner[(K, V)](1), appContext = self.appContext)

  }

  def findByKey(f: K => Boolean): HDM[(K, V)] = {
    if (self.dependency == NToOne && self.func.isInstanceOf[ParGroupByFunc[_, K]]) {
      val h = self.children.head.asInstanceOf[ParHDM[_, _]]
      val head = h.asInstanceOf[ParHDM[_, Any]]
      val gb = self.func.asInstanceOf[ParGroupByFunc[Any, K]]
      val fk: Any => Boolean = f.compose(gb.f)
      val filtered = self.children.map { c =>
        c.asInstanceOf[ParHDM[_, Any]]
          .copy(keepPartition = true, dependency = OneToOne, partitioner = new KeepPartitioner[Any](1))
          .filter { e => fk(e)}
          .copy(keepPartition = head.keepPartition, dependency = head.dependency, partitioner = head.partitioner)
      }
      self.asInstanceOf[ParHDM[Any, (K, V)]].copy(children = filtered).asInstanceOf[HDM[(K, V)]]
    } else
      new DFM[(K, V), (K, V)](children = Seq(self), dependency = OneToOne, func = new FindByKey(f), distribution = self.distribution, location = self.location, keepPartition = true, partitioner = new KeepPartitioner[(K, V)](1), appContext = self.appContext)
  }


  def findByValue(f: V => Boolean): HDM[(K, V)] = {
    new DFM[(K, V), (K, V)](children = Seq(self), dependency = OneToOne, func = new FindByValue(f), distribution = self.distribution, location = self.location, keepPartition = true, partitioner = new KeepPartitioner[(K, V)](1), appContext = self.appContext)

  }

  def swap(): HDM[(V, K)] = {
    self.map(t => (t._2, t._1))
  }

  def cogroupByKey[U: ClassTag](other: HDM[(K, U)]): HDM[(K, Iterable[V], Iterable[U])] = ???


  def joinByKeyOpt[U: ClassTag](hdm: HDM[(K, U)]): HDM[(K, (V, U))] = {
    self.cogroupByKey(hdm).mapPartitions { arr =>
      arr.flatMap { tup =>
        for {r <- tup._2; u <- tup._3} yield {
          (tup._1, (r, u))
        }
      }
    }
  }

  def joinByKey[U: ClassTag](hdm: HDM[(K, U)]): HDM[(K, (V, U))] = {
    self.cogroup(hdm, _._1, (kv: (K, U)) => kv._1).mapPartitions { arr =>
      arr.map { data =>
        (data._1, data._2._1.map(_._2), data._2._2.map(_._2))
      }.flatMap { tup =>
        for {r <- tup._2; u <- tup._3} yield {
          (tup._1, (r, u))
        }
      }
    }
  }

}




class GroupedSeqHDM[K:ClassTag,V:ClassTag](self:ParHDM[_,(K, Iterable[V])]) extends Serializable{
  
  def mapValuesByKey[R:ClassTag](f: V => R):ParHDM[(K, Iterable[V]), (K, Iterable[R])] = {
    val func = (v: Iterable[V]) => v.map(f)
    new DFM[(K, Iterable[V]),(K, Iterable[R])](children = Seq(self), dependency = OneToOne, func = new MapValues[Iterable[V],K,Iterable[R]](func), distribution = self.distribution, location = self.location, keepPartition = true, partitioner = new KeepPartitioner[(K, Iterable[R])](1), appContext = self.appContext)

  }

  def findValuesByKey(f: V => Boolean):ParHDM[(K, Iterable[V]), (K, Iterable[V])] = {
    new DFM[(K, Iterable[V]),(K, Iterable[V])](children = Seq(self), dependency = OneToOne, func = new FindValuesByKey(f), distribution = self.distribution, location = self.location, keepPartition = true, partitioner = new KeepPartitioner[(K, Iterable[V])](1), appContext = self.appContext)
  }

  def reduceValues(f :(V,V) => V): ParHDM[(K,Iterable[V]), (K,V)] = {
    new DFM[(K, Iterable[V]),(K, V)](children = Seq(self),
      dependency = OneToOne,
      func = new MapValues[Iterable[V],K,V](_.reduce(f)),
      distribution = self.distribution,
      location = self.location,
      keepPartition = true,
      partitioner = new KeepPartitioner[(K, V)](1),
      appContext = self.appContext)
  }

}
