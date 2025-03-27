package com.example;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

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
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

public class MainFrame extends JFrame {
    private JComboBox<String> automataComboBox;
    private JButton loadFileButton, graphButton, tokenReportButton, errorReportButton;
    private JTextArea fileContentArea, errorArea;
     private JTable tokenTable;
    private DefaultTableModel tokenTableModel, errorTableModel;
    private LexicalAnalyzer analyzer; // Variable de instancia para el analizador léxico


    
    public MainFrame() {
        setTitle("AFD Graph - Visualización de Autómatas");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initUI();
        
    }
    
    private void initUI() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        
        // Panel superior con botones
        JPanel topPanel = new JPanel();
        loadFileButton = new JButton("Cargar Archivo .lfp");
        automataComboBox = new JComboBox<>();
        graphButton = new JButton("Graficar");
        
        loadFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cargarArchivo();
            }
        });


        
        graphButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                analizarContenido();
            }
        });
        
        topPanel.add(loadFileButton);
        topPanel.add(automataComboBox);
        topPanel.add(graphButton);

        // Panel inferior con botones de reportes
        JPanel bottomPanel = new JPanel();
        tokenReportButton = new JButton("Reporte de Tokens");
        errorReportButton = new JButton("Reporte de Errores");

         // Asignar funcionalidad al botón "Reporte de Tokens"
         tokenReportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generarArchivoHTML();
            }
        });

        // Asignar funcionalidad al botón "Reporte de Errores"
        errorReportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generarArchivoErroresHTML();
            }
        });

        bottomPanel.add(tokenReportButton);
        bottomPanel.add(errorReportButton);

        // Áreas de texto para mostrar contenido del archivo y errores
        fileContentArea = new JTextArea(15, 50);
        fileContentArea.setEditable(false);
        JScrollPane fileScrollPane = new JScrollPane(fileContentArea);

        errorArea = new JTextArea(10, 50);
        errorArea.setEditable(false);
        JScrollPane errorScrollPanel = new JScrollPane(errorArea);

         // Tabla para mostrar tokens
       tokenTableModel = new DefaultTableModel(new String[]{"Tipo de Token", "Valor"}, 0);
       tokenTable = new JTable(tokenTableModel);
        JScrollPane tokenScrollPane = new JScrollPane(tokenTable);
        
        // Tabla para mostrar errores
    errorTableModel = new DefaultTableModel(new String[]{"Error", "Descripción"}, 0);
    JTable errorTable = new JTable(errorTableModel);
    JScrollPane errorScrollPane = new JScrollPane(errorTable);


        // Panel central para las áreas de texto
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new GridLayout(3, 1)); // Cambiar a 3 filas
        centerPanel.add(new JLabel("Contenido del Archivo:"));
        centerPanel.add(fileScrollPane);
        centerPanel.add(new JLabel("Errores Léxicos:"));
        centerPanel.add(errorScrollPane); // Mostrar tabla de errores
        centerPanel.add(new JLabel("Tokens Encontrados:"));
        centerPanel.add(tokenScrollPane);

        // Agregar paneles a la ventana
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(centerPanel, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        add(panel);
    }

    
    
    private void cargarArchivo() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int resultado = fileChooser.showOpenDialog(this);

        if (resultado == JFileChooser.APPROVE_OPTION) {
            File archivoSeleccionado = fileChooser.getSelectedFile();

            try {
                // Leer el contenido del archivo
                StringBuilder content = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new FileReader(archivoSeleccionado))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        content.append(line).append("\n");
                    }
                }

                // Mostrar el contenido del archivo en el área de texto
                fileContentArea.setText(content.toString());

                // Procesar el contenido para llenar el JComboBox
                analyzer = new LexicalAnalyzer(); // Crear una nueva instancia del analizador
                analyzer.analyze(content.toString());

                // Limpiar el JComboBox y agregar los nombres de los autómatas
                automataComboBox.removeAllItems();
                for (LexicalAnalyzer.Automaton automaton : analyzer.getAutomata()) {
                    automataComboBox.addItem(automaton.getNombre());
                }

                // Mostrar el primer autómata en el área de texto
                if (automataComboBox.getItemCount() > 0) {
                    automataComboBox.setSelectedIndex(0);
                    mostrarAutomataSeleccionado();
                }

                // Mostrar mensaje de éxito después de llenar el JComboBox
                JOptionPane.showMessageDialog(this, "Archivo cargado: " + archivoSeleccionado.getName(), "Éxito", JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al leer el archivo: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void mostrarAutomataSeleccionado() {
        String nombreSeleccionado = (String) automataComboBox.getSelectedItem();
        if (nombreSeleccionado != null && analyzer != null) {
            for (LexicalAnalyzer.Automaton automaton : analyzer.getAutomata()) {
                if (automaton.getNombre().equals(nombreSeleccionado)) {
                    fileContentArea.setText(automaton.toString());
                    break;
                }
            }
        }
    }

    private void analizarContenido() {
        String content = fileContentArea.getText();

        if (content.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El área de texto está vacía. Por favor, ingrese contenido para analizar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Analizar el contenido
        LexicalAnalyzer analyzer = new LexicalAnalyzer();
        analyzer.analyze(content);

        // Mostrar errores léxicos en el área de texto
        if (analyzer.getErrors().isEmpty()) {
            errorArea.setText("Análisis léxico completado correctamente. No se encontraron errores.");
        } else {
            StringBuilder errorText = new StringBuilder();
            for (String error : analyzer.getErrors()) {
                errorText.append(error).append("\n");
            }
            errorArea.setText(errorText.toString());
        }

        errorTableModel.setRowCount(0); // Limpiar la tabla de errores
        for (String error : analyzer.getErrors()) {
            String[] errorParts = error.split(": ", 2);
            errorTableModel.addRow(errorParts);
        }

        // Mostrar tokens en la tabla
        tokenTableModel.setRowCount(0); // Limpiar la tabla
        for (String token : analyzer.getTokens()) {
            String[] tokenParts = token.split(": ", 2);
            tokenTableModel.addRow(tokenParts);
        }
    }

     private void generarArchivoHTML() {
        try {
            File htmlFile = new File("tokens.html");
            try (PrintWriter writer = new PrintWriter(htmlFile)) {
                writer.println("<html>");
                writer.println("<head><title>Reporte de Tokens</title></head>");
                writer.println("<body>");
                writer.println("<h1>Reporte de Tokens</h1>");
                writer.println("<table border='1'>");
                writer.println("<tr><th>Tipo de Token</th><th>Valor</th></tr>");

                for (int i = 0; i < tokenTableModel.getRowCount(); i++) {
                    String tipo = tokenTableModel.getValueAt(i, 0).toString();
                    String valor = tokenTableModel.getValueAt(i, 1).toString();
                    writer.println("<tr><td>" + tipo + "</td><td>" + valor + "</td></tr>");
                }

                writer.println("</table>");
                writer.println("</body>");
                writer.println("</html>");
            }

            JOptionPane.showMessageDialog(this, "Archivo HTML generado exitosamente: " + htmlFile.getAbsolutePath(), "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al generar el archivo HTML: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void generarArchivoErroresHTML() {
        try {
            File htmlFile = new File("errores.html");
            try (PrintWriter writer = new PrintWriter(htmlFile)) {
                writer.println("<html>");
                writer.println("<head><title>Reporte de Errores</title></head>");
                writer.println("<body>");
                writer.println("<h1>Reporte de Errores</h1>");
                writer.println("<table border='1'>");
                writer.println("<tr><th>Error</th><th>Descripción</th></tr>");
    
                for (int i = 0; i < errorTableModel.getRowCount(); i++) {
                    String error = errorTableModel.getValueAt(i, 0).toString();
                    String descripcion = errorTableModel.getValueAt(i, 1).toString();
                    writer.println("<tr><td>" + error + "</td><td>" + descripcion + "</td></tr>");
                }
    
                writer.println("</table>");
                writer.println("</body>");
                writer.println("</html>");
            }
    
            JOptionPane.showMessageDialog(this, "Archivo HTML de errores generado exitosamente: " + htmlFile.getAbsolutePath(), "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al generar el archivo HTML de errores: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
 

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }

    public class LexicalAnalyzer {
        private List<String> tokens;
        private List<String> errors;
        private List<Automaton> automata; // Lista para almacenar los autómatas
    
        public LexicalAnalyzer() {
            tokens = new ArrayList<>();
            errors = new ArrayList<>();
            automata = new ArrayList<>();
        }
        
        

        public List<Automaton> getAutomata() {
            return automata;
        }
    
        public void analyze(String content) {
            tokens.clear();
            errors.clear();
            automata.clear();
        
            int length = content.length();
            StringBuilder currentToken = new StringBuilder();
            int openBraces = 0; // Contador para '{'
        
            Automaton currentAutomaton = null;
        
            // Conjunto de símbolos válidos
            String validSymbols = "{}(),\":";
        
            for (int i = 0; i < length; i++) {
                char c = content.charAt(i);
        
                if (Character.isWhitespace(c)) {
                    continue; // Ignorar espacios en blanco
                } else if (validSymbols.indexOf(c) != -1) {
                    // Símbolo válido
                    tokens.add("SIMBOLO: " + c);
        
                    if (c == '{') {
                        openBraces++;
                        if (currentAutomaton == null) {
                            currentAutomaton = new Automaton();
                        }
                    } else if (c == '}') {
                        if (openBraces > 0) {
                            openBraces--;
                            if (openBraces == 0 && currentAutomaton != null) {
                                automata.add(currentAutomaton);
                                currentAutomaton = null;
                            }
                        } else {
                            errors.add("Error: Símbolo '}' no tiene un '{' correspondiente en posición " + i);
                        }
                    }
                } else if (Character.isLetter(c)) {
                    // Procesar identificadores
                    currentToken.append(c);
                    while (i + 1 < length && (Character.isLetterOrDigit(content.charAt(i + 1)) || content.charAt(i + 1) == '_')) {
                        i++;
                        currentToken.append(content.charAt(i));
                    }
                    String token = currentToken.toString();
                    tokens.add("IDENTIFICADOR: " + token);
        
                    // Manejar palabras clave dentro del autómata actual
                    if (currentAutomaton != null) {
                        if (token.equals("descripcion")) {
                            currentAutomaton.setDescripcion(extractValue(content, i + 1));
                        } else if (token.equals("estados")) {
                            currentAutomaton.setEstados(extractList(content, i + 1));
                        } else if (token.equals("alfabeto")) {
                            currentAutomaton.setAlfabeto(extractList(content, i + 1));
                        } else if (token.equals("inicial")) {
                            currentAutomaton.setInicial(extractValue(content, i + 1));
                        } else if (token.equals("finales")) {
                            currentAutomaton.setFinales(extractList(content, i + 1));
                        } else if (token.equals("transiciones")) {
                            currentAutomaton.setTransiciones(extractTransitions(content, i + 1));
                        }
                    }
        
                    currentToken.setLength(0);
                } else if (Character.isDigit(c)) {
                    // Procesar números
                    currentToken.append(c);
                    while (i + 1 < length && Character.isDigit(content.charAt(i + 1))) {
                        i++;
                        currentToken.append(content.charAt(i));
                    }
                    tokens.add("NUMERO: " + currentToken.toString());
                    currentToken.setLength(0);
                } else if (c == '$' || c == '+' || c == '/' || c == '@' || c== '?' || c == '!' || c == '%' || c == '&' || c == '#' || c == '*' || c == '^' || c == '~' || c == '`' || c == '|' || c == '\\' || c == '¿') {  
                    // Detectar símbolos no permitidos específicos
                    errors.add("Error: Símbolo no permitido '" + c + "' en posición " + i);
                } else {
                    // Detectar cualquier otro carácter no válido
                    tokens.add("SIMBOLO: " + c); // Considerar como símbolo válido
                }
            }
        
            // Verificar si hay llaves sin cerrar
            if (openBraces > 0) {
                errors.add("Error: Falta cerrar " + openBraces + " llave(s) '}'");
            }
        
            // Validar cada autómata
            for (Automaton automaton : automata) {
                automaton.validate(errors);
            }
        }

        private String extractValue(String content, int startIndex) {
            int start = content.indexOf(":", startIndex) + 1;
            int end = content.indexOf(",", start);
            if (start > 0 && end > start) {
                return content.substring(start, end).trim().replace("\"", "");
            }
            errors.add("Error: Valor no válido para definición en posición " + startIndex);
            return null;
        }
    
        private List<String> extractList(String content, int startIndex) {
            int start = content.indexOf("[", startIndex) + 1;
            int end = content.indexOf("]", start);
            if (start > 0 && end > start) {
                String[] items = content.substring(start, end).split(",");
                List<String> list = new ArrayList<>();
                for (String item : items) {
                    // Eliminar comillas y espacios en blanco
                    list.add(item.trim().replace("\"", ""));
                }
                return list;
            }
            errors.add("Error: Lista no válida en posición " + startIndex);
            return new ArrayList<>();
        }
    
        private List<String[]> extractTransitions(String content, int startIndex) {
            int start = content.indexOf("{", startIndex) + 1;
            int end = content.indexOf("}", start);
            List<String[]> transitions = new ArrayList<>();
            if (start > 0 && end > start) {
                // Extraer el bloque de transiciones
                String transitionsBlock = content.substring(start, end).trim();
                String[] stateTransitions = transitionsBlock.split("\\),"); // Dividir por '),'
        
                for (String stateTransition : stateTransitions) {
                    stateTransition = stateTransition.trim();
                    if (stateTransition.endsWith(")")) {
                        stateTransition = stateTransition.substring(0, stateTransition.length() - 1); // Eliminar ')'
                    }
        
                    // Separar el estado origen de las transiciones
                    String[] stateParts = stateTransition.split("=", 2);
                    if (stateParts.length == 2) {
                        String state = stateParts[0].trim(); // Estado origen
                        String transitionsList = stateParts[1].trim();
        
                        // Procesar cada transición individual
                        transitionsList = transitionsList.replace("(", "").replace(")", ""); // Eliminar paréntesis
                        String[] individualTransitions = transitionsList.split(",");
        
                        for (String transition : individualTransitions) {
                            String[] parts = transition.split("->");
                            if (parts.length == 2) {
                                String symbol = parts[0].trim().replace("\"", ""); // Símbolo
                                String targetState = parts[1].trim(); // Estado destino
        
                                // Agregar la transición a la lista
                                transitions.add(new String[]{state, symbol, targetState});
                            } else {
                                errors.add("Error: Transición no válida '" + transition + "'");
                            }
                        }
                    } else {
                        errors.add("Error: Transición no válida '" + stateTransition + "'");
                    }
                }
            } else {
                errors.add("Error: Transiciones no válidas en posición " + startIndex);
            }
            return transitions;
        }
    
        public List<String> getTokens() {
            return tokens;
        }
    
        public List<String> getErrors() {
            return errors;
        }
    
        // Clase para representar un autómata
        private static class Automaton {
            private String nombre; // Nombre del autómata
            private String descripcion;
            private List<String> estados;
            private List<String> alfabeto;
            private String inicial;
            private List<String> finales;
            private List<String[]> transiciones;
    
            public Automaton() {
                estados = new ArrayList<>();
                alfabeto = new ArrayList<>();
                finales = new ArrayList<>();
                transiciones = new ArrayList<>();
            }
            
            

            public void setNombre(String nombre) {
                this.nombre = nombre;
            }
    
            public String getNombre() {
                return nombre;
            }
    
            public void setDescripcion(String descripcion) {
                this.descripcion = descripcion;
            }
    
            public void setEstados(List<String> estados) {
                this.estados = estados;
            }
    
            public void setAlfabeto(List<String> alfabeto) {
                this.alfabeto = alfabeto;
            }
    
            public void setInicial(String inicial) {
                this.inicial = inicial;
            }
    
            public void setFinales(List<String> finales) {
                this.finales = finales;
            }
    
            public void setTransiciones(List<String[]> transiciones) {
                this.transiciones = transiciones;
            }
    
            public void validate(List<String> errors) {
                // Validar estado inicial
                if (inicial == null || !estados.contains(inicial)) {
                    errors.add("Error: El estado inicial '" + inicial + "' no está definido en los estados.");
                }
            
                // Validar estados finales
                for (String estadoFinal : finales) {
                    if (!estados.contains(estadoFinal)) {
                        errors.add("Error: El estado final '" + estadoFinal + "' no está definido en los estados.");
                    }
                }
            
                // Validar transiciones
                for (String[] transicion : transiciones) {
                    if (!estados.contains(transicion[0])) {
                        errors.add("Error: El estado de origen '" + transicion[0] + "' no está definido en los estados.");
                    }
                    String simbolo = transicion[1].trim(); // Eliminar espacios en blanco
                    if (!alfabeto.contains(simbolo)) {
                        errors.add("Error: El símbolo '" + simbolo + "' no está definido en el alfabeto.");
                    }
                    if (!estados.contains(transicion[2])) {
                        errors.add("Error: El estado de destino '" + transicion[2] + "' no está definido en los estados.");
                    }
                }
            }
    
            @Override
            public String toString() {
                StringBuilder sb = new StringBuilder();
                sb.append(nombre).append(": {\n");
                sb.append("  descripcion: \"").append(descripcion).append("\",\n");
                sb.append("  estados: ").append(estados).append(",\n");
                sb.append("  alfabeto: ").append(alfabeto).append(",\n");
                sb.append("  inicial: ").append(inicial).append(",\n");
                sb.append("  finales: ").append(finales).append(",\n");
                sb.append("  transiciones: {\n");
                for (String[] transicion : transiciones) {
                    sb.append("    ").append(transicion[0]).append(" = (\"").append(transicion[1]).append("\" -> ").append(transicion[2]).append("),\n");
                }
                sb.append("  }\n");
                sb.append("}\n");
                return sb.toString();
            }
        }
    }
}


