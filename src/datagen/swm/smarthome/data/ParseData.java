package swm.smarthome.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class ParseData {
	private static final String FILE_PATH = "data.txt";
	private static final String UNKNOWN_KEY = "<unknown>";
	private static final int UNKNOWN_WT = 1; 

	public static void main(String[] args) {

		Map<States, Integer> startCounts = new HashMap<>();
		Map<States, Map<States, Integer>> aMap = new HashMap<States, Map<States, Integer>>();
		Map<States, Map<String, Integer>> bMap = new HashMap<States, Map<String, Integer>>();

		Set<String> obsWords = new HashSet<String>();
		try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
			String line;
			while ((line = br.readLine()) != null) {
				String[] words = line.split(" ");
				States prevState = null;
				States startState = null;
				for (String word : words) {
					String[] annotWord = word.split("<|>");
					String obsWord = annotWord[0];
					obsWords.add(obsWord);
					States currState = States.fromValue(annotWord[1]);
					addBMapEntry(bMap, currState, obsWord);
					if (prevState == null) {
						startState = currState;
						prevState = currState;
						continue;
					}
					addAMapEntry(aMap, prevState, currState);

					prevState = currState;
				}
				Integer startCount = startCounts.get(startState);
				if (startCount == null) {
					startCount = 0;
				}
				startCounts.put(startState, startCount + 1);

			}
			
			addUnknownValues(bMap);
			
			States[] statesList = States.values();
			int stateSize = statesList.length;
			String[] stateNames = new String[stateSize];
			int statecount = 0;
			for (States states : statesList) {
				stateNames[statecount++] = states.value();
			}

			double[][] aMatrix = new double[stateSize][stateSize];
			double[][] bMatrix = new double[stateSize][obsWords.size() + 1];
			double[] startStates = new double[stateSize];
			convertAMapToMatrix(aMap, aMatrix);

			Map<String, Integer> indexMap = new LinkedHashMap<String, Integer>();
			int obsIndex = 0;
			for (String word : obsWords) {
				indexMap.put(word, obsIndex++);
			}
			indexMap.put(UNKNOWN_KEY, obsIndex++);
			convertBMapToMatrix(bMap, bMatrix, indexMap);

			convertStartStateToMatrix(startCounts, startStates);

			writeArrToFile(stateNames, "output" + File.separator + "states.txt");
			writeArrToFile(indexMap.keySet().toArray(new String[indexMap.size()]), "output"
					+ File.separator + "observations.txt");
			writeStartProbToFile(startStates, "output" + File.separator + "startprob.txt");
			writeMatrixToFile(aMatrix, "output" + File.separator + "amatrix.csv");
			writeMatrixToFile(bMatrix, "output" + File.separator + "bmatrix.csv");

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void addUnknownValues(Map<States, Map<String, Integer>> bMap) {
		for ( Entry<States, Map<String, Integer>> entry : bMap.entrySet()) {
			Map<String, Integer> val = entry.getValue();
			val.put(UNKNOWN_KEY, UNKNOWN_WT);
		}
	}

	private static void writeStartProbToFile(double[] startStates, String fileName) {
		try {
			DecimalFormat two = new DecimalFormat("#0.00");
			BufferedWriter br = new BufferedWriter(new FileWriter(fileName));
			StringBuilder sb = new StringBuilder();
			int count = 0;
			for (double row : startStates) {
				sb.append(two.format(row));
				if (count++ < startStates.length - 1) {
					sb.append(",");
				}
			}
			br.write(sb.toString());
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void convertStartStateToMatrix(Map<States, Integer> startCounts,
			double[] startStates) {

		int sum = 0;
		for (Integer val : startCounts.values()) {
			sum += val;
		}
		for (States state : States.values()) {
			if (!startCounts.containsKey(state) || startCounts.get(state) == null) {
				startStates[state.index()] = 0;
			} else {
				startStates[state.index()] = (startCounts.get(state) * 1.0) / sum;
			}
		}
	}

	private static void convertBMapToMatrix(Map<States, Map<String, Integer>> bMap,
			double[][] bMatrix, Map<String, Integer> indexMap) {

		for (Entry<States, Map<String, Integer>> fromEntry : bMap.entrySet()) {
			int sum = 0;
			States state = fromEntry.getKey();
			Map<String, Integer> transStates = fromEntry.getValue();
			for (Integer val : transStates.values()) {
				sum += val;
			}

			for (Entry<String, Integer> toEntry : transStates.entrySet()) {
				String obs = toEntry.getKey();
				if (toEntry.getValue() != null) {
					bMatrix[state.index()][indexMap.get(obs)] = (toEntry.getValue() * 1.0) / sum;
				}
			}
		}
	}

	private static void convertAMapToMatrix(Map<States, Map<States, Integer>> aMap,
			double[][] aMatrix) {
		for (Entry<States, Map<States, Integer>> fromEntry : aMap.entrySet()) {
			int sum = 0;
			States prev = fromEntry.getKey();
			Map<States, Integer> transStates = fromEntry.getValue();
			for (Integer val : transStates.values()) {
				sum += val;
			}

			for (Entry<States, Integer> toEntry : transStates.entrySet()) {
				States next = toEntry.getKey();
				if (toEntry.getValue() != null) {
					aMatrix[prev.index()][next.index()] = (toEntry.getValue() * 1.0) / sum;
				}
			}
		}
	}

	private static void addBMapEntry(Map<States, Map<String, Integer>> bMap, States currState,
			String obsWord) {
		if (bMap.get(currState) == null) {
			bMap.put(currState, new HashMap<String, Integer>());
		}
		if (bMap.get(currState).get(obsWord) == null) {
			bMap.get(currState).put(obsWord, 0);
		}
		Integer aCount = bMap.get(currState).get(obsWord);
		bMap.get(currState).put(obsWord, aCount + 1);
	}

	private static void addAMapEntry(Map<States, Map<States, Integer>> aMap, States prevState,
			States currState) {
		if (aMap.get(prevState) == null) {
			aMap.put(prevState, new HashMap<States, Integer>());
		}
		if (aMap.get(prevState).get(currState) == null) {
			aMap.get(prevState).put(currState, 0);
		}
		Integer aCount = aMap.get(prevState).get(currState);
		aMap.get(prevState).put(currState, aCount + 1);
	}

	private static void writeArrToFile(String[] strArr, String fileName) {
		try {
			BufferedWriter br = new BufferedWriter(new FileWriter(fileName));
			StringBuilder sb = new StringBuilder();
			boolean firstRow = true;
			for (String row : strArr) {
				if (firstRow) {
					firstRow = false;
				} else {
					sb.append("\n");
				}
				sb.append(row);
			}

			br.write(sb.toString());
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void writeMatrixToFile(double[][] matrix, String fileName) {
		try {
			DecimalFormat two = new DecimalFormat("#0.00");
			BufferedWriter br = new BufferedWriter(new FileWriter(fileName));
			StringBuilder sb = new StringBuilder();
			boolean firstRow = true;
			for (double[] row : matrix) {
				if (firstRow) {
					firstRow = false;
				} else {
					sb.append("\n");
				}
				int count = 0;
				for (double val : row) {
					sb.append(two.format(val));
					if (count++ < row.length - 1) {
						sb.append(",");
					}
				}
			}

			br.write(sb.toString());
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
