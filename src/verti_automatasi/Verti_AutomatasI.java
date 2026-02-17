/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package verti_automatasi;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author samAF
 */
public class Verti_AutomatasI {

    // Regex base para tipos de lexema.
    private static final String REGEX_IDENTIFICADOR = "[a-zA-Z_][a-zA-Z0-9_]*";
    private static final String REGEX_ENTERO = "[0-9]+";
    private static final String REGEX_FLOTANTE = "[0-9]+\\.[0-9]+";
    private static final String REGEX_CADENA = "\"[^\"]*\"";
    private static final String REGEX_BOOLEANO = "true|false";
    private static final String REGEX_PALABRA_RESERVADA = "println|print|fn|main|let|mut|i32|f64|bool|String|str|char|if|else|loop|while|for|return";
    private static final String REGEX_SIMBOLO = "->|!|\\(|\\)|,|\\\"|\\{|\\}|;|:|=|\\+|-|\\*|/|%";
    private static final String REGEX_ESPACIOS = "[ \\t\\n\\r]+";
    private static final String REGEX_COMENTARIO_LINEA = "//[^\\n]*";
    private static final String REGEX_COMENTARIO_BLOQUE = "/\\*[\\s\\S]*?\\*/";

    // Patrones compilados para reutilizar en el análisis.
    private static final Pattern PATRON_IDENTIFICADOR = Pattern.compile(REGEX_IDENTIFICADOR);
    private static final Pattern PATRON_ENTERO = Pattern.compile(REGEX_ENTERO);
    private static final Pattern PATRON_FLOTANTE = Pattern.compile(REGEX_FLOTANTE);
    private static final Pattern PATRON_CADENA = Pattern.compile(REGEX_CADENA);
    private static final Pattern PATRON_BOOLEANO = Pattern.compile(REGEX_BOOLEANO);
    private static final Pattern PATRON_PALABRA_RESERVADA = Pattern.compile(REGEX_PALABRA_RESERVADA);
    private static final Pattern PATRON_SIMBOLO = Pattern.compile(REGEX_SIMBOLO);
    private static final Pattern PATRON_ESPACIOS = Pattern.compile(REGEX_ESPACIOS);
    private static final Pattern PATRON_COMENTARIO_LINEA = Pattern.compile(REGEX_COMENTARIO_LINEA);
    private static final Pattern PATRON_COMENTARIO_BLOQUE = Pattern.compile(REGEX_COMENTARIO_BLOQUE);

    // Catálogo de símbolos permitidos.
    private static final Map<String, String> TOKENS_SIMBOLOS = new LinkedHashMap<>();

    static {
        TOKENS_SIMBOLOS.put("->", "SYM_ARROW");
        TOKENS_SIMBOLOS.put("!", "SYM_BANG");
        TOKENS_SIMBOLOS.put("(", "SYM_LPAREN");
        TOKENS_SIMBOLOS.put(")", "SYM_RPAREN");
        TOKENS_SIMBOLOS.put(",", "SYM_COMMA");
        TOKENS_SIMBOLOS.put("\"", "SYM_QUOTE");
        TOKENS_SIMBOLOS.put("{", "SYM_LBRACE");
        TOKENS_SIMBOLOS.put("}", "SYM_RBRACE");
        TOKENS_SIMBOLOS.put(";", "SYM_SEMICOLON");
        TOKENS_SIMBOLOS.put(":", "SYM_COLON");
        TOKENS_SIMBOLOS.put("=", "SYM_ASSIGN");
        TOKENS_SIMBOLOS.put("+", "SYM_PLUS");
        TOKENS_SIMBOLOS.put("-", "SYM_MINUS");
        TOKENS_SIMBOLOS.put("*", "SYM_STAR");
        TOKENS_SIMBOLOS.put("/", "SYM_SLASH");
        TOKENS_SIMBOLOS.put("%", "SYM_PERCENT");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Verti_AutomatasI::iniciarGui);
    }

    private static void iniciarGui() {
        // Ventana principal del analizador.
        JFrame frame = new JFrame("Verti - Analizador");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Área de código con numeración de líneas.
        JTextArea entradaArea = new JTextArea(12, 60);
        JScrollPane entradaScroll = crearEditorConNumerosDeLinea(entradaArea);

        // Tabla léxica: lexema, token y error.
        DefaultTableModel lexicoModel = new DefaultTableModel(
                new Object[]{"Lexema", "Token", "Error"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable lexicoTable = new JTable(lexicoModel);

        // Secciones de análisis.
        JTabbedPane analisisTabs = new JTabbedPane();
        analisisTabs.addTab("Análisis léxico", new JScrollPane(lexicoTable));
        analisisTabs.addTab("Análisis sintáctico", new JPanel(new BorderLayout()));
        analisisTabs.addTab("Análisis semántico", new JPanel(new BorderLayout()));
        //analisisTabs.addTab("Código intermedio", new JPanel(new BorderLayout()));

        JButton abrirBtn = new JButton("Abrir archivo");
        JButton analizarBtn = new JButton("Analizar");
        JButton limpiarBtn = new JButton("Limpiar");

        JLabel rutaLabel = new JLabel("Archivo: (sin seleccionar)");

        abrirBtn.addActionListener(e -> {
            // Carga texto desde archivo al editor.
            JFileChooser chooser = new JFileChooser();
            int result = chooser.showOpenDialog(frame);
            if (result == JFileChooser.APPROVE_OPTION) {
                try {
                    String contenido = Files.readString(chooser.getSelectedFile().toPath(), StandardCharsets.UTF_8);
                    entradaArea.setText(contenido);
                    rutaLabel.setText("Archivo: " + chooser.getSelectedFile().getName());
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(frame,
                            "No se pudo leer el archivo seleccionado.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        analizarBtn.addActionListener(e -> {
            // Ejecuta el análisis léxico del contenido actual.
            String texto = entradaArea.getText();
            if (texto == null || texto.trim().isEmpty()) {
                JOptionPane.showMessageDialog(frame,
                        "Ingresa o carga un texto para analizar.",
                        "Aviso",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            analizarLexicoYMostrarTabla(texto, lexicoModel);
            analisisTabs.setSelectedIndex(0);
        });

        limpiarBtn.addActionListener(e -> {
            // Limpia editor, tabla y etiqueta de archivo.
            entradaArea.setText("");
            lexicoModel.setRowCount(0);
            rutaLabel.setText("Archivo: (sin seleccionar)");
        });

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(abrirBtn);
        topPanel.add(analizarBtn);
        topPanel.add(limpiarBtn);

        JPanel rutaPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        rutaPanel.add(rutaLabel);

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                entradaScroll,
                analisisTabs);
        splitPane.setResizeWeight(0.5);

        frame.setLayout(new BorderLayout());
        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(splitPane, BorderLayout.CENTER);
        frame.add(rutaPanel, BorderLayout.SOUTH);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static JScrollPane crearEditorConNumerosDeLinea(JTextArea entradaArea) {
        // Scroll del editor y panel de números de línea.
        JScrollPane scrollPane = new JScrollPane(entradaArea);

        JTextArea lineasArea = new JTextArea("1");
        lineasArea.setEditable(false);
        lineasArea.setFocusable(false);
        lineasArea.setFont(entradaArea.getFont());
        lineasArea.setBackground(new Color(245, 245, 245));
        scrollPane.setRowHeaderView(lineasArea);

        Runnable actualizarLineas = () -> {
            // Regenera la columna de numeración.
            int totalLineas = entradaArea.getLineCount();
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i <= totalLineas; i++) {
                sb.append(i).append(System.lineSeparator());
            }
            lineasArea.setText(sb.toString());
        };

        entradaArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                SwingUtilities.invokeLater(actualizarLineas);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                SwingUtilities.invokeLater(actualizarLineas);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                SwingUtilities.invokeLater(actualizarLineas);
            }
        });

        return scrollPane;
    }

    private static void analizarLexicoYMostrarTabla(String texto, DefaultTableModel lexicoModel) {
        // Punto de entrada del análisis léxico.
        List<LexicoItem> items = analizarLexico(texto);
        cargarTablaLexica(lexicoModel, items);
    }

    private static List<LexicoItem> analizarLexico(String texto) {
        // Recorrido secuencial del texto fuente.
        List<LexicoItem> items = new ArrayList<>();
        int i = 0;
        int linea = 1;
        int columna = 1;

        while (i < texto.length()) {
            char actual = texto.charAt(i);

            // Ignora espacios y actualiza posición.
            if (PATRON_ESPACIOS.matcher(String.valueOf(actual)).matches()) {
                if (actual == '\n') {
                    linea++;
                    columna = 1;
                } else {
                    columna++;
                }
                i++;
                continue;
            }

            // Reconoce comentarios de línea y bloque.
            if (actual == '/' && i + 1 < texto.length()) {
                char siguiente = texto.charAt(i + 1);
                if (siguiente == '/') {
                    int inicioComentario = i;
                    int inicioLineaComentario = linea;
                    int inicioColumnaComentario = columna;
                    i += 2;
                    columna += 2;
                    while (i < texto.length() && texto.charAt(i) != '\n') {
                        i++;
                        columna++;
                    }
                    String comentarioLinea = texto.substring(inicioComentario, i);
                    if (PATRON_COMENTARIO_LINEA.matcher(comentarioLinea).matches()) {
                        items.add(new LexicoItem(comentarioLinea, "comentario_linea", "", inicioLineaComentario, inicioColumnaComentario));
                    } else {
                        items.add(new LexicoItem(comentarioLinea, "error", "Comentario de línea inválido", inicioLineaComentario, inicioColumnaComentario));
                    }
                    continue;
                }
                if (siguiente == '*') {
                    int inicioLinea = linea;
                    int inicioColumna = columna;
                    int inicioComentario = i;
                    i += 2;
                    columna += 2;
                    boolean cerrado = false;
                    while (i < texto.length()) {
                        if (texto.charAt(i) == '\n') {
                            linea++;
                            columna = 1;
                            i++;
                            continue;
                        }
                        if (texto.charAt(i) == '*' && i + 1 < texto.length() && texto.charAt(i + 1) == '/') {
                            i += 2;
                            columna += 2;
                            cerrado = true;
                            break;
                        }
                        i++;
                        columna++;
                    }
                    if (cerrado) {
                        String comentarioBloque = texto.substring(inicioComentario, i);
                        if (PATRON_COMENTARIO_BLOQUE.matcher(comentarioBloque).matches()) {
                            items.add(new LexicoItem(comentarioBloque, "comentario_bloque", "", inicioLinea, inicioColumna));
                        } else {
                            items.add(new LexicoItem(comentarioBloque, "error", "Comentario de bloque inválido", inicioLinea, inicioColumna));
                        }
                    } else {
                        items.add(new LexicoItem("/*", "error", "Comentario de bloque sin cerrar", inicioLinea, inicioColumna));
                    }
                    continue;
                }
            }

            // Reconoce identificadores, booleanos y palabras reservadas.
            if (Character.isLetter(actual) || actual == '_') {
                int inicio = i;
                int inicioColumna = columna;
                while (i < texto.length()) {
                    char c = texto.charAt(i);
                    if (Character.isLetterOrDigit(c) || c == '_') {
                        i++;
                        columna++;
                    } else {
                        break;
                    }
                }
                String lexema = texto.substring(inicio, i);
                if (PATRON_BOOLEANO.matcher(lexema).matches()) {
                    items.add(new LexicoItem(lexema, "booleano", "", linea, inicioColumna));
                } else if (PATRON_PALABRA_RESERVADA.matcher(lexema).matches()) {
                    items.add(new LexicoItem(lexema, "palabra reservada", "", linea, inicioColumna));
                } else if (PATRON_IDENTIFICADOR.matcher(lexema).matches()) {
                    items.add(new LexicoItem(lexema, "identificador", "", linea, inicioColumna));
                } else {
                    items.add(new LexicoItem(lexema, "error", "Identificador inválido", linea, inicioColumna));
                }
                continue;
            }

            // Reconoce números enteros y flotantes.
            if (Character.isDigit(actual)) {
                int inicio = i;
                int inicioColumna = columna;
                boolean tienePunto = false;
                while (i < texto.length()) {
                    char c = texto.charAt(i);
                    if (Character.isDigit(c)) {
                        i++;
                        columna++;
                    } else if (c == '.' && !tienePunto && i + 1 < texto.length() && Character.isDigit(texto.charAt(i + 1))) {
                        tienePunto = true;
                        i++;
                        columna++;
                    } else {
                        break;
                    }
                }
                String lexemaNumero = texto.substring(inicio, i);
                if (PATRON_FLOTANTE.matcher(lexemaNumero).matches() || PATRON_ENTERO.matcher(lexemaNumero).matches()) {
                    items.add(new LexicoItem(lexemaNumero, "numero", "", linea, inicioColumna));
                } else {
                    items.add(new LexicoItem(lexemaNumero, "error", "Número inválido", linea, inicioColumna));
                }
                continue;
            }

            // Reconoce cadenas entre comillas dobles.
            if (actual == '"') {
                int inicioColumna = columna;
                int inicioLinea = linea;
                StringBuilder literal = new StringBuilder();
                literal.append(actual);
                i++;
                columna++;
                boolean cerrado = false;

                while (i < texto.length()) {
                    char c = texto.charAt(i);
                    literal.append(c);
                    i++;

                    if (c == '\n') {
                        linea++;
                        columna = 1;
                    } else {
                        columna++;
                    }

                    if (c == '"' && literal.charAt(literal.length() - 2) != '\\') {
                        cerrado = true;
                        break;
                    }
                }

                if (cerrado) {
                    String lexemaCadena = literal.toString();
                    if (PATRON_CADENA.matcher(lexemaCadena).matches()) {
                        items.add(new LexicoItem(lexemaCadena, "cadena", "", inicioLinea, inicioColumna));
                    } else {
                        items.add(new LexicoItem(lexemaCadena, "error", "Cadena inválida", inicioLinea, inicioColumna));
                    }
                } else {
                    items.add(new LexicoItem(literal.toString(), "error", "Cadena sin cerrar", inicioLinea, inicioColumna));
                }
                continue;
            }

            // Reconoce símbolo compuesto: flecha.
            if (actual == '-' && i + 1 < texto.length() && texto.charAt(i + 1) == '>') {
                String simboloFlecha = "->";
                if (PATRON_SIMBOLO.matcher(simboloFlecha).matches()) {
                    items.add(new LexicoItem(simboloFlecha, "simbolo", "", linea, columna));
                } else {
                    items.add(new LexicoItem(simboloFlecha, "error", "Símbolo inválido", linea, columna));
                }
                i += 2;
                columna += 2;
                continue;
            }

            // Reconoce símbolos simples definidos.
            String simboloSimple = String.valueOf(actual);
            if (TOKENS_SIMBOLOS.containsKey(simboloSimple) && !"\"".equals(simboloSimple)
                    && PATRON_SIMBOLO.matcher(simboloSimple).matches()) {
                items.add(new LexicoItem(simboloSimple, "simbolo", "", linea, columna));
                i++;
                columna++;
                continue;
            }

            // Cualquier otro carácter se marca como error.
            items.add(new LexicoItem(String.valueOf(actual), "error", "Caracter no reconocido", linea, columna));
            i++;
            columna++;
        }

        return items;
    }

    private static void cargarTablaLexica(DefaultTableModel lexicoModel, List<LexicoItem> items) {
        // Carga los resultados en la tabla de la GUI.
        lexicoModel.setRowCount(0);
        for (LexicoItem item : items) {
            String errorConPosicion = item.error.isEmpty()
                    ? ""
                    : item.error + " [L" + item.linea + ":C" + item.columna + "]";
            lexicoModel.addRow(new Object[]{item.lexema, item.token, errorConPosicion});
        }
    }

    private static class LexicoItem {
        // Estructura mínima de un token léxico.
        private final String lexema;
        private final String token;
        private final String error;
        private final int linea;
        private final int columna;

        private LexicoItem(String lexema, String token, String error, int linea, int columna) {
            this.lexema = lexema;
            this.token = token;
            this.error = error;
            this.linea = linea;
            this.columna = columna;
        }
    }

}
