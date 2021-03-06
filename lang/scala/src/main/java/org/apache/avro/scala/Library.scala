/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.avro.scala

import java.io.InputStream
import java.io.ByteArrayOutputStream

import org.apache.avro.{Schema, AvroRuntimeException}
import org.apache.avro.io.Decoder
import org.apache.avro.io.DecoderFactory
import org.apache.avro.io.DecoderFactory
import org.apache.avro.io.DecoderFactory
import org.apache.avro.io.Encoder
import org.apache.avro.io.DecoderFactory
import org.apache.avro.io.EncoderFactory
import org.apache.avro.specific.{SpecificData, SpecificDatumReader, SpecificDatumWriter, SpecificRecord}
import runtime.ScalaRunTime

class NotImplemented(msg: String) extends AvroRuntimeException(msg)
class NotAvailable(msg: String) extends AvroRuntimeException(msg)

/** Interface for objects that can be encoded into an Avro encoder. */
trait Encodable {
  /**
   * Serializes this object into the specified Avro encoder.
   *
   * @param encoder the Avro encoder to serialize to.
   */
  def encode(encoder: Encoder): Unit
}

/** Interface for objects that can be decoded from an Avro decoder. */
trait Decodable {
  /**
   * Deserializes this object from the specified Avro decoder.
   *
   * @param decoder the Avro decoder to deserialize from.
   */
  def decode(decoder: Decoder): Unit
}

/** Abstract base class for all Scala records. */
trait RecordBase
  extends SpecificRecord
  with Encodable
  with Product
  with Serializable {

  override def put(index: Int, value: AnyRef): Unit = {
    throw new org.apache.avro.scala.NotAvailable("Immutable record cannot be modified")
  }

  def toJson: String = Records.toJson(this)

  def productArity: Int = getSchema.getFields.size

  def productElement(n: Int) = get(n)

  override def equals(other: Any): Boolean = other match {
    case that: SpecificRecord =>
      (this canEqual that) &&
      ScalaRunTime._equals(this, that)
    case _ => false
  }

  override def hashCode: Int = ScalaRunTime._hashCode(this)

  override def toString: String = toJson
}

/** Trait for immutable Scala records. */
trait ImmutableRecordBase extends RecordBase {
  override def put(index: Int, value: AnyRef): Unit = {
    throw new org.apache.avro.scala.NotAvailable("Immutable record cannot be modified")
  }
}

/** Trait for mutable Scala records. */
trait MutableRecordBase[T] extends RecordBase with Decodable {
  def build(): T
}

/** Helpers to work with records. */
object Records {

  /**
   * Serializes the specified record into an array of bytes, using the SpecificDatumWriter.
   *
   * @param record The record to serialize.
   * @return The binary representation of the record as an array of bytes.
   */
  def serialize(record: SpecificRecord): Array[Byte] = {
    val output = new ByteArrayOutputStream()
    val encoder = EncoderFactory.get.directBinaryEncoder(output, null)
    val writer = new SpecificDatumWriter[SpecificRecord](record.getSchema)
    writer.write(record, encoder)
    // encoder.flush() // not necessary with direct binary encoder
    return output.toByteArray
  }

  /**
   * Deserializes an array of bytes into the specified record.
   *
   * @param record Deserializes into this (mutable) record.
   * @param input Byte input stream to deserialize from.
   * @return The deserialized record.
   */
  def deserialize[T <: SpecificRecord](record: T, input: InputStream): T = {
    val decoder = DecoderFactory.get.directBinaryDecoder(input, null)
    val reader = new SpecificDatumReader[SpecificRecord](record.getSchema)
    reader.read(record, decoder)
    return record
  }

  /**
   * Serializes the specified record into a JSON string.
   *
   * @param record The record to serialize.
   * @return A string with the JSON representation of the specified record.
   */
  def toJson(record: SpecificRecord): String = {
    val output = new ByteArrayOutputStream()
    val encoder = EncoderFactory.get.jsonEncoder(record.getSchema, output)
    val writer = new SpecificDatumWriter[SpecificRecord](record.getSchema)
    writer.write(record, encoder)
    encoder.flush()
    return new String(output.toByteArray)
  }

  private val scalaNamespaceClassLoader = new ScalaNamespaceSuffixSchemaClassLoader

  /**
  * Deserializes a JSON string into the specified mutable record.
  *
  * @param mutableRecord Deserializes into this (mutable) record.
  * @param input JSON string to deserialize from.
  * @return The deserialized record.
  */
  def mutableFromJson[T <: MutableRecordBase[_]](mutableRecord: T, input: String): T =  {
    val schema = mutableRecord.getSchema
    val decoder = (new DecoderFactory).jsonDecoder(schema, input)
    jsonReader[T](schema).read(mutableRecord, decoder)
  }

  /**
  * Deserializes a JSON string to an immutable record with the specified schema.
  *
  * @param schema Deserializes using this schema.
  * @param input JSON string to deserialize from.
  * @return The deserialized record.
  */
  def fromJson[T <: ImmutableRecordBase](schema: Schema, input: String): T = {
    val decoder = (new DecoderFactory).jsonDecoder(schema, input)
    val mutableRecord = jsonReader[MutableRecordBase[T]](schema)
                        .read(null.asInstanceOf[MutableRecordBase[T]], decoder)
    mutableRecord.build
  }

  private def jsonReader[T <: MutableRecordBase[_]](schema: Schema): SpecificDatumReader[T] = {
    val specificData = new SpecificData(scalaNamespaceClassLoader)
    new SpecificDatumReader[T](schema, schema, specificData)
  }

  /**
   * Encodes a record into an array of bytes.
   *
   * @param encodable Encodable record.
   * @return Binary encoded representation of the record, as an array of bytes.
   */
  def encode(encodable: Encodable): Array[Byte] = {
    val output = new ByteArrayOutputStream()
    val encoder = EncoderFactory.get.directBinaryEncoder(output, null)
    encodable.encode(encoder)
    return output.toByteArray
  }

  /**
   * Decodes a record from a stream of bytes.
   *
   * @param decodable Mutable record to decode into.
   * @param input Stream of bytes.
   * @return The given mutable record, with the fields from the decoded binary stream merged in.
   */
  def decode[T <: Decodable](decodable: T, input: InputStream): T = {
    val decoder = DecoderFactory.get.directBinaryDecoder(input, null)
    decodable.decode(decoder)
    return decodable
  }
}

object Conversions {
  import collection.JavaConverters._

  /* From http://stackoverflow.com/a/10957405 */
  def scalaToJava(x: Any): Any = { // TODO: make sure we return mutable objects when needed
    x match {
      case x: List[_] => x.map(scalaToJava).asJava
      case x: collection.Map[_, _] =>
        mutableMapAsJavaMapConverter(
          collection.mutable.Map(x.mapValues(scalaToJava).toSeq: _*)
        ).asJava
      case x: collection.mutable.Set[_] => x.map(scalaToJava).asJava
      case x: collection.mutable.Buffer[_] => x.map(scalaToJava).asJava
      case x: Iterable[_] => x.map(scalaToJava).asJava
      case x: Iterator[_] => x.map(scalaToJava).asJava
      case x: Array[_] => x.map(scalaToJava).toArray
      case _ => x
    }
  }

  def javaToScala(x: Any): Any = {
    x match {
      case x: java.util.List[_] =>
        collection.mutable.ListBuffer(x.asScala.map(javaToScala): _*)
      case x: java.util.Map[_, _] =>
        collection.mutable.Map(x.asScala.map(
          kv => kv._1.toString -> javaToScala(kv._2)
        ).toSeq: _*)
      case x: java.util.Set[_] =>
        collection.mutable.Set(x.asScala.map(javaToScala).toSeq: _*)
      case u: org.apache.avro.util.Utf8 => x.toString
      case x => x
    }
  }
}

/** Base interface for union types (except the one compiled to Option[X]). */
trait UnionData
  extends Encodable {

  /** @return A generic reference to the object stored held by this union class. */
  def getData(): Any

  override def encode(encoder: Encoder)
}
