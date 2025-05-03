package com.example;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

public class MainFrame extends JFrame {

    private JTextArea fileContentArea;
    private JLabel imageArea;
    private JTable errorTable;
    private DefaultTableModel errorTableModel;
    private JComboBox<String> worldSelector;
    private List<String> tokens;
    private List<String> errors;
    private List<String> worlds;
    
    // Estructuras para almacenar información del mundo
    private Map<String, List<Place>> worldPlaces = new HashMap<>();
    private Map<String, List<Connection>> worldConnections = new HashMap<>();
    private Map<String, List<WorldObject>> worldObjects = new HashMap<>();
    private String currentWorld = null;

    public MainFrame() {
        setTitle("Generador Visual de Mapas Narrativos");
        setSize(1000, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initComponents();
    }

    // Clases para almacenar los elementos del mundo
    private static class Place {
        String name;
        String type;
        int x, y;
        
        public Place(String name, String type, int x, int y) {
            this.name = name;
            this.type = type;
            this.x = x;
            this.y = y;
        }
    }

    private static class Connection {
        String from;
        String to;
        String type;
        
        public Connection(String from, String to, String type) {
            this.from = from;
            this.to = to;
            this.type = type;
        }
    }

    private static class WorldObject {
        String name;
        String type;
        String location; // Nombre del lugar o coordenadas
        
        public WorldObject(String name, String type, String location) {
            this.name = name;
            this.type = type;
            this.location = location;
        }
    }

    private void initComponents() {
        tokens = new ArrayList<>();
        errors = new ArrayList<>();
        worlds = new ArrayList<>();

        fileContentArea = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(fileContentArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Área de texto"));

        imageArea = new JLabel("Área de imagen", SwingConstants.CENTER);
        imageArea.setBorder(BorderFactory.createTitledBorder("Área de imagen"));

        errorTableModel = new DefaultTableModel(new String[]{"Error", "Tipo"}, 0);
        errorTable = new JTable(errorTableModel);
        JScrollPane errorScrollPane = new JScrollPane(errorTable);
        errorScrollPane.setBorder(BorderFactory.createTitledBorder("Errores"));

        JPanel rightPanel = new JPanel(new GridLayout(2, 1));
        rightPanel.add(imageArea);
        rightPanel.add(errorScrollPane);

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton reportButton = new JButton("Generar reportes");
        JButton graphButton = new JButton("Generar gráfico"); // Nuevo botón
        worldSelector = new JComboBox<>();
        topPanel.add(reportButton);
        topPanel.add(graphButton); // Agregar el nuevo botón
        topPanel.add(worldSelector);
        

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton loadButton = new JButton("Cargar archivo");
        JButton clearButton = new JButton("Limpiar área");
        JButton analyzeButton = new JButton("Analizar archivo");
        bottomPanel.add(loadButton);
        bottomPanel.add(clearButton);
        bottomPanel.add(analyzeButton);

        JPanel centerPanel = new JPanel(new GridLayout(1, 2));
        centerPanel.add(leftPanel);
        centerPanel.add(rightPanel);

        setLayout(new BorderLayout());
        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        loadButton.addActionListener(e -> cargarArchivo());
        clearButton.addActionListener(e -> fileContentArea.setText(""));
        analyzeButton.addActionListener(e -> analizarArchivo());
        reportButton.addActionListener(e -> generarReportes());
        graphButton.addActionListener(e -> {
            String selectedWorld = (String) worldSelector.getSelectedItem();
            if (selectedWorld != null) {
                generarGrafico(selectedWorld);
            } else {
                JOptionPane.showMessageDialog(this, "Por favor, selecciona un mundo.");
            }
        });
    }

    private void cargarArchivo() {
        JFileChooser chooser = new JFileChooser();
        int r = chooser.showOpenDialog(this);
        if (r == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                StringBuilder contenido = new StringBuilder();
                String linea;
                while ((linea = reader.readLine()) != null) {
                    contenido.append(linea).append("\n");
                }
                fileContentArea.setText(contenido.toString());
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }

    private void analizarArchivo() {
        tokens.clear();
        errors.clear();
        worlds.clear();
        worldSelector.removeAllItems();
        errorTableModel.setRowCount(0);
        
        // Limpiar las estructuras de datos para el mundo
        worldPlaces.clear();
        worldConnections.clear();
        worldObjects.clear();
        currentWorld = null;

        String content = fileContentArea.getText();
        int llavesAbiertas = 0;
        int parentesisAbiertos = 0;
        int length = content.length();
        StringBuilder currentToken = new StringBuilder();
        String validSymbols = "{}():,-";
        int line = 1;
        int column = 1;

        for (int i = 0; i < length; i++) {
            char c = content.charAt(i);

            if (c == '\n') {
                line++;
                column = 1;
                continue;
            }

            if (Character.isWhitespace(c)) {
                column++;
                continue;
            }

            if (validSymbols.indexOf(c) != -1) {
                tokens.add("SIMBOLO: " + c + " Línea: " + line + " Columna: " + column);
                if (c == '{') llavesAbiertas++;
                if (c == '}') llavesAbiertas--;
                if (c == '(') parentesisAbiertos++;
                if (c == ')') parentesisAbiertos--;
                column++;
            } else if (c == '\"') {
                int startCol = column;
                StringBuilder stringLiteral = new StringBuilder();
                i++;
                column++;
                while (i < length && content.charAt(i) != '\"') {
                    if (content.charAt(i) == '\n') {
                        agregarErrorLexico("cadena sin cerrar", line, column);
                        break;
                    }
                    stringLiteral.append(content.charAt(i));
                    i++;
                    column++;
                }
                if (i < length && content.charAt(i) == '\"') {
                    tokens.add("CADENA: \"" + stringLiteral + "\" Línea: " + line + " Columna: " + startCol);
                    if (tokens.size() > 1 && tokens.get(tokens.size() - 2).contains("IDENTIFICADOR: world")) {
                        worlds.add(stringLiteral.toString());
                        worldSelector.addItem(stringLiteral.toString());
                    }
                    column++;
                } else {
                    agregarErrorLexico("falta comilla de cierre para cadena", line, startCol);
                }
            } else if (Character.isLetter(c)) {
                int startCol = column;
                currentToken.append(c);
                while (i + 1 < length && (Character.isLetterOrDigit(content.charAt(i + 1)) || content.charAt(i + 1) == '_')) {
                    i++;
                    currentToken.append(content.charAt(i));
                    column++;
                }
                String token = currentToken.toString();
                tokens.add("IDENTIFICADOR: " + token + " Línea: " + line + " Columna: " + startCol);
                currentToken.setLength(0);
                column++;
            } else if (Character.isDigit(c)) {
                int startCol = column;
                currentToken.append(c);
                while (i + 1 < length && Character.isDigit(content.charAt(i + 1))) {
                    i++;
                    currentToken.append(content.charAt(i));
                    column++;
                }
                tokens.add("NUMERO: " + currentToken.toString() + " Línea: " + line + " Columna: " + startCol);
                currentToken.setLength(0);
                column++;
            } 
            // En el método analizarArchivo(), modifica la parte del análisis de dígitos para aceptar números negativos
            // Agrega este caso justo antes del bloque para Character.isDigit(c)
            else if (c == '-' && i + 1 < length && Character.isDigit(content.charAt(i + 1))) {
                int startCol = column;
                currentToken.append(c);
                i++;
                column++;
                currentToken.append(content.charAt(i));
                
                while (i + 1 < length && Character.isDigit(content.charAt(i + 1))) {
                    i++;
                    currentToken.append(content.charAt(i));
                    column++;
                }
                
                tokens.add("NUMERO: " + currentToken.toString() + " Línea: " + line + " Columna: " + startCol);
                currentToken.setLength(0);
                column++;
            }
            else {
                agregarErrorLexico("símbolo no permitido '" + c + "'", line, column);
                column++;
            }
        }

        // Verificar símbolos balanceados
        if (llavesAbiertas > 0)
            agregarErrorLexico("falta cerrar " + llavesAbiertas + " llave(s) '}'", -1, -1);
        else if (llavesAbiertas < 0)
            agregarErrorLexico("llave(s) '}' sin apertura correspondiente", -1, -1);

        if (parentesisAbiertos > 0)
            agregarErrorLexico("falta cerrar " + parentesisAbiertos + " paréntesis ')'", -1, -1);
        else if (parentesisAbiertos < 0)
            agregarErrorLexico("paréntesis ')' sin apertura correspondiente", -1, -1);

        analizarSintaxis();

        if (errors.isEmpty()) {
            imageArea.setText("Análisis léxico y sintáctico correctos.");
        } else {
            imageArea.setText("Errores encontrados.");
        }
    }

    private void analizarSintaxis() {
        int i = 0;
        int total = tokens.size();
    
        while (i < total) {
            String token = tokens.get(i);
    
            if (token.startsWith("IDENTIFICADOR: world")) {
                i++;
                if (i < total && tokens.get(i).startsWith("CADENA:")) {
                    String worldName = extraerCadena(tokens.get(i));
                    currentWorld = worldName;
                    
                    // Inicializar listas para este mundo
                    worldPlaces.put(currentWorld, new ArrayList<>());
                    worldConnections.put(currentWorld, new ArrayList<>());
                    worldObjects.put(currentWorld, new ArrayList<>());
                    
                    i++;
                    if (i < total && tokens.get(i).contains("SIMBOLO: {")) {
                        i++;
                        // Procesar instrucciones dentro del bloque
                        while (i < total && !tokens.get(i).contains("SIMBOLO: }")) {
                            String current = tokens.get(i);
    
                            if (current.startsWith("IDENTIFICADOR: place")) {
                                i = analizarPlace(i + 1);
                            } else if (current.startsWith("IDENTIFICADOR: connect")) {
                                i = analizarConnect(i + 1);
                            } else if (current.startsWith("IDENTIFICADOR: object")) {
                                i = analizarObject(i + 1);
                            } else {
                                agregarErrorSintactico("Instrucción no válida o inesperada: " + current);
                                break;
                            }
    
                            if (i == -1) break; // Error crítico, saltamos este bloque
                        }
    
                        if (i < total && tokens.get(i).contains("SIMBOLO: }")) {
                            // Fin del bloque
                            i++;
                            // Después de cerrar un bloque, si hay una coma, la saltamos
                            if (i < total && tokens.get(i).contains("SIMBOLO: ,")) {
                                i++;
                            }
                        } else {
                            agregarErrorSintactico("Se esperaba '}' para cerrar el bloque world");
                        }
                    } else {
                        agregarErrorSintactico("Se esperaba '{' después del nombre del mundo");
                    }
                } else {
                    agregarErrorSintactico("Se esperaba una cadena con el nombre del mundo después de 'world'");
                }
            } else {
                // Si no es un bloque world, pasamos al siguiente token
                i++;
            }
            // Quitamos el break para que continue analizando más bloques world
        }
    }
    
    private int analizarPlace(int i) {
        if (i + 6 >= tokens.size()) {
            agregarErrorSintactico("Estructura incompleta en 'place'");
            return -1;
        }
    
        if (!tokens.get(i).startsWith("IDENTIFICADOR:")) {
            agregarErrorSintactico("Se esperaba nombre del lugar después de 'place'");
            return -1;
        }
        if (!tokens.get(i + 1).contains("SIMBOLO: :")) {
            agregarErrorSintactico("Se esperaba ':' después del nombre del lugar");
            return -1;
        }
        if (!tokens.get(i + 2).startsWith("IDENTIFICADOR:")) {
            agregarErrorSintactico("Se esperaba tipo del lugar después de ':'");
            return -1;
        }
        if (!tokens.get(i + 3).startsWith("IDENTIFICADOR: at")) {
            agregarErrorSintactico("Se esperaba 'at' después del tipo del lugar");
            return -1;
        }
        if (!tokens.get(i + 4).contains("SIMBOLO: (")) {
            agregarErrorSintactico("Se esperaba '(' en coordenadas");
            return -1;
        }
        if (!tokens.get(i + 5).startsWith("NUMERO:")) {
            agregarErrorSintactico("Se esperaba número X en coordenadas");
            return -1;
        }
        if (!tokens.get(i + 6).contains("SIMBOLO: ,")) {
            agregarErrorSintactico("Se esperaba ',' entre coordenadas");
            return -1;
        }
        if (!tokens.get(i + 7).startsWith("NUMERO:")) {
            agregarErrorSintactico("Se esperaba número Y en coordenadas");
            return -1;
        }
        if (!tokens.get(i + 8).contains("SIMBOLO: )")) {
            agregarErrorSintactico("Se esperaba ')' para cerrar coordenadas");
            return -1;
        }
        
        // Extraer la información del lugar
        String placeName = extraerIdentificador(tokens.get(i));
        String placeType = extraerIdentificador(tokens.get(i + 2));
        int x = Integer.parseInt(extraerNumero(tokens.get(i + 5)));
        int y = Integer.parseInt(extraerNumero(tokens.get(i + 7)));
        
        if (currentWorld != null) {
            worldPlaces.get(currentWorld).add(new Place(placeName, placeType, x, y));
        }
    
        return i + 9;
    }
    
    private int analizarConnect(int i) {
        if (i + 4 >= tokens.size()) {
            agregarErrorSintactico("Estructura incompleta en 'connect'");
            return -1;
        }
    
        if (!tokens.get(i).startsWith("IDENTIFICADOR:")) {
            agregarErrorSintactico("Se esperaba lugar A después de 'connect'");
            return -1;
        }
        if (!tokens.get(i + 1).startsWith("IDENTIFICADOR: to")) {
            agregarErrorSintactico("Se esperaba 'to' después del lugar A");
            return -1;
        }
        if (!tokens.get(i + 2).startsWith("IDENTIFICADOR:")) {
            agregarErrorSintactico("Se esperaba lugar B después de 'to'");
            return -1;
        }
        if (!tokens.get(i + 3).startsWith("IDENTIFICADOR: with")) {
            agregarErrorSintactico("Se esperaba 'with' después del lugar B");
            return -1;
        }
        if (!tokens.get(i + 4).startsWith("CADENA:")) {
            agregarErrorSintactico("Se esperaba tipo de conexión entre comillas");
            return -1;
        }
        
        // Extraer la información de la conexión
        String fromPlace = extraerIdentificador(tokens.get(i));
        String toPlace = extraerIdentificador(tokens.get(i + 2));
        String connectionType = extraerCadena(tokens.get(i + 4));
        
        if (currentWorld != null) {
            worldConnections.get(currentWorld).add(
                new Connection(fromPlace, toPlace, connectionType));
        }
    
        return i + 5;
    }
    
    private int analizarObject(int i) {
        if (i + 5 >= tokens.size()) {
            agregarErrorSintactico("Estructura incompleta en 'object'");
            return -1;
        }
    
        if (!tokens.get(i).startsWith("CADENA:")) {
            agregarErrorSintactico("Se esperaba nombre del objeto en comillas");
            return -1;
        }
        if (!tokens.get(i + 1).contains("SIMBOLO: :")) {
            agregarErrorSintactico("Se esperaba ':' después del nombre del objeto");
            return -1;
        }
        if (!tokens.get(i + 2).startsWith("IDENTIFICADOR:")) {
            agregarErrorSintactico("Se esperaba tipo del objeto");
            return -1;
        }
        if (!tokens.get(i + 3).startsWith("IDENTIFICADOR: at")) {
            agregarErrorSintactico("Se esperaba 'at' después del tipo");
            return -1;
        }
        
        // Extraer la información del objeto
        String objectName = extraerCadena(tokens.get(i));
        String objectType = extraerIdentificador(tokens.get(i + 2));
        String location;
    
        // caso A: at IDENTIFICADOR
        if (tokens.get(i + 4).startsWith("IDENTIFICADOR:")) {
            location = extraerIdentificador(tokens.get(i + 4));
            
            if (currentWorld != null) {
                worldObjects.get(currentWorld).add(
                    new WorldObject(objectName, objectType, location));
            }
            
            return i + 5;
        }
    
        // caso B: at (NUM , NUM)
        if (tokens.get(i + 4).contains("SIMBOLO: (") &&
            tokens.get(i + 5).startsWith("NUMERO:") &&
            tokens.get(i + 6).contains("SIMBOLO: ,") &&
            tokens.get(i + 7).startsWith("NUMERO:") &&
            tokens.get(i + 8).contains("SIMBOLO: )")) {
            
            int x = Integer.parseInt(extraerNumero(tokens.get(i + 5)));
            int y = Integer.parseInt(extraerNumero(tokens.get(i + 7)));
            location = x + "," + y;
            
            if (currentWorld != null) {
                worldObjects.get(currentWorld).add(
                    new WorldObject(objectName, objectType, location));
            }
            
            return i + 9;
        }
    
        agregarErrorSintactico("Estructura inválida para ubicación del objeto");
        return -1;
    }

    private void agregarErrorLexico(String mensaje, int linea, int columna) {
        String info = (linea == -1) ? "Línea: - Columna: -" : "Línea: " + linea + " Columna: " + columna;
        errors.add("Error léxico: " + mensaje + " " + info);
        errorTableModel.addRow(new String[]{mensaje + " " + info, "Léxico"});
    }

    private void agregarErrorSintactico(String mensaje) {
        // Intentar determinar la línea y columna del token actual
        String info = "";
        if (!tokens.isEmpty()) {
            // Obtener la última posición conocida del análisis
            String lastToken = tokens.get(Math.min(tokens.size() - 1, Math.max(0, tokens.size() - 1)));
            String[] parts = lastToken.split(" Línea:");
            if (parts.length > 1) {
                info = " Línea:" + parts[1];
            }
        }
        
        errors.add("Error sintáctico: " + mensaje + info);
        errorTableModel.addRow(new String[]{mensaje + info, "Sintáctico"});
    }

    private void generarReportes() {
        try (PrintWriter writer = new PrintWriter("tokens.html")) {
            writer.println("<html><head><title>Tokens</title></head><body><table border='1'><tr><th>Token</th><th>Detalle</th></tr>");
            for (String token : tokens) {
                String[] parts = token.split(" Línea:");
                writer.printf("<tr><td>%s</td><td>%s</td></tr>\n", parts[0], parts.length > 1 ? parts[1] : "");
            }
            writer.println("</table></body></html>");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al generar tokens.html");
        }

        try (PrintWriter writer = new PrintWriter("errores.html")) {
            writer.println("<html><head><title>Errores</title><style>table { border-collapse: collapse; width: 100%; } th, td { border: 1px solid #ddd; padding: 8px; text-align: left; } th { background-color: #f2f2f2; }</style></head><body>");
            writer.println("<h2>Reporte de Errores</h2>");
            writer.println("<table><tr><th>Error</th><th>Tipo</th><th>Ubicación</th></tr>");
            
            for (String error : errors) {
                String tipo = error.contains("léxico") ? "Léxico" : "Sintáctico";
                String ubicacion = "No disponible";
                
                // Extraer la ubicación (línea y columna)
                if (error.contains("Línea:")) {
                    int lineaIndex = error.indexOf("Línea:");
                    ubicacion = error.substring(lineaIndex);
                }
                
                // Extraer el mensaje de error sin la información de ubicación
                String msg = error;
                if (error.contains("Línea:")) {
                    msg = error.substring(0, error.indexOf("Línea:"));
                }
                msg = msg.replace("Error léxico: ", "").replace("Error sintáctico: ", "").trim();
                
                writer.printf("<tr><td>%s</td><td>%s</td><td>%s</td></tr>\n", msg, tipo, ubicacion);
            }
            
            writer.println("</table></body></html>");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al generar errores.html");
        }

        JOptionPane.showMessageDialog(this, "Archivos de reporte generados correctamente.");
    }
    
    private String normalizarTipo(String tipo) {
        // Convertir a minúsculas y normalizar caracteres especiales
        return tipo.toLowerCase()
            .replace("á", "a")
            .replace("é", "e")
            .replace("í", "i")
            .replace("ó", "o")
            .replace("ú", "u")
            .replace("-", " ");
    }

    private void generarGrafico(String worldName) {
        if (!worldPlaces.containsKey(worldName) || worldPlaces.get(worldName).isEmpty()) {
            JOptionPane.showMessageDialog(this, "El mundo seleccionado no tiene elementos para graficar.");
            return;
        }
        
        try {
            // Generar archivo DOT
            StringBuilder dot = new StringBuilder();
            dot.append("digraph \"").append(worldName).append("\" {\n");
            dot.append("  rankdir=LR;\n");
            dot.append("  node [fontname=\"Arial\", fontsize=12];\n");
            dot.append("  edge [fontname=\"Arial\", fontsize=10];\n");
            
            // Agregar nodos (lugares)
            int minX = Integer.MAX_VALUE;
            int minY = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE;
            int maxY = Integer.MIN_VALUE;

            // Primero, determinar los límites del plano
            for (Place place : worldPlaces.get(worldName)) {
                minX = Math.min(minX, place.x);
                minY = Math.min(minY, place.y);
                maxX = Math.max(maxX, place.x);
                maxY = Math.max(maxY, place.y);
            }

            // Calcular el desplazamiento para evitar coordenadas negativas en Graphviz
            int offsetX = minX < 0 ? -minX : 0;
            int offsetY = minY < 0 ? -minY : 0;

            // Agregar nodos con coordenadas ajustadas
            for (Place place : worldPlaces.get(worldName)) {
                String normalizedType = normalizarTipo(place.type);
                String[] style = tiposLugar.getOrDefault(normalizedType, new String[]{"ellipse", "gray"});
                
                // Ajustar coordenadas para el plano de Graphviz (donde Y crece hacia abajo)
                int adjustedX = place.x + offsetX;
                int adjustedY = maxY - place.y + offsetY; // Invertir Y para que crezca hacia abajo
                
                dot.append("  \"").append(place.name).append("\" [");
                dot.append("shape=").append(style[0]).append(", ");
                dot.append("fillcolor=\"").append(style[1]).append("\", ");
                dot.append("style=filled, ");
                dot.append("label=\"").append(place.name).append("\\n(").append(place.type).append(")\", ");
                dot.append("pos=\"").append(adjustedX).append(",").append(adjustedY).append("!\"];\n");
            }
            
            // Agregar conexiones
            if (worldConnections.containsKey(worldName) && worldConnections.get(worldName) != null) {
                for (Connection conn : worldConnections.get(worldName)) {
                    String normalizedType = normalizarTipo(conn.type);
                    String[] style = tiposConexion.getOrDefault(normalizedType, new String[]{"solid", "black"});
                    dot.append("  \"").append(conn.from).append("\" -> \"").append(conn.to).append("\" [");
                    dot.append("label=\"").append(conn.type).append("\", ");
                    dot.append("style=").append(style[0]).append(", ");
                    dot.append("color=\"").append(style[1]).append("\"];\n");
                }
            }
            
            // Agregar objetos
            if (worldObjects.containsKey(worldName) && worldObjects.get(worldName) != null) {
                for (WorldObject obj : worldObjects.get(worldName)) {
                    String normalizedType = normalizarTipo(obj.type);
                    String[] style = tiposObjeto.getOrDefault(normalizedType, new String[]{"box", "yellow"});
                    String objNodeName = "obj_" + obj.name.replaceAll("\\s+", "_");
                    
                    dot.append("  \"").append(objNodeName).append("\" [");
                    dot.append("shape=").append(style[0]).append(", ");
                    dot.append("fillcolor=\"").append(style[1]).append("\", ");
                    dot.append("style=filled, ");
                    dot.append("label=\"").append(obj.name).append("\\n(").append(obj.type).append(")\"];\n");
                    
                    // Si el objeto está ubicado en un lugar, conectarlo con una línea punteada
                    if (!obj.location.contains(",")) { // No es coordenada, es un lugar
                        dot.append("  \"").append(objNodeName).append("\" -> \"").append(obj.location)
                           .append("\" [style=dashed, dir=none, color=gray];\n");
                    }
                }
            }
            
            dot.append("}\n");
            
            // Guardar el archivo DOT
            File dotFile = new File("graph_" + worldName + ".dot");
            try (PrintWriter writer = new PrintWriter(dotFile)) {
                writer.print(dot.toString());
            }
            
            // Ejecutar Graphviz para generar la imagen
            ProcessBuilder pb = new ProcessBuilder("dot", "-Tpng", dotFile.getAbsolutePath(), "-o", 
                                                 "graph_" + worldName + ".png");
            Process process = pb.start();
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                // Mostrar la imagen generada
                File imgFile = new File("graph_" + worldName + ".png");
                if (imgFile.exists()) {
                    imageArea.setIcon(new ImageIcon(imgFile.getAbsolutePath()));
                    imageArea.setText("");
                    JOptionPane.showMessageDialog(this, "Gráfico generado con éxito.");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Error al generar el gráfico. Asegúrate de tener Graphviz instalado.");
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al generar el gráfico: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Métodos auxiliares para extraer información de los tokens
    private String extraerCadena(String token) {
        // Extraer el contenido entre comillas
        int inicioComillas = token.indexOf('"');
        int finComillas = token.lastIndexOf('"');
        if (inicioComillas != -1 && finComillas != -1 && inicioComillas != finComillas) {
            return token.substring(inicioComillas + 1, finComillas);
        }
        return "";
    }

    private String extraerIdentificador(String token) {
        // Extraer el identificador después de "IDENTIFICADOR: "
        if (token.startsWith("IDENTIFICADOR: ")) {
            String[] parts = token.split(" Línea:");
            return parts[0].substring("IDENTIFICADOR: ".length());
        }
        return "";
    }

    private String extraerNumero(String token) {
        // Extraer el número después de "NUMERO: "
        if (token.startsWith("NUMERO: ")) {
            String[] parts = token.split(" Línea:");
            return parts[0].substring("NUMERO: ".length());
        }
        return "0";
    }
        
    // Mapeos de tipos quemados
    Map<String, String[]> tiposLugar = new HashMap<>() {{
        put("playa",     new String[]{"ellipse", "lightblue"});
        put("cueva",     new String[]{"box", "gray"});
        put("templo",    new String[]{"octagon", "gold"});
        put("jungla",    new String[]{"parallelogram", "forestgreen"});
        put("montaña",   new String[]{"triangle", "sienna"});
        put("montana",   new String[]{"triangle", "sienna"});     // Versión sin tilde
        put("pueblo",    new String[]{"house", "burlywood"});
        put("isla",      new String[]{"invtriangle", "lightgoldenrod"});
        put("río",       new String[]{"hexagon", "deepskyblue"});
        put("rio",       new String[]{"hexagon", "deepskyblue"}); // Versión sin tilde
        put("volcán",    new String[]{"doublecircle", "orangered"});
        put("volcan",    new String[]{"doublecircle", "orangered"}); // Versión sin tilde
        put("pantano",   new String[]{"trapezium", "darkseagreen"});
    }};

    Map<String, String[]> tiposObjeto = new HashMap<>() {{
        put("tesoro",        new String[]{"box3d", "gold"});
        put("llave",         new String[]{"pentagon", "lightsteelblue"});
        put("arma",          new String[]{"diamond", "orangered"});
        put("objeto mágico", new String[]{"component", "violet"});
        put("objeto magico", new String[]{"component", "violet"}); // Versión sin tilde
        put("poción",        new String[]{"cylinder", "plum"});
        put("pocion",        new String[]{"cylinder", "plum"});    // Versión sin tilde
        put("trampa",        new String[]{"hexagon", "crimson"});
        put("libro",         new String[]{"note", "navajowhite"});
        put("herramienta",   new String[]{"folder", "darkkhaki"});
        put("bandera",       new String[]{"tab", "white"});
        put("gema",          new String[]{"egg", "deepskyblue"});
    }};

    Map<String, String[]> tiposConexion = new HashMap<>() {{
        put("camino",     new String[]{"solid", "black"});
        put("puente",     new String[]{"dotted", "gray"});
        put("sendero",    new String[]{"dashed", "saddlebrown"});
        put("carretera",  new String[]{"solid", "darkgray"});
        put("nado",       new String[]{"dashed", "deepskyblue"});
        put("lancha",     new String[]{"solid", "blue"});
        put("teleférico", new String[]{"dotted", "purple"});
        put("teleferico", new String[]{"dotted", "purple"});      // Versión sin tilde
    }};

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}