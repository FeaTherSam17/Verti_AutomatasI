/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package verti_automatasi;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        String ruta = "entradas2.txt";
        try {
            String cadena = Files.readString(Paths.get(ruta), StandardCharsets.UTF_8);
            cadena = eliminarComentarios(cadena);
            analizarLexico(cadena);
            List<String> instrucciones = extraerInstrucciones(cadena);
            analisisSintactico(instrucciones);
        } catch (IOException ex) {
            System.out.println("No se encontró el archivo '" + ruta + "'.");
        }
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

    private static boolean analizarLexico(String cadena) {
        System.out.println("Análisis Léxico");

        System.out.println("\nSímbolos especiales encontrados:");
        boolean encontradaSimbolo = false;
        for (int i = 0; i < cadena.length(); i++) {
            char c = cadena.charAt(i);
            for (Map.Entry<Integer, String> entry : SIMBOLOS.entrySet()) {
                if (entry.getValue().charAt(0) == c) {
                    System.out.println("  Token " + entry.getKey() + ": " + entry.getValue());
                    encontradaSimbolo = true;
                }
            }
        }
        if (!encontradaSimbolo) {
            System.out.println("Ninguno");
        }

        String cadenaNormalizada = separarCadena(cadena);
        String[] tokens = cadenaNormalizada.trim().isEmpty()
                ? new String[0]
                : cadenaNormalizada.trim().split("\\s+");

        System.out.println("\nPalabras clave encontradas:");
        boolean encontradaClave = false;
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
                    System.out.println("  Token " + idClave + ": " + tokenUpper);
                    encontradaClave = true;
                }
            }
        }
        if (!encontradaClave) {
            System.out.println("Ninguna");
        }

        System.out.println("\nVerificación de errores léxicos:");
        boolean errores = false;
        for (String token : tokens) {
            if (!esTokenValido(token)) {
                System.out.println("  Error léxico: token inválido -> '" + token + "'");
                errores = true;
            }
        }
        if (!errores) {
            System.out.println("Ninguno\n");
        }

        return true;
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
    
}
