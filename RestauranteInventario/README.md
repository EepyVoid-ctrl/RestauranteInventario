# Sistema de Inventario y Ventas para Restaurantes

Proyecto final de **ISWZ1103 – Programación II** (Universidad de las Américas, Ingeniería de Software).
Aplicación de escritorio en **Java (Swing)** que controla los insumos de un restaurante, calcula el costo
de cada plato a partir de su receta, registra ventas con factura, descuenta el inventario automáticamente
y genera reportes de rentabilidad y consumo.

**Integrantes:** Minerva Sáenz (líder, Módulo 3), Helen Benalcazar (análisis y diseño, Módulo 2),
Nicolas Izurieta (desarrollo, Módulo 1 e interfaz), Davis Cuenca (pruebas, Módulo 4).
**Docente:** Bernarda Sandoval.

## Requisitos
* JDK 17 o superior (usa *records*). No necesita librerías externas.

## Cómo compilar y ejecutar
```bash
./compilar.sh      # Windows: compilar.bat
./ejecutar.sh      # Windows: ejecutar.bat   (opción --vacio para iniciar sin datos de ejemplo)
./pruebas.sh       # ejecuta los 18 casos de prueba y regenera las capturas en evidencias/
```
La primera vez se cargan datos de ejemplo del restaurante «Sabor Andino». Los datos se guardan en
`datos/restaurante.dat` después de cada cambio.

## Estructura (paquetes)
| Paquete | Contenido |
|---|---|
| `modelo` | Entidades y reglas propias de cada objeto: `Insumo` (abstracta), `InsumoPerecedero`, `InsumoNoPerecedero`, `Plato`, `Ingrediente`, `Venta`, `DetalleVenta`, `Factura`, `Cliente`, `MetodoPago` (interfaz) y sus tres implementaciones, `Validar`, `ValidacionException` |
| `negocio` | Casos de uso: `InventarioServicio` (RF01–05), `MenuServicio` (RF06–10), `VentaServicio` (RF11–14), `ReporteServicio` (RF15–16), `BaseDatos`, `DatosDemo` |
| `interfaz` | Pantallas Swing: `VentanaPrincipal`, `PanelInventario`, `PanelMenu`, `PanelVentas`, `PanelReportes`, `Ui`, `Main` |
| `pruebas` | `PruebasFuncionales`: casos de prueba CP1–CP18 con captura de pantalla |

## Conceptos de POO aplicados
* **Encapsulamiento:** atributos privados con validación en los *setters*; el stock solo cambia con `reabastecer` y `descontar`.
* **Herencia:** `InsumoPerecedero` e `InsumoNoPerecedero` extienden la clase abstracta `Insumo`.
* **Polimorfismo:** `getTipo()` / `getAlertaConservacion()` según el tipo de insumo; `MetodoPago` con tres implementaciones.

## Documentación
* `docs/diagrama_clases_modelo.png` y `docs/diagrama_capas.png`: diagramas UML.
* `evidencias/`: capturas y resultados de los casos de prueba.
