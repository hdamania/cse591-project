package swm.smarthome.data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.List;

public class Data_preprocessing {

	public static void preProcessData(String input_file_path,int no_sentences)
	{
		String str;
		
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(input_file_path));
			
			FileWriter writer1 = new FileWriter("stanford_train_data.txt");
			FileWriter  writer2 = new FileWriter("stanford_test_data.txt");
			FileWriter  writer3 = new FileWriter("our_test_dataset.txt");
			int total_size = no_sentences;
			int train_size = (int)(0.8  * total_size);
			int str_count = 0;
			String test_data_set_sentence;
			
			String word;
			String class_of_word;
			String output_string;
			
			
			while ((str = br.readLine()) != null) {
			
				List<String> tokens = Arrays.asList(str.split(" "));
				test_data_set_sentence = "";
				
				++str_count;
				  
				for(int i=0;i<tokens.size();i++)
				{
					String[] word_and_class = tokens.get(i).split("<");
					word = word_and_class[0];
					word  = word.trim();
					test_data_set_sentence = test_data_set_sentence+word;
					if(i!=tokens.size()-1)
						test_data_set_sentence +=" ";
					class_of_word = word_and_class[1].substring(0,word_and_class[1].length()-1);
					class_of_word = class_of_word.trim();
				    output_string = word + '\t'+class_of_word;
				  
				    
				    if(str_count <= train_size)
				    {
				     writer1.write(output_string);
				     writer1.write("\n");
				    }
				    else
				    {
				    	writer2.write(output_string);
					    writer2.write("\n");
					    test_data_set_sentence.trim();
					    if(i== (tokens.size()-1))
					    {
					      writer3.write(test_data_set_sentence);
					      writer3.write("\n");
					    }
				    }
				    
				}
				
			
			}

			writer1.close();
			writer2.close();
			writer3.close();
	 
		} catch (Exception e) {
			e.printStackTrace();
		
		
	}
	}
}
	
