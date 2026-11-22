#!/bin/sh
# Ejecuta los 18 casos de prueba funcionales y regenera las capturas en evidencias/
java -Djava.awt.headless=true -cp out pruebas.PruebasFuncionales evidencias
