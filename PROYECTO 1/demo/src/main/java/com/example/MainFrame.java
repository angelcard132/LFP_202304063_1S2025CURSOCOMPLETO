package com.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class MainFrame extends JFrame {
    private JComboBox<String> automataComboBox;
    private JButton loadFileButton, graphButton, tokenReportButton, errorReportButton;
    private JTextArea fileContentArea, errorArea;
     private JTable tokenTable;
    private DefaultTableModel tokenTableModel, errorTableModel;

    
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
        JScrollPane errorScrollPane = new JScrollPane(errorArea);

         // Tabla para mostrar tokens
       tokenTableModel = new DefaultTableModel(new String[]{"Tipo de Token", "Valor"}, 0);
       tokenTable = new JTable(tokenTableModel);
        JScrollPane tokenScrollPane = new JScrollPane(tokenTable);
   

        // Panel central para las áreas de texto
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new GridLayout(2, 1));
        centerPanel.add(new JLabel("Contenido del Archivo:"));
        centerPanel.add(fileScrollPane);
        centerPanel.add(new JLabel("Errores Léxicos:"));
        centerPanel.add(errorScrollPane);
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
            JOptionPane.showMessageDialog(this, "Archivo cargado: " + archivoSeleccionado.getName(), "Éxito", JOptionPane.INFORMATION_MESSAGE);

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
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al leer el archivo: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
    
        public LexicalAnalyzer() {
            tokens = new ArrayList<>();
            errors = new ArrayList<>();
        }
    
        public void analyze(String content) {
            tokens.clear();
            errors.clear();
    
            int length = content.length();
            StringBuilder currentToken = new StringBuilder();
            int openBraces = 0; // Contador para '{'
            int openBrackets = 0; // Contador para '['
    
            for (int i = 0; i < length; i++) {
                char c = content.charAt(i);
    
                if (Character.isWhitespace(c)) {
                    // Ignorar espacios en blanco
                    continue;
                } else if (c == '{') {
                    openBraces++;
                    tokens.add("SIMBOLO: " + c);
                } else if (c == '}') {
                    if (openBraces > 0) {
                        openBraces--;
                        tokens.add("SIMBOLO: " + c);
                    } else {
                        errors.add("Error: Símbolo '}' no tiene un '{' correspondiente en posición " + i);
                    }
                } else if (c == '[') {
                    openBrackets++;
                    tokens.add("SIMBOLO: " + c);
                } else if (c == ']') {
                    if (openBrackets > 0) {
                        openBrackets--;
                        tokens.add("SIMBOLO: " + c);
                    } else {
                        errors.add("Error: Símbolo ']' no tiene un '[' correspondiente en posición " + i);
                    }
                } else if (c == '(' || c == ')') {
                    // Reconocer paréntesis como símbolos válidos
                    tokens.add("SIMBOLO: " + c);
                } else if (c == '=') {
                    // Reconocer el símbolo '=' como válido
                    tokens.add("SIMBOLO: " + c);
                } else if (c == ':') {
                    tokens.add("SIMBOLO: " + c);
                } else if (c == ',') {
                    tokens.add("SIMBOLO: " + c);
                } else if (c == '"') {
                    // Procesar cadenas
                    int start = i;
                    i++;
                    while (i < length && content.charAt(i) != '"') {
                        currentToken.append(content.charAt(i));
                        i++;
                    }
                    if (i < length && content.charAt(i) == '"') {
                        tokens.add("CADENA: \"" + currentToken.toString() + "\"");
                        currentToken.setLength(0);
                    } else {
                        errors.add("Error: Cadena no cerrada iniciada en posición " + start);
                    }
                } else if (Character.isLetter(c)) {
                    // Procesar identificadores
                    currentToken.append(c);
                    while (i + 1 < length && (Character.isLetterOrDigit(content.charAt(i + 1)) || content.charAt(i + 1) == '_')) {
                        i++;
                        currentToken.append(content.charAt(i));
                    }
                    tokens.add("IDENTIFICADOR: " + currentToken.toString());
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
                } else if (c == '-' && i + 1 < length && content.charAt(i + 1) == '>') {
                    // Procesar flechas (->)
                    tokens.add("FLECHA: ->");
                    i++;
                } else {
                    // Caracter no reconocido
                    errors.add("Error: Caracter no reconocido '" + c + "' en posición " + i);
                }
            }
    
            // Verificar si hay corchetes o llaves sin cerrar
            if (openBraces > 0) {
                errors.add("Error: Falta cerrar " + openBraces + " llave(s) '}'");
            }
            if (openBrackets > 0) {
                errors.add("Error: Falta cerrar " + openBrackets + " corchete(s) ']'");
            }
        }
    
        public List<String> getTokens() {
            return tokens;
        }
    
        public List<String> getErrors() {
            return errors;
        }
    }
}


