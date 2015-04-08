import java.util.*;
import java.io.*;

public class Generator {

	private ArrayList<Device> devices;
	private ArrayList<Action> actions;
	private ArrayList<Location> locations;


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
	
	// generates all the data based on provided devices, actions, locations and template sentences
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
			for (int i =0; i<numberOfCombinations; i++) {
				String newSentence = sentence;

				// System.out.println("i = " + i);
				// System.out.println("action = " + (i%actions.size()));
				// System.out.println("device = " + (i/actions.size()));
				// System.out.println("locati = " + ((i/actions.size())%devices.size()));

				if (hasActions) {
					newSentence = sentence.replaceAll("<action>", actions.get(i % actions.size()).getName());
				}

				if (hasDevices) {
					newSentence = newSentence.replaceAll("<device>", devices.get((i/actions.size())%devices.size()).getName());
				}
				
				if (hasLocations) {
					newSentence = newSentence.replaceAll("<location>", locations.get(i/(actions.size() * devices.size())).getName());
				}
				

				// System.out.println(newSentence);
				updatedSentences.add(newSentence);
			}


			allData.addAll(updatedSentences);
		}

		try
		{
			FileWriter writer = new FileWriter("data.txt"); 
			for(String str: allData) {
				writer.write(str);
				writer.write("\n");
			}
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}