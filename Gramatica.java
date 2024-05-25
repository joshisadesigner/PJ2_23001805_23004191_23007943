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
                    String[] parts = line.split("->"); // [W,abX]
                    String nonTerminal = parts[0].trim();  // nonTerminal = W
                    String[] rules = parts[1].trim().split("\\|"); // rules = abX
                    productions.computeIfAbsent(nonTerminal, k -> new ArrayList<>()).addAll(Arrays.asList(rules));
                }
            }
        }

        System.out.println("nonTerminals" + nonTerminals + "\n--------");
        System.out.println("terminals: " + terminals + "\n--------");
        System.out.println("startSymbol: " + startSymbol + "\n--------");

        // Imprimir el Map
        AFN.printMagenta("", true, "Productions:");
        for (Map.Entry<String, List<String>> entry : productions.entrySet()) {
            AFN.printMagenta("", true, entry.getKey() + " -> " + String.join(" | ", entry.getValue()));
        }
        System.out.println("--------");
    }

    public void toAFN(String afnPath) throws FileNotFoundException {
        File outputFile = new File(afnPath);
        File parentDir = outputFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs(); // /new/file.afn
        }

        try (PrintWriter out = new PrintWriter(afnPath)) {
            out.println(String.join(",", terminals)); // [a,b,c,d]
            Map<String, Integer> stateMap = new HashMap<>();
            ArrayList<String> rulesList = new ArrayList<>();
            int stateCount;
            int currentState = 1; // Estado inicial 1

            stateMap.put(startSymbol, currentState++); // W, 1

            for (String nt : nonTerminals) { // [W, X, Y, Z]
                if (!stateMap.containsKey(nt)) {
                    stateMap.put(nt, currentState++);
                }
            }

            int finalState = currentState; // currentState = 5
            stateCount = finalState + 1; // currentState = 6
            out.println(stateCount);
            out.println(finalState);

            List<List<Set<String>>> transitions = new ArrayList<>();
            for (int i = 0; i <= terminals.size(); i++) { // Incluye transición de Lambda
                List<Set<String>> stateTransitions = new ArrayList<>();
                for (int j = 0; j < currentState; j++) {
                    stateTransitions.add(new HashSet<>());
                }
                transitions.add(stateTransitions);
            }

            System.out.println("stateMap: [");
            for (Map.Entry<String, Integer> entry : stateMap.entrySet()) {
                System.out.println(" Non-Terminal: " + entry.getKey() + ", State: " + entry.getValue());
            }
            System.out.println("]\n--------");

            for (Map.Entry<String, List<String>> entry : productions.entrySet()) {
                String fromNonTerminal = entry.getKey(); // W
                int fromState = stateMap.get(fromNonTerminal); // 1
                int prevState = 0; // Para manjar las transiciones de Lambda, 2
                int nextState;

                System.out.println("entry: " + entry);

                for (String rule : entry.getValue()) {
                    AFN.printMagenta("", true, "|- Rule: " + rule + ", ");

                    if (!rulesList.contains(rule)) {
                        rulesList.add(rule);
                    }

                    for (int i = 0; i < rule.length(); i++) {
                        String symbol = String.valueOf(rule.charAt(i));

                        if (terminals.contains(symbol)) {
                            int symbolIndex = terminals.indexOf(symbol) + 1; // debug only

                            if (i == rule.length() - 1) { // 2 = 2
                                nextState = ++fromState; // 6
                                ++prevState;
                            } else {
                                if (Character.isUpperCase(rule.charAt(i + 1))) {
                                    nextState = stateMap.get(String.valueOf(rule.charAt(i + 1))) + 1;
                                    ++prevState;
                                } else {
                                    nextState = fromState + 1;
                                }
                            }

                            AFN.printMagenta(AFN.ANSI_YELLOW, true, "|  |- symbol: " + symbol + ", line: " + symbolIndex );
                            System.out.println("|  |  |- Row : " + symbolIndex + ", ");
                            System.out.println("|  |  |- Col : " + prevState + ", ");
                            System.out.println("|  |  |- Num : " + nextState + ", ");
                            System.out.println("|  |  |- fromState: " + fromState + ", ");

                            transitions.get(symbolIndex).get(prevState).add(String.valueOf(nextState));

                        } else if (nonTerminals.contains(symbol)) {
                            nextState = stateMap.get(symbol);
                            AFN.printMagenta(AFN.ANSI_GREEN, true, "|  |- *symbol: " + symbol + ", line: 0" );
                            System.out.println("|  |  |- *prevState: " + prevState + ", ");
                            System.out.println("|  |  |- *nextState = " + nextState + ", ");
                            System.out.println("|  |  |- *fromState: " + fromState + ", ");

                            transitions.get(0).get(prevState).add(String.valueOf(nextState)); // Lambada transitions
                        }
                    }
                }
            }
            System.out.println("--------");

            System.out.println("Transitions");
            for (int i = 0; i< transitions.size(); i++) {
                System.out.print(((i == 0) ? "λ" : terminals.get(i - 1)) + ": ");
                List<Set<String>> stateTransistions = transitions.get(i);
                for (int j = 0; j < stateTransistions.size(); j++) {
                    System.out.print(stateTransistions.get(j) + ((j == stateTransistions.size() - 1) ? "" : ","));
                }
                System.out.println();
            }
            int symbolCounter = 0; // Inicializa un contador para llevar el seguimiento de las transiciones de símbolos

            for (List<Set<String>> symbolTransitions : transitions) { // Itera sobre cada conjunto de transiciones simbólicas

                // Verifica si es la primera transición
                boolean isFirstTransition = symbolCounter == 0; // Verifica si el contador de símbolos es 0, lo que significa que es la primera transición

                symbolCounter++; // Incrementa el contador de símbolos

                for (int i = 0; i < symbolTransitions.size(); i++) { // Itera sobre cada conjunto de transiciones de estado dentro de la transición simbólica
                    Set<String> stateTransition = symbolTransitions.get(i);

                    if (isFirstTransition && stateTransition.isEmpty()) { // Si es la primera transición y el conjunto de transiciones de estado está vacío

                        // Si es la primera transición y el estado es vacío, imprime el estado actual
                        out.print(currentState); // Imprime el estado actual seguido de una coma

                    } else if (stateTransition.isEmpty()) { // Si no es la primera transición y el conjunto de transiciones de estado está vacío

                        out.print("0"); // Imprime "0," para indicar que no hay transición

                    } else { // Si el conjunto de transiciones de estado no está vacío

                        List<String> stateList = new ArrayList<>(); // Crea una lista para almacenar los estados como cadenas

                        for (String state : stateTransition) { // Itera sobre cada estado en el conjunto de transiciones de estado
                            stateList.add(state.toString()); // Agrega el estado a la lista como una cadena
                        }

                        out.print(String.join(";", stateList)); // Imprime los estados en la lista separados por punto y coma, seguidos de una coma
                    }

                    if (i < symbolTransitions.size() - 1) { // Check if it's not the last element
                        out.print(","); // Print a comma
                    }
                }

                out.println(); // Imprime un salto de línea después de procesar todas las transiciones de estado para un símbolo
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
                System.out.println("La cadena '" + line + "' es " + "rechazada" + " por la gramática.");
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
