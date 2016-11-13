package org.nicta.wdy.hdm.serializer

import java.nio.ByteBuffer

import org.nicta.wdy.hdm.executor.HDMContext

/**
 * Created by tiantian on 8/03/16.
 */
class DynamicAkkaSerializer extends akka.serialization.Serializer{

  val serializer = HDMContext.DEFAULT_SERIALIZER
  
  override def identifier: Int = 1

  override def includeManifest: Boolean = false

  override def fromBinary(bytes: Array[Byte], manifest: Option[Class[_]]): AnyRef = {
    serializer.deserialize(ByteBuffer.wrap(bytes))
  }

  override def toBinary(o: AnyRef): Array[Byte] = {
    serializer.serialize(o).array()
  }

}
