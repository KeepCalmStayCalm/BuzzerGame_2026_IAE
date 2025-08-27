#!/bin/bash

# make sure to apt install openjdk-17-jdk maven openjfx
export OPENJFX=/usr/share/openjfx/lib/
java --module-path $OPENJFX --add-modules javafx.controls,javafx.fxml -jar target/Wiss_BuzzerGame-0.0.1-SNAPSHOT.jar application.RaspiBuzzer

