#!/bin/bash

export OPENJFX=/usr/lib/jvm/javafx-sdk-17.0.15/lib
java --module-path $OPENJFX --add-modules javafx.controls,javafx.fxml -jar target/IFZ826_LW_Buzzer-0.0.1-SNAPSHOT-jar-with-dependencies.jar

