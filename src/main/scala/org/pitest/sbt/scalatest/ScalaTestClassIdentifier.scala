package org.pitest.sbt.scalatest

import org.pitest.classinfo.ClassInfo
import org.pitest.testapi.BaseTestClassIdentifier

class ScalaTestClassIdentifier extends BaseTestClassIdentifier {
  // TODO - too simple?
  override def isATestClass(info: ClassInfo) = info.getName.asJavaName.endsWith("Spec")
}
