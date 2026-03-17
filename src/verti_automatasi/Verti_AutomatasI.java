/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package verti_automatasi;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
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
    private static final String REGEX_PALABRA_RESERVADA = "println|print|fn|main|let|mut|i8|i16|i32|i64|f8|f16|f32|f64|bool|String|str|char|if|else|loop|while|for|return";
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
        JTextArea entradaArea = new JTextArea(24, 100);
        entradaArea.setFont(new Font("Monospaced", Font.PLAIN, 18));
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
        // Aumentar tamaño de fuente y altura de fila para mejor legibilidad
        lexicoTable.setFont(new Font("SansSerif", Font.PLAIN, 15));
        lexicoTable.setRowHeight(22);
        lexicoTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 15));

        JTextArea sintacticoArea = new JTextArea(12, 60);
        sintacticoArea.setEditable(false);
        // Fuente compatible para mostrar emojis en los mensajes.
            sintacticoArea.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));

        JTextArea semanticoArea = new JTextArea(12, 60);
        semanticoArea.setEditable(false);
        // Usar la misma fuente y tamaño que el área de análisis sintáctico
            semanticoArea.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));

        JTextArea intermedioArea = new JTextArea(12, 60);
        intermedioArea.setEditable(false);
        intermedioArea.setFont(new Font("Monospaced", Font.PLAIN, 16));

        // Secciones de análisis.
        JTabbedPane analisisTabs = new JTabbedPane();
        JScrollPane lexicoScroll = new JScrollPane(lexicoTable);
        // Hacer la barra de desplazamiento más ancha y más rápida
        lexicoScroll.getVerticalScrollBar().setPreferredSize(new Dimension(20, Integer.MAX_VALUE));
        lexicoScroll.getVerticalScrollBar().setUnitIncrement(16);
        lexicoScroll.getHorizontalScrollBar().setPreferredSize(new Dimension(20, Integer.MAX_VALUE));
        // Cambiar la fuente de las pestañas
        analisisTabs.setFont(new Font("Segoe UI", Font.BOLD, 18));
        //Agregar pestañas de análisis
        analisisTabs.addTab("Análisis léxico", lexicoScroll);
        analisisTabs.addTab("Análisis sintáctico", new JScrollPane(sintacticoArea));
        analisisTabs.addTab("Análisis semántico", new JScrollPane(semanticoArea));
        analisisTabs.addTab("Código intermedio", new JScrollPane(intermedioArea));

        JButton abrirBtn = new JButton("Abrir archivo");
        JButton analizarBtn = new JButton("Analizar");
        JButton limpiarBtn = new JButton("Limpiar");

        JLabel rutaLabel = new JLabel("Archivo: (sin seleccionar)");

        // Aumentar tamaño de fuente de controles generales
        Font controlFont = new Font("SansSerif", Font.PLAIN, 16);
        abrirBtn.setFont(controlFont);
        analizarBtn.setFont(controlFont);
        limpiarBtn.setFont(controlFont);
        rutaLabel.setFont(new Font("SansSerif", Font.PLAIN, 15));

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
            analizarSemanticoYMostrarTexto(items, semanticoArea);
            analizarIntermedioYMostrarTexto(items, intermedioArea);
            analisisTabs.setSelectedIndex(0);
        });

        limpiarBtn.addActionListener(e -> {
            // Limpia editor, tabla y etiqueta de archivo.
            entradaArea.setText("");
            lexicoModel.setRowCount(0);
            sintacticoArea.setText("");
            semanticoArea.setText("");
            intermedioArea.setText("");
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

        // Ajustar fuentes de menú para coherencia
        Font menuFont = new Font("SansSerif", Font.PLAIN, 15);
        menuBar.setFont(menuFont);
        fileMenu.setFont(menuFont);
        openItem.setFont(menuFont);
        saveItem.setFont(menuFont);
        saveAsItem.setFont(menuFont);
        exitItem.setFont(menuFont);

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

    private static void analizarSemanticoYMostrarTexto(List<LexicoItem> itemsLexicos, JTextArea semanticoArea) {
        List<SemanticoError> errores = analizarSemantico(itemsLexicos);
        cargarTextoSemantico(semanticoArea, errores);
    }

    private static List<SemanticoError> analizarSemantico(List<LexicoItem> itemsLexicos) {
        AnalizadorSemantico analizador = new AnalizadorSemantico(itemsLexicos);
        analizador.analizarPrograma();
        return analizador.getErrores();
    }

    private static void analizarIntermedioYMostrarTexto(List<LexicoItem> itemsLexicos, JTextArea intermedioArea) {
        List<ExpresionIntermedia> expresiones = analizarCodigoIntermedio(itemsLexicos);
        cargarTextoIntermedio(intermedioArea, expresiones);
    }

    private static List<ExpresionIntermedia> analizarCodigoIntermedio(List<LexicoItem> itemsLexicos) {
        GeneradorCodigoIntermedio generador = new GeneradorCodigoIntermedio(itemsLexicos);
        return generador.generar();
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
            // salida.append("No hay error 👌👿🔥💪");
            salida.append("No se detectó ningun error sintáctico");
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

    private static void cargarTextoSemantico(JTextArea semanticoArea, List<SemanticoError> errores) {
        StringBuilder salida = new StringBuilder();
        if (errores.isEmpty()) {
            salida.append("No hay error semántico");
            semanticoArea.setText(salida.toString());
            semanticoArea.setCaretPosition(0);
            return;
        }

        for (SemanticoError error : errores) {
            salida.append("ERROR SEMANTICO en la linea ")
                    .append(error.linea)
                    .append(": ")
                    .append(error.detalle)
                    .append(System.lineSeparator());
        }

        semanticoArea.setText(salida.toString());
        semanticoArea.setCaretPosition(0);
    }

    private static void cargarTextoIntermedio(JTextArea intermedioArea, List<ExpresionIntermedia> expresiones) {
        StringBuilder salida = new StringBuilder();

        if (expresiones.isEmpty()) {
            salida.append("No se encontraron expresiones de asignación para generar código intermedio.");
            intermedioArea.setText(salida.toString());
            intermedioArea.setCaretPosition(0);
            return;
        }

        for (ExpresionIntermedia expresion : expresiones) {
            salida.append("Línea ")
                    .append(expresion.linea)
                    .append(" -> ")
                    .append(expresion.destino)
                    .append(" = ")
                    .append(expresion.infijo)
                    .append(System.lineSeparator());
            salida.append("Infijo   : ").append(expresion.infijo).append(System.lineSeparator());
            salida.append("Prefijo  : ").append(expresion.prefijo).append(System.lineSeparator());
            salida.append("Postfijo : ").append(expresion.postfijo).append(System.lineSeparator());
            salida.append("Cuádruplos:").append(System.lineSeparator());
            salida.append(String.format("%-4s | %-8s | %-12s | %-12s | %-12s%n",
                    "#", "Operador", "Arg1", "Arg2", "Resultado"));

            for (Cuadruplo cuadruplo : expresion.cuadruplos) {
                salida.append(String.format("%-4d | %-8s | %-12s | %-12s | %-12s%n",
                        cuadruplo.indice,
                        cuadruplo.operador,
                        cuadruplo.arg1,
                        cuadruplo.arg2,
                        cuadruplo.resultado));
            }
            salida.append(System.lineSeparator());
        }

        intermedioArea.setText(salida.toString());
        intermedioArea.setCaretPosition(0);
    }

    private static class ExpresionIntermedia {
        private final int linea;
        private final String destino;
        private final String infijo;
        private final String prefijo;
        private final String postfijo;
        private final List<Cuadruplo> cuadruplos;

        private ExpresionIntermedia(int linea, String destino, String infijo,
                String prefijo, String postfijo, List<Cuadruplo> cuadruplos) {
            this.linea = linea;
            this.destino = destino;
            this.infijo = infijo;
            this.prefijo = prefijo;
            this.postfijo = postfijo;
            this.cuadruplos = cuadruplos;
        }
    }

    private static class Cuadruplo {
        private final int indice;
        private final String operador;
        private final String arg1;
        private final String arg2;
        private final String resultado;

        private Cuadruplo(int indice, String operador, String arg1, String arg2, String resultado) {
            this.indice = indice;
            this.operador = operador;
            this.arg1 = arg1;
            this.arg2 = arg2;
            this.resultado = resultado;
        }
    }

    private static class GeneradorCodigoIntermedio {
        private final List<ParserToken> tokens;

        private GeneradorCodigoIntermedio(List<LexicoItem> itemsLexicos) {
            this.tokens = new ArrayList<>();
            construirTokens(itemsLexicos);
        }

        private List<ExpresionIntermedia> generar() {
            List<ExpresionIntermedia> resultado = new ArrayList<>();
            int i = 0;

            while (i < tokens.size()) {
                ParserToken token = tokens.get(i);
                if ("EOF".equals(token.categoria)) {
                    break;
                }

                if ("let".equals(token.lexema)) {
                    int[] nuevoIndice = procesarDeclaracion(resultado, i);
                    i = nuevoIndice[0];
                    continue;
                }

                if ("IDENTIFICADOR".equals(token.categoria)
                        && i + 1 < tokens.size()
                        && "=".equals(tokens.get(i + 1).lexema)) {
                    int[] nuevoIndice = procesarAsignacion(resultado, i);
                    i = nuevoIndice[0];
                    continue;
                }

                i++;
            }

            return resultado;
        }

        private int[] procesarDeclaracion(List<ExpresionIntermedia> salida, int inicio) {
            int i = inicio + 1;
            if (i < tokens.size() && "mut".equals(tokens.get(i).lexema)) {
                i++;
            }

            if (i >= tokens.size() || !"IDENTIFICADOR".equals(tokens.get(i).categoria)) {
                return new int[]{avanzarHastaFinSentencia(inicio)};
            }

            String destino = tokens.get(i).lexema;
            int linea = tokens.get(i).linea;
            i++;

            if (i < tokens.size() && ":".equals(tokens.get(i).lexema)) {
                i++;
                if (i < tokens.size()) {
                    i++;
                }
            }

            if (i >= tokens.size() || !"=".equals(tokens.get(i).lexema)) {
                return new int[]{avanzarHastaFinSentencia(i)};
            }

            i++;
            int finExpr = buscarFinExpresion(i);
            if (finExpr <= i) {
                return new int[]{Math.min(finExpr + 1, tokens.size())};
            }

            List<String> expr = extraerExpresion(i, finExpr);
            ExpresionIntermedia intermedia = construirExpresion(linea, destino, expr);
            if (intermedia != null) {
                salida.add(intermedia);
            }

            return new int[]{Math.min(finExpr + 1, tokens.size())};
        }

        private int[] procesarAsignacion(List<ExpresionIntermedia> salida, int inicio) {
            String destino = tokens.get(inicio).lexema;
            int linea = tokens.get(inicio).linea;
            int i = inicio + 2;
            int finExpr = buscarFinExpresion(i);

            if (finExpr <= i) {
                return new int[]{Math.min(finExpr + 1, tokens.size())};
            }

            List<String> expr = extraerExpresion(i, finExpr);
            ExpresionIntermedia intermedia = construirExpresion(linea, destino, expr);
            if (intermedia != null) {
                salida.add(intermedia);
            }

            return new int[]{Math.min(finExpr + 1, tokens.size())};
        }

        private ExpresionIntermedia construirExpresion(int linea, String destino, List<String> exprInfija) {
            if (exprInfija.isEmpty()) {
                return null;
            }

            List<String> postfijo = convertirInfijoAPostfijo(exprInfija);
            if (postfijo.isEmpty()) {
                return null;
            }

            String prefijo = convertirPostfijoAPrefijo(postfijo);
            List<Cuadruplo> cuadruplos = construirCuadruplos(postfijo, destino);

            return new ExpresionIntermedia(
                    linea,
                    destino,
                    String.join(" ", exprInfija),
                    prefijo,
                    String.join(" ", postfijo),
                    cuadruplos
            );
        }

        private List<String> extraerExpresion(int inicio, int finExclusivo) {
            List<String> expresion = new ArrayList<>();
            for (int i = inicio; i < finExclusivo && i < tokens.size(); i++) {
                String lexema = tokens.get(i).lexema;
                if (esOperador(lexema) || "(".equals(lexema) || ")".equals(lexema) || esOperando(lexema)) {
                    expresion.add(lexema);
                }
            }
            return expresion;
        }

        private int buscarFinExpresion(int inicio) {
            int profundidadParentesis = 0;
            int i = inicio;
            while (i < tokens.size()) {
                String lexema = tokens.get(i).lexema;
                if ("(".equals(lexema)) {
                    profundidadParentesis++;
                } else if (")".equals(lexema) && profundidadParentesis > 0) {
                    profundidadParentesis--;
                } else if (";".equals(lexema) && profundidadParentesis == 0) {
                    return i;
                }
                i++;
            }
            return i;
        }

        private int avanzarHastaFinSentencia(int indice) {
            int i = Math.max(0, indice);
            while (i < tokens.size() && !";".equals(tokens.get(i).lexema) && !"EOF".equals(tokens.get(i).categoria)) {
                i++;
            }
            return Math.min(i + 1, tokens.size());
        }

        private List<String> convertirInfijoAPostfijo(List<String> infijo) {
            List<String> salida = new ArrayList<>();
            Deque<String> operadores = new ArrayDeque<>();

            for (String token : infijo) {
                if (esOperando(token)) {
                    salida.add(token);
                    continue;
                }

                if ("(".equals(token)) {
                    operadores.push(token);
                    continue;
                }

                if (")".equals(token)) {
                    while (!operadores.isEmpty() && !"(".equals(operadores.peek())) {
                        salida.add(operadores.pop());
                    }
                    if (!operadores.isEmpty() && "(".equals(operadores.peek())) {
                        operadores.pop();
                    }
                    continue;
                }

                if (esOperador(token)) {
                    while (!operadores.isEmpty()
                            && esOperador(operadores.peek())
                            && precedencia(operadores.peek()) >= precedencia(token)) {
                        salida.add(operadores.pop());
                    }
                    operadores.push(token);
                }
            }

            while (!operadores.isEmpty()) {
                String op = operadores.pop();
                if (!"(".equals(op) && !")".equals(op)) {
                    salida.add(op);
                }
            }

            return salida;
        }

        private String convertirPostfijoAPrefijo(List<String> postfijo) {
            Deque<String> pila = new ArrayDeque<>();

            for (String token : postfijo) {
                if (esOperando(token)) {
                    pila.push(token);
                    continue;
                }

                if (esOperador(token) && pila.size() >= 2) {
                    String der = pila.pop();
                    String izq = pila.pop();
                    pila.push(token + " " + izq + " " + der);
                }
            }

            return pila.isEmpty() ? "" : pila.peek();
        }

        private List<Cuadruplo> construirCuadruplos(List<String> postfijo, String destino) {
            List<Cuadruplo> cuadruplos = new ArrayList<>();
            Deque<String> pila = new ArrayDeque<>();
            int temp = 0;
            int indice = 1;

            for (String token : postfijo) {
                if (esOperando(token)) {
                    pila.push(token);
                    continue;
                }

                if (esOperador(token) && pila.size() >= 2) {
                    String der = pila.pop();
                    String izq = pila.pop();
                    String temporal = "t" + (++temp);
                    cuadruplos.add(new Cuadruplo(indice++, token, izq, der, temporal));
                    pila.push(temporal);
                }
            }

            if (!pila.isEmpty()) {
                String resultadoExpr = pila.pop();
                cuadruplos.add(new Cuadruplo(indice, "=", resultadoExpr, "", destino));
            }

            return cuadruplos;
        }

        private boolean esOperador(String lexema) {
            return "+".equals(lexema)
                    || "-".equals(lexema)
                    || "*".equals(lexema)
                    || "/".equals(lexema)
                    || "%".equals(lexema);
        }

        private boolean esOperando(String lexema) {
            if (lexema == null || lexema.isEmpty()) {
                return false;
            }

            if ("true".equals(lexema) || "false".equals(lexema)) {
                return true;
            }

            if (lexema.startsWith("\"") && lexema.endsWith("\"") && lexema.length() >= 2) {
                return true;
            }

            return PATRON_IDENTIFICADOR.matcher(lexema).matches()
                    || PATRON_ENTERO.matcher(lexema).matches()
                    || PATRON_FLOTANTE.matcher(lexema).matches();
        }

        private int precedencia(String operador) {
            switch (operador) {
                case "*":
                case "/":
                case "%":
                    return 2;
                case "+":
                case "-":
                    return 1;
                default:
                    return 0;
            }
        }

        private void construirTokens(List<LexicoItem> itemsLexicos) {
            for (LexicoItem item : itemsLexicos) {
                if ("comentario_linea".equals(item.token)
                        || "comentario_bloque".equals(item.token)
                        || "error".equals(item.token)) {
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

                tokens.add(new ParserToken(item.lexema, categoria, item.linea, item.columna));
            }

            tokens.add(new ParserToken("EOF", "EOF", -1, -1));
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

    private static class SemanticoError {
        // Línea donde ocurrió el error semántico.
        private final int linea;
        // Mensaje de error semántico.
        private final String detalle;

        private SemanticoError(int linea, String detalle) {
            this.linea = linea;
            this.detalle = detalle;
        }
    }

    private static class Simbolo {
        // Nombre del identificador.
        private final String nombre;
        // Tipo del símbolo.
        private final String tipo;
        // Indica si permite asignaciones posteriores.
        private final boolean esMutable;
        // Nivel de ámbito donde fue declarado.
        private final int ambito;
        // Indica si tiene valor asignado.
        private boolean inicializada;
        // Marca cuando el símbolo representa una función.
        private final boolean esFuncion;
        // Tipos de parámetros para funciones.
        private final List<String> tiposParametros;

        private Simbolo(String nombre, String tipo, boolean esMutable, int ambito, boolean inicializada,
                boolean esFuncion, List<String> tiposParametros) {
            this.nombre = nombre;
            this.tipo = tipo;
            this.esMutable = esMutable;
            this.ambito = ambito;
            this.inicializada = inicializada;
            this.esFuncion = esFuncion;
            this.tiposParametros = tiposParametros;
        }
    }

    private static class TablaSimbolos {
        // Pila de ámbitos activos.
        private final Deque<Map<String, Simbolo>> pilaAmbitos = new ArrayDeque<>();

        // Entra a un nuevo ámbito.
        private void entrarAmbito() {
            pilaAmbitos.push(new HashMap<>());
        }

        // Sale del ámbito actual.
        private void salirAmbito() {
            if (!pilaAmbitos.isEmpty()) {
                pilaAmbitos.pop();
            }
        }

        // Nivel actual de anidación.
        private int nivelActual() {
            return Math.max(0, pilaAmbitos.size() - 1);
        }

        // Declara variable en el ámbito actual.
        private boolean declararVariable(String nombre, String tipo, boolean esMutable, boolean inicializada) {
            if (pilaAmbitos.isEmpty()) {
                entrarAmbito();
            }
            Map<String, Simbolo> ambitoActual = pilaAmbitos.peek();
            if (ambitoActual.containsKey(nombre)) {
                return false;
            }
            ambitoActual.put(nombre,
                    new Simbolo(nombre, tipo, esMutable, nivelActual(), inicializada, false, new ArrayList<>()));
            return true;
        }

        // Declara función en el ámbito global.
        private boolean declararFuncion(String nombre, String tipoRetorno, List<String> tiposParametros) {
            if (pilaAmbitos.isEmpty()) {
                entrarAmbito();
            }

            Map<String, Simbolo> ambitoGlobal = null;
            for (Map<String, Simbolo> ambito : pilaAmbitos) {
                ambitoGlobal = ambito;
            }
            if (ambitoGlobal == null) {
                ambitoGlobal = pilaAmbitos.peek();
            }

            if (ambitoGlobal.containsKey(nombre)) {
                return false;
            }

            ambitoGlobal.put(nombre,
                    new Simbolo(nombre, tipoRetorno, false, 0, true, true, new ArrayList<>(tiposParametros)));
            return true;
        }

        // Busca símbolo desde el ámbito más interno.
        private Simbolo buscar(String nombre) {
            for (Map<String, Simbolo> ambito : pilaAmbitos) {
                if (ambito.containsKey(nombre)) {
                    return ambito.get(nombre);
                }
            }
            return null;
        }
    }

    private static class SemanticoParseException extends RuntimeException {
    }

    private static class AnalizadorSemantico {
        // Flujo de tokens para análisis semántico.
        private final List<ParserToken> tokens;
        // Errores semánticos acumulados.
        private final List<SemanticoError> errores;
        // Tabla de símbolos con ámbitos.
        private final TablaSimbolos tabla;
        // Índice de token actual.
        private int actual;

        private static final Set<String> TIPOS_VALIDOS = new HashSet<>(Arrays.asList(
                "i8", "i16", "i32", "i64", "f8", "f16", "f32", "f64", "bool", "String", "str", "char"
        ));

        // Inicializa el analizador semántico.
        private AnalizadorSemantico(List<LexicoItem> itemsLexicos) {
            this.tokens = new ArrayList<>();
            this.errores = new ArrayList<>();
            this.tabla = new TablaSimbolos();
            this.actual = 0;
            construirTokens(itemsLexicos);
        }

        // Devuelve los errores semánticos encontrados.
        private List<SemanticoError> getErrores() {
            return errores;
        }

        // Ejecuta el análisis semántico completo.
        private void analizarPrograma() {
            tabla.entrarAmbito();
            registrarFunciones();
            actual = 0;

            while (!esFin()) {
                try {
                    if (verificaLexema("fn")) {
                        analizarFuncion();
                    } else {
                        avanzar();
                    }
                } catch (SemanticoParseException ex) {
                    sincronizarPrograma();
                }
            }
        }

        // Convierte tokens léxicos al formato del semántico.
        private void construirTokens(List<LexicoItem> itemsLexicos) {
            for (LexicoItem item : itemsLexicos) {
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

                tokens.add(new ParserToken(item.lexema, categoria, item.linea, item.columna));
            }

            tokens.add(new ParserToken("EOF", "EOF", -1, -1));
        }

        // Primera pasada para registrar funciones.
        private void registrarFunciones() {
            int i = 0;
            while (i < tokens.size()) {
                ParserToken token = tokens.get(i);
                if (!"fn".equals(token.lexema)) {
                    i++;
                    continue;
                }

                if (i + 1 >= tokens.size()) {
                    break;
                }

                ParserToken nombreTok = tokens.get(i + 1);
                if (!("IDENTIFICADOR".equals(nombreTok.categoria) || "main".equals(nombreTok.lexema))) {
                    i++;
                    continue;
                }

                int cursor = i + 2;
                List<String> tiposParametros = new ArrayList<>();
                if (cursor < tokens.size() && "(".equals(tokens.get(cursor).lexema)) {
                    cursor++;
                    while (cursor < tokens.size() && !")".equals(tokens.get(cursor).lexema)) {
                        if (cursor + 2 < tokens.size() && "IDENTIFICADOR".equals(tokens.get(cursor).categoria)
                                && ":".equals(tokens.get(cursor + 1).lexema)
                                && esTipoValido(tokens.get(cursor + 2).lexema)) {
                            tiposParametros.add(tokens.get(cursor + 2).lexema);
                            cursor += 3;
                            if (cursor < tokens.size() && ",".equals(tokens.get(cursor).lexema)) {
                                cursor++;
                            }
                        } else {
                            cursor++;
                        }
                    }
                }

                String tipoRetorno = "unit";
                if (cursor + 1 < tokens.size() && "->".equals(tokens.get(cursor + 1).lexema)) {
                    if (cursor + 2 < tokens.size()) {
                        tipoRetorno = tokens.get(cursor + 2).lexema;
                    }
                }

                boolean declarada = tabla.declararFuncion(nombreTok.lexema, tipoRetorno, tiposParametros);
                if (!declarada) {
                    errores.add(new SemanticoError(nombreTok.linea,
                            "La función '" + nombreTok.lexema + "' ya fue declarada"));
                }

                i++;
            }
        }

        // Analiza una función y su cuerpo.
        private void analizarFuncion() {
            consumirLexema("fn");
            ParserToken nombre = consumirNombreFuncion();
            consumirLexema("(");

            tabla.entrarAmbito();

            if (!verificaLexema(")")) {
                analizarParametros();
            }

            consumirLexema(")");

            String tipoRetorno = "unit";
            if (coincideLexema("->")) {
                tipoRetorno = consumirTipo();
            }

            analizarBloque(tipoRetorno);
            tabla.salirAmbito();

            if (nombre != null && "main".equals(nombre.lexema) && !"unit".equals(tipoRetorno)) {
                errores.add(new SemanticoError(nombre.linea,
                        "La función 'main' no debería declarar tipo de retorno"));
            }
        }

        // Registra parámetros como símbolos del ámbito de función.
        private void analizarParametros() {
            analizarParametro();
            while (coincideLexema(",")) {
                analizarParametro();
            }
        }

        private void analizarParametro() {
            ParserToken nombre = consumirIdentificador();
            consumirLexema(":");
            String tipo = consumirTipo();

            if (nombre != null && tipo != null) {
                boolean declarada = tabla.declararVariable(nombre.lexema, tipo, true, true);
                if (!declarada) {
                    errores.add(new SemanticoError(nombre.linea,
                            "La variable '" + nombre.lexema + "' ya fue declarada en este ámbito"));
                }
            }
        }

        // Analiza un bloque con su propio ámbito.
        private void analizarBloque(String tipoRetornoFuncion) {
            consumirLexema("{");
            tabla.entrarAmbito();

            while (!verificaLexema("}") && !esFin()) {
                try {
                    analizarSentencia(tipoRetornoFuncion);
                } catch (SemanticoParseException ex) {
                    sincronizarSentencia();
                }
            }

            consumirLexema("}");
            tabla.salirAmbito();
        }

        // Despacha la validación por tipo de sentencia.
        private void analizarSentencia(String tipoRetornoFuncion) {
            if (coincideLexema("let")) {
                analizarDeclaracion();
                consumirLexema(";");
                return;
            }

            if (verificaCategoria("IDENTIFICADOR") && verificaSiguienteLexema("=")) {
                analizarAsignacion();
                consumirLexema(";");
                return;
            }

            if (verificaLexema("print") || verificaLexema("println")) {
                analizarSalida();
                consumirLexema(";");
                return;
            }

            if (coincideLexema("if")) {
                analizarIf(tipoRetornoFuncion);
                return;
            }

            if (coincideLexema("while")) {
                String tipoCond = analizarExpr();
                if (!"bool".equals(tipoCond) && !"desconocido".equals(tipoCond)) {
                    errores.add(new SemanticoError(anterior().linea,
                            "La condición del 'while' debe ser booleana"));
                }
                analizarBloque(tipoRetornoFuncion);
                return;
            }

            if (coincideLexema("loop")) {
                analizarBloque(tipoRetornoFuncion);
                return;
            }

            if (coincideLexema("return")) {
                String tipoRet = "unit";
                if (!verificaLexema(";")) {
                    tipoRet = analizarExpr();
                }
                if (!sonTiposCompatibles(tipoRetornoFuncion, tipoRet)) {
                    errores.add(new SemanticoError(anterior().linea,
                            "Tipo de retorno incorrecto: se esperaba '" + tipoRetornoFuncion
                            + "', se encontró '" + tipoRet + "'"));
                }
                consumirLexema(";");
                return;
            }

            analizarExpr();
            consumirLexema(";");
        }

        // Valida declaración y compatibilidad de tipo inicial.
        private void analizarDeclaracion() {
            boolean esMutable = coincideLexema("mut");
            ParserToken nombre = consumirIdentificador();

            String tipoDeclarado = null;
            if (coincideLexema(":")) {
                tipoDeclarado = consumirTipo();
            }

            String tipoValor = null;
            boolean inicializada = false;
            if (coincideLexema("=")) {
                tipoValor = analizarExpr();
                inicializada = true;
            }

            String tipoFinal = tipoDeclarado != null ? tipoDeclarado : tipoValor;
            if (tipoFinal == null) {
                tipoFinal = "desconocido";
            }

            if (nombre != null) {
                boolean declarada = tabla.declararVariable(nombre.lexema, tipoFinal, esMutable, inicializada);
                if (!declarada) {
                    errores.add(new SemanticoError(nombre.linea,
                            "La variable '" + nombre.lexema + "' ya fue declarada en este ámbito"));
                }
            }

            if (tipoDeclarado != null && tipoValor != null && !sonTiposCompatibles(tipoDeclarado, tipoValor)) {
                int linea = nombre != null ? nombre.linea : verActual().linea;
                errores.add(new SemanticoError(linea,
                        "Tipo incorrecto: se esperaba '" + tipoDeclarado + "', se encontró '" + tipoValor + "'"));
            }
        }

        // Valida existencia, mutabilidad y tipos en asignación.
        private void analizarAsignacion() {
            ParserToken nombre = consumirIdentificador();
            consumirLexema("=");
            String tipoValor = analizarExpr();

            if (nombre == null) {
                return;
            }

            Simbolo simbolo = tabla.buscar(nombre.lexema);
            if (simbolo == null) {
                errores.add(new SemanticoError(nombre.linea,
                        "Variable '" + nombre.lexema + "' no declarada"));
                return;
            }

            if (!simbolo.esMutable) {
                errores.add(new SemanticoError(nombre.linea,
                        "No se puede asignar a '" + nombre.lexema + "' porque no es mutable"));
            }

            if (!sonTiposCompatibles(simbolo.tipo, tipoValor)) {
                errores.add(new SemanticoError(nombre.linea,
                        "Tipo incorrecto: se esperaba '" + simbolo.tipo + "', se encontró '" + tipoValor + "'"));
            }

            simbolo.inicializada = true;
        }

        // Valida argumentos de print/println y tipos imprimibles.
        private void analizarSalida() {
            ParserToken salidaTok = coincideLexema("print") ? anterior() : null;
            if (salidaTok == null && coincideLexema("println")) {
                salidaTok = anterior();
            }

            consumirLexema("!");
            consumirLexema("(");

            int placeholders = -1;
            int parametrosFormato = 0;

            if (!verificaLexema(")")) {
                ParserToken posibleFormato = verActual();
                List<String> tiposArgs = new ArrayList<>();
                tiposArgs.add(analizarExpr());

                if ("CADENA".equals(posibleFormato.categoria)) {
                    placeholders = contarPlaceholders(posibleFormato.lexema);
                }

                while (coincideLexema(",")) {
                    tiposArgs.add(analizarExpr());
                    parametrosFormato++;
                }

                for (String tipoArg : tiposArgs) {
                    if (!"desconocido".equals(tipoArg) && !esTipoImprimible(tipoArg)) {
                        int linea = salidaTok != null ? salidaTok.linea : verActual().linea;
                        errores.add(new SemanticoError(linea,
                                "El tipo '" + tipoArg + "' no se puede imprimir"));
                    }
                }

                if (placeholders > 0 && placeholders != parametrosFormato) {
                    int linea = salidaTok != null ? salidaTok.linea : verActual().linea;
                    errores.add(new SemanticoError(linea,
                            "Cantidad de placeholders '{}' (" + placeholders
                            + ") no coincide con parámetros de formato (" + parametrosFormato + ")"));
                }
            }

            consumirLexema(")");
        }

        // Cuenta ocurrencias de '{}' en literales de formato.
        private int contarPlaceholders(String literalCadena) {
            if (literalCadena == null || literalCadena.length() < 2) {
                return 0;
            }

            String contenido = literalCadena.substring(1, literalCadena.length() - 1);
            int contador = 0;
            for (int i = 0; i < contenido.length() - 1; i++) {
                if (contenido.charAt(i) == '{' && contenido.charAt(i + 1) == '}') {
                    contador++;
                    i++;
                }
            }
            return contador;
        }

        // Analiza argumentos y devuelve tipos inferidos.
        private List<String> analizarArgumentos() {
            List<String> tipos = new ArrayList<>();
            tipos.add(analizarExpr());
            while (coincideLexema(",")) {
                tipos.add(analizarExpr());
            }
            return tipos;
        }

        // Valida condición booleana y bloques if/else.
        private void analizarIf(String tipoRetornoFuncion) {
            String tipoCond = analizarExpr();
            if (!"bool".equals(tipoCond) && !"desconocido".equals(tipoCond)) {
                errores.add(new SemanticoError(anterior().linea,
                        "La condición del 'if' debe ser booleana"));
            }

            analizarBloque(tipoRetornoFuncion);

            if (coincideLexema("else")) {
                if (coincideLexema("if")) {
                    analizarIf(tipoRetornoFuncion);
                } else {
                    analizarBloque(tipoRetornoFuncion);
                }
            }
        }

        // Analiza expresión con precedencia de suma/resta.
        private String analizarExpr() {
            String tipoIzq = analizarTermino();
            while (coincideLexema("+", "-")) {
                ParserToken op = anterior();
                String tipoDer = analizarTermino();
                tipoIzq = combinarTiposAritmeticos(tipoIzq, tipoDer, op);
            }
            return tipoIzq;
        }

        // Analiza término con precedencia de multiplicación/división.
        private String analizarTermino() {
            String tipoIzq = analizarFactor();
            while (coincideLexema("*", "/", "%")) {
                ParserToken op = anterior();
                String tipoDer = analizarFactor();
                tipoIzq = combinarTiposAritmeticos(tipoIzq, tipoDer, op);
            }
            return tipoIzq;
        }

        // Analiza factor: literal, variable, llamada o subexpresión.
        private String analizarFactor() {
            if (coincideCategoria("NUMERO")) {
                String lexema = anterior().lexema;
                return lexema.contains(".") ? "f64" : "i32";
            }

            if (coincideCategoria("CADENA")) {
                return "String";
            }

            if (coincideCategoria("BOOLEANO")) {
                return "bool";
            }

            if (coincideCategoria("IDENTIFICADOR")) {
                ParserToken id = anterior();

                if (coincideLexema("(")) {
                    List<String> tiposArgs = new ArrayList<>();
                    if (!verificaLexema(")")) {
                        tiposArgs = analizarArgumentos();
                    }
                    consumirLexema(")");
                    return validarLlamadaFuncion(id, tiposArgs);
                }

                Simbolo simbolo = tabla.buscar(id.lexema);
                if (simbolo == null) {
                    errores.add(new SemanticoError(id.linea,
                            "Variable '" + id.lexema + "' no declarada"));
                    return "desconocido";
                }

                if (!simbolo.inicializada) {
                    errores.add(new SemanticoError(id.linea,
                            "Variable '" + id.lexema + "' usada antes de inicializar"));
                }

                return simbolo.tipo;
            }

            if (coincideLexema("(")) {
                String tipo = analizarExpr();
                consumirLexema(")");
                return tipo;
            }

            ParserToken token = verActual();
            errores.add(new SemanticoError(token.linea,
                    "Expresión inválida para análisis semántico"));
            throw new SemanticoParseException();
        }

        // Valida llamada a función y retorna tipo de retorno.
        private String validarLlamadaFuncion(ParserToken id, List<String> tiposArgs) {
            Simbolo funcion = tabla.buscar(id.lexema);
            if (funcion == null || !funcion.esFuncion) {
                errores.add(new SemanticoError(id.linea,
                        "Función '" + id.lexema + "' no declarada"));
                return "desconocido";
            }

            if (funcion.tiposParametros.size() != tiposArgs.size()) {
                errores.add(new SemanticoError(id.linea,
                        "Cantidad de argumentos incorrecta en llamada a '" + id.lexema + "'"));
            } else {
                for (int i = 0; i < tiposArgs.size(); i++) {
                    if (!sonTiposCompatibles(funcion.tiposParametros.get(i), tiposArgs.get(i))) {
                        errores.add(new SemanticoError(id.linea,
                                "Tipo de argumento incorrecto en llamada a '" + id.lexema + "'"));
                        break;
                    }
                }
            }

            return funcion.tipo;
        }

        // Combina tipos aritméticos y valida operandos numéricos.
        private String combinarTiposAritmeticos(String izq, String der, ParserToken operador) {
            if (!sonTiposNumericos(izq) || !sonTiposNumericos(der)) {
                errores.add(new SemanticoError(operador.linea,
                        "Operación aritmética requiere tipos numéricos"));
                return "desconocido";
            }

            if (esTipoFlotante(izq) || esTipoFlotante(der)) {
                return "f64";
            }
            return "i32";
        }

        // Consume lexema obligatorio para avanzar el análisis.
        private ParserToken consumirLexema(String lexema) {
            if (verificaLexema(lexema)) {
                return avanzar();
            }
            throw new SemanticoParseException();
        }

        // Consume identificador o main para nombre de función.
        private ParserToken consumirNombreFuncion() {
            if (verificaCategoria("IDENTIFICADOR") || verificaLexema("main")) {
                return avanzar();
            }
            throw new SemanticoParseException();
        }

        // Consume identificador para variables/parámetros.
        private ParserToken consumirIdentificador() {
            if (verificaCategoria("IDENTIFICADOR")) {
                return avanzar();
            }
            throw new SemanticoParseException();
        }

        // Consume y valida tipo permitido.
        private String consumirTipo() {
            if (esTipoValido(verActual().lexema)) {
                return avanzar().lexema;
            }
            throw new SemanticoParseException();
        }

        // Sincroniza hasta inicio probable de función.
        private void sincronizarPrograma() {
            while (!esFin() && !verificaLexema("fn")) {
                avanzar();
            }
        }

        // Sincroniza hasta frontera probable de sentencia.
        private void sincronizarSentencia() {
            while (!esFin()) {
                if (verificaLexema(";")) {
                    avanzar();
                    return;
                }

                if (verificaLexema("}") || verificaLexema("let") || verificaLexema("if")
                        || verificaLexema("while") || verificaLexema("loop")
                        || verificaLexema("return") || verificaLexema("print")
                        || verificaLexema("println") || verificaLexema("fn")
                        || verificaCategoria("IDENTIFICADOR")) {
                    return;
                }

                avanzar();
            }
        }

        // Verifica si el token actual coincide con lexema.
        private boolean verificaLexema(String lexema) {
            if (esFin()) {
                return false;
            }
            return lexema.equals(verActual().lexema);
        }

        // Verifica si el siguiente token coincide con lexema.
        private boolean verificaSiguienteLexema(String lexema) {
            if (actual + 1 >= tokens.size()) {
                return false;
            }
            return lexema.equals(tokens.get(actual + 1).lexema);
        }

        // Verifica categoría del token actual.
        private boolean verificaCategoria(String categoria) {
            if (esFin()) {
                return false;
            }
            return categoria.equals(verActual().categoria);
        }

        // Consume si coincide alguno de los lexemas.
        private boolean coincideLexema(String... lexemas) {
            for (String lexema : lexemas) {
                if (verificaLexema(lexema)) {
                    avanzar();
                    return true;
                }
            }
            return false;
        }

        // Consume si coincide la categoría.
        private boolean coincideCategoria(String categoria) {
            if (verificaCategoria(categoria)) {
                avanzar();
                return true;
            }
            return false;
        }

        // Avanza al siguiente token.
        private ParserToken avanzar() {
            if (!esFin()) {
                actual++;
            }
            return anterior();
        }

        // Indica fin de entrada.
        private boolean esFin() {
            return "EOF".equals(verActual().categoria);
        }

        // Devuelve token actual.
        private ParserToken verActual() {
            return tokens.get(actual);
        }

        // Devuelve token anterior consumido.
        private ParserToken anterior() {
            return tokens.get(actual - 1);
        }

        // Verifica tipos numéricos soportados.
        private boolean sonTiposNumericos(String tipo) {
            return tipo != null && tipo.matches("i(8|16|32|64)|f(8|16|32|64)");
        }

        // Verifica si el tipo es flotante.
        private boolean esTipoFlotante(String tipo) {
            return tipo != null && tipo.matches("f(8|16|32|64)");
        }

        // Reglas de compatibilidad entre tipos.
        private boolean sonTiposCompatibles(String esperado, String recibido) {
            if (esperado == null || recibido == null) {
                return false;
            }
            if ("unit".equals(esperado) && "unit".equals(recibido)) {
                return true;
            }
            if (esperado.equals(recibido)) {
                return true;
            }
            if (sonTiposNumericos(esperado) && sonTiposNumericos(recibido)) {
                return true;
            }
            if (("String".equals(esperado) && "str".equals(recibido))
                    || ("str".equals(esperado) && "String".equals(recibido))) {
                return true;
            }
            return "desconocido".equals(recibido);
        }

        // Determina si un tipo se puede imprimir en salida.
        private boolean esTipoImprimible(String tipo) {
            return sonTiposNumericos(tipo)
                    || "bool".equals(tipo)
                    || "String".equals(tipo)
                    || "str".equals(tipo)
                    || "char".equals(tipo);
        }

        // Verifica si un lexema es tipo permitido.
        private boolean esTipoValido(String tipo) {
            return TIPOS_VALIDOS.contains(tipo);
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
            "i8", "i16", "i32", "i64", "f8", "f16", "f32", "f64", "bool", "String", "str", "char"
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
                        "Se esperaba tipo válido (i8, i16, i32, i64, f8, f16, f32, f64, bool, String, str, char)"
                ));
            } else {
                reportarError(token, "Se esperaba tipo válido (i8, i16, i32, i64, f8, f16, f32, f64, bool, String, str, char)");
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
