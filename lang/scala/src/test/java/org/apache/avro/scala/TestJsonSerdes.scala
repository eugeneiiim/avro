package org.apache.avro.scala

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import org.apache.avro.scala.test.generated.scala._
import scala.collection.mutable

@RunWith(classOf[JUnitRunner])
class TestJsonSerdes
  extends FunSuite {

  def jsonSerdesIsIdentity[I <: ImmutableRecordBase, M <: MutableRecordBase[I]](label: String, record: => I, mutableRecord: => M) {
    test("%s (mutable)" format label) {
      assert(mutableRecord === Records.mutableFromJson[M](mutableRecord, Records.toJson(mutableRecord)))
    }

    test("%s (immutable)" format label) {
      val record2 = Records.fromJson[I](record.getSchema, Records.toJson(record))
      assert(record === record2)
      assert(record.getClass === record2.getClass)
    }
  }

  jsonSerdesIsIdentity[EmptyRecord, MutableEmptyRecord]("empty record",
    new EmptyRecord,
    new MutableEmptyRecord)

  jsonSerdesIsIdentity[Container, MutableContainer]("nested record",
    new Container(contained = new Contained(data = 1)),
    new MutableContainer(contained = new MutableContained(data = 1)))

  jsonSerdesIsIdentity[RecordWithString, MutableRecordWithString]("record with string",
    new RecordWithString("a"),
    new MutableRecordWithString("a"))

  private def recordWithAllTypes = new RecordWithAllTypes(
    nullField  = null,
    booleanField = false,
    intField = 1,
    longField = 1L,
    floatField = 1.0f,
    doubleField = 1.0,
    stringField = "string",
    bytesField = List(1, 2, 3),
    fixedField = List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16),  // TODO(taton) the size of the array should be validated
    intArrayField = List(1, 2, 3),
    intMapField = Map("x" -> 1, "y" -> 2),
    intArrayArrayField = List(List(1, 2), List(3, 4)),
    intMapMapField = Map("a" -> Map("x" -> 1), "b" -> Map("y" -> 2)))

  private def mutableRecordWithAllTypes = new MutableRecordWithAllTypes(
    nullField  = null,
    booleanField = false,
    intField = 1,
    longField = 1L,
    floatField = 1.0f,
    doubleField = 1.0,
    stringField = "string",
    bytesField = mutable.Buffer[Byte](1, 2, 3),
    fixedField = Array[Byte](1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16),  // TODO(taton) the size of the array should be validated
    intArrayField = mutable.ArrayBuffer[Int](1, 2, 3),
    intMapField = mutable.HashMap[String, Int]("x" -> 1, "y" -> 2),
    intArrayArrayField = mutable.Buffer[mutable.Buffer[Int]](mutable.Buffer(1, 2), mutable.Buffer(3, 4)),
    intMapMapField = mutable.HashMap[String, mutable.Map[String, Int]]("a" -> mutable.HashMap("x" -> 1), "b" -> mutable.HashMap("y" -> 2)))

  jsonSerdesIsIdentity[RecordWithAllTypes, MutableRecordWithAllTypes]("record with all types",
    recordWithAllTypes,
    mutableRecordWithAllTypes
  )

}

