import java.io.*;
import java.util.*;

class Personaje {
    private String nombre;
    private int salud;
    private int ataque;
    private int defensa;
    private int vida;

    public Personaje(String nombre, int salud, int ataque, int defensa) {
        this.nombre = nombre;
        this.salud = salud;
        this.ataque = ataque;
        this.defensa = defensa;
        this.vida = salud * 10; // Vida inicial es 10 veces la salud
    }
    public String getNombre() { return nombre; }
    public int getSalud() { return salud; }
    public int getAtaque() { return ataque; }
    public int getDefensa() { return defensa; }
    public int getVida() { return vida; }

    public void recibirDanio(int danio) {
        this.vida -= danio;
    }
    public void restaurarVida() {
        this.vida = this.salud * 10;
    }
    @Override
    public String toString() {
        return String.format("%-15s | %-6d | %-6d | %-6d | %-6d", nombre, salud, ataque, defensa, vida);
    }
}

public class LFPBattleMenu {
    private static List<Personaje> personajes = new ArrayList<>();
    private static List<Personaje> sobrevivientes = new ArrayList<>();
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
                    iniciarJuego();
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
        System.out.println(String.format("%-15s | %-6s | %-6s | %-6s | %-6s", "Nombre", "Salud", "Ataque", "Defensa", "Vida"));
        System.out.println("-------------------------------------------------------------");

        for (Personaje p : personajes) {
            System.out.println(p);
        }
    }

    private static void iniciarJuego() {
        if (personajes.isEmpty()) {
            System.out.println("No hay personajes cargados. Primero cargue un archivo.");
            return;
        }

        sobrevivientes = new ArrayList<>(personajes);
        while (sobrevivientes.size() > 1) {
            List<Personaje> ganadores = new ArrayList<>();
            for (int i = 0; i < sobrevivientes.size(); i += 2) {
                if (i + 1 < sobrevivientes.size()) {
                    Personaje ganador = combatir(sobrevivientes.get(i), sobrevivientes.get(i + 1));
                    ganador.restaurarVida(); // Restaurar vida al ganador
                    ganadores.add(ganador);
                } else {
                    sobrevivientes.get(i).restaurarVida(); // Restaurar vida al que avanza automáticamente
                    ganadores.add(sobrevivientes.get(i));
                }
            }
            sobrevivientes = ganadores;
        }

        System.out.println("El campeón del torneo es: " + sobrevivientes.get(0).getNombre());
    }

    private static Personaje combatir(Personaje p1, Personaje p2) {
        System.out.println("\nCombate entre " + p1.getNombre() + " y " + p2.getNombre());
        while (p1.getVida() > 0 && p2.getVida() > 0) {
            int danio1 = Math.max(0, p1.getAtaque() - p2.getDefensa());
            p2.recibirDanio(danio1);
            System.out.println(p1.getNombre() + " ataca a " + p2.getNombre() + " y causa " + danio1 + " de daño. Vida restante de " + p2.getNombre() + ": " + p2.getVida());

            if (p2.getVida() <= 0) break;

            int danio2 = Math.max(0, p2.getAtaque() - p1.getDefensa());
            p1.recibirDanio(danio2);
            System.out.println(p2.getNombre() + " ataca a " + p1.getNombre() + " y causa " + danio2 + " de daño. Vida restante de " + p1.getNombre() + ": " + p1.getVida());
        }

        Personaje ganador = p1.getVida() > 0 ? p1 : p2;
        System.out.println("El ganador es: " + ganador.getNombre());
        return ganador;
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
            writer.write("th { background-color:rgb(120, 118, 240); color: white; }");
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