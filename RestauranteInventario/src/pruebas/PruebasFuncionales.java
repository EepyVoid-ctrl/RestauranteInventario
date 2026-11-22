package pruebas;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import javax.imageio.ImageIO;
import javax.swing.*;

import interfaz.*;
import modelo.*;
import negocio.*;

/**
 * Ejecuta los casos de prueba funcionales del sistema contra la lógica real (sin datos simulados),
 * imprime los resultados y genera una captura de pantalla por caso.
 * <p>Uso: {@code java -Djava.awt.headless=true -cp out pruebas.PruebasFuncionales carpeta_salida}
 */
public class PruebasFuncionales {

    /** Servicios sobre una base en memoria con datos de ejemplo. */
    static class Ctx {
        final BaseDatos db = BaseDatos.enMemoria();
        final InventarioServicio inv = new InventarioServicio(db);
        final MenuServicio menu = new MenuServicio(db);
        final VentaServicio ventas = new VentaServicio(db);
        final ReporteServicio rep = new ReporteServicio(db);
        Ctx() { DatosDemo.cargar(db); }
        Insumo insumo(String nombre) {
            for (Insumo i : inv.consultar(nombre, null, null)) if (i.getNombre().equals(nombre)) return i;
            throw new IllegalStateException("No existe " + nombre);
        }
        Plato plato(String nombre) {
            for (Plato p : menu.consultar(nombre)) if (p.getNombre().equals(nombre)) return p;
            throw new IllegalStateException("No existe " + nombre);
        }
    }

    static class Caso {
        String id, nombre, rf, descripcion, entrada, esperado, claveOk;
        int tab; boolean mono; boolean error; int subTab;
        Function<Ctx, String> accion;
        String obtenido; boolean paso; Ctx ctx;
        Caso(String id, String rf, String nombre, String descripcion, String entrada, String esperado, String claveOk,
             int tab, Function<Ctx, String> accion) {
            this.id = id; this.rf = rf; this.nombre = nombre; this.descripcion = descripcion; this.entrada = entrada;
            this.esperado = esperado; this.claveOk = claveOk; this.tab = tab; this.accion = accion;
        }
        Caso error() { this.error = true; return this; }
        Caso mono() { this.mono = true; return this; }
        Caso sub(int s) { this.subTab = s; return this; }
    }

    static String ejecutarCapturandoError(java.util.function.Supplier<String> s) {
        try { return s.get(); } catch (ValidacionException e) { return e.getMessage(); }
    }

    static List<Caso> casos() {
        List<Caso> c = new ArrayList<>();
        c.add(new Caso("CP1", "RF01", "Validación de registro de insumo (campos vacíos)",
            "Verificar que el sistema no permita registrar un insumo si falta un dato obligatorio.",
            "Nombre: \"Sal\", Categoría: Despensa, Unidad: kg, Cantidad comprada: (vacío), Costo total: 2.00, Stock mínimo: 1",
            "Mensaje de error claro indicando que la cantidad comprada es obligatoria.", "Cantidad comprada» es obligatorio", 0,
            x -> ejecutarCapturandoError(() -> {
                double cant = Validar.numero("Cantidad comprada", "");
                x.inv.registrar("Sal", "Despensa", "kg", cant, 2, 1, null);
                return "Registrado";
            })).error());
        c.add(new Caso("CP2", "RF01", "Registro de insumo válido con costo unitario",
            "Verificar que se almacene el insumo con código único y que calcule costo total ÷ cantidad.",
            "Nombre: \"Harina\", Categoría: Despensa, Unidad: kg, Cantidad: 10, Costo total: 12.00, Stock mínimo: 3",
            "Insumo registrado con código único y costo unitario $1.20 por kg.", "Costo unitario: $1.20", 0,
            x -> {
                Insumo i = x.inv.registrar("Harina", "Despensa", "kg", 10, 12, 3, null);
                return "Insumo registrado con código " + i.getCodigo() + ". Costo unitario: $" + Ui.f2(i.getCostoUnitario()) + " por kg.";
            }));
        c.add(new Caso("CP3", "RF02", "Consulta y filtro de insumos por estado",
            "Verificar que el listado se filtre por nombre, categoría y estado.",
            "Categoría: Carnes, Estado: Normal (datos de ejemplo cargados)",
            "Listado con Pollo y Carne de res, con stock, costo unitario y estado.", "Carne de res", 0,
            x -> {
                List<Insumo> r = x.inv.consultar("", "Carnes", "Normal");
                StringBuilder sb = new StringBuilder("Insumos encontrados (" + r.size() + "): ");
                for (int i = 0; i < r.size(); i++) sb.append(i > 0 ? ", " : "").append(r.get(i).getNombre())
                        .append(" [stock ").append(Ui.f2(r.get(i).getStock())).append(' ').append(r.get(i).getUnidadMedida())
                        .append(", $").append(Ui.f2(r.get(i).getCostoUnitario())).append(", ").append(r.get(i).getEstado()).append(']');
                return sb.toString();
            }));
        c.add(new Caso("CP4", "RF03", "Reabastecimiento y recálculo de costo unitario",
            "Verificar que al comprar más stock se sume la cantidad y se recalcule el costo unitario (promedio ponderado).",
            "Insumo: Arroz (35.45 kg a $1.20/kg tras las ventas de ejemplo). Nueva compra: 50 kg, costo total $80.00",
            "Stock 85.45 kg y costo unitario $1.43 (promedio ponderado).", "Stock: 85.45 kg", 0,
            x -> {
                Insumo a = x.insumo("Arroz");
                x.inv.actualizar(a.getCodigo(), "Arroz", "Granos", 10, 50, 80);
                return "Insumo actualizado. Stock: " + Ui.f2(a.getStock()) + " kg — Costo unitario: $" + Ui.f2(a.getCostoUnitario()) + ".";
            }));
        c.add(new Caso("CP5", "RF04", "Eliminación de insumo usado en una receta",
            "Verificar que el sistema impida eliminar un insumo asociado a recetas.",
            "Eliminar el insumo \"Pollo\" (usado en Arroz con pollo)",
            "Mensaje de rechazo que indica los platos que lo utilizan.", "Arroz con pollo", 0,
            x -> ejecutarCapturandoError(() -> { x.inv.eliminar(x.insumo("Pollo").getCodigo()); return "Eliminado"; })).error());
        c.add(new Caso("CP6", "RF05", "Alerta de reabastecimiento (estado Crítico)",
            "Verificar que un insumo con stock menor o igual al mínimo cambie a «Crítico» y aparezca en la alerta.",
            "Registrar \"Cilantro\": cantidad 2 kg, costo $3.00, stock mínimo 5",
            "Estado Crítico y alerta visible con la lista de insumos a reabastecer.", "Crítico", 0,
            x -> {
                Insumo i = x.inv.registrar("Cilantro", "Vegetales", "kg", 2, 3, 5, LocalDate.now().plusDays(2));
                StringBuilder sb = new StringBuilder("Estado de Cilantro: " + i.getEstado() + ". Requieren reabastecimiento: ");
                List<Insumo> al = x.inv.alertasReabastecimiento();
                for (int k = 0; k < al.size(); k++) sb.append(k > 0 ? ", " : "").append(al.get(k).getNombre());
                return sb.toString();
            }));
        c.add(new Caso("CP7", "RF06", "Registro de plato con receta",
            "Verificar que se cree un plato vinculando insumos y cantidades, calculando su costo de producción.",
            "Plato: \"Tortilla de papa\", PVP: 4.00, Receta: Papa 0.30 kg + Huevos 2 unidad",
            "Plato registrado con costo de producción $0.63 y PVP $4.00.", "Costo de producción: $0.63", 1,
            x -> {
                Plato p = x.menu.registrar("Tortilla de papa", 4.00, List.of(
                        new Ingrediente(x.insumo("Papa"), 0.30), new Ingrediente(x.insumo("Huevos"), 2)));
                return "Plato registrado con código " + p.getCodigo() + ". Costo de producción: $" + Ui.f2(p.getCostoProduccion()) + " — PVP: $" + Ui.f2(p.getPvp());
            }));
        c.add(new Caso("CP8", "RF07", "Consulta del catálogo de platos",
            "Verificar que se muestren los platos con costo, PVP y margen, y que la búsqueda por nombre funcione.",
            "Búsqueda por nombre: \"arroz\"",
            "Listado con el plato «Arroz con pollo», su costo de producción, PVP y margen.", "Arroz con pollo", 1,
            x -> {
                List<Plato> r = x.menu.consultar("arroz");
                StringBuilder sb = new StringBuilder("Platos encontrados (" + r.size() + "): ");
                for (Plato p : r) sb.append(p.getNombre()).append(" — costo $").append(Ui.f2(p.getCostoProduccion()))
                        .append(", PVP $").append(Ui.f2(p.getPvp())).append(", margen ").append(Ui.f2(p.getMargenPorcentual())).append(" %");
                return sb.toString();
            }));
        c.add(new Caso("CP9", "RF08", "Actualización de PVP de un plato",
            "Verificar que al modificar el PVP el plato se actualice y se recalcule su margen.",
            "Plato: Lomo saltado, nuevo PVP: 9.50 (antes 8.90)",
            "Plato actualizado con PVP $9.50 y nuevo margen.", "PVP $9.50", 1,
            x -> {
                Plato p = x.plato("Lomo saltado");
                x.menu.actualizar(p.getCodigo(), p.getNombre(), 9.50, new ArrayList<>(p.getReceta()));
                return "Plato actualizado: PVP $" + Ui.f2(p.getPvp()) + ", costo $" + Ui.f2(p.getCostoProduccion())
                        + ", margen " + Ui.f2(p.getMargenPorcentual()) + " %.";
            }));
        c.add(new Caso("CP10", "RF09", "Eliminación de plato con ventas registradas",
            "Verificar que un plato con ventas no se borre, sino que quede inactivo para conservar el historial.",
            "Eliminar el plato \"Llapingachos\" (tiene ventas en los datos de ejemplo)",
            "Mensaje que indica que el plato se marcó como INACTIVO.", "INACTIVO", 1,
            x -> x.menu.eliminar(x.plato("Llapingachos").getCodigo())));
        c.add(new Caso("CP11", "RF10", "Validación de precio menor al costo",
            "Verificar que el sistema advierta pérdida cuando el PVP es menor o igual al costo de producción.",
            "Plato: \"Pollo premium\", PVP: 1.00, Receta: Pollo 0.40 kg (costo $1.60)",
            "Plato registrado con advertencia de que el PVP es menor o igual al costo.", "ADVERTENCIA", 1,
            x -> {
                Plato p = x.menu.registrar("Pollo premium", 1.00, List.of(new Ingrediente(x.insumo("Pollo"), 0.40)));
                return p.getAdvertenciaPrecio();
            }));
        c.add(new Caso("CP12", "RF11", "Registro de venta con total calculado",
            "Verificar que el sistema cargue el PVP, calcule subtotal por plato y total, y registre la venta con su método de pago.",
            "2 × Arroz con pollo ($6.50) + 1 × Llapingachos ($4.50), pago: Tarjeta",
            "Venta registrada con subtotal $17.50, IVA $2.63 y total $20.13.", "TOTAL A PAGAR:   $20.13", 2,
            x -> {
                Map<Plato, Integer> it = new LinkedHashMap<>();
                it.put(x.plato("Arroz con pollo"), 2);
                it.put(x.plato("Llapingachos"), 1);
                Factura f = x.ventas.registrar(Cliente.consumidorFinal(), it, new PagoTarjeta());
                return f.generarTexto();
            }).mono());
        c.add(new Caso("CP13", "RF12", "Descuento automático de inventario",
            "Verificar que al confirmar una venta se descuente del stock la cantidad de insumos de la receta.",
            "Stock inicial de Arroz tras 8 ventas de ejemplo; nueva venta: 10 × Arroz con pollo (0.15 kg de arroz por porción)",
            "El stock de Arroz disminuye 1.50 kg sin intervención manual.", "disminuyó 1.50", 2,
            x -> {
                Insumo arroz = x.insumo("Arroz");
                double antes = arroz.getStock();
                Map<Plato, Integer> it = new LinkedHashMap<>();
                it.put(x.plato("Arroz con pollo"), 10);
                x.ventas.registrar(Cliente.consumidorFinal(), it, new PagoEfectivo());
                return "Stock de Arroz antes: " + Ui.f2(antes) + " kg, después: " + Ui.f2(arroz.getStock())
                        + " kg. El stock disminuyó " + Ui.f2(antes - arroz.getStock()) + " kg.";
            }));
        c.add(new Caso("CP14", "RF12", "Venta bloqueada por stock insuficiente",
            "Verificar que el sistema rechace la venta e informe el insumo faltante.",
            "Venta de 200 × Arroz con pollo (requiere 50 kg de pollo; disponible menos)",
            "Venta rechazada indicando el insumo insuficiente.", "stock insuficiente", 2,
            x -> ejecutarCapturandoError(() -> {
                Map<Plato, Integer> it = new LinkedHashMap<>();
                it.put(x.plato("Arroz con pollo"), 200);
                x.ventas.registrar(Cliente.consumidorFinal(), it, new PagoEfectivo());
                return "Venta registrada";
            })).error());
        c.add(new Caso("CP15", "RF13", "Generación de factura con datos del cliente",
            "Verificar que la factura tenga número único, datos del cliente, detalle, subtotal, IVA, total y método de pago.",
            "Cliente: Ana Torres, 1722334455, 0987654321; 1 × Lomo saltado ($8.90); pago: Transferencia",
            "Factura con subtotal $8.90, IVA $1.34 y total $10.24.", "TOTAL A PAGAR:   $10.24", 2,
            x -> {
                Map<Plato, Integer> it = new LinkedHashMap<>();
                it.put(x.plato("Lomo saltado"), 1);
                return x.ventas.registrar(new Cliente("Ana Torres", "1722334455", "0987654321"), it, new PagoTransferencia()).generarTexto();
            }).mono());
        c.add(new Caso("CP16", "RF14", "Consulta de facturas por identificación",
            "Verificar que se pueda filtrar el historial de facturas y ver el total y método de pago.",
            "Identificación: 1712345678 (cliente María López en los datos de ejemplo)",
            "Listado de las facturas de María López con fecha, total y método de pago.", "María López", 2,
            x -> {
                List<Factura> r = x.ventas.consultarFacturas(null, null, "", "1712345678");
                StringBuilder sb = new StringBuilder("Facturas encontradas: " + r.size() + ". Ejemplo: ");
                if (!r.isEmpty()) sb.append(r.get(0).getNumero()).append(" — ").append(r.get(0).getCliente().getNombre())
                        .append(" — total $").append(Ui.f2(r.get(0).getTotal())).append(" — ").append(r.get(0).getVenta().getMetodoPago().getNombre());
                return sb.toString();
            }).sub(1));
        c.add(new Caso("CP17", "RF15", "Reporte de rentabilidad por plato",
            "Verificar margen unitario, margen porcentual, unidades vendidas y utilidad total, ordenados por rentabilidad.",
            "Rango: últimos 30 días (datos de ejemplo)",
            "Tabla por plato ordenada de mayor a menor utilidad total.", "Lomo saltado", 3,
            x -> {
                List<ReporteServicio.FilaRentabilidad> r = x.rep.rentabilidad(LocalDate.now().minusDays(30), LocalDate.now());
                StringBuilder sb = new StringBuilder("Platos en el reporte: " + r.size() + ". ");
                for (ReporteServicio.FilaRentabilidad f : r) sb.append(f.plato()).append(": ").append(f.unidades()).append(" uds, margen ")
                        .append(Ui.f2(f.margenPorcentual())).append(" %, utilidad $").append(Ui.f2(f.utilidadTotal())).append(". ");
                return sb.toString().trim();
            }));
        c.add(new Caso("CP18", "RF16", "Reporte de consumo y días de cobertura",
            "Verificar el consumo total, el consumo diario promedio y los días de cobertura de cada insumo.",
            "Rango: últimos 30 días, categoría: Carnes",
            "Pollo y Carne de res con consumo total, consumo diario promedio y días de cobertura.", "Pollo", 3,
            x -> {
                List<ReporteServicio.FilaConsumo> r = x.rep.consumo(LocalDate.now().minusDays(30), LocalDate.now(), "Carnes");
                StringBuilder sb = new StringBuilder();
                for (ReporteServicio.FilaConsumo f : r) sb.append(f.insumo()).append(": consumo ").append(Ui.f2(f.consumoTotal()))
                        .append(' ').append(f.unidad()).append(", diario ").append(Ui.f2(f.consumoDiario()))
                        .append(", cobertura ").append(Ui.f2(f.diasCobertura())).append(" días. ");
                return sb.toString().trim();
            }));
        return c;
    }

    // ---------- Capturas ----------

    static BufferedImage capturar(Caso c) {
        Ctx x = c.ctx;
        JPanel panel;
        switch (c.tab) {
            case 0: {
                PanelInventario p = new PanelInventario(x.inv);
                if (c.id.equals("CP1")) p.rellenarFormulario("Sal", "", "2.00", "1");
                panel = p; break;
            }
            case 1: panel = new PanelMenu(x.menu, x.inv); break;
            case 2: { PanelVentas p = new PanelVentas(x.menu, x.ventas, x.inv); p.seleccionarPestana(c.subTab); panel = p; break; }
            default: panel = new PanelReportes(x.rep, x.inv);
        }
        int w = 1150, h = 640;
        JPanel marco = new JPanel(new BorderLayout());
        JLabel cab = new JLabel("  Restaurante — Control de inventario, recetas y ventas   [" + c.rf + "]");
        cab.setOpaque(true); cab.setBackground(Ui.PRIMARIO); cab.setForeground(Color.WHITE);
        cab.setFont(cab.getFont().deriveFont(Font.BOLD, 16f)); cab.setBorder(BorderFactory.createEmptyBorder(10, 6, 10, 6));
        marco.add(cab, BorderLayout.NORTH);
        marco.add(panel, BorderLayout.CENTER);
        marco.setSize(w, h);
        prepararTablas(marco);
        layoutRec(marco);
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.WHITE); g.fillRect(0, 0, w, h);
        marco.paint(g);

        // Ventana de mensaje con el resultado real, superpuesta
        JComponent contenido;
        if (c.mono) {
            JTextArea a = new JTextArea(c.obtenido); a.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13)); a.setEditable(false);
            contenido = a;
        } else {
            JLabel l = new JLabel("<html><body style='width:380px'>" + c.obtenido.replace("&", "&amp;").replace("<", "&lt;") + "</body></html>");
            contenido = l;
        }
        JOptionPane op = new JOptionPane(contenido, c.error ? JOptionPane.ERROR_MESSAGE
                : (c.obtenido.startsWith("ADVERTENCIA") ? JOptionPane.WARNING_MESSAGE : JOptionPane.INFORMATION_MESSAGE),
                JOptionPane.DEFAULT_OPTION);
        String titulo = c.error ? "Validación" : (c.obtenido.startsWith("ADVERTENCIA") ? "Advertencia" : "Información");
        JPanel ventana = new JPanel(new BorderLayout());
        JLabel barra = new JLabel("  " + titulo); barra.setOpaque(true); barra.setBackground(new Color(0xDDDDDD));
        barra.setBorder(BorderFactory.createEmptyBorder(5, 4, 5, 4));
        ventana.add(barra, BorderLayout.NORTH); ventana.add(op, BorderLayout.CENTER);
        ventana.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        Dimension d = ventana.getPreferredSize();
        int ancho = Math.min(d.width, 700), alto = d.height;
        ventana.setSize(ancho, alto); layoutRec(ventana);
        int x0 = Math.max(10, (w - ancho) / 2 - 160), y0 = (h - alto) / 2 + 40;
        g.translate(x0, y0); ventana.paint(g); g.translate(-x0, -y0);
        g.dispose();
        return img;
    }

    /** Sin ventana real no se llama a addNotify, así que se instalan a mano los encabezados de las tablas. */
    static void prepararTablas(Component c) {
        if (c instanceof JScrollPane) {
            JScrollPane sp = (JScrollPane) c;
            if (sp.getViewport().getView() instanceof JTable) {
                sp.setColumnHeaderView(((JTable) sp.getViewport().getView()).getTableHeader());
            }
        }
        if (c instanceof Container) for (Component k : ((Container) c).getComponents()) prepararTablas(k);
    }

    static void layoutRec(Container c) {
        c.doLayout();
        for (Component k : c.getComponents()) if (k instanceof Container) layoutRec((Container) k);
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "true");
        Path salida = Path.of(args.length > 0 ? args[0] : "evidencias");
        Files.createDirectories(salida);
        try {
            for (UIManager.LookAndFeelInfo i : UIManager.getInstalledLookAndFeels())
                if ("Metal".equals(i.getName())) UIManager.setLookAndFeel(i.getClassName());
        } catch (Exception ignorada) { }

        List<Caso> casos = casos();
        int pasaron = 0;
        StringBuilder tsv = new StringBuilder();
        for (Caso c : casos) {
            c.ctx = new Ctx();
            try {
                c.obtenido = c.accion.apply(c.ctx);
            } catch (Exception e) {
                c.obtenido = "EXCEPCIÓN NO CONTROLADA: " + e;
            }
            c.paso = c.obtenido != null && c.obtenido.contains(c.claveOk);
            if (c.paso) pasaron++;
            System.out.printf("%-5s %-5s %-6s %s%n        -> %s%n", c.id, c.rf, c.paso ? "OK" : "FALLÓ", c.nombre, c.obtenido.replace("\n", " | "));
            String archivo = c.id.toLowerCase() + ".png";
            try {
                ImageIO.write(capturar(c), "png", salida.resolve(archivo).toFile());
            } catch (IOException | RuntimeException e) {
                System.err.println("No se pudo generar la captura de " + c.id + ": " + e);
            }
            tsv.append(String.join("\t", c.id, c.rf, c.nombre, c.descripcion, c.entrada, c.esperado,
                    c.obtenido.replace("\n", "\\n").replace("\t", " "), c.paso ? "OK" : "FALLÓ", archivo)).append('\n');
        }
        Files.writeString(salida.resolve("resultados.tsv"), tsv.toString());
        System.out.println("\nCasos superados: " + pasaron + " de " + casos.size());
        if (pasaron != casos.size()) System.exit(1);
    }
}
