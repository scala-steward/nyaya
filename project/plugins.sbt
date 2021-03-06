addSbtPlugin("ch.epfl.lamp"       % "sbt-dotty"                % "0.5.3")
addSbtPlugin("com.github.sbt"     % "sbt-pgp"                  % "2.1.2")
addSbtPlugin("com.github.sbt"     % "sbt-release"              % "1.0.15")
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.0.0")
addSbtPlugin("org.scala-js"       % "sbt-scalajs"              % "1.5.0")
addSbtPlugin("org.xerial.sbt"     % "sbt-sonatype"             % "3.9.5")
addSbtPlugin("pl.project13.scala" % "sbt-jmh"                  % "0.4.0")

libraryDependencies += "org.scala-js" %% "scalajs-env-jsdom-nodejs" % "1.1.0"
