# Projektdokumentation: Buzzer-Game IAE

## 1. Projektübersicht
Das Projekt **«BuzzerGame»** ist ein Java-basiertes interaktives Spiel für den Raspberry Pi. Es wurde für ein Firmenprojekt der **IAE** im Jahr 2026 adaptiert. Die Hardware-Interaktion erfolgt über physische Taster (Buzzer), die an die GPIO-Pins des Raspberry Pi angeschlossen sind. Die grafische Benutzeroberfläche wird mit JavaFX realisiert und die Projektverwaltung erfolgt über Maven (`mvn`).

## 2. Bedienungsanleitung

### Spielablauf
* **Anmeldung**: Spieler melden sich vor jeder Runde durch Drücken ihres Buzzers an (mindestens zwei Teilnehmer erforderlich).
* **Fragerunde**: Die Anzeige der Fragen endet automatisch nach der vordefinierten Zeit oder sobald alle beteiligten Spieler eine Antwort abgegeben haben.
* **Ergebnis**: Die Ergebnis-Anzeige schaltet nach 10 Sekunden automatisch weiter oder kann manuell mit der **Leertaste** übersprungen werden.

### Einstellungen
Im Menü „Einstellungen bearbeiten“ können folgende Konfigurationen vorgenommen werden:
* **Fragenkatalog**: Auswahl der Datei im **CSV-Format** (Excel-Dateien werden nicht unterstützt).
* **Runden-Konfiguration**: Festlegung der Fragenanzahl pro Runde und der Zeit pro Frage.
* **Hardware-Check**: Echtzeit-Anzeige aller Tasterzustände der angeschlossenen Buzzer zur Funktionskontrolle.

## 3. Raspberry Pi Setup

### Bildschirmauflösung
Die Anwendung ist für Auflösungen zwischen 1600x1000 und 1920x1080 px optimiert. Die Einstellung erfolgt wie folgt:
1. Menü (Himbeere oben links) -> Preferences -> **Screen Configuration**.
2. Rechtsklick auf den HDMI-Monitor und die gewünschte **Resolution** auswählen.

## 4. Entwicklung & Versionsverwaltung

### Repository (IAE Fork)
Das Projekt wird über GitHub verwaltet und für die IAE unter folgendem Link geführt:
`https://github.com/KeepCalmStayCalm/BuzzerGame_2026_IAE.git`

### Systemanforderungen (Requirements)
* **Software**: `wiringpi`, `openjfx11`.
* **IDE**: Die Eclipse-IDE wird für den direkten Einsatz auf dem Raspberry Pi nicht empfohlen; alternativ kann **Geany** verwendet werden.

### Build-Befehle
* **Kompilieren und Starten**:
    ```bash
    mvn javafx:run
    ```
* **Paketieren (Deployment)**:
    ```bash
    mvn clean compile package
    ```
