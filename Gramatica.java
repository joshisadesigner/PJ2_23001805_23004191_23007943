import java.io.*;
import java.util.*;

public class Gramatica {
    private List<String> nonTerminals;
    private List<String> terminals;
    private String startSymbol;
    private Map<String, List<String>> productions;

    public Gramatica(String path) throws FileNotFoundException {
        loadGrammar(path);
    }

    private void loadGrammar(String path) throws FileNotFoundException {
        try (Scanner scanner = new Scanner(new File(path))) {
            nonTerminals = Arrays.asList(scanner.nextLine().trim().replaceAll("\\s+", "").split(","));
            terminals = Arrays.asList(scanner.nextLine().trim().replaceAll("\\s+", "").split(","));
            startSymbol = scanner.nextLine().trim();
            productions = new HashMap<>();
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (!line.isEmpty()) {
                    String[] parts = line.split("->");
                    String nonTerminal = parts[0].trim();
                    String[] rules = parts[1].trim().split("\\|");
                    productions.computeIfAbsent(nonTerminal, k -> new ArrayList<>()).addAll(Arrays.asList(rules));
                }
            }
        }
    }

    public void toAFN(String afnPath) throws FileNotFoundException {
        File outputFile = new File(afnPath);
        File parentDir = outputFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        try (PrintWriter out = new PrintWriter(afnPath)) {
            out.println(String.join(",", terminals));
            int stateCount = nonTerminals.size() + 2; // +1 para el estado final y +1 para el estado inicial
            out.println(stateCount);
            int finalState = stateCount - 1;
            out.println(finalState);

            List<List<Set<Integer>>> transitions = new ArrayList<>();
            for (int i = 0; i < terminals.size(); i++) {
                List<Set<Integer>> stateTransitions = new ArrayList<>();
                for (int j = 0; j < stateCount; j++) {
                    stateTransitions.add(new HashSet<>());
                }
                transitions.add(stateTransitions);
            }

            for (Map.Entry<String, List<String>> entry : productions.entrySet()) {
                String fromNonTerminal = entry.getKey();
                int fromState = nonTerminals.indexOf(fromNonTerminal);
                if (fromState == -1) {
                    fromState = nonTerminals.size();
                }
                for (String rule : entry.getValue()) {
                    int currentState = fromState;
                    for (int i = 0; i < rule.length(); i++) {
                        String symbol = String.valueOf(rule.charAt(i));
                        if (terminals.contains(symbol)) {
                            int symbolIndex = terminals.indexOf(symbol);
                            int nextState;

                            if (i == rule.length() - 1) {
                                nextState = finalState;
                            } else {
                                nextState = stateCount++;
                                for (List<Set<Integer>> stateTransitions : transitions) {
                                    stateTransitions.add(new HashSet<>());
                                }
                            }

                            transitions.get(symbolIndex).get(currentState).add(nextState);
                            currentState = nextState;
                        } else if (nonTerminals.contains(symbol)) {
                            if (i != rule.length() - 1) {
                                throw new IllegalArgumentException("No terminal '" + symbol + "' debe ser el último símbolo en la regla '" + rule + "'");
                            }

                            int nextState = nonTerminals.indexOf(symbol);
                            transitions.get(terminals.size() - 1).get(currentState).add(nextState);
                        } else {
                            throw new IllegalArgumentException("Símbolo '" + symbol + "' no encontrado en las listas de terminales o no terminales.");
                        }
                    }
                }
            }

            for (List<Set<Integer>> symbolTransitions : transitions) {
                for (Set<Integer> stateTransition : symbolTransitions) {
                    if (stateTransition.isEmpty()) {
                        out.print("0,");
                    } else {
                        List<String> stateList = new ArrayList<>();
                        for (Integer state : stateTransition) {
                            stateList.add(state.toString());
                        }
                        out.print(String.join(";", stateList) + ",");
                    }
                }
                out.println();
            }
        }
    }

    public void toAFD(String afdPath) throws FileNotFoundException {
        toAFN("temp.afn");
        AFN afn = new AFN("temp.afn");
        afn.toAFD(afdPath);
    }

    public void check() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            System.out.println("Ingrese cadenas para evaluar (una vacía para terminar):");
            String line;
            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                boolean accepted = false; // Implementar lógica de evaluación
                System.out.println("La cadena '" + line + "' es " + (accepted ? "aceptada" : "rechazada") + " por la gramática.");
            }
        } catch (IOException e) {
            System.err.println("Error al leer entrada: " + e.getMessage());
        }
    }

    public static void main(String[] args) throws FileNotFoundException {
        if (args.length < 2) {
            System.out.println("Uso: java Gramatica path_gramatica (-afn|-afd|-check) [archivo_salida]");
            return;
        }

        String pathGramatica = args[0];
        String modo = args[1];
        String archivoSalida = (args.length > 2) ? args[2] : null;

        Gramatica gramatica = new Gramatica(pathGramatica);

        switch (modo) {
            case "-afn":
                if (archivoSalida == null) {
                    System.out.println("Se requiere un archivo de salida para el modo -afn.");
                    return;
                }
                gramatica.toAFN(archivoSalida);
                System.out.println("AFN generado en: " + archivoSalida);
                break;
            case "-afd":
                if (archivoSalida == null) {
                    System.out.println("Se requiere un archivo de salida para el modo -afd.");
                    return;
                }
                gramatica.toAFN("temp.afn");
                AFN afn = new AFN("temp.afn");
                afn.toAFD(archivoSalida);
                System.out.println("AFD generado en: " + archivoSalida);
                break;
            case "-check":
                gramatica.check();
                break;
            default:
                System.out.println("Modo de ejecución no reconocido: " + modo);
        }
    }
}
