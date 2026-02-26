#!/usr/bin/env bash
declare SCRIPT_DIR="$(cd "${0%/*}" ; pwd)"

sudo java \
  -Dglass.platform=gtk \
  -Djdk.gtk.version=2 \
  --module-path "${SCRIPT_DIR}:${SCRIPT_DIR}/lib" \
  --add-modules javafx.controls,javafx.fxml,java.prefs \
  -jar "${SCRIPT_DIR}/IAE_BuzzerGame-0.0.1-SNAPSHOT.jar"
