#!/usr/bin/env bash
# Verzeichnis des Skripts ermitteln
declare SCRIPT_DIR="$(cd "${0%/*}" ; pwd)"

# Wir starten die JAR-Datei direkt. 
# Der Classpath beinhaltet die JAR selbst und alle Bibliotheken im lib-Ordner.
sudo java \
  -Dglass.platform=gtk \
  -Djdk.gtk.version=2 \
  --module-path "${SCRIPT_DIR}/lib:${SCRIPT_DIR}/IAE_Buzzergame_SNAPSHOT_0.0.2.jar" \
  --add-modules javafx.controls,javafx.fxml \
  -jar "${SCRIPT_DIR}/IAE_Buzzergame_SNAPSHOT_0.0.2.jar"
