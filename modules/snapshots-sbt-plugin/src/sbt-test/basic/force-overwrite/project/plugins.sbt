addSbtPlugin(
  "com.indoorvivants.snapshots" % "sbt-snapshots" % sys.props("plugin.version")
)
addSbtPlugin("org.scala-native" % "sbt-scala-native"  % "0.5.6")
addSbtPlugin("com.eed3si9n"     % "sbt-projectmatrix" % "0.9.1")
addSbtPlugin("org.scala-js"     % "sbt-scalajs"       % "1.18.2")
