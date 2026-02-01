#!/bin/bash

# ins Verzeichnis wechseln, in dem das Skript liegt
cd "$(dirname "$0")"

./gradlew quarkusDev
