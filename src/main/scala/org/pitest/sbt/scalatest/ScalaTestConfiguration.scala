package org.pitest.sbt.scalatest

import org.pitest.extension.common.NoTestSuiteFinder
import org.pitest.functional.Option
import org.pitest.help.PitHelpError
import org.pitest.testapi._

class ScalaTestConfiguration extends org.pitest.testapi.Configuration{
  override def testClassIdentifier(): TestClassIdentifier = new ScalaTestClassIdentifier

  override def verifyEnvironment(): Option[PitHelpError] = Option.none()

  override def testUnitFinder(): TestUnitFinder = new ScalaTestUnitFinder

  override def testSuiteFinder(): TestSuiteFinder = new NoTestSuiteFinder
}
