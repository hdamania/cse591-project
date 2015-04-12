package swm.smarthome.data;


public class Main
{
	public static void main(String[] args) {
		Generator dataGenerator = new Generator();
		dataGenerator.loadAllData();
		dataGenerator.generateSentences();
	}
}