import java.io.*;
import java.util.*;

/*
	Utilice esta clase para guardar la informacion de su
	AFN. NO DEBE CAMBIAR LOS NOMBRES DE LA CLASE NI DE LOS
	METODOS que ya existen, sin embargo, usted es libre de
	agregar los campos y metodos que desee.
*/
public class AFN {
	// Variables para marcar estados de aceptación y almacenar transiciones
	private boolean[] acceptanceStates;
	private List<List<List<List<String>>>> transitions = new ArrayList<>();
	private String[] alphabet; // Almacenar el alfabeto de entrada

	// Constantes para manejo de color en la consola
	public static final String ANSI_MAGENTA = "\033[0;35m";
	public static final String ANSI_RESET = "\u001B[0m";

	/*
        Implemente el constructor de la clase AFN
        que recibe como argumento un string que
        representa el path del archivo que contiene
        la informacion del AFN (i.e. "Documentos/archivo.AFN").
        Puede utilizar la estructura de datos que desee
    */
	public static void printMagenta(boolean newLine, String... messages) {
		System.out.print(ANSI_MAGENTA);
		for (String msg : messages) {
			System.out.print(msg + " ");
		}
		if (newLine) {
			System.out.println(ANSI_RESET);
		} else {
			System.out.print(ANSI_RESET);
		}
	}

	// Constructor que carga un AFN desde un archivo
	public AFN(String path) throws FileNotFoundException {
		loadAFN(path);
	}

	// Método para cargar los datos del AFN desde un archivo especificado
	public void loadAFN(String path) throws FileNotFoundException {
		File file = new File(path);
		Scanner scanner = new Scanner(file);

		// Leer y almacenar el alfabeto
		alphabet = scanner.nextLine().split(",");
		int maxSymbols = alphabet.length + 1; // Contando lambda

		// Leer cantidad de estados
		int maxStates = Integer.parseInt(scanner.nextLine());

		// Marcar estados de aceptación
		acceptanceStates = new boolean[maxStates + 1];
		String[] finalStates = scanner.nextLine().split(",");
		for (String state : finalStates) {
			int finalStateIndex = Integer.parseInt(state.trim());
			acceptanceStates[finalStateIndex] = true;
		}

		// Inicializar la estructura para almacenar transiciones
		for (int i = 0; i < maxSymbols; i++) {
			List<List<List<String>>> symbolTransitions = new ArrayList<>();
			for (int j = 0; j < maxStates + 1; j++) {
				symbolTransitions.add(new ArrayList<>());
			}
			transitions.add(symbolTransitions);
		}

		// Itera sobre cada símbolo en el alfabeto, incluyendo las transiciones lambda (asumido en el índice 0).
		for (int symbolIndex = 0; symbolIndex < maxSymbols; symbolIndex++) {
			// Comprueba si hay más líneas en el archivo para leer. Cada línea representa las transiciones para un símbolo específico.
			if (scanner.hasNextLine()) {
				// Lee la próxima línea del archivo, que contiene las transiciones para el símbolo actual.
				String line = scanner.nextLine();

				// Divide la línea por comas para obtener las transiciones de cada estado.
				// Cada elemento del array representa las transiciones desde un estado específico para el símbolo actual.
				String[] stateTransitions = line.split(",");

				// Itera sobre cada estado para el cual se han definido transiciones en la línea.
				for (int state = 0; state < maxStates; state++) {
					// Divide las transiciones de cada estado por punto y coma para obtener transiciones múltiples desde un mismo estado.
					// Por ejemplo, si un estado puede transitar a varios estados en una misma entrada, estarán separados por punto y coma.
					String[] stateTransitionsMultiple = stateTransitions[state].split(";");

					// Accede a la lista de listas de transiciones para el símbolo y estado actual.
					// Esta estructura permite almacenar múltiples destinos de transición para cada combinación de símbolo y estado.
					List<List<String>> currentTransitions = transitions.get(symbolIndex).get(state); // transitions[][]

					// Agrega la nueva lista de transiciones (convertida de array a lista) a la lista de transiciones del estado actual.
					currentTransitions.add(List.of(stateTransitionsMultiple));
				}
			}
		}

//		printTransitionsTable(maxSymbols, maxStates);
		scanner.close();
	}

	// Método para imprimir tabla de transiciones, útil para depuración
	public void printTransitionsTable(int maxSymbols, int maxStates) {
		System.out.format("%-10s %-20s %s%n", "Símbolo", "Estado", "Transiciones");
		printMagenta(true, "---------------------------------------------------------");
		for (int i = 0; i < maxSymbols; i++) {
			String symbol = (i == 0) ? "λ" : alphabet[i - 1];
			for (int j = 0; j < maxStates; j++) {
				List<List<String>> transitionLists = transitions.get(i).get(j);
				StringBuilder transitionDescription = new StringBuilder();
				for (List<String> transition : transitionLists) {
					transitionDescription.append(transition.toString() + ", ");
				}
				if (transitionDescription.length() > 0)
					transitionDescription.setLength(transitionDescription.length() - 2);
				else
					transitionDescription.append("{}");
				System.out.format("%-10s %-20s %s%n", "'" + symbol + "'", "Estado " + j, transitionDescription.toString());
			}
			System.out.println();
		}
	}

	/*
		Implemente el metodo accept, que recibe como argumento
		un String que representa la cuerda a evaluar, y devuelve
		un boolean dependiendo de si la cuerda es aceptada o no
		por el AFN. Recuerde lo aprendido en el proyecto 1.
	*/
	public boolean accept(String string) {
		Set<Integer> currentStates = new HashSet<>();
		currentStates.add(1); // Asumiendo que el estado inicial es siempre 1

		currentStates = applyLambdaTransitions(currentStates);
		Map<Character, Integer> symbolToIndex = new HashMap<>();
		for (int i = 0; i < alphabet.length; i++) {
			symbolToIndex.put(alphabet[i].charAt(0), i + 1);
		}

		// Itera sobre cada carácter de la cadena de entrada.
		for (char c : string.toCharArray()) {
			// Comprueba si el carácter actual está en el alfabeto del autómata.
			// Si no está, la cadena no es aceptada porque el autómata no tiene transiciones para ese carácter.
			if (!symbolToIndex.containsKey(c)) {
				return false; // Si el carácter no está en el alfabeto, la cadena no es aceptada
			}

			// Obtiene el índice del símbolo correspondiente al carácter actual, necesario para acceder a sus transiciones.
			int symbolIndex = symbolToIndex.get(c);

			// Prepara un nuevo conjunto para almacenar los estados que se pueden alcanzar desde los estados actuales usando el carácter actual.
			Set<Integer> nextStates = new HashSet<>();

			// Itera sobre cada estado actual en el conjunto de estados actuales.
			for (int currentState : currentStates) {
				// Obtiene las transiciones para el estado actual basadas en el carácter actual.
				List<List<String>> transitionsForState = transitions.get(symbolIndex).get(currentState);

				// Itera sobre cada lista de transiciones (cada lista es un grupo de estados destino).
				for (List<String> transitionList : transitionsForState) {
					// Itera sobre cada estado destino en la lista de transiciones.
					for (String nextState : transitionList) {
						// Intenta convertir el estado de destino a entero y agregarlo al conjunto de los próximos estados.
						try {
							nextStates.add(Integer.parseInt(nextState.trim()));
						} catch (NumberFormatException e) {
							// Captura y maneja la excepción si el número de estado no se puede convertir a entero.
							System.err.println("Error parsing state number: " + nextState);
						}
					}
				}
			}

			// Aplica transiciones lambda a los estados recién alcanzados para incluir todos los estados posibles alcanzables.
			currentStates = applyLambdaTransitions(nextStates);

			// Si después de aplicar transiciones, incluyendo las lambda, no hay estados alcanzables,
			// la cadena no puede ser aceptada y se retorna false.
			if (currentStates.isEmpty()) {
				return false;
			}
		}

		for (int state : currentStates) {
			if (acceptanceStates[state]) {
				return true;
			}
		}

		return false;
	}

	// Método para aplicar transiciones lambda
	private Set<Integer> applyLambdaTransitions(Set<Integer> currentStates) {
		Set<Integer> statesWithLambda = new HashSet<>(currentStates);
		boolean changed;

		// Bucle para aplicar transiciones lambda. Se repite mientras se encuentren nuevos estados alcanzables mediante transiciones lambda.
		do {
			// Inicializa 'changed' a false para rastrear si se encuentra algún nuevo estado en esta iteración.
			changed = false;
			// 'newStates' almacena los nuevos estados alcanzados en esta iteración para evitar modificar 'statesWithLambda' mientras se itera.
			Set<Integer> newStates = new HashSet<>();

			// Itera sobre cada estado actual en el conjunto 'statesWithLambda'.
			for (int state : statesWithLambda) {
				// Accede a las transiciones lambda para el estado actual. Lambda se asume en la posición 0 del array de transiciones.
				List<List<String>> lambdaTransitions = transitions.get(0).get(state);

				// Itera sobre cada lista de transiciones lambda disponibles para el estado actual.
				for (List<String> transition : lambdaTransitions) {
					// Itera sobre cada estado destino en la lista de transiciones lambda.
					for (String nextState : transition) {
						// Convierte el estado destino a entero y lo recorta para eliminar espacios en blanco.
						int nextStateInt = Integer.parseInt(nextState.trim());

						// Intenta agregar el estado destino al conjunto 'statesWithLambda'. Si es nuevo, lo agrega a 'newStates'.
						if (statesWithLambda.add(nextStateInt)) {
							// Agrega el estado a 'newStates' y marca que hubo un cambio.
							newStates.add(nextStateInt);
							changed = true;
						}
					}
				}
			}

			// Agrega todos los nuevos estados encontrados a 'statesWithLambda' para considerarlos en la siguiente iteración.
			statesWithLambda.addAll(newStates);
		} while (changed); // Continúa mientras se encuentren nuevos estados.


		return statesWithLambda;
	}

	/*
		Implemente el metodo toAFD. Este metodo debe generar un archivo
		de texto que contenga los datos de un AFD segun las especificaciones
		del proyecto.
	*/
	public void toAFD(String afdPath) throws FileNotFoundException {
		Map<Set<Integer>, Integer> afdStateMap = new HashMap<>();
		List<Set<Integer>> afdStates = new ArrayList<>();
		List<List<Integer>> afdTransitions = new ArrayList<>();
		Queue<Set<Integer>> statesToProcess = new LinkedList<>();

		Set<Integer> startState = applyLambdaTransitions(Collections.singleton(1));
		statesToProcess.add(startState);
		afdStateMap.put(startState, 1);
		afdStates.add(startState);

		// Itera sobre cada símbolo en el alfabeto del AFN
		for (int i = 0; i < alphabet.length; i++) {
			// Agrega una nueva lista a afdTransitions para manejar las transiciones del AFD para el i-ésimo símbolo del alfabeto
			// La lista se inicializa con tantos ceros como estados en el AFD, donde cada cero representa un estado de transición no definido
			// o un estado de error por defecto.
			//
			// Collections.nCopies(afdStates.size(), 0) crea una lista prellenada con ceros. El número de ceros corresponde al número de estados
			// en el AFD, asegurando que cada estado tenga una entrada de transición (aunque sea un estado de error inicialmente).
			afdTransitions.add(new ArrayList<>(Collections.nCopies(afdStates.size(), 0)));
		}


		while (!statesToProcess.isEmpty()) {
			Set<Integer> currentState = statesToProcess.poll();
			int afdStateIndex = afdStateMap.get(currentState);

			while (afdTransitions.size() <= afdStateIndex) {
				afdTransitions.add(new ArrayList<>(Collections.nCopies(alphabet.length, 0)));
			}

			for (int symbolIndex = 1; symbolIndex <= alphabet.length; symbolIndex++) {
				Set<Integer> newState = new HashSet<>();
				// Itera sobre cada estado actual en el conjunto de estados actuales
				for (int state : currentState) {
					// Verifica si el índice del estado actual es menor que el tamaño de la lista de transiciones para el símbolo actual
					// Esto es necesario para evitar errores de índice fuera de límites al acceder a la lista
					if (state < transitions.get(symbolIndex).size()) {
						// Obtiene la lista de listas de transiciones para el estado actual bajo el símbolo actual
						// Cada lista interna representa un conjunto de estados a los que se puede llegar desde 'state' bajo el símbolo de 'symbolIndex'
						List<List<String>> transitionsForState = this.transitions.get(symbolIndex).get(state);

						// Itera sobre cada lista de transiciones, donde cada lista contiene estados de destino en formato de cadena
						for (List<String> transition : transitionsForState) {
							// Itera sobre cada estado de destino en la lista de transición
							for (String nextState : transition) {
								// Convierte el estado de destino de formato de cadena a entero y elimina espacios en blanco
								// Luego, agrega el estado convertido al conjunto de nuevos estados
								// Esto acumula todos los estados alcanzables desde el estado actual bajo el símbolo actual
								newState.add(Integer.parseInt(nextState.trim()));
							}
						}
					}
				}


				newState = applyLambdaTransitions(newState);

				if (!afdStateMap.containsKey(newState)) {
					afdStates.add(newState);
					afdStateMap.put(newState, afdStates.size());
					statesToProcess.add(newState);
				}

				while (afdTransitions.get(afdStateIndex).size() <= symbolIndex) {
					afdTransitions.get(afdStateIndex).add(0);
				}

				int nextStateIndex = afdStateMap.getOrDefault(newState, 0);
				afdTransitions.get(afdStateIndex).set(symbolIndex - 1, nextStateIndex);
			}
		}

		try (PrintWriter out = new PrintWriter(afdPath)) {
			out.println(String.join(",", alphabet));
			out.println(afdStates.size());

			// Crea un StringJoiner para construir una lista de estados de aceptación separada por comas.
			StringJoiner acceptStatesJoiner = new StringJoiner(",");

			// Itera sobre todos los estados en el AFD.
			for (int i = 0; i < afdStates.size(); i++) {
				// Obtiene el conjunto de estados del AFN que componen el i-ésimo estado del AFD.
				Set<Integer> state = afdStates.get(i);

				// Itera sobre cada estado del AFN que forma parte del estado actual del AFD.
				for (int afnState : state) {
					// Comprueba si el estado actual del AFN es un estado de aceptación.
					if (acceptanceStates[afnState]) {
						// Si es un estado de aceptación, añade el índice del estado del AFD al StringJoiner.
						// Los índices en el AFD comienzan en 1, por lo que se añade 1 al índice actual.
						acceptStatesJoiner.add(Integer.toString(i + 1));

						// Sal del bucle interno una vez que se encuentra un estado de aceptación,
						// ya que no es necesario verificar los demás estados del AFN en este estado del AFD.
						break;
					}
				}
			}

			// Escribe la lista de estados de aceptación generada al archivo de salida.
			// El resultado es una cadena de números separados por comas, donde cada número representa un estado de aceptación en el AFD.
			out.println(acceptStatesJoiner.toString());

			for (int i = 1; i <= afdStateMap.size(); i++) {
				List<Integer> transitionList = afdTransitions.get(i);
				StringJoiner transitionJoiner = new StringJoiner(",");
				for (int nextState : transitionList) {
					transitionJoiner.add(Integer.toString(nextState));
				}
				out.println(transitionJoiner.toString());
			}
		}
	}

	/*
		El metodo main debe recibir como primer argumento el path
		donde se encuentra el archivo ".afd" y debe empezar a evaluar
		cuerdas ingresadas por el usuario una a una hasta leer una cuerda vacia (""),
		en cuyo caso debe terminar. Tiene la libertad de implementar este metodo
		de la forma que desee. Si se envia la bandera "-to-afd", entonces en vez de
		evaluar, debe generar un archivo .afd
	*/
	public static void main(String[] args) throws FileNotFoundException {
		if (args.length == 0) {
			System.out.println("No se ha proporcionado el path del archivo.");
			return;
		}

		AFN afnFile = new AFN(args[0]);

		// Comprueba si el segundo argumento del programa es "-to-afd", lo que indica la solicitud de convertir el AFN a AFD.
		if (args.length > 1 && args[1].equals("-to-afd")) {
			// Verifica si se ha proporcionado un tercer argumento, que debería ser el path de salida para el AFD.
			if (args.length < 3) {
				// Imprime un mensaje de error si no se ha proporcionado el path de salida.
				System.out.println("No se ha proporcionado el path de salida para el AFD.");
				return; // Termina la ejecución si no se proporciona el path de salida.
			}
			// Llama al método para convertir el AFN a AFD, pasando el path de salida como argumento.
			afnFile.toAFD(args[2]);
			// Imprime un mensaje indicando que la conversión ha sido completada y muestra el path donde se guardaron los resultados.
			System.out.println("Conversión a AFD completada. Resultados guardados en: " + args[2]);
		} else {
			// Si no se pide convertir a AFD, entra en modo de evaluación de cadenas.

			// Prepara para leer cadenas de entrada desde la consola.
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			// Imprime instrucciones para el usuario.
			System.out.println("Ingrese cadenas para evaluar (una vacía para terminar):");

			// Lee líneas desde la consola en un bucle.
			String line;
			try {
				while ((line = reader.readLine()) != null && !line.isEmpty()) {
					// Llama al método 'accept' del objeto afnFile para determinar si la cadena es aceptada por el AFN.
					boolean accepted = afnFile.accept(line);
					// Imprime el resultado de la evaluación para cada cadena ingresada.
					System.out.println("La cadena '" + line + "' es " + (accepted ? "aceptada" : "rechazada") + " por el AFN.");
				}
			} catch (IOException e) {
				// Captura y reporta errores que puedan ocurrir durante la lectura de entrada.
				System.err.println("Error al leer entrada: " + e.getMessage());
			}
		}

	}
}