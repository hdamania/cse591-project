package swm.smarthome.data;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Evaluate_results {
	


	private static void loadMapfromCSV(String fileName,String Mapvalue,Map<String, String> state_obs_map) {
		BufferedReader br = null;
		String line = "";
		String splitToken = ", ";
		int i = 0;
		
			
		try {
			
			br = new BufferedReader(new FileReader(fileName));
			
			while ((line = br.readLine()) != null) {
	 
				String[] lineWords = line.split(splitToken);
				
				for(i = 0;i<lineWords.length;i++)
				{
				  state_obs_map.put(lineWords[i], Mapvalue);
				}
			}

			
	 
		} catch (FileNotFoundException e) {
			e.printStackTrace();

			
		} catch (IOException e) {
			e.printStackTrace();

			
		}
	} 
	
	
	
	public static void evaluate(String file_name)
	{
	
	
		Map<String,Integer> states_map = new HashMap<String,Integer>();
		String[] state_names = {"action","device","location","other"};
		BufferedReader br = null;
		String line = "";
		String splitToken = "\t";
		int[][] measures = new int[4][3];
		
		states_map.put("action",0);
		states_map.put("device",1);
		states_map.put("location",2);
		states_map.put("other",3);
		
		final int TP = 0;
		final int FP = 1;
		final int FN = 2;
		int classifier_index = 0;
		
		
try {
			
			Map<String, String>  state_obs_map = new HashMap<String,String>();
			
			loadMapfromCSV("devices.csv", "device",state_obs_map);
			loadMapfromCSV("locations - new.csv", "location",state_obs_map);
			loadMapfromCSV("actions.csv", "action",state_obs_map);

			state_obs_map.put("in", "other");
			state_obs_map.put("the", "other");
			
			
			br = new BufferedReader(new FileReader(file_name));
			
		
			int correct_state_index = 0;
			int incorrect_state_index = 0;
			float precision = 0;
			float recall = 0;
			
			while ((line = br.readLine()) != null) {
	 
				//System.out.println(Action_map.size()+","+ Location_map.size()+","+Devices_map.size());
				String[] lineWords = line.split(splitToken);
				
				for(int l = 0;l<lineWords.length;l++)
				{
					lineWords[l].trim();
				}
				
				classifier_index = lineWords.length -1;
				
				String correct_state = state_obs_map.get(lineWords[0]); 
				System.out.println(lineWords[0]);
				correct_state_index = states_map.get(correct_state);
				if(correct_state.equals(lineWords[classifier_index]))
				{
					measures[correct_state_index][TP]++;
				}
				else
				{
					incorrect_state_index = states_map.get(lineWords[classifier_index]);
					measures[incorrect_state_index][FP]++;
					measures[correct_state_index][FN]++;
				}	
				
			}
			
			int i = 0;
			int j = 0;
			
			System.out.println("State\tTP\tFP\tFN\tPrecion\tRecall");
			String measures_values;
			for(i = 0;i<4;i++)
			{
				measures_values = state_names[i];
				for(j = 0;j<3;j++)
				{
					measures_values  = measures_values + "\t"+measures[i][j];
				}
			
			
				precision  = ((float)measures[i][TP]/(float)(measures[i][TP]+measures[i][FP]));
				
				measures_values = measures_values + "\t"+ precision; 
				recall  = ((float)measures[i][TP]/((float)measures[i][TP]+measures[i][FN]));
			
				measures_values = measures_values + "\t"+ recall;
				
				System.out.println(measures_values);
			}
			
	 
		} catch (FileNotFoundException e) {
			e.printStackTrace();

			
		} catch (IOException e) {
			e.printStackTrace();

			
		}

		
	}

	
}
