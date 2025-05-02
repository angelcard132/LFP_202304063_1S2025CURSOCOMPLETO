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

    public MainFrame() {
        setTitle("Generador Visual de Mapas Narrativos");
        setSize(1000, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initComponents();
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
        worldSelector = new JComboBox<>();
        topPanel.add(reportButton);
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
            } else {
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
    
                            if (i == -1) break; // Error crítico, salimos
                        }
    
                        if (i < total && tokens.get(i).contains("SIMBOLO: }")) {
                            // Fin del bloque
                            i++;
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
                agregarErrorSintactico("Se esperaba 'world' al inicio del archivo");
            }
    
            break; // solo analizamos un bloque world por ahora
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
    
        // caso A: at IDENTIFICADOR
        if (tokens.get(i + 4).startsWith("IDENTIFICADOR:")) {
            return i + 5;
        }
    
        // caso B: at (NUM , NUM)
        if (tokens.get(i + 4).contains("SIMBOLO: (") &&
            tokens.get(i + 5).startsWith("NUMERO:") &&
            tokens.get(i + 6).contains("SIMBOLO: ,") &&
            tokens.get(i + 7).startsWith("NUMERO:") &&
            tokens.get(i + 8).contains("SIMBOLO: )")) {
            return i + 9;
        }
    
        agregarErrorSintactico("Estructura inválida para ubicación del objeto");
        return -1;
    }

    private void agregarErrorLexico(String mensaje, int linea, int columna) {
        String info = (linea == -1) ? "Línea: - Columna: -" : "Línea: " + linea + " Columna: " + columna;
        errors.add("Error léxico: " + mensaje + " " + info);
        errorTableModel.addRow(new String[]{mensaje, "Léxico"});
    }

    private void agregarErrorSintactico(String mensaje) {
        errors.add("Error sintáctico: " + mensaje + " Línea: - Columna: -");
        errorTableModel.addRow(new String[]{mensaje, "Sintáctico"});
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
            writer.println("<html><head><title>Errores</title></head><body><table border='1'><tr><th>Error</th><th>Tipo</th></tr>");
            for (String error : errors) {
                String tipo = error.contains("léxico") ? "Léxico" : "Sintáctico";
                String msg = error.split(" Línea")[0].replace("Error léxico: ", "").replace("Error sintáctico: ", "");
                writer.printf("<tr><td>%s</td><td>%s</td></tr>\n", msg, tipo);
            }
            writer.println("</table></body></html>");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al generar errores.html");
        }

        JOptionPane.showMessageDialog(this, "Archivos de reporte generados correctamente.");
    }
        // Mapeos de tipos quemados
    Map<String, String[]> tiposLugar = new HashMap<>() {{
        put("playa",     new String[]{"ellipse", "lightblue"});
        put("cueva",     new String[]{"box", "gray"});
        put("templo",    new String[]{"octagon", "gold"});
        put("jungla",    new String[]{"parallelogram", "forestgreen"});
        put("montaña",   new String[]{"triangle", "sienna"});
        put("pueblo",    new String[]{"house", "burlywood"});
        put("isla",      new String[]{"invtriangle", "lightgoldenrod"});
        put("río",       new String[]{"hexagon", "deepskyblue"});
        put("volcán",    new String[]{"doublecircle", "orangered"});
        put("pantano",   new String[]{"trapezium", "darkseagreen"});
    }};

    Map<String, String[]> tiposObjeto = new HashMap<>() {{
        put("tesoro",        new String[]{"box3d", "gold"});
        put("llave",         new String[]{"pentagon", "lightsteelblue"});
        put("arma",          new String[]{"diamond", "orangered"});
        put("objeto mágico", new String[]{"component", "violet"});
        put("poción",        new String[]{"cylinder", "plum"});
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
    }};

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
