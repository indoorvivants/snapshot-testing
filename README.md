<!--toc:start-->
- [snapshots-testing](#snapshots-testing)
- [Installation (SBT)](#installation-sbt)
- [Usage](#usage)
  - [Scala.js](#scalajs)
<!--toc:end-->


[![sbt-snapshots Scala version support](https://index.scala-lang.org/indoorvivants/snapshot-testing/sbt-snapshots/latest.svg)](https://index.scala-lang.org/indoorvivants/snapshot-testing/sbt-snapshots)

## snapshots-testing

This is a micro library to aid with [snapshot testing](https://jestjs.io/docs/snapshot-testing).

The meat of the project is actually in build tool integration - I wanted to
have an experience similar to that of [Insta](https://insta.rs/docs/cli/) with 
its Cargo integration. Currently only SBT is supported, but Mill support 
can be added easily, as the project is already structured in a way that favours that.

One of the goals was being able to work with Scala.js and Scala Native - so the Scala.js portion of the runtime includes small Node.js bindings
to the `fs` module for filesystem operations.

The runtime is published for 2.12, 2.13, 3 and JVM, JS, Native.


## Installation (SBT)
To add the plugin to your SBT build:

1. `addSbtPlugin("com.indoorvivants.snapshots" % "sbt-snapshots" % "VERSION")` to your `project/plugins.sbt` (see VERSION on the badge above)
2. In the project where you would like to use snapshot testing, add these settings:

    ```scala
    .settings(
      snapshotsPackageName          := "example",
      snapshotsIntegrations         += SnapshotIntegration.MUnit // if using MUnit
    )
    .enablePlugins(SnapshotsPlugin)
    ```

    Package name is the only required setting, because the plugin will 
    generate a source file that ties build-time filesystem locations with 
    runtime test execution.

The library provides no diffing capabilities, instead delegating that 
task to the test framework of choice.

## Usage

SBT tasks:

- `snapshotsCheck` - interactively accept modified snapshots (if there are any)
- `snapshotsAcceptAll` - accept all modified snapshots
- `snapshotsDiscardAll` - discard all snapshot changes

SBT settings: 
- `snapshotsIntegrations` - list of test framework integrations to generate in test sources
  
  Instead of providing a separate dependency for each test framework, the plugin generates 
  a single-file integration. This helps avoid the dependency hell of incompatible framework 
  versions. It might seem weird at first, because it is, but I believe it's the only way to 
  stay sane.

- `snapshotsPackageName` - package name to use for generated file

[Sample MUnit tests](modules/example/src/test/scala/MunitExampleTests.scala)

You can see what the workflow looks like by

1. Modifying the sample test above
2. Running `example/test` and observing a failure
3. Running `example/snapshotsCheck` and accepting a diff
4. Running `example/test` again and it should succeed
5. The snapshot file will be changed on disk

Here's the same workflow in video format:

https://github.com/indoorvivants/snapshot-testing/assets/1052965/eaef5f88-641b-4bec-85be-1e7458379a58


### Scala.js

The snapshots runtime will only run under Node.js (or any runtime where `require("node:fs")` import can succeed).

To make sure module import is enabled, you should ensure you're using the correct module kind - 
for example commonJS:

```scala
scalaJSLinkerConfig ~= (_.withModuleKind(ModuleKind.CommonJSModule))
```
