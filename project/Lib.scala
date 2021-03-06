import sbt._
import Keys._
import com.jsuereth.sbtpgp.PgpKeys._
import dotty.tools.sbtplugin.DottyPlugin.autoImport._
import sbtcrossproject.CrossProject
import sbtcrossproject.CrossPlugin.autoImport._
import scalajscrossproject.ScalaJSCrossPlugin.autoImport._
import xerial.sbt.Sonatype.autoImport._

object Lib {
  type CPE = CrossProject => CrossProject
  type PE = Project => Project

  class ConfigureBoth(val jvm: PE, val js: PE) {
    def jvmConfigure(f: PE) = new ConfigureBoth(f compose jvm, js)
    def  jsConfigure(f: PE) = new ConfigureBoth(jvm, f compose js)
  }

  def ConfigureBoth(both: PE) = new ConfigureBoth(both, both)

  implicit def _configureBothToCPE(p: ConfigureBoth): CPE =
    _.jvmConfigure(p.jvm).jsConfigure(p.js)

  def addCommandAliases(m: (String, String)*): PE = {
    val s = m.map(p => addCommandAlias(p._1, p._2)).reduce(_ ++ _)
    _.settings(s: _*)
  }

  implicit class CrossProjectExt(val cp: CrossProject) extends AnyVal {
    def bothConfigure(fs: PE*): CrossProject =
      fs.foldLeft(cp)((q, f) =>
        q.jvmConfigure(f).jsConfigure(f))
  }
  implicit def CrossProjectExtB(b: CrossProject.Builder) =
    new CrossProjectExt(b)

  def publicationSettings(ghProject: String) = ConfigureBoth(
    _.settings(
      publishTo := sonatypePublishToBundle.value,
      pomExtra :=
        <scm>
          <connection>scm:git:github.com/japgolly/{ghProject}</connection>
          <developerConnection>scm:git:git@github.com:japgolly/{ghProject}.git</developerConnection>
          <url>github.com:japgolly/{ghProject}.git</url>
        </scm>
        <developers>
          <developer>
            <id>japgolly</id>
            <name>David Barri</name>
          </developer>
        </developers>))
    .jsConfigure(
      sourceMapsToGithub(ghProject))

  def sourceMapsToGithub(ghProject: String): PE =
    p => p.settings(
      scalacOptions ++= {
        val _isDotty    = isDotty.value
        val _isSnapshot = isSnapshot.value
        val ver         = version.value
        if (_isSnapshot)
          Nil
        else {
          val a = p.base.toURI.toString.replaceFirst("[^/]+/?$", "")
          val g = s"https://raw.githubusercontent.com/japgolly/$ghProject"
          val flag = if (_isDotty) "-scalajs-mapSourceURI" else "-P:scalajs:mapSourceURI"
          s"$flag:$a->$g/v$ver/" :: Nil
        }
      }
    )

  def preventPublication: PE =
    _.settings(
      skip in publish    := true,
      publish            := {},
      publishLocal       := {},
      publishSigned      := {},
      publishLocalSigned := {},
      publishArtifact    := false,
      publishTo          := Some(Resolver.file("Unused transient repository", target.value / "fakepublish")),
      packagedArtifacts  := Map.empty)
    // .disablePlugins(plugins.IvyPlugin)
}
