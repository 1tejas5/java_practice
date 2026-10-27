#!/usr/bin/env bash
# MiniBank build + run (Git Bash / Linux / macOS)
#   ./run.sh          guided demo
#   ./run.sh cli      interactive banking console
#   ./run.sh test     unit tests
set -euo pipefail
cd "$(dirname "$0")"

rm -rf out/classes
javac -d out/classes $(find src test -name '*.java')

if [[ "${1:-demo}" == "test" ]]; then
  java -cp out/classes com.minibank.testing.TestRunner
else
  java -cp out/classes com.minibank.App "$@"
fi
