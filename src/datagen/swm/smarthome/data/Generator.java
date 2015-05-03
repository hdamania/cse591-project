package swm.smarthome.data;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Generator {

	private ArrayList<Device> devices;
	private ArrayList<Action> actions;
	private ArrayList<Location> locations;
	private static final int SENTENCE_COUNT = 3000;
	private Random randomGen = new Random();

	// just a helper to load from csv
	private static ArrayList<String> loadFromCSVFile(String fileName) {
		BufferedReader br = null;
		String line = "";
		String splitToken = ", ";

		try {
			br = new BufferedReader(new FileReader(fileName));
			ArrayList<String> words = new ArrayList<String>();
			while ((line = br.readLine()) != null) {

				String[] lineWords = line.split(splitToken);
				words.addAll(Arrays.asList(lineWords));
			}

			return words;

		} catch (FileNotFoundException e) {
			e.printStackTrace();

			return new ArrayList<String>();
		} catch (IOException e) {
			e.printStackTrace();

			return new ArrayList<String>();
		}
	}

	private static ArrayList<Device> loadDevices() {
		ArrayList<String> stringDevices = Generator.loadFromCSVFile("devices.csv");
		ArrayList<Device> devices = new ArrayList<Device>();

		for (String deviceName : stringDevices) {
			Device d = new Device(deviceName);
			devices.add(d);
		}

		return devices;
	}

	private static ArrayList<Action> loadActions() {
		ArrayList<String> stringActions = Generator.loadFromCSVFile("actions.csv");
		ArrayList<Action> actions = new ArrayList<Action>();

		for (String actionName : stringActions) {
			Action a = new Action(actionName);
			actions.add(a);
		}

		return actions;
	}

	private static ArrayList<Location> loadLocations() {
		ArrayList<String> stringLocations = Generator.loadFromCSVFile("locations.csv");
		ArrayList<Location> locations = new ArrayList<Location>();

		for (String locationName : stringLocations) {
			Location l = new Location(locationName);
			locations.add(l);
		}

		return locations;
	}

	// reads all csv data into an array of its classes
	public void loadAllData() {
		devices = Generator.loadDevices();
		actions = Generator.loadActions();
		locations = Generator.loadLocations();
	}

	// generates all the data based on provided devices, actions, locations and
	// template sentences
	public void generateSentences() {
		ArrayList<String> sentences = Generator.loadFromCSVFile("sentences.csv");
		ArrayList<String> allData = new ArrayList<String>();

		for (String sentence : sentences) {
			List<String> tokens = Arrays.asList(sentence.split(" "));

			// get number of possible sentences for each template
			int numberOfCombinations = 1;
			boolean hasActions = false;
			boolean hasDevices = false;
			boolean hasLocations = false;

			if (tokens.contains("<action>")) {
				numberOfCombinations *= actions.size();
				hasActions = true;
			}

			if (tokens.contains("<device>")) {
				numberOfCombinations *= devices.size();
				hasDevices = true;
			}

			if (tokens.contains("<location>")) {
				numberOfCombinations *= locations.size();
				hasLocations = true;
			}

			// System.out.println("num " + numberOfCombinations);
			ArrayList<String> updatedSentences = new ArrayList<String>();
			for (int i = 0; i < numberOfCombinations; i++) {
				String newSentence = sentence;

				// System.out.println("i = " + i);
				// System.out.println("action = " + (i%actions.size()));
				// System.out.println("device = " + (i/actions.size()));
				// System.out.println("locati = " +
				// ((i/actions.size())%devices.size()));

				if (hasActions) {
					newSentence = sentence.replaceAll("<action>", actions.get(i % actions.size())
							.getName() + "<action>");
				}

				if (hasDevices) {
					newSentence = newSentence.replaceAll("<device>",
							devices.get((i / actions.size()) % devices.size()).getName()
									+ "<device>");
				}

				if (hasLocations) {
					newSentence = newSentence.replaceAll("<location>",
							locations.get(i / (actions.size() * devices.size())).getName()
									+ "<location>");
				}

				// System.out.println(newSentence);
				updatedSentences.add(newSentence);
			}

			allData.addAll(updatedSentences);
		}

		try {
			FileWriter writer = new FileWriter("data.txt");
			for (String str : allData) {
				writer.write(str);
				writer.write("\n");
			}
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void generateRandomSentences() {
		ArrayList<String> sentences = Generator.loadFromCSVFile("sentences.csv");
		String[] sentenceArr = sentences.toArray(new String[sentences.size()]);
		Device[] deviceArr = devices.toArray(new Device[devices.size()]);
		Action[] actionsArr = actions.toArray(new Action[actions.size()]);
		Location[] locationsArr = locations.toArray(new Location[locations.size()]);
		
		List<String> sentenceList = new ArrayList<String>();

		for (int i = 0; i < SENTENCE_COUNT; i++) {
			randomGen = new Random();
			String sentence = sentenceArr[randomGen.nextInt(sentenceArr.length)];
			sentence = sentence.replaceAll("<action>",
					actionsArr[randomGen.nextInt(actionsArr.length)].getName() + "<action>");
			sentence = sentence.replaceAll("<device>",
					deviceArr[randomGen.nextInt(deviceArr.length)].getName() + "<device>");
			sentence = sentence.replaceAll("<location>",
					locationsArr[randomGen.nextInt(locationsArr.length)].getName() + "<location>");
			sentenceList.add(sentence);
		}
		
		try {
			FileWriter writer = new FileWriter("dataRandom.txt");
			for (String str : sentenceList) {
				writer.write(str);
				writer.write("\n");
			}
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}