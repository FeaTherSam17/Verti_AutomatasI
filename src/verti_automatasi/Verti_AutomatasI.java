package verti_automatasi;

import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class Verti_AutomatasI {

    // ============================================================
    // SECCIÓN 1: TABLA DE TOKENS
    // ============================================================

    private static final Map<String, TokenInfo> tablaTokens = new HashMap<>();

    static class TokenInfo {
        String tipo;
        int numero;

        TokenInfo(String tipo, int numero) {
            this.tipo = tipo;
            this.numero = numero;
        }
    }

    static {
        // Palabras reservadas
        tablaTokens.put("fn", new TokenInfo("Palabra reservada", 1));
        tablaTokens.put("let", new TokenInfo("Palabra reservada", 2));
        tablaTokens.put("if", new TokenInfo("Palabra reservada", 3));
        tablaTokens.put("else", new TokenInfo("Palabra reservada", 4));
        tablaTokens.put("println!", new TokenInfo("Palabra reservada", 5));
        tablaTokens.put("print!", new TokenInfo("Palabra reservada", 6));
        tablaTokens.put("const", new TokenInfo("Palabra reservada", 7));
        tablaTokens.put("static", new TokenInfo("Palabra reservada", 8));
        tablaTokens.put("struct", new TokenInfo("Palabra reservada", 9));

        // Operadores dobles
        tablaTokens.put("==", new TokenInfo("Igual que", 19));
        tablaTokens.put("!=", new TokenInfo("Distinto de", 20));

        // Símbolos simples
        tablaTokens.put("(", new TokenInfo("Paréntesis apertura", 9));
        tablaTokens.put(")", new TokenInfo("Paréntesis cierre", 10));
        tablaTokens.put("{", new TokenInfo("Llave apertura", 11));
        tablaTokens.put("}", new TokenInfo("Llave cierre", 12));
        tablaTokens.put(";", new TokenInfo("Punto y coma", 13));
        tablaTokens.put("=", new TokenInfo("Asignación", 14));
        tablaTokens.put("+", new TokenInfo("Suma", 15));
        tablaTokens.put("-", new TokenInfo("Resta", 16));
        tablaTokens.put("*", new TokenInfo("Multiplicación", 17));
        tablaTokens.put("/", new TokenInfo("División", 18));
        tablaTokens.put(">", new TokenInfo("Mayor que", 21));
        tablaTokens.put("<", new TokenInfo("Menor que", 22));
        tablaTokens.put(",", new TokenInfo("Coma", 23));
        tablaTokens.put(":", new TokenInfo("Anotación tipo", 24));
        tablaTokens.put("\"", new TokenInfo("Comilla doble", 8));
    }

    // ============================================================
    // SECCIÓN 2: MAIN
    // ============================================================

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Verti_AutomatasI::iniciarGui);
    }

    // ============================================================
    // SECCIÓN 3: GUI (TU INTERFAZ ORIGINAL)
    // ============================================================

    private static void iniciarGui() {

        JFrame frame = new JFrame("Verti - Analizador");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTextArea entradaArea = new JTextArea(12, 60);
        JTextArea salidaArea = new JTextArea(12, 60);
        salidaArea.setEditable(false);

        DefaultTableModel lexicoModel = new DefaultTableModel(
                new Object[]{"Tipo", "Token"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable lexicoTable = new JTable(lexicoModel);

        JButton analizarBtn = new JButton("Analizar");
        JButton limpiarBtn = new JButton("Limpiar");
        JLabel rutaLabel = new JLabel("Archivo: (sin seleccionar)");

        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("File");

        JFileChooser chooser = new JFileChooser();
        File[] archivoActual = new File[1];

        menu.add("Open").addActionListener(e -> {
            if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                try {
                    File f = chooser.getSelectedFile();
                    archivoActual[0] = f;
                    entradaArea.setText(Files.readString(f.toPath(), StandardCharsets.UTF_8));
                    rutaLabel.setText("Archivo: " + f.getName());
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(frame, "Error al leer archivo");
                }
            }
        });

        menu.add("Save As...").addActionListener(e -> {
            if (chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
                try {
                    File f = chooser.getSelectedFile();
                    archivoActual[0] = f;
                    Files.write(f.toPath(), entradaArea.getText().getBytes(StandardCharsets.UTF_8));
                    rutaLabel.setText("Archivo: " + f.getName());
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(frame, "Error al guardar archivo");
                }
            }
        });

        menu.addSeparator();
        menu.add("Exit").addActionListener(e -> System.exit(0));
        menuBar.add(menu);
        frame.setJMenuBar(menuBar);

        analizarBtn.addActionListener(e -> {

            lexicoModel.setRowCount(0);

            String texto = entradaArea.getText();
            if (texto.trim().isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Ingresa código.");
                return;
            }

            salidaArea.setText(analizarPrograma(texto, lexicoModel));
        });

        limpiarBtn.addActionListener(e -> {
            entradaArea.setText("");
            salidaArea.setText("");
            lexicoModel.setRowCount(0);
        });

        JPanel topPanel = new JPanel();
        topPanel.add(analizarBtn);
        topPanel.add(limpiarBtn);

        JSplitPane bottom = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(lexicoTable),
                new JScrollPane(salidaArea));

        JSplitPane mainSplit = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(entradaArea),
                bottom);

        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(mainSplit, BorderLayout.CENTER);
        frame.add(rutaLabel, BorderLayout.SOUTH);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    // ============================================================
    // SECCIÓN 4: ANALIZADOR LÉXICO
    // ============================================================

    private static String analizarPrograma(String texto, DefaultTableModel modelo) {

        StringBuilder salida = new StringBuilder();
        int i = 0;

        while (i < texto.length()) {

            char c = texto.charAt(i);

            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }

            // Operadores dobles
            if (i + 1 < texto.length()) {
                String doble = texto.substring(i, i + 2);
                if (tablaTokens.containsKey(doble)) {
                    agregarToken(doble, modelo);
                    salida.append("Token: ").append(doble).append("\n");
                    i += 2;
                    continue;
                }
            }

            // Símbolos simples
            String simple = String.valueOf(c);
            if (tablaTokens.containsKey(simple)) {
                agregarToken(simple, modelo);
                salida.append("Token: ").append(simple).append("\n");
                i++;
                continue;
            }

            // Números
            if (Character.isDigit(c)) {
                StringBuilder numero = new StringBuilder();
                while (i < texto.length() &&
                        Character.isDigit(texto.charAt(i))) {
                    numero.append(texto.charAt(i));
                    i++;
                }
                modelo.addRow(new Object[]{"Número", numero.toString()});
                salida.append("Número: ").append(numero).append("\n");
                continue;
            }

            // Identificadores
            if (Character.isLetter(c) || c == '_') {
                StringBuilder palabra = new StringBuilder();
                while (i < texto.length() &&
                        (Character.isLetterOrDigit(texto.charAt(i))
                                || texto.charAt(i) == '_'
                                || texto.charAt(i) == '!')) {
                    palabra.append(texto.charAt(i));
                    i++;
                }

                String lexema = palabra.toString();

                if (tablaTokens.containsKey(lexema)) {
                    agregarToken(lexema, modelo);
                    salida.append("Reservada: ").append(lexema).append("\n");
                } else {
                    modelo.addRow(new Object[]{"Identificador", lexema});
                    salida.append("Identificador: ").append(lexema).append("\n");
                }
                continue;
            }

            salida.append("Símbolo no reconocido: ")
                  .append(c)
                  .append("\n");
            i++;
        }

        return salida.toString();
    }

    // ============================================================
    // SECCIÓN 5: MÉTODO AUXILIAR
    // ============================================================

    private static void agregarToken(String lexema,
                                     DefaultTableModel modelo) {

        TokenInfo t = tablaTokens.get(lexema);
        modelo.addRow(new Object[]{t.tipo, lexema});
    }
}
