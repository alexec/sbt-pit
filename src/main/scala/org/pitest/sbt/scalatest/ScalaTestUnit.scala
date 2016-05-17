package org.pitest.sbt.scalatest

import org.pitest.testapi.{AbstractTestUnit, Description, ResultCollector}
import org.pitest.util.{IsolationUtils, Unchecked}
import org.scalatest.Suite

class ScalaTestUnit(clazz: Class[_]) extends AbstractTestUnit(new Description(clazz.getName, clazz)) {
  override def execute(loader: ClassLoader, rc: ResultCollector) = {
    val suite = clazz.newInstance().asInstanceOf[Suite]

    assert(!IsolationUtils.loaderDetectionStrategy.fromDifferentLoader(clazz, loader))

    rc.notifyStart(getDescription)
    try {
      suite.execute()
    } catch {
      case t: Throwable => throw Unchecked.translateCheckedException(t)
    } finally {
      rc.notifyEnd(getDescription)
    }
  }
}
