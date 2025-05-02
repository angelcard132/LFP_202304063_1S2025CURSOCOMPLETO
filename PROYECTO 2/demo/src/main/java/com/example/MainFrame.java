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
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

public class MainFrame extends JFrame {

    private JTextArea fileContentArea;
    private JLabel imageArea;
    private JComboBox<String> worldSelector;
    private List<String> tokens;
    private List<String> errors;
    private List<String> worlds;

    public MainFrame() {
        setTitle("Generador Visual de Mapas Narrativos");
        setSize(950, 600);
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

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(imageArea, BorderLayout.CENTER);

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

        // Acciones
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

        String content = fileContentArea.getText();

        int length = content.length();
        StringBuilder currentToken = new StringBuilder();

        String validSymbols = "{}():,";
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
                column++;
            } else if (c == '\"') {
                int startCol = column;
                StringBuilder stringLiteral = new StringBuilder();
                i++;
                column++;
                while (i < length && content.charAt(i) != '\"') {
                    if (content.charAt(i) == '\n') {
                        errors.add("Error: cadena sin cerrar. Línea: " + line + " Columna: " + column);
                        break;
                    }
                    stringLiteral.append(content.charAt(i));
                    i++;
                    column++;
                }
                if (i < length && content.charAt(i) == '\"') {
                    tokens.add("CADENA: \"" + stringLiteral.toString() + "\" Línea: " + line + " Columna: " + startCol);
                    column++;

                    if (tokens.size() > 1 && tokens.get(tokens.size() - 2).contains("IDENTIFICADOR: world")) {
                        worlds.add(stringLiteral.toString());
                        worldSelector.addItem(stringLiteral.toString());
                    }

                } else {
                    errors.add("Error: falta comilla de cierre para cadena en Línea: " + line + " Columna: " + startCol);
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
                errors.add("Error: símbolo no permitido '" + c + "' Línea: " + line + " Columna: " + column);
                column++;
            }
        }

        if (errors.isEmpty()) {
            imageArea.setText("Análisis léxico correcto. Selecciona un mapa para visualizar.");
        } else {
            imageArea.setText("Errores encontrados. Revisa el archivo.");
        }
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
            writer.println("<html><head><title>Errores</title></head><body><table border='1'><tr><th>Error</th><th>Detalle</th></tr>");
            for (String error : errors) {
                String[] parts = error.split(" Línea:");
                writer.printf("<tr><td>%s</td><td>%s</td></tr>\n", parts[0], parts.length > 1 ? parts[1] : "");
            }
            writer.println("</table></body></html>");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al generar errores.html");
        }

        JOptionPane.showMessageDialog(this, "Archivos de reporte generados correctamente.");
    }
 
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
