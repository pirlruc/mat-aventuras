# Scorecard checks that stay open

## Binary-Artifacts

`gradle/wrapper/gradle-wrapper.jar` is the Gradle wrapper binary. Scorecard's
Binary-Artifacts check walks the tree and flags that jar. The
`wrapper-validation` job in `.github/workflows/ci.yml` checks the wrapper
checksum. That job does not clear the tree check. Leave the jar in the
repository. Deleting it removes the wrapper.
