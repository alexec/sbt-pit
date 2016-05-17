package org.pitest.sbt

import java.net.URLDecoder
import java.util

import org.pitest.mutationtest.config.{PluginServices, ReportOptions}
import org.pitest.mutationtest.tooling.EntryPoint
import org.pitest.sbt.PitKeys._
import org.pitest.testapi.TestGroupConfig
import org.pitest.util.{Glob, IsolationUtils}
import sbt.Keys._
import sbt._

import scala.collection.JavaConverters._

object PitPlugin extends Plugin {

  override lazy val projectSettings = Seq(
    threads := 1, 
    maxMutationsPerClass := 0,
    verbose := false, 
    timestampedReports := true,
    mutationUnitSize := 0,
    timeoutFactor := 1.25f,
    timeoutConst := 4000,
    mutateStaticInitializers := false,
    detectInlinedCode := true,
    
    mutators := Seq(),
    outputFormats := Seq("HTML"),
    includedGroups := Seq(),
    excludedGroups := Seq(),
    
    dependencyDistance := -1,
    reportPath <<= crossTarget(_ / "pit-reports"),
    targetClasses <<= organization.apply( s => Seq(s + ".*")),
    targetTests <<= organization.apply( s => Seq(s + ".*")),  
    excludedMethods := Seq(),
    avoidCallsTo := Seq(),
    excludedClasses := Seq(),
    engine := "gregor",
    
    // using built in javaOptions for the jvmargs . . . is this the right thing to do?
    pitConfiguration <<= (engine, mutators, outputFormats, javaOptions in Test, includedGroups, excludedGroups) map Configuration,
    mutableCodePaths <<= classDirectory in Compile map (f => Seq(f)),
    pathSettings <<= (baseDirectory, reportPath, mutableCodePaths, fullClasspath in Test, sourceDirectories in Compile) map PathSettings dependsOn (compile in Compile),
    filterSettings <<= (targetClasses, targetTests, dependencyDistance) map FilterSettings,
    excludes <<= (excludedClasses, excludedMethods, avoidCallsTo) map Excludes,
    options <<=  (detectInlinedCode,mutateStaticInitializers,threads,maxMutationsPerClass,verbose,timestampedReports,mutationUnitSize,timeoutFactor,timeoutConst) map Options,
    pitestTask <<= (options, pitConfiguration, pathSettings, filterSettings, excludes) map runPitest)

  def runPitest(options : Options, conf : Configuration, paths: PathSettings, filters: FilterSettings, excludes: Excludes): Unit = {

    val originalCL = Thread.currentThread().getContextClassLoader
    Thread.currentThread().setContextClassLoader(PitPlugin.getClass.getClassLoader)
    
    try {
      val ps = new PluginServices(IsolationUtils.getContextClassLoader)
      val pit = new EntryPoint
      val data = makeReportOptions(options, conf, paths, filters, excludes, ps)
      val env = System.getenv
      val result = pit.execute( paths.baseDir, data, ps, env)
      
      if ( result.getError.hasSome ) {
        result.getError.value().printStackTrace()
      }
      
    } finally {
      Thread.currentThread().setContextClassLoader(originalCL)
    }

    println("done")

  }

  def makeReportOptions(options: Options, config: Configuration
                       , paths: PathSettings
                       , filters: FilterSettings
                       , excludes: Excludes
                       , ps: PluginServices): ReportOptions = {
    
    val data = new ReportOptions
    data.setReportDir(paths.targetPath.getAbsolutePath)
    data.setClassPathElements(plainCollection(makeClasspath(paths, ps)))
    data.setCodePaths(plainCollection(paths.mutatablePath map (p => p.getPath)))
    data.setTargetClasses(plainCollection(filters.targetClasses map toGlob))
    data.setTargetTests(plainCollection(filters.targetTests map toGlob))
    data.setDependencyAnalysisMaxDistance(filters.dependencyDistance)
    data.setSourceDirs(plainCollection(paths.sources))
    data.setVerbose(options.verbose)
    data.setDetectInlinedCode(options.detectInlinedCode)
    data.setMutateStaticInitializers(options.mutateStaticInitializers)
    data.setNumberOfThreads(options.threads)
    data.setVerbose(options.verbose)
    data.setShouldCreateTimestampedReports(options.timestampedReports)
    data.setMutationUnitSize(options.mutationUnitSize)
    data.setTimeoutFactor(options.timeoutFactor)
    data.setTimeoutConstant(options.timeoutConst)
    data.setExcludedClasses(plainCollection(excludes.excludedClasses map toGlob))
    data.setExcludedMethods(plainCollection(excludes.excludedMethods map toGlob))
    data.setLoggingClasses(plainCollection(excludes.excludedMethods))

    data.setMutationEngine(config.engine)
    data.setMutators(plainCollection(config.mutators))
    data.addOutputFormats(plainCollection(config.outputFormats))

    val conf = new TestGroupConfig(plainCollection(config.excludedGroups), plainCollection(config.includedGroups))
    data.setGroupConfig(conf)

    data
  }
  
  def toGlob(s : String ) = new Glob(s)
  /**
   * The implicit conversion to collection is not enough - we need a plain
   * old java collection class to satisfy xstream
   */
  def plainCollection [T](s : Seq[T]) : java.util.List[T] = {
    import scala.collection.JavaConversions._
    new util.ArrayList(s)
  }

  private def makeClasspath(paths: org.pitest.sbt.PathSettings, ps : PluginServices ) : Seq[String] = {
    val cp = paths.classPath.files map (f => f.getPath.trim())
    val services = ps.findClientClasspathPlugins().asScala 
    val pluginPaths = services map ( c => pathTo(c.getClass) )
    cp ++  pluginPaths.toSet
  }
  
  private def pathTo(c : Class[_] ) = {
    val p = c.getProtectionDomain.getCodeSource.getLocation.getPath
    URLDecoder.decode(p, "UTF-8")
  } 

}