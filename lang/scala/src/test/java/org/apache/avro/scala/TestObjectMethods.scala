package org.apache.avro.scala

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import org.apache.avro.scala.test.generated.scala._
import org.apache.avro.scala.test.generated.scala.MutableContainer
import org.apache.avro.scala.test.generated.scala.MutableContained

@RunWith(classOf[JUnitRunner])
class TestEquals
  extends FunSuite with ShouldMatchers {

  test("empty record (mutable != null)") {
    assert(new MutableEmptyRecord != null)
  }

  test("empty record (immutable != null)") {
    assert(new EmptyRecord != null)
  }

  test("empty record (mutable)") {
    (expect
      (new MutableEmptyRecord)
      (new MutableEmptyRecord))
  }

  test("empty record (immutable)") {
    (expect
      (new EmptyRecord)
      (new EmptyRecord))
  }

  test("empty record (mutable == immutable)") {
    (expect
      (new MutableEmptyRecord)
      (new EmptyRecord))
  }

  test("record with string (mutable)") {
    (expect
      (new MutableRecordWithString("a"))
      (new MutableRecordWithString("a")))
  }

  test("record with string (mutable != mutable)") {
    new MutableRecordWithString("a") should not equal(new MutableRecordWithString("b"))
  }

  test("record with string (immutable)") {
    (expect
      (new RecordWithString("a"))
      (new RecordWithString("a")))
  }

  test("record with string (immutable != immutable)") {
    new RecordWithString("a") should not equal(new RecordWithString("b"))
  }

  test("record with string (mutable == immutable)") {
    (expect
      (new MutableRecordWithString("a"))
      (new RecordWithString("a")))
  }

  test("record with string (mutable != immutable)") {
    new MutableRecordWithString("a") should not equal(new RecordWithString("b"))
  }

  test("nested record (mutable)") {
    (expect
      (new MutableContainer(contained = new MutableContained(data = 1)))
      (new MutableContainer(contained = new MutableContained(data = 1))))
  }

  test("nested record (immutable)") {
    (expect
      (new Container(contained = new Contained(data = 1)))
      (new Container(contained = new Contained(data = 1))))
  }

  test("nested record (mutable == immutable)") {
    (expect
      (new MutableContainer(contained = new MutableContained(data = 1)))
      (new Container(contained = new Contained(data = 1))))
  }

}

@RunWith(classOf[JUnitRunner])
class TestHashCode
  extends FunSuite {

  test("empty record (mutable)") {
    (expect
      ((new MutableEmptyRecord).hashCode())
      ((new MutableEmptyRecord).hashCode()))
  }

  test("empty record (immutable)") {
    (expect
      ((new EmptyRecord).hashCode())
      ((new EmptyRecord).hashCode()))
  }

  test("empty record (mutable == immutable)") {
    (expect
      ((new MutableEmptyRecord).hashCode())
      ((new EmptyRecord).hashCode()))
  }

  test("nested record (mutable)") {
    (expect
      ((new MutableContainer(contained = new MutableContained(data = 1))).hashCode())
      ((new MutableContainer(contained = new MutableContained(data = 1))).hashCode()))
  }

  test("nested record (immutable)") {
    (expect
      ((new Container(contained = new Contained(data = 1))).hashCode())
      ((new Container(contained = new Contained(data = 1))).hashCode()))
  }

  test("nested record (mutable == immutable)") {
    (expect
      ((new MutableContainer(contained = new MutableContained(data = 1))).hashCode())
      ((new Container(contained = new Contained(data = 1))).hashCode()))
  }

}


