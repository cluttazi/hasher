#!/usr/bin/env bash
# Delegates to the sbt on PATH (sbt 1.x reads project/build.properties).
exec sbt "$@"
