import java.io.*;
import java.util.*;

class Personaje {
    private String nombre;
    private int salud;
    private int ataque;
    private int defensa;

    public Personaje(String nombre, int salud, int ataque, int defensa) {
        this.nombre = nombre;
        this.salud = salud;
        this.ataque = ataque;
        this.defensa = defensa;
    }

    public String getNombre() { return nombre; }
    public int getSalud() { return salud; }
    public int getAtaque() { return ataque; }
    public int getDefensa() { return defensa; }

    @Override
    public String toString() {
        return String.format("%-15s | %-6d | %-6d | %-6d", nombre, salud, ataque, defensa);
    }
}

public class LFPBattleMenu {
    private static List<Personaje> personajes = new ArrayList<>();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean ejecutando = true;

        while (ejecutando) {
            System.out.println("\n===== LFP Battle =====");
            System.out.println("1. Cargar archivo");
            System.out.println("2. Jugar");
            System.out.println("3. Generar reporte de mayor ataque");
            System.out.println("4. Generar reporte con mayor defensa");
            System.out.println("5. Mostrar información del desarrollador");
            System.out.println("6. Mostrar personajes cargados");
            System.out.println("7. Salir");
            System.out.print("Seleccione una opción: ");

            int opcion;
            try {
                opcion = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Por favor, ingrese un número válido.");
                continue;
            }

            switch (opcion) {
                case 1:
                    System.out.print("Ingrese la ruta del archivo .lfp: ");
                    String rutaArchivo = scanner.nextLine();
                    cargarArchivo(rutaArchivo);
                    break;
                case 2:
                    System.out.println("Iniciando juego...");
                    // Implementar lógica de juego
                    break;
                case 3:
                    generarReporte("ataque");
                    break;
                case 4:
                    generarReporte("defensa");
                    break;
                case 5:
                    System.out.println("Desarrollador: [Nombre del estudiante]");
                    System.out.println("Carnet: [Número de carnet]");
                    break;
                case 6:
                    mostrarPersonajes();
                    break;
                case 7:
                    System.out.println("Saliendo del juego...");
                    ejecutando = false;
                    break;
                default:
                    System.out.println("Opción no válida. Intente de nuevo.");
            }
        }
        scanner.close();
    }

    private static void cargarArchivo(String ruta) {
        personajes.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(ruta))) {
            String linea;
            boolean esPrimeraLinea = true;

            while ((linea = br.readLine()) != null) {
                if (esPrimeraLinea) {
                    esPrimeraLinea = false;
                    continue;
                }
                String[] datos = linea.split("\\|");
                if (datos.length == 4) {
                    String nombre = datos[0];
                    int salud = Integer.parseInt(datos[1]);
                    int ataque = Integer.parseInt(datos[2]);
                    int defensa = Integer.parseInt(datos[3]);
                    personajes.add(new Personaje(nombre, salud, ataque, defensa));
                }
            }
            System.out.println("Archivo cargado exitosamente. Se encontraron " + personajes.size() + " personajes.");
        } catch (IOException e) {
            System.out.println("Error al leer el archivo: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Error en el formato del archivo. Verifique los datos.");
        }
    }

    private static void mostrarPersonajes() {
        if (personajes.isEmpty()) {
            System.out.println("No hay personajes cargados. Primero cargue un archivo.");
            return;
        }

        System.out.println("\n===== Lista de Personajes =====");
        System.out.println(String.format("%-15s | %-6s | %-6s | %-6s", "Nombre", "Salud", "Ataque", "Defensa"));
        System.out.println("--------------------------------------------");

        for (Personaje p : personajes) {
            System.out.println(p);
        }
    }

    public static void generarReporte(String tipo) {
        if (personajes.isEmpty()) {
            System.out.println("No hay personajes cargados. Primero cargue un archivo.");
            return;
        }

        List<Personaje> top5;
        String titulo;
        String archivoSalida;

        if (tipo.equals("ataque")) {
            top5 = personajes.stream()
                    .sorted(Comparator.comparingInt(Personaje::getAtaque).reversed())
                    .limit(5)
                    .toList();
            titulo = "Top 5 - Mayor Ataque";
            archivoSalida = "reporte_ataque.html";
        } else {
            top5 = personajes.stream()
                    .sorted(Comparator.comparingInt(Personaje::getDefensa).reversed())
                    .limit(5)
                    .toList();
            titulo = "Top 5 - Mayor Defensa";
            archivoSalida = "reporte_defensa.html";
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(archivoSalida))) {
            writer.write("<html><head><title>" + titulo + "</title>");
            writer.write("<style>");
            writer.write("body { font-family: Arial, sans-serif; text-align: center; }");
            writer.write("table { width: 40%; margin: auto; border-collapse: collapse; }");
            writer.write("th, td { border: 1px solid black; padding: 10px; text-align: center; }");
            writer.write("th { background-color: #4CAF50; color: white; }");
            writer.write("</style></head><body>");
            writer.write("<h2>" + titulo + "</h2>");

            if (tipo.equals("ataque")) {
                writer.write("<table><tr><th>Nombre</th><th>Ataque</th></tr>");
                for (Personaje p : top5) {
                    writer.write("<tr><td>" + p.getNombre() + "</td><td>" + p.getAtaque() + "</td></tr>");
                }
            } else {
                writer.write("<table><tr><th>Nombre</th><th>Defensa</th></tr>");
                for (Personaje p : top5) {
                    writer.write("<tr><td>" + p.getNombre() + "</td><td>" + p.getDefensa() + "</td></tr>");
                }
            }

            writer.write("</table></body></html>");
            System.out.println("Reporte generado: " + archivoSalida);
        } catch (IOException e) {
            System.out.println("Error al generar el reporte: " + e.getMessage());
        }
    }
}
