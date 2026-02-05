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

        JButton abrirBtn = new JButton("Abrir archivo");
        JButton analizarBtn = new JButton("Analizar");
        JButton limpiarBtn = new JButton("Limpiar");

        JLabel rutaLabel = new JLabel("Archivo: (sin seleccionar)");

        abrirBtn.addActionListener(e -> {
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

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(abrirBtn);
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
