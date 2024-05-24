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
        AFN.printMagenta(true, "Productions:");
        for (Map.Entry<String, List<String>> entry : productions.entrySet()) {
            AFN.printMagenta(true, entry.getKey() + " -> " + String.join(" | ", entry.getValue()));
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
            int stateCount;
            Map<String, Integer> stateMap = new HashMap<>();
            int currentState = 1; // Estado inicial 1
//            System.out.println("currentState = " + currentState);
//            System.out.println("currentState++ = " + currentState++);

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

            List<List<Set<Integer>>> transitions = new ArrayList<>();
            for (int i = 0; i <= terminals.size(); i++) { // Incluye transición de Lambda
                List<Set<Integer>> stateTransitions = new ArrayList<>();
                for (int j = 0; j < currentState; j++) {
                    stateTransitions.add(new HashSet<>());
                }
                transitions.add(stateTransitions);
            }

            System.out.println("stateMap: \n[");
            for (Map.Entry<String, Integer> entry : stateMap.entrySet()) {
                System.out.println("    Non-Terminal: " + entry.getKey() + ", State: " + entry.getValue());
            }
            System.out.println("]\n--------");

            int loopProductionsCounter = 1;

            for (Map.Entry<String, List<String>> entry : productions.entrySet()) {
                String fromNonTerminal = entry.getKey(); // W

                int fromState = stateMap.get(fromNonTerminal); // 1

                System.out.println("PRODUCTIONS: loop " + loopProductionsCounter++ + "/" + (productions.size()));
                System.out.print("|- fromNonTerminal: " + entry.getKey() + ", ");
                System.out.println("fromState: " + fromState);

                int loopEntryCounter = 1;

                for (String rule : entry.getValue()) {

                    System.out.println("| |- ENTRY: loop " + loopEntryCounter++ + "/" + (entry.getValue().size()));
                    System.out.print("| | |- Rule: " + rule + ", ");

                    currentState = fromState; // 2

                    System.out.println("currentState: " + currentState);

                    int prevState = currentState; // Para manjar las transiciones de Lambda, 2
                    for (int i = 0; i < rule.length(); i++) {
                        String symbol = String.valueOf(rule.charAt(i));

                        System.out.println("| | | |- RULE: loop " + (i + 1) + "/" + rule.length());
                        System.out.print("| | | | |- symbol: " + symbol + ", ");

                        if (terminals.contains(symbol)) {
                            int symbolIndex = terminals.indexOf(symbol) + 1; //1
                            int nextState;

                            System.out.print("symbolIndex: " + symbolIndex + ", ");

                            if (i == rule.length() - 1) { // 2 = 2
                                nextState = finalState; // 6

                                System.out.print("nextState: " + nextState + ", ");
//                                System.out.print("rule loop final... ");
                            } else {
                                if (Character.isUpperCase(rule.charAt(i + 1))) {
                                    nextState = stateMap.get(String.valueOf(rule.charAt(i + 1)));

                                    System.out.print("nextState > " + nextState + ", ");
                                    System.out.print("nextCharUpper: " + Character.isUpperCase(rule.charAt(i + 1)) + ", ");

                                    i++;
                                } else {
                                    nextState = currentState + 1;
                                    currentState = nextState;

                                    System.out.print("nextState = " + nextState + ", ");
                                    System.out.print("currentState = " + currentState + ", ");
                                }
                            }
                            transitions.get(symbolIndex).get(prevState).add(nextState);
                            prevState = nextState; // Update prevState for next transition

                            System.out.println("prevState: " + prevState + ", ");

                        } else if (nonTerminals.contains(symbol)) {
                            int nextState = stateMap.get(symbol);

                            System.out.println("nextState = " + nextState + ", ");

                            transitions.get(0).get(prevState).add(nextState); // Lambada transitions
                        }
                        System.out.println("| | | |");
                    }
                    System.out.println("| |");
                }
            }
            System.out.println("--------");

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
