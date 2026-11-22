#!/bin/sh
# Compila todo el código fuente en la carpeta out/ (requiere JDK 17 o superior)
mkdir -p out
javac -encoding UTF-8 -d out $(find src -name "*.java") && echo "Compilación correcta."
