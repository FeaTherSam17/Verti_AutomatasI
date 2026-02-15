package verti_automatasi;

import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.*;
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
        tablaTokens.put("main", new TokenInfo("Palabra reservada", 9));

        // Símbolos simples
        tablaTokens.put("(", new TokenInfo("Paréntesis apertura", 10));
        tablaTokens.put(")", new TokenInfo("Paréntesis cierre", 11));
        tablaTokens.put("{", new TokenInfo("Llave apertura", 12));
        tablaTokens.put("}", new TokenInfo("Llave cierre", 13));
        tablaTokens.put(";", new TokenInfo("Punto y coma", 14));
        tablaTokens.put("=", new TokenInfo("Asignación", 15));
        tablaTokens.put("+", new TokenInfo("Suma", 16));
        tablaTokens.put("-", new TokenInfo("Resta", 17));
        tablaTokens.put("*", new TokenInfo("Multiplicación", 18));
        tablaTokens.put("/", new TokenInfo("División", 19));
        tablaTokens.put(">", new TokenInfo("Mayor que", 20));
        tablaTokens.put("<", new TokenInfo("Menor que", 21));
        tablaTokens.put(",", new TokenInfo("Coma", 22));
        tablaTokens.put(":", new TokenInfo("Anotación tipo", 23));
        tablaTokens.put("\"", new TokenInfo("Comilla doble", 24));

           // Operadores dobles
        tablaTokens.put("==", new TokenInfo("Igual que", 25));
        tablaTokens.put("!=", new TokenInfo("Distinto de", 26));
    }

    // ============================================================
    // SECCIÓN 2: Main que ejecuta la GUI
    // ============================================================

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Verti_AutomatasI::iniciarGui);
    }

    // ============================================================
    // SECCIÓN 3: GUI
    // ============================================================

    private static void iniciarGui() {

        //Creación de la ventana principal

        JFrame frame = new JFrame("Verti - Analizador");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTextArea entradaArea = new JTextArea(12, 60);
        // Salidas separadas: errores léxicos y sintácticos. La tabla léxica mostrará los tokens con ID.
        JTextArea lexicoErrOut = new JTextArea(12, 30);
        JTextArea sintacticoOut = new JTextArea(12, 30);
        lexicoErrOut.setEditable(false);
        sintacticoOut.setEditable(false);

        // Área de numeración (gutter) para `entradaArea`
        entradaArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        entradaArea.setLineWrap(false);
        JScrollPane entradaScroll = new JScrollPane(entradaArea);
        JTextArea lineNumbers = new JTextArea("1\n");
        lineNumbers.setEditable(false);
        lineNumbers.setBackground(new Color(240,240,240));
        lineNumbers.setFont(entradaArea.getFont());
        lineNumbers.setBorder(null);
        lineNumbers.setMargin(new Insets(4,6,4,6));
        entradaScroll.setRowHeaderView(lineNumbers);

        // Mantener los números sincronizados con el contenido
        entradaArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void update() { actualizarLineNumbers(entradaArea, lineNumbers); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { update(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { update(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
        });
        // también actualizar al iniciar
        actualizarLineNumbers(entradaArea, lineNumbers);

        DefaultTableModel lexicoModel = new DefaultTableModel(
            new Object[]{"ID", "Tipo", "Token"}, 0) {
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

        menu.add("Save").addActionListener(e -> {
            if (archivoActual[0] != null) {
                try {
                    Files.write(archivoActual[0].toPath(), entradaArea.getText().getBytes(StandardCharsets.UTF_8));
                    rutaLabel.setText("Archivo: " + archivoActual[0].getName());
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(frame, "Error al guardar archivo");
                }
            } else {
                JOptionPane.showMessageDialog(frame, "No hay archivo abierto. Usa 'Save As...'");
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

            // Análisis léxico (llena la tabla y devuelve mensajes de error léxico)
            String lexMsg = analizarPrograma(texto, lexicoModel);
            lexicoErrOut.setText(lexMsg);

            // Análisis sintáctico básico
            sintacticoOut.setText(analizarSintactico(texto));
        });

        limpiarBtn.addActionListener(e -> {
            entradaArea.setText("");
            lexicoModel.setRowCount(0);
            lexicoErrOut.setText("");
            sintacticoOut.setText("");
        });

        JPanel topPanel = new JPanel();
        topPanel.add(analizarBtn);
        topPanel.add(limpiarBtn);

        // Panel inferior dividido en 3 columnas: Tabla léxica | Errores léxicos | Sintáctico
        JSplitPane rightSplit = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT,
            new JScrollPane(lexicoErrOut),
            new JScrollPane(sintacticoOut));

        JSplitPane bottom = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT,
            new JScrollPane(lexicoTable),
            rightSplit);

        JSplitPane mainSplit = new JSplitPane(
            JSplitPane.VERTICAL_SPLIT,
            entradaScroll,
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

//Funcion que analiza el programa, llena la tabla de tokens y devuelve un string con los errores léxicos encontrados
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
                modelo.addRow(new Object[]{100, "Número", numero.toString()});
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
                    modelo.addRow(new Object[]{101, "Identificador", lexema});
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
    // SECCIÓN 5: MÉTODO AUXILIAR del analisis lexico + 
    // ============================================================

    private static void agregarToken(String lexema,
                                     DefaultTableModel modelo) {

        TokenInfo t = tablaTokens.get(lexema);
        modelo.addRow(new Object[]{t.numero, t.tipo, lexema});
    }

    private static void actualizarLineNumbers(JTextArea textArea, JTextArea gutter){
        int lines = textArea.getLineCount();
        StringBuilder sb = new StringBuilder();
        for(int i=1;i<=lines;i++){
            sb.append(i).append(System.lineSeparator());
        }
        // asegurar que siempre haya al menos un número
        if(sb.length()==0) sb.append("1").append(System.lineSeparator());
        gutter.setText(sb.toString());
    }

    // Analizador sintáctico básico: verifica balanceo de paréntesis y llaves
    private static String analizarSintactico(String texto){
      
        //Tipo de dato para acumular errores sintácticos encontrados
        StringBuilder sb = new StringBuilder();

        //=============================================================
        // Verificar la estrucura de fn main() { }
        //=============================================================

        /*patron para detectar la funcion main y su estructura
        //Con la siguiente estrucura (espacios en blanco)fn (espacios) main () {cualquier cosa}(espacios opcionales)
        */
        Pattern mainPattern = Pattern.compile("^\\s*fn\\s+main\\s*\\(\\s*\\)\\s*\\{([\\s\\S]*)\\}\\s*$");
        
        //Verificar que exista la función main con la estructura correcta
        Matcher mainMatcher = mainPattern.matcher(texto);
        
        if(!mainMatcher.matches()){
            sb.append("Error sintáctico: La función main no tiene la estructura correcta. Debe ser: fn main() { ... }\n");
            return sb.toString();
        }

        //Obtención del cuerpo/contenido del main
        String mainBody = mainMatcher.group(1);

        //=============================================================
        // Verificar el balance entre parentesis y llaves
        //=============================================================

        int parOpen=0, parClose=0, braceOpen=0, braceClose=0;
        for(char c: texto.toCharArray()){
            if(c=='(') parOpen++;
            if(c==')') parClose++;
            if(c=='{') braceOpen++;
            if(c=='}') braceClose++;



        }
        
        if(parOpen!=parClose) sb.append("Error sintáctico: paréntesis desbalanceados ("+parOpen+" vs "+parClose+")\n");
        if(braceOpen!=braceClose) sb.append("Error sintáctico: llaves desbalanceadas ("+braceOpen+" vs "+braceClose+")\n");

        //=============================================================
        // Verificar que no haya código fuera de main
         // Cualquier código antes de `fn main` o después de la última `}` es considerado error sintáctico       
        //=============================================================

        //Encuentra la coincidencia del main y verifica si hay o no algo antes o despupes del main 
        String antesMain = texto.substring(0, mainMatcher.start()).trim();
        String despuesMain = texto.substring(mainMatcher.end()).trim();
        if(!antesMain.isEmpty()) sb.append("Error sintáctico: código antes de la función main no permitido\n");
        if(!despuesMain.isEmpty()) sb.append("Error sintáctico: código después de la última llave no permitido\n");


        //=============================================================
        //Validaci´´o´n de lineas interndas del main
        //=============================================================


        if(sb.length()==0) sb.append("Sin errores sintácticos detectados.");
        return sb.toString();
    }

    // Analizador semántico básico: detecta identificadores repetidos declarados con `let`
    private static String analizarSemantico(String texto){
        Pattern p = Pattern.compile("\\blet\\s+([A-Za-z_][A-Za-z0-9_]*)");
        Matcher m = p.matcher(texto);
        Set<String> seen = new HashSet<>();
        Set<String> dup = new HashSet<>();
        while(m.find()){
            String id = m.group(1);
            if(!seen.add(id)) dup.add(id);
        }
        if(dup.isEmpty()) return "Sin errores semánticos detectados.";
        StringBuilder sb = new StringBuilder();
        sb.append("Errores semánticos: identificadores duplicados declarados con 'let':\n");
        for(String d: dup) sb.append(" - ").append(d).append('\n');
        return sb.toString();
    }
}
