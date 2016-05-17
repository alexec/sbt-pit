package org.pitest.sbt.scalatest

import org.pitest.classinfo.ClassByteArraySource
import org.pitest.testapi.{Configuration, TestGroupConfig, TestPluginFactory}

class ScalaTestPluginFactory extends TestPluginFactory {
  override def createTestFrameworkConfiguration(config: TestGroupConfig, source: ClassByteArraySource): Configuration = new ScalaTestConfiguration

  override def description(): String = "ScalaTest"
}
