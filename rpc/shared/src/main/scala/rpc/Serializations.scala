package rpc

import java.nio.ByteBuffer
import boopickle.Default._
import chameleon._
import scala.util.{Failure, Success, Try}


trait Serializations {

  // Serialize exceptions
  implicit val exPickler = exceptionPickler

  // Copied this method so that we can avoid multiple `import chameleon.ext.boopickle._`
  // See https://github.com/cornerman/chameleon/blob/master/chameleon/src/main/scala/ext/boopickle.scala
  implicit def boopickleSerializerDeserializer[T: Pickler]: SerializerDeserializer[T, ByteBuffer] = {
    new Serializer[T, ByteBuffer] with Deserializer[T, ByteBuffer] {
      override def serialize(arg: T): ByteBuffer = Pickle.intoBytes(arg)
      override def deserialize(arg: ByteBuffer): Either[Throwable, T] = {
        Try(Unpickle.apply[T].fromBytes(arg)) match {
          case Success(arg) => Right(arg)
          case Failure(t)   => Left(t)
        }
      }
    }
  }
}
