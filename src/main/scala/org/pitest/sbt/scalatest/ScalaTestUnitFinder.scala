package org.pitest.sbt.scalatest

import java.util
import java.util.Collections

import org.pitest.testapi.{TestUnit, TestUnitFinder}

class ScalaTestUnitFinder extends TestUnitFinder {
  override def findTestUnits(clazz: Class[_]): util.List[TestUnit] = Collections.singletonList(new ScalaTestUnit(clazz))
}
