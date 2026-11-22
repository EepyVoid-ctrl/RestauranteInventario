package negocio;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import modelo.*;

/** Carga datos de ejemplo (restaurante "Sabor Andino") para probar el sistema rápidamente. */
public final class DatosDemo {
    private DatosDemo() { }

    public static void cargar(BaseDatos db) {
        InventarioServicio inv = new InventarioServicio(db);
        MenuServicio menu = new MenuServicio(db);
        VentaServicio ventas = new VentaServicio(db);
        LocalDate hoy = LocalDate.now();

        Insumo pollo = inv.registrar("Pollo", "Carnes", "kg", 40, 160, 8, hoy.plusDays(5));
        Insumo res = inv.registrar("Carne de res", "Carnes", "kg", 25, 150, 6, hoy.plusDays(4));
        Insumo arroz = inv.registrar("Arroz", "Granos", "kg", 50, 60, 10, null);
        Insumo papa = inv.registrar("Papa", "Vegetales", "kg", 60, 45, 12, hoy.plusDays(10));
        Insumo tomate = inv.registrar("Tomate", "Vegetales", "kg", 15, 22.5, 4, hoy.plusDays(2));
        Insumo queso = inv.registrar("Queso", "Lácteos", "kg", 10, 55, 3, hoy.plusDays(7));
        Insumo aceite = inv.registrar("Aceite", "Despensa", "l", 20, 50, 5, null);
        Insumo huevo = inv.registrar("Huevos", "Lácteos", "unidad", 120, 24, 30, hoy.plusDays(12));

        Plato p1 = menu.registrar("Arroz con pollo", 6.50, List.of(
                new Ingrediente(pollo, 0.25), new Ingrediente(arroz, 0.15), new Ingrediente(aceite, 0.02)));
        Plato p2 = menu.registrar("Lomo saltado", 8.90, List.of(
                new Ingrediente(res, 0.22), new Ingrediente(papa, 0.20), new Ingrediente(tomate, 0.08),
                new Ingrediente(arroz, 0.15)));
        Plato p3 = menu.registrar("Llapingachos", 4.50, List.of(
                new Ingrediente(papa, 0.30), new Ingrediente(queso, 0.08), new Ingrediente(huevo, 1)));

        MetodoPago[] mp = MetodoPago.todos();
        Cliente final_ = Cliente.consumidorFinal();
        int[][] plan = { {10, 8, 3}, {9, 6, 5}, {8, 9, 4}, {6, 7, 6}, {5, 10, 2}, {3, 8, 7}, {2, 9, 3}, {1, 6, 4} };
        for (int d = 0; d < plan.length; d++) {
            Map<Plato, Integer> items = new LinkedHashMap<>();
            items.put(p1, plan[d][1]);
            items.put(p2, plan[d][2]);
            items.put(p3, 2 + d % 3);
            ventas.registrar(d % 2 == 0 ? final_ : new Cliente("María López", "1712345678", "0991234567"),
                    items, mp[d % 3], LocalDateTime.now().minusDays(plan[d][0]).withHour(13).withMinute(15));
        }
    }
}
