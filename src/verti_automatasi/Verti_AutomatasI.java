/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package verti_automatasi;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author samAF
 */
public class Verti_AutomatasI {

    private static final String ALFABETO = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private static final Map<Integer, String> PALABRAS_CLAVE = new HashMap<>();
    private static final Map<Integer, String> SIMBOLOS = new HashMap<>();

    private static final char[] SEPARADORES = new char[]{' ', '\n', '\t', '{', '}', '(', ')', '=', ';', '-', '#', ','};

    static {
        PALABRAS_CLAVE.put(1, "COLOR");
        PALABRAS_CLAVE.put(2, "MIX");
        PALABRAS_CLAVE.put(3, "SHOW");
        PALABRAS_CLAVE.put(4, "BGN");
        PALABRAS_CLAVE.put(5, "END");

        SIMBOLOS.put(6, "=");
        SIMBOLOS.put(7, ";");
        SIMBOLOS.put(8, "(");
        SIMBOLOS.put(9, ")");
        SIMBOLOS.put(10, "{");
        SIMBOLOS.put(11, "}");
        SIMBOLOS.put(12, "-");
        SIMBOLOS.put(13, "#");
        SIMBOLOS.put(14, ",");
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Verti_AutomatasI::iniciarGui);
    }

    private static void iniciarGui() {
        JFrame frame = new JFrame("Verti - Analizador");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTextArea entradaArea = new JTextArea(12, 60);
        JTextArea salidaArea = new JTextArea(12, 60);
        salidaArea.setEditable(false);

        DefaultTableModel lexicoModel = new DefaultTableModel(
                new Object[]{"Tipo", "Token", "Valor"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable lexicoTable = new JTable(lexicoModel);
        
        // Declarar variables que se usarán en el menú
        JButton analizarBtn = new JButton("Analizar");
        JButton limpiarBtn = new JButton("Limpiar");
        JLabel rutaLabel = new JLabel("Archivo: (sin seleccionar)");

        // ==================== CREACIÓN DE UN MENU DESPLEGABLE ====================
        // JMenuBar: Es la barra contenedora de menús que se coloca en la parte superior de la ventana
        javax.swing.JMenuBar menuBar = new javax.swing.JMenuBar();
        
        // JMenu: Crea un menú llamado "File" que contendrá las opciones
        JMenu menu = new JMenu("File");
        
        // ==================== OPCIÓN 1: OPEN ====================
        // Permite al usuario seleccionar y abrir un archivo de texto
        menu.add("Open").addActionListener(e -> {
            // JFileChooser: Abre un diálogo para seleccionar archivos
            JFileChooser chooser = new JFileChooser();
            int result = chooser.showOpenDialog(frame);
            
            // APPROVE_OPTION: Verifica si el usuario seleccionó un archivo (no canceló)
            if (result == JFileChooser.APPROVE_OPTION) {
                try {
                    // Lee el contenido del archivo seleccionado en UTF-8
                    String contenido = Files.readString(chooser.getSelectedFile().toPath(), StandardCharsets.UTF_8);
                    // Coloca el contenido en el área de entrada para analizar
                    entradaArea.setText(contenido);
                } catch (IOException ex) {
                    // Si hay error al leer el archivo, muestra un mensaje de error
                    JOptionPane.showMessageDialog(frame,
                            "No se pudo leer el archivo seleccionado.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        // Variable para almacenar la ruta del archivo actual (para función Save)
        java.io.File[] archivoActual = new java.io.File[1];
        
        // ==================== OPCIÓN 2: Save ====================
        // Guarda el contenido actual manteniendo la ruta del archivo
        // Si no hay archivo asociado, abre el diálogo "Save As..."
        menu.add("Save").addActionListener(e -> {
            String texto = entradaArea.getText();
            
            // Verifica que hay contenido para guardar
            if (texto == null || texto.trim().isEmpty()) {
                JOptionPane.showMessageDialog(frame,
                        "No hay contenido para guardar.",
                        "Aviso",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Si ya hay un archivo asociado, guarda directamente
            if (archivoActual[0] != null) {
                try {
                    // Files.write: Escribe el contenido en el archivo (lo sobrescribe)
                    // StandardCharsets.UTF_8: Especifica la codificación de caracteres
                    Files.write(archivoActual[0].toPath(), texto.getBytes(StandardCharsets.UTF_8));
                    JOptionPane.showMessageDialog(frame,
                            "Archivo guardado exitosamente.",
                            "Éxito",
                            JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(frame,
                            "No se pudo guardar el archivo.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } else {
                // Si no hay archivo, abre el diálogo de "Save As..."
                JOptionPane.showMessageDialog(frame,
                        "Primero selecciona una ubicación con 'Save As...'",
                        "Información",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        // ==================== OPCIÓN 3: Save As... ====================
        // Abre un diálogo para guardar el archivo con una nueva ubicación o nombre
        menu.add("Save As...").addActionListener(e -> {
            String texto = entradaArea.getText();
            
            // Verifica que hay contenido para guardar
            if (texto == null || texto.trim().isEmpty()) {
                JOptionPane.showMessageDialog(frame,
                        "No hay contenido para guardar.",
                        "Aviso",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // JFileChooser: Componente para seleccionar ubicación de archivo
            JFileChooser chooser = new JFileChooser();
            // showSaveDialog: Abre el diálogo de guardar archivo
            int result = chooser.showSaveDialog(frame);
            
            // Verifica si el usuario confirmó la operación
            if (result == JFileChooser.APPROVE_OPTION) {
                try {
                    // Obtiene la ruta seleccionada por el usuario
                    java.io.File archivo = chooser.getSelectedFile();
                    // Guarda la referencia del archivo para usar en "Save"
                    archivoActual[0] = archivo;
                    
                    // Escribe el contenido en la ubicación seleccionada
                    Files.write(archivo.toPath(), texto.getBytes(StandardCharsets.UTF_8));
                    
                    // Actualiza la etiqueta mostrando el nombre del archivo guardado
                    rutaLabel.setText("Archivo: " + archivo.getName());
                    
                    JOptionPane.showMessageDialog(frame,
                            "Archivo guardado como: " + archivo.getName(),
                            "Éxito",
                            JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(frame,
                            "No se pudo guardar el archivo.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // ==================== SEPARADOR ====================
        // Añade una línea separadora visual entre opciones del menú
        menu.addSeparator();
        
        // ==================== OPCIÓN 3: EXIT ====================
        // Cierra la aplicación
        menu.add("Exit").addActionListener(e -> System.exit(0));
        
        // ==================== AGREGACIÓN DEL MENÚ A LA BARRA ====================
        // Agrega el menú "File" a la barra de menús
        menuBar.add(menu);
        
        // ==================== ESTABLECER LA BARRA DE MENÚS EN LA VENTANA ====================
        // setJMenuBar: Coloca la barra de menús en la parte superior del frame
        // Este es el paso imprescindible para que el menú aparezca en la ventana
        frame.setJMenuBar(menuBar);

        analizarBtn.addActionListener(e -> {
            String texto = entradaArea.getText();
            if (texto == null || texto.trim().isEmpty()) {
                JOptionPane.showMessageDialog(frame,
                        "Ingresa o carga un texto para analizar.",
                        "Aviso",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            String salida = analizarYCapturarSalida(texto, lexicoModel);
            salidaArea.setText(salida);
            salidaArea.setCaretPosition(0);
        });

        limpiarBtn.addActionListener(e -> {
            entradaArea.setText("");
            salidaArea.setText("");
            rutaLabel.setText("Archivo: (sin seleccionar)");
        });

        // Panel superior que contiene los botones de acceso rápido
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        //topPanel.add(abrirBtn);
        topPanel.add(analizarBtn);
        topPanel.add(limpiarBtn);

        JPanel rutaPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        rutaPanel.add(rutaLabel);

        JSplitPane bottomSplit = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT,
            new JScrollPane(lexicoTable),
            new JScrollPane(salidaArea));
        bottomSplit.setResizeWeight(0.5);

        JSplitPane splitPane = new JSplitPane(
            JSplitPane.VERTICAL_SPLIT,
            new JScrollPane(entradaArea),
            bottomSplit);
        splitPane.setResizeWeight(0.5);

        frame.setLayout(new BorderLayout());
        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(splitPane, BorderLayout.CENTER);
        frame.add(rutaPanel, BorderLayout.SOUTH);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static String analizarYCapturarSalida(String texto, DefaultTableModel lexicoModel) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        try (PrintStream ps = new PrintStream(buffer, true, StandardCharsets.UTF_8)) {
            System.setOut(ps);
            String cadena = eliminarComentarios(texto);
            List<LexicoItem> items = analizarLexico(cadena);
            cargarTablaLexica(lexicoModel, items);
            List<String> instrucciones = extraerInstrucciones(cadena);
            analisisSintactico(instrucciones);
        } finally {
            System.setOut(originalOut);
        }
        return buffer.toString(StandardCharsets.UTF_8);
    }

    private static String separarCadena(String cadena) {
        String resultado = cadena;
        for (char sep : SEPARADORES) {
            resultado = resultado.replace(sep, ' ');
        }
        return resultado;
    }

    private static boolean esTokenValido(String token) {
        for (int i = 0; i < token.length(); i++) {
            if (ALFABETO.indexOf(token.charAt(i)) == -1) {
                return false;
            }
        }
        return true;
    }

    private static List<LexicoItem> analizarLexico(String cadena) {
        List<LexicoItem> items = new ArrayList<>();
        for (int i = 0; i < cadena.length(); i++) {
            char c = cadena.charAt(i);
            for (Map.Entry<Integer, String> entry : SIMBOLOS.entrySet()) {
                if (entry.getValue().charAt(0) == c) {
                    items.add(new LexicoItem("Simbolo", String.valueOf(entry.getKey()), entry.getValue()));
                }
            }
        }

        String cadenaNormalizada = separarCadena(cadena);
        String[] tokens = cadenaNormalizada.trim().isEmpty()
                ? new String[0]
                : cadenaNormalizada.trim().split("\\s+");
        for (String token : tokens) {
            String tokenUpper = token.toUpperCase();
            if (PALABRAS_CLAVE.containsValue(tokenUpper)) {
                int idClave = -1;
                for (Map.Entry<Integer, String> entry : PALABRAS_CLAVE.entrySet()) {
                    if (entry.getValue().equals(tokenUpper)) {
                        idClave = entry.getKey();
                        break;
                    }
                }
                if (idClave != -1) {
                    items.add(new LexicoItem("Palabra clave", String.valueOf(idClave), tokenUpper));
                }
            }
        }
        for (String token : tokens) {
            if (!esTokenValido(token)) {
                items.add(new LexicoItem("Error", "-", "token invalido -> '" + token + "'"));
            }
        }

        return items;
    }

    private static void cargarTablaLexica(DefaultTableModel lexicoModel, List<LexicoItem> items) {
        lexicoModel.setRowCount(0);
        for (LexicoItem item : items) {
            lexicoModel.addRow(new Object[]{item.tipo, item.token, item.valor});
        }
    }

    private static List<String> extraerInstrucciones(String cadena) {
        Pattern patron = Pattern.compile("BGN\\s*\\{(.+?)\\}\\s*END", Pattern.DOTALL);
        Matcher match = patron.matcher(cadena);
        if (!match.find()) {
            System.out.println("Error: estructura del programa incorrecta. Falta BGN{...}END");
            return new ArrayList<>();
        }
        String bloque = match.group(1);
        String[] partes = bloque.trim().split(";");
        List<String> instrucciones = new ArrayList<>();
        for (String instr : partes) {
            String limpio = instr.trim();
            if (!limpio.isEmpty()) {
                instrucciones.add(limpio + ";");
            }
        }
        return instrucciones;
    }

    private static void analisisSintactico(List<String> instrucciones) {
        int i = 1;
        for (String instr : instrucciones) {
            analizarInstruccion(instr.trim(), i);
            i++;
        }
    }

    private static void analizarInstruccion(String instr, int i) {
        String[] validadores = new String[]{"mixAsignacion", "mix", "color", "show"};
        for (String validador : validadores) {
            ValidacionResultado resultado;
            switch (validador) {
                case "mixAsignacion":
                    resultado = validarMixAsignacion(instr);
                    break;
                case "mix":
                    resultado = validarMix(instr);
                    break;
                case "color":
                    resultado = validarColor(instr);
                    break;
                default:
                    resultado = validarShow(instr);
                    break;
            }
            if (resultado.esValido) {
                System.out.println("Instrucción " + i + " válida: '" + instr + "'\n");
                return;
            } else if (resultado.mensajeError != null) {
                System.out.println("Error en instrucción " + i + ": '" + instr + "'\n" + resultado.mensajeError);
                return;
            }
        }
        System.out.println("Error en instrucción " + i + ": '" + instr + "'\nInstrucción no reconocida o mal formada.");
    }

    private static ValidacionResultado validarColor(String instr) {
        if (instr.matches("COLOR\\s+([a-zA-Z_][a-zA-Z0-9_]*)\\s*=\\s*(#[A-F0-9]{6})\\s*;")) {
            return new ValidacionResultado(true, null);
        } else if (instr.contains("COLOR")) {
            if (!instr.matches(".*#[A-F0-9]{6}.*")) {
                return new ValidacionResultado(false,
                        "El color hexadecimal no es válido. Debe tener el formato: #RRGGBB (ej. #FF00FF)\n");
            }
            if (!instr.matches(".*[a-zA-Z_][a-zA-Z0-9_]*.*")) {
                return new ValidacionResultado(false,
                        "El identificador no es válido. Debe comenzar con letra o guion bajo.\n");
            }
            return new ValidacionResultado(false, "Formato inválido en la declaración de color.");
        }
        return new ValidacionResultado(false, null);
    }

    private static ValidacionResultado validarMix(String instr) {
        if (instr.matches("MIX\\s*\\(\\s*[a-zA-Z_][a-zA-Z0-9_]*\\s*,\\s*[a-zA-Z_][a-zA-Z0-9_]*\\s*\\)\\s*;")) {
            return new ValidacionResultado(true, null);
        } else if (instr.contains("MIX")) {
            return new ValidacionResultado(false,
                    "Formato inválido. El formato debe seguir la estructura: MIX(color1, color2);\n");
        }
        return new ValidacionResultado(false, null);
    }

    private static ValidacionResultado validarMixAsignacion(String instr) {
        if (instr.matches("COLOR\\s+[a-zA-Z_][a-zA-Z0-9_]*\\s*=\\s*MIX\\s*\\(\\s*[a-zA-Z_][a-zA-Z0-9_]*\\s*,\\s*[a-zA-Z_][a-zA-Z0-9_]*\\s*\\)\\s*;")) {
            return new ValidacionResultado(true, null);
        } else if (instr.contains("MIX") && instr.contains("COLOR")) {
            return new ValidacionResultado(false,
                    "Formato inválido. El formato debe seguir la estructura: COLOR id = MIX(color1, color2);\n");
        }
        return new ValidacionResultado(false, null);
    }

    private static ValidacionResultado validarShow(String instr) {
        if (instr.matches("SHOW\\s*\\(\\s*[a-zA-Z_][a-zA-Z0-9_]*\\s*\\)\\s*;")) {
            return new ValidacionResultado(true, null);
        } else if (instr.contains("SHOW")) {
            return new ValidacionResultado(false,
                    "Formato inválido. El formato debe seguir la estructura: SHOW(id);\n");
        }
        return new ValidacionResultado(false, null);
    }

    private static String eliminarComentarios(String cadena) {
        String[] lineas = cadena.split("\\n", -1);
        List<String> resultado = new ArrayList<>();
        for (int i = 0; i < lineas.length; i++) {
            String linea = lineas[i];
            String lineaStrip = linea.trim();
            int numeroLinea = i + 1;
            if (lineaStrip.startsWith("--")) {
                System.out.println("Línea " + numeroLinea + " ignorada por comentario: " + lineaStrip);
                continue;
            } else if (lineaStrip.matches("^-[^-].*")) {
                System.out.println("Línea " + numeroLinea + " contiene comentario mal formado: " + lineaStrip);
                resultado.add(lineaStrip);
            } else if (lineaStrip.contains("--")) {
                int pos = lineaStrip.indexOf("--");
                System.out.println("Línea " + numeroLinea + " con comentario al final: " + lineaStrip.substring(pos));
                resultado.add(lineaStrip.substring(0, pos));
            } else {
                resultado.add(linea);
            }
        }
        return String.join("\n", resultado);
    }

    private static class ValidacionResultado {
        private final boolean esValido;
        private final String mensajeError;

        private ValidacionResultado(boolean esValido, String mensajeError) {
            this.esValido = esValido;
            this.mensajeError = mensajeError;
        }
    }

    private static class LexicoItem {
        private final String tipo;
        private final String token;
        private final String valor;

        private LexicoItem(String tipo, String token, String valor) {
            this.tipo = tipo;
            this.token = token;
            this.valor = valor;
        }
    }
    
}
