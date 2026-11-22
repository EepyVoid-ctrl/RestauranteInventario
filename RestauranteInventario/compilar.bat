@echo off
REM Compila todo el codigo fuente en la carpeta out\ (requiere JDK 17 o superior)
if not exist out mkdir out
dir /s /b src\*.java > fuentes.txt
javac -encoding UTF-8 -d out @fuentes.txt && echo Compilacion correcta.
