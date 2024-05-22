import java.io.*;
import java.util.*;

public class Gramatica {
    private Set<String> nonTerminals;
    private Set<String> terminals;
    private String startSymbol;
    private List<String> productionRules;

    public Gramatica(String path) throws FileNotFoundException {
        loadGrammar(path);
    }

    private void loadGrammar(String path) throws FileNotFoundException {
        File file = new File(path);
        Scanner scanner = new Scanner(file);

        nonTerminals = new HashSet<>(Arrays.asList(scanner.nextLine().split(",")));
        terminals = new HashSet<>(Arrays.asList(scanner.nextLine().split(",")));
        startSymbol = scanner.nextLine().trim();
        productionRules = new ArrayList<>();
        while (scanner.hasNextLine()) {
            productionRules.add(scanner.nextLine().trim());
        }
        scanner.close();
    }

    public void toAFN(String afnPath) throws FileNotFoundException {
        Set<String> states = new HashSet<>();
        Map<String, Integer> stateIndex = new HashMap<>();
        int stateCounter = 0;

        String initialState = "0";
        String finalState = "5";

        states.add(initialState);
        states.add(finalState);

        stateIndex.put(initialState, stateCounter++);
        stateIndex.put(finalState, stateCounter++);

        Map<String, Map<String, List<String>>> transitions = new HashMap<>();
        for (String state : states) {
            transitions.put(state, new HashMap<>());
            for (String terminal : terminals) {
                transitions.get(state).put(terminal, new ArrayList<>());
            }
        }

        for (String rule : productionRules) {
            String[] parts = rule.split("->");
            String lhs = parts[0].trim();
            String rhs = parts[1].trim();

            if (!stateIndex.containsKey(lhs)) {
                stateIndex.put(lhs, stateCounter++);
                states.add(lhs);
                transitions.put(lhs, new HashMap<>());
                for (String terminal : terminals) {
                    transitions.get(lhs).put(terminal, new ArrayList<>());
                }
            }

            String currentState = lhs;
            for (char c : rhs.toCharArray()) {
                String symbol = String.valueOf(c);
                if (!stateIndex.containsKey(currentState)) {
                    stateIndex.put(currentState, stateCounter++);
                    states.add(currentState);
                    transitions.put(currentState, new HashMap<>());
                    for (String terminal : terminals) {
                        transitions.get(currentState).put(terminal, new ArrayList<>());
                    }
                }

                if (terminals.contains(symbol)) {
                    String nextState = String.valueOf(stateCounter);
                    stateIndex.put(nextState, stateCounter++);
                    states.add(nextState);
                    transitions.put(nextState, new HashMap<>());
                    for (String terminal : terminals) {
                        transitions.get(nextState).put(terminal, new ArrayList<>());
                    }
                    transitions.get(currentState).get(symbol).add(nextState);
                    currentState = nextState;
                } else {
                    transitions.get(currentState).get("λ").add(symbol);
                }
            }
            transitions.get(currentState).get("λ").add(finalState);
        }

        try (PrintWriter out = new PrintWriter(afnPath)) {
            out.println(String.join(",", terminals));
            out.println(states.size());
            out.println(finalState);

            for (String terminal : terminals) {
                for (String state : states) {
                    List<String> trans = transitions.get(state).get(terminal);
                    out.print(String.join(",", trans));
                    out.print(",");
                }
                out.println();
            }
        }
    }

    public void checkGrammar() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Ingrese cadenas para evaluar (una vacía para terminar):");

        String line;
        try {
            while ((line = scanner.nextLine()) != null && !line.isEmpty()) {
                // Aquí se puede implementar la lógica de evaluación de cuerdas usando la gramática directamente
                // Esta parte debe ser implementada según los detalles específicos del proyecto
                System.out.println("Evaluar la cuerda: " + line);
                // Placeholder para lógica de aceptación
                boolean accepted = false; // Implementar la lógica de aceptación
                System.out.println("La cadena '" + line + "' es " + (accepted ? "aceptada" : "rechazada") + " por la gramática.");
            }
        } catch (Exception e) {
            System.err.println("Error al leer entrada: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }

    public static void main(String[] args) throws FileNotFoundException {
        if (args.length < 2) {
            System.out.println("Uso: java Gramatica <path_gramatica> (-afn|-afd|-check) [archivo_salida]");
            return;
        }

        Gramatica gramatica = new Gramatica(args[0]);

        switch (args[1]) {
            case "-afn":
                if (args.length < 3) {
                    System.out.println("No se ha proporcionado el archivo de salida para el AFN.");
                    return;
                }
                gramatica.toAFN(args[2]);
                System.out.println("Conversión a AFN completada. Resultados guardados en: " + args[2]);
                break;

            case "-afd":
                if (args.length < 3) {
                    System.out.println("No se ha proporcionado el archivo de salida para el AFD.");
                    return;
                }
                String afnPath = args[2].replace(".afd", ".afn");
                gramatica.toAFN(afnPath);
                AFN afn = new AFN(afnPath);
                afn.toAFD(args[2]);
                System.out.println("Conversión a AFD completada. Resultados guardados en: " + args[2]);
                break;

            case "-check":
                gramatica.checkGrammar();
                break;

            default:
                System.out.println("Bandera no reconocida. Uso: java Gramatica <path_gramatica> (-afn|-afd|-check) [archivo_salida]");
                break;
        }
    }
}
