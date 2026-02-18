/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package verti_automatasi;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
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
        // Referencia al archivo actualmente abierto (null si no hay archivo asociado)
        AtomicReference<File> currentFile = new AtomicReference<>(null);

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

        JTextArea sintacticoArea = new JTextArea(12, 60);
        sintacticoArea.setEditable(false);
        // Fuente compatible para mostrar emojis en los mensajes.
        sintacticoArea.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));

        // Secciones de análisis.
        JTabbedPane analisisTabs = new JTabbedPane();
        analisisTabs.addTab("Análisis léxico", new JScrollPane(lexicoTable));
        analisisTabs.addTab("Análisis sintáctico", new JScrollPane(sintacticoArea));
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
                    File sel = chooser.getSelectedFile();
                    String contenido = Files.readString(sel.toPath(), StandardCharsets.UTF_8);
                    entradaArea.setText(contenido);
                    currentFile.set(sel);
                    rutaLabel.setText("Archivo: " + sel.getName());
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
            List<LexicoItem> items = analizarLexicoYMostrarTabla(texto, lexicoModel);
            analizarSintacticoYMostrarTexto(items, sintacticoArea);
            analisisTabs.setSelectedIndex(0);
        });

        limpiarBtn.addActionListener(e -> {
            // Limpia editor, tabla y etiqueta de archivo.
            entradaArea.setText("");
            lexicoModel.setRowCount(0);
            sintacticoArea.setText("");
            currentFile.set(null);
            rutaLabel.setText("Archivo: (sin seleccionar)");
        });

        // Barra de menú: File -> Save, Save as..., Exit
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem openItem = new JMenuItem("Open");
        JMenuItem saveItem = new JMenuItem("Save");
        JMenuItem saveAsItem = new JMenuItem("Save as...");
        JMenuItem exitItem = new JMenuItem("Exit");

        openItem.addActionListener(ev -> {
            // Reutiliza la acción del botón "Abrir archivo"
            abrirBtn.doClick();
        });

        saveItem.addActionListener(ev -> {
            // Si hay un archivo abierto, sobrescribir; si no, abrir Save As
            File f = currentFile.get();
            if (f != null) {
                try {
                    Files.writeString(f.toPath(), entradaArea.getText(), StandardCharsets.UTF_8);
                    JOptionPane.showMessageDialog(frame, "Archivo guardado: " + f.getName(), "Guardado", JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(frame, "No se pudo guardar el archivo.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                // Delegar a Save As
                saveAsItem.doClick();
            }
        });

        saveAsItem.addActionListener(ev -> {
            JFileChooser saver = new JFileChooser();
            int r = saver.showSaveDialog(frame);
            if (r == JFileChooser.APPROVE_OPTION) {
                File destino = saver.getSelectedFile();
                try {
                    Files.writeString(destino.toPath(), entradaArea.getText(), StandardCharsets.UTF_8);
                    currentFile.set(destino);
                    rutaLabel.setText("Archivo: " + destino.getName());
                    JOptionPane.showMessageDialog(frame, "Archivo guardado: " + destino.getName(), "Guardado", JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(frame, "No se pudo guardar el archivo.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        exitItem.addActionListener(ev -> {
            int opcion = JOptionPane.showConfirmDialog(frame, "¿Deseas salir?", "Confirmar salida", JOptionPane.YES_NO_OPTION);
            if (opcion == JOptionPane.YES_OPTION) {
                frame.dispose();
                System.exit(0);
            }
        });

        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.add(saveAsItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);
        frame.setJMenuBar(menuBar);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
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
        // Abrir en pantalla completa (ventana maximizada)
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
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

    private static List<LexicoItem> analizarLexicoYMostrarTabla(String texto, DefaultTableModel lexicoModel) {
        // Punto de entrada del análisis léxico.
        List<LexicoItem> items = analizarLexico(texto);
        cargarTablaLexica(lexicoModel, items);
        return items;
    }

    private static void analizarSintacticoYMostrarTexto(List<LexicoItem> itemsLexicos, JTextArea sintacticoArea) {
        List<SintacticoError> errores = analizarSintactico(itemsLexicos);
        cargarTextoSintactico(sintacticoArea, errores);
    }

    private static List<SintacticoError> analizarSintactico(List<LexicoItem> itemsLexicos) {
        Parser parser = new Parser(itemsLexicos);
        parser.parsearPrograma();
        return parser.getErrores();
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
                    if (c == '\n') {
                        break;
                    }

                    literal.append(c);
                    i++;
                    columna++;

                    if (c == '"' && (literal.length() < 2 || literal.charAt(literal.length() - 2) != '\\')) {
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

    private static void cargarTextoSintactico(JTextArea sintacticoArea, List<SintacticoError> errores) {
        // Construye la salida textual del análisis sintáctico.
        StringBuilder salida = new StringBuilder();
        if (errores.isEmpty()) {
            // Mensaje cuando no se detectan errores.
            salida.append("No hay error 👌👿🔥💪");
            sintacticoArea.setText(salida.toString());
            sintacticoArea.setCaretPosition(0);
            return;
        }

        // Lista cada error con su línea.
        for (SintacticoError error : errores) {
            salida.append("ERROR SINTACTICO en la linea ")
                    .append(error.linea)
                    .append(": ")
                    .append(error.detalle)
                    .append(System.lineSeparator());
        }
        sintacticoArea.setText(salida.toString());
        sintacticoArea.setCaretPosition(0);
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

    private static class SintacticoError {
        // Línea donde ocurrió el error.
        private final int linea;
        // Columna donde ocurrió el error.
        private final int columna;
        // Mensaje descriptivo del error.
        private final String detalle;

        private SintacticoError(int linea, int columna, String detalle) {
            this.linea = linea;
            this.columna = columna;
            this.detalle = detalle;
        }
    }

    private static class ParserToken {
        // Texto del token.
        private final String lexema;
        // Categoría normalizada para el parser.
        private final String categoria;
        // Posición de línea del token.
        private final int linea;
        // Posición de columna del token.
        private final int columna;

        private ParserToken(String lexema, String categoria, int linea, int columna) {
            this.lexema = lexema;
            this.categoria = categoria;
            this.linea = linea;
            this.columna = columna;
        }
    }

    // Excepción de control para recuperación de errores.
    private static class ParseException extends RuntimeException {
    }

    // Parser descendente recursivo para la gramática definida.
    private static class Parser {
        // Flujo de tokens para análisis sintáctico.
        private final List<ParserToken> tokens;
        // Lista acumulada de errores sintácticos.
        private final List<SintacticoError> errores;
        // Índice del token actual.
        private int actual;

        // Tipos válidos permitidos por la gramática.
        private static final Set<String> TIPOS_VALIDOS = new HashSet<>(Arrays.asList(
                "i32", "f64", "bool", "String", "str", "char"
        ));

        // Inicializa estado y normaliza tokens de entrada.
        private Parser(List<LexicoItem> itemsLexicos) {
            this.tokens = new ArrayList<>();
            this.errores = new ArrayList<>();
            this.actual = 0;
            construirTokens(itemsLexicos);
        }

        // Devuelve todos los errores detectados.
        private List<SintacticoError> getErrores() {
            return errores;
        }

        // Convierte tokens léxicos al formato interno del parser.
        private void construirTokens(List<LexicoItem> itemsLexicos) {
            for (LexicoItem item : itemsLexicos) {
                // Los comentarios no afectan la sintaxis.
                if ("comentario_linea".equals(item.token) || "comentario_bloque".equals(item.token)) {
                    continue;
                }

                String categoria;
                switch (item.token) {
                    case "palabra reservada":
                        categoria = "PALABRA_RESERVADA";
                        break;
                    case "identificador":
                        categoria = "IDENTIFICADOR";
                        break;
                    case "numero":
                        categoria = "NUMERO";
                        break;
                    case "cadena":
                        categoria = "CADENA";
                        break;
                    case "booleano":
                        categoria = "BOOLEANO";
                        break;
                    case "simbolo":
                        categoria = "SIMBOLO";
                        break;
                    default:
                        continue;
                }

                // Agrega token válido a la secuencia sintáctica.
                tokens.add(new ParserToken(item.lexema, categoria, item.linea, item.columna));
            }

            // Marca explícita de fin de entrada.
            tokens.add(new ParserToken("EOF", "EOF", -1, -1));
        }

        // Regla inicial: programa ::= { funcion }.
        private void parsearPrograma() {
            while (!esFin()) {
                try {
                    if (verificaLexema("fn")) {
                        parsearFuncion();
                    } else {
                        ParserToken token = verActual();
                        reportarError(token, "Se esperaba 'fn' al inicio de una función");
                        sincronizarPrograma();
                    }
                } catch (ParseException ex) {
                    sincronizarPrograma();
                }
            }
        }

        // funcion ::= "fn" (identificador|main) ... bloque
        private void parsearFuncion() {
            consumirLexema("fn", "Se esperaba 'fn'");
            consumirNombreFuncion("Se esperaba identificador de función o 'main'");
            consumirLexema("(", "Se esperaba '(' después del nombre de función");

            if (!verificaLexema(")")) {
                parsearParametros();
            }

            consumirLexema(")", "Se esperaba ')' al cerrar parámetros");

            if (coincideLexema("->")) {
                parsearTipo();
            }

            parsearBloque();
        }

        // parametros ::= parametro { "," parametro }
        private void parsearParametros() {
            parsearParametro();
            while (coincideLexema(",")) {
                parsearParametro();
            }
        }

        // parametro ::= identificador ":" tipo
        private void parsearParametro() {
            consumirIdentificador("Se esperaba identificador de parámetro");
            consumirLexema(":", "Se esperaba ':' en el parámetro");
            parsearTipo();
        }

        // bloque ::= "{" { sentencia } "}"
        private void parsearBloque() {
            consumirLexema("{", "Se esperaba '{' para iniciar bloque");

            while (!verificaLexema("}") && !esFin()) {
                try {
                    parsearSentencia();
                } catch (ParseException ex) {
                    sincronizarSentencia();
                }
            }

            consumirLexema("}", "Se esperaba '}' para cerrar bloque");
        }

        // Selecciona qué producción de sentencia aplicar.
        private void parsearSentencia() {
            if (coincideLexema("let")) {
                parsearDeclaracion();
                consumirPuntoYComa("después de declaración");
                return;
            }

            if (verificaIdentificador() && verificaSiguienteLexema("=")) {
                parsearAsignacion();
                consumirPuntoYComa("después de asignación");
                return;
            }

            if (verificaLexema("print") || verificaLexema("println")) {
                parsearSalida();
                consumirPuntoYComa("después de salida");
                return;
            }

            if (coincideLexema("if")) {
                parsearIf();
                return;
            }

            if (coincideLexema("while")) {
                parsearWhile();
                return;
            }

            if (coincideLexema("loop")) {
                parsearLoop();
                return;
            }

            if (coincideLexema("return")) {
                parsearReturn();
                consumirPuntoYComa("después de return");
                return;
            }

            parsearExpr();
            consumirPuntoYComa("después de expresión");
        }

        // declaracion ::= let [mut] id [:tipo] [=expr]
        private void parsearDeclaracion() {
            coincideLexema("mut");
            consumirIdentificador("Se esperaba identificador en declaración");

            if (coincideLexema(":")) {
                parsearTipo();
            }

            if (coincideLexema("=")) {
                parsearExpr();
            }
        }

        // asignacion ::= identificador "=" expr
        private void parsearAsignacion() {
            consumirIdentificador("Se esperaba identificador en asignación");
            consumirLexema("=", "Se esperaba '=' en asignación");
            parsearExpr();
        }

        // salida ::= (print|println) ! ( [argumentos] )
        private void parsearSalida() {
            if (!(coincideLexema("print") || coincideLexema("println"))) {
                ParserToken token = verActual();
                reportarError(token, "Se esperaba 'print' o 'println'");
                throw new ParseException();
            }

            consumirLexema("!", "Se esperaba '!' después de print/println");
            consumirLexema("(", "Se esperaba '(' en salida");

            if (!verificaLexema(")")) {
                parsearArgumentos();
            }

            consumirLexema(")", "Se esperaba ')' en salida");
        }

        // argumentos ::= expr { "," expr }
        private void parsearArgumentos() {
            parsearExpr();
            while (coincideLexema(",")) {
                parsearExpr();
            }
        }

        // if_stmt ::= if expr bloque [ else (bloque | if_stmt) ]
        private void parsearIf() {
            parsearExpr();
            parsearBloque();

            if (coincideLexema("else")) {
                if (coincideLexema("if")) {
                    parsearIf();
                } else {
                    parsearBloque();
                }
            }
        }

        // while_stmt ::= while expr bloque
        private void parsearWhile() {
            parsearExpr();
            parsearBloque();
        }

        // loop_stmt ::= loop bloque
        private void parsearLoop() {
            parsearBloque();
        }

        // return_stmt ::= return [expr]
        private void parsearReturn() {
            if (!verificaLexema(";")) {
                parsearExpr();
            }
        }

        // expr ::= termino { (+|-) termino }
        private void parsearExpr() {
            parsearTermino();
            while (coincideLexema("+", "-")) {
                parsearTermino();
            }
        }

        // termino ::= factor { (*|/|%) factor }
        private void parsearTermino() {
            parsearFactor();
            while (coincideLexema("*", "/", "%")) {
                parsearFactor();
            }
        }

        // factor ::= literal | identificador | llamada | (expr)
        private void parsearFactor() {
            if (coincideCategoria("NUMERO") || coincideCategoria("CADENA") || coincideCategoria("BOOLEANO")) {
                return;
            }

            if (coincideCategoria("IDENTIFICADOR")) {
                if (coincideLexema("(")) {
                    if (!verificaLexema(")")) {
                        parsearArgumentos();
                    }
                    consumirLexema(")", "Se esperaba ')' al cerrar llamada");
                }
                return;
            }

            if (coincideLexema("(")) {
                parsearExpr();
                consumirLexema(")", "Se esperaba ')' en expresión");
                return;
            }

            ParserToken token = verActual();
            reportarError(token, "Se esperaba literal, identificador, llamada o '(' expresión ')' ");
            throw new ParseException();
        }

        // Valida tipos permitidos por el lenguaje.
        private void parsearTipo() {
            ParserToken token = verActual();
            if (TIPOS_VALIDOS.contains(token.lexema)) {
                avanzar();
                return;
            }

            // Si falta tipo después de ':' o '->', reporta en la línea de esa marca.
            ParserToken referencia = actual > 0 ? anterior() : token;
            if ((":".equals(referencia.lexema) || "->".equals(referencia.lexema)) && referencia.linea > 0) {
                errores.add(new SintacticoError(
                        referencia.linea,
                        referencia.columna,
                        "Se esperaba tipo válido (i32, f64, bool, String, str, char)"
                ));
            } else {
                reportarError(token, "Se esperaba tipo válido (i32, f64, bool, String, str, char)");
            }
            throw new ParseException();
        }

        // Intenta consumir cualquiera de los lexemas indicados.
        private boolean coincideLexema(String... lexemas) {
            for (String lexema : lexemas) {
                if (verificaLexema(lexema)) {
                    avanzar();
                    return true;
                }
            }
            return false;
        }

        // Intenta consumir una categoría concreta.
        private boolean coincideCategoria(String categoria) {
            if (verificaCategoria(categoria)) {
                avanzar();
                return true;
            }
            return false;
        }

        // Verifica lexema sin avanzar.
        private boolean verificaLexema(String lexema) {
            if (esFin()) {
                return false;
            }
            return lexema.equals(verActual().lexema);
        }

        // Verifica el lexema del siguiente token.
        private boolean verificaSiguienteLexema(String lexema) {
            if (actual + 1 >= tokens.size()) {
                return false;
            }
            return lexema.equals(tokens.get(actual + 1).lexema);
        }

        // Verifica categoría sin consumir.
        private boolean verificaCategoria(String categoria) {
            if (esFin()) {
                return false;
            }
            return categoria.equals(verActual().categoria);
        }

        // Verifica si el token actual es identificador.
        private boolean verificaIdentificador() {
            return verificaCategoria("IDENTIFICADOR");
        }

        // Consume un lexema obligatorio o reporta error.
        private ParserToken consumirLexema(String lexema, String mensaje) {
            if (verificaLexema(lexema)) {
                return avanzar();
            }
            reportarError(verActual(), mensaje);
            throw new ParseException();
        }

        // Reporta ';' faltante usando la línea de la sentencia previa.
        private ParserToken consumirPuntoYComa(String contexto) {
            if (verificaLexema(";")) {
                return avanzar();
            }

            ParserToken referencia = actual > 0 ? anterior() : verActual();
            reportarError(referencia, "Se esperaba ';' " + contexto);
            throw new ParseException();
        }

        // Consume un identificador obligatorio.
        private ParserToken consumirIdentificador(String mensaje) {
            if (verificaIdentificador()) {
                return avanzar();
            }
            reportarError(verActual(), mensaje);
            throw new ParseException();
        }

        // Acepta nombre de función como id o main.
        private ParserToken consumirNombreFuncion(String mensaje) {
            if (verificaIdentificador() || verificaLexema("main")) {
                return avanzar();
            }
            reportarError(verActual(), mensaje);
            throw new ParseException();
        }

        // Registra error sintáctico con posición.
        private void reportarError(ParserToken token, String mensaje) {
            errores.add(new SintacticoError(token.linea, token.columna, mensaje));
        }

        // Salta tokens hasta el inicio de una función.
        private void sincronizarPrograma() {
            while (!esFin() && !verificaLexema("fn")) {
                avanzar();
            }
        }

        // Salta tokens hasta un punto seguro de sentencia.
        private void sincronizarSentencia() {
            while (!esFin()) {
                if (verificaLexema(";")) {
                    avanzar();
                    return;
                }

                // También permite reanudar cuando inicia una sentencia con identificador.
                if (verificaIdentificador()) {
                    return;
                }

                if (verificaLexema("}") || verificaLexema("let") || verificaLexema("if")
                        || verificaLexema("while") || verificaLexema("loop")
                        || verificaLexema("return") || verificaLexema("print")
                        || verificaLexema("println") || verificaLexema("fn")) {
                    return;
                }

                avanzar();
            }
        }

        // Avanza al siguiente token y devuelve el anterior.
        private ParserToken avanzar() {
            if (!esFin()) {
                actual++;
            }
            return anterior();
        }

        // Indica si el parser llegó al final.
        private boolean esFin() {
            return "EOF".equals(verActual().categoria);
        }

        // Obtiene el token actual.
        private ParserToken verActual() {
            return tokens.get(actual);
        }

        // Obtiene el token consumido más reciente.
        private ParserToken anterior() {
            return tokens.get(actual - 1);
        }
    }

}
