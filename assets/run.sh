#!/usr/bin/env bash
declare SCRIPT_DIR="$(cd "${0%/*}" ; pwd)"
java \
  -Dglass.platform=gtk \
  -Djava.library.path="${SCRIPT_DIR}/openjfx/lib" \
  -Dmonocle.platform.traceConfig=false \
  -Dprism.verbose=false \
  -Djavafx.verbose=false \
  --module-path ".:${SCRIPT_DIR}/openjfx/lib:${SCRIPT_DIR}/lib" \
  --add-modules javafx.controls \
  --module buzzer.app/application.GameController $@
