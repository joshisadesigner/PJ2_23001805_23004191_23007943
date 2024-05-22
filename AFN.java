import java.io.*;
import java.util.*;

public class AFN {
	private boolean[] acceptanceStates;
	private List<List<List<List<String>>>> transitions = new ArrayList<>();
	private String[] alphabet;

	public AFN(String path) throws FileNotFoundException {
		loadAFN(path);
	}

	private void loadAFN(String path) throws FileNotFoundException {
		File file = new File(path);
		Scanner scanner = new Scanner(file);

		alphabet = scanner.nextLine().split(",");
		int maxSymbols = alphabet.length + 1;

		int maxStates = Integer.parseInt(scanner.nextLine());

		acceptanceStates = new boolean[maxStates];
		String[] finalStates = scanner.nextLine().split(",");
		for (String state : finalStates) {
			int finalStateIndex = Integer.parseInt(state.trim());
			acceptanceStates[finalStateIndex] = true;
		}

		for (int i = 0; i < maxSymbols; i++) {
			List<List<List<String>>> symbolTransitions = new ArrayList<>();
			for (int j = 0; j < maxStates; j++) {
				symbolTransitions.add(new ArrayList<>());
			}
			transitions.add(symbolTransitions);
		}

		for (int symbolIndex = 0; symbolIndex < maxSymbols; symbolIndex++) {
			if (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				String[] stateTransitions = line.split(",");

				for (int state = 0; state < maxStates; state++) {
					String[] stateTransitionsMultiple = stateTransitions[state].split(";");

					List<List<String>> currentTransitions = transitions.get(symbolIndex).get(state);

					currentTransitions.add(Arrays.asList(stateTransitionsMultiple));
				}
			}
		}
		scanner.close();
	}

	public void toAFD(String afdPath) throws FileNotFoundException {
		Map<Set<Integer>, Integer> afdStateMap = new HashMap<>();
		List<Set<Integer>> afdStates = new ArrayList<>();
		List<List<Integer>> afdTransitions = new ArrayList<>();
		Queue<Set<Integer>> statesToProcess = new LinkedList<>();

		Set<Integer> startState = applyLambdaTransitions(Collections.singleton(0));
		statesToProcess.add(startState);
		afdStateMap.put(startState, 0);
		afdStates.add(startState);

		while (!statesToProcess.isEmpty()) {
			Set<Integer> currentState = statesToProcess.poll();
			int afdStateIndex = afdStateMap.get(currentState);

			while (afdTransitions.size() <= afdStateIndex) {
				afdTransitions.add(new ArrayList<>(Collections.nCopies(alphabet.length, 0)));
			}

			for (int symbolIndex = 0; symbolIndex < alphabet.length; symbolIndex++) {
				Set<Integer> newState = new HashSet<>();

				for (int state : currentState) {
					if (state < transitions.get(symbolIndex + 1).size()) {
						List<List<String>> transitionsForState = transitions.get(symbolIndex + 1).get(state);

						for (List<String> transition : transitionsForState) {
							for (String nextState : transition) {
								newState.add(Integer.parseInt(nextState.trim()));
							}
						}
					}
				}

				newState = applyLambdaTransitions(newState);

				if (!afdStateMap.containsKey(newState)) {
					afdStates.add(newState);
					afdStateMap.put(newState, afdStates.size() - 1);
					statesToProcess.add(newState);
				}

				afdTransitions.get(afdStateIndex).set(symbolIndex, afdStateMap.get(newState));
			}
		}

		try (PrintWriter out = new PrintWriter(afdPath)) {
			out.println(String.join(",", alphabet));
			out.println(afdStates.size());

			StringJoiner acceptStatesJoiner = new StringJoiner(",");
			for (int i = 0; i < afdStates.size(); i++) {
				Set<Integer> state = afdStates.get(i);
				for (int afnState : state) {
					if (acceptanceStates[afnState]) {
						acceptStatesJoiner.add(Integer.toString(i));
						break;
					}
				}
			}
			out.println(acceptStatesJoiner.toString());

			for (List<Integer> transitionList : afdTransitions) {
				StringJoiner transitionJoiner = new StringJoiner(",");
				for (int nextState : transitionList) {
					transitionJoiner.add(Integer.toString(nextState));
				}
				out.println(transitionJoiner.toString());
			}
		}
	}

	private Set<Integer> applyLambdaTransitions(Set<Integer> states) {
		Set<Integer> closure = new HashSet<>(states);
		Queue<Integer> queue = new LinkedList<>(states);

		while (!queue.isEmpty()) {
			int state = queue.poll();
			if (state >= transitions.get(0).size()) {
				continue;
			}
			List<List<String>> lambdaTransitions = transitions.get(0).get(state);

			for (List<String> transition : lambdaTransitions) {
				for (String nextState : transition) {
					int nextStateInt = Integer.parseInt(nextState.trim());
					if (closure.add(nextStateInt)) {
						queue.add(nextStateInt);
					}
				}
			}
		}
		return closure;
	}
}
