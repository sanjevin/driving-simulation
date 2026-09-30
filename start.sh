#!/usr/bin/env bash
set -euo pipefail

project_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
build_dir="$(mktemp -d "${TMPDIR:-/tmp}/driving-simulator.XXXXXX")"
trap 'rm -rf "$build_dir"' EXIT

if [[ -n "${JAVA_HOME:-}" && -x "${JAVA_HOME}/bin/javac" ]]; then
  javac_cmd="${JAVA_HOME}/bin/javac"
else
  javac_cmd="javac"
fi

find "$project_dir/src/main/java" -type f -name '*.java' \
  -exec "$javac_cmd" --release 17 -d "$build_dir" {} +
java -cp "$build_dir" com.drivingsimulator.DrivingSimulator "$@"
