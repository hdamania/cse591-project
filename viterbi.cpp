#include <stdio.h>
#include <stdlib.h>
#include <iostream>
#include <string>
#include <map>
#include <vector>
#include <deque>
#include <fstream>
#include <sstream>
#include <iterator>
#include <cstring>
#define OBS_LEN_MAX 10

using namespace std;

float *pi;

float **a;

float **b;
	
float **delta;

vector<string> observations;
vector<string> states; 

int num_states = 0;
int num_obs = 0; 


typedef map<int, string> StringMap_t;
//typedef deque<int> IntVec_t;
typedef map<int, int> IntVec_t;

StringMap_t g_stateMap;
StringMap_t g_obsMap;
map<string,int> obs_number_map;

vector<string> read_from_file(const char* file_name)
{
	
    vector<string> list;

    string word;
    ifstream infile(file_name);
    while (infile >> word) 
    {
        list.push_back(word);
    }

    return list;
}





void fill_matrix(const char * file_name,float **mat,int r,int c)
{
   
   int i = 0;
   int  j = 0;
   size_t pos = 0;
   string delimiter = ",";

   string word;
   ifstream infile(file_name);
  
   while (infile >> word) 
   {   
	
	std::string token;
	while ((pos = word.find(delimiter)) != std::string::npos) 
        {
   
    	  token = word.substr(0, pos);

	  
          if(i<r && j < c)
          {
           mat[i][j] = atof(token.c_str());
          }
	  	
          word.erase(0, pos + delimiter.length());
 	  j++;
        }

	if(word.length() != 0)
        {
           mat[i][j] = atof(word.c_str()); 
        }           
	

	i++;
	j = 0;

   }

 }
   
   


void load_model_params()
{
    int i = 0;
    int j = 0;
    states = read_from_file("output/states.txt");
    observations = read_from_file("output/observations.txt"); 
    num_states = (int) states.size();
    num_obs = (int)observations.size();
    
    a = (float**) malloc(sizeof(float*) * num_states);
    for(i = 0;i<num_states;i++)
    {
	a[i] = (float*)malloc(sizeof(float)*num_states);
    }


    b = (float**) malloc(sizeof(float*) * num_states);
    for(i = 0;i<num_states;i++)
    {
	b[i] = (float*)malloc(sizeof(float)*num_obs);
    }    
    
    
    fill_matrix("output/amatrix.csv",a,num_states,num_states);

    
    fill_matrix("output/bmatrix.csv",b,num_states,num_obs);

    pi = (float *)malloc(sizeof(float)* num_states);
    
    cout<<"States\n";

    for(i = 0;i<num_states;i++)
    {
	cout<<states.at(i)<<"\n";

    }


   cout<<"--------------A_Matrix----------------\n";
    for(i = 0;i<num_states;i++)
    {


	for(j = 0;j<num_states;j++)
        {
	   cout<<a[i][j]<<"\t";
	}
	cout<<"\n";
   }


   cout<<"-----------------B_Matrix---------------\n";
   for(i = 0;i<num_states;i++)
    {


	for(j = 0;j<num_obs;j++)
        {
	   cout<<b[i][j]<<"\t";
	}
	cout<<"\n";
   }

    cout<<"---------------Observations-----------------\n";

    for(i = 0;i<num_obs;i++)
    {
	cout<<observations.at(i)<<"\n";
    }

    cout<<"---------start probabilty matrix---------\n";

    fill_matrix("output/startprob.txt",&pi,1,num_states);

    for(i = 0 ; i< num_states;i++)
       cout<< pi[i]<<"\t";

    cout<<"\n";

    for (i = 0; i < num_states; i++)
    {

	g_stateMap.insert(make_pair<int, string>(i, states.at(i)));
    }

    for (i = 0; i < num_obs; i++)
    {
	obs_number_map[observations.at(i)] = i;
	g_obsMap.insert(make_pair<int, string>(i, observations.at(i)));
    }

}



float viterbi_forward(int num_states, float *sp, float **tp, float **op, int *obs, int obs_len, float **delta, int cur_state, IntVec_t &path)
{
	int i, max_state, max_prob_state = 1;
	float prod = 0.0, max_prod, max_path_prob = 0.0;

	//printf("obs_len=%d, cur_state=%d\n", obs_len, cur_state);
	// return memoized state
	if (delta[cur_state][obs_len-1] > -0.5)
	{
		return delta[cur_state][obs_len-1];
	}

	// at t = 0
	if (obs_len == 1)
	{
		if (op[cur_state][obs[0]] == 0)
		{
			delta[cur_state][0] = sp[cur_state] * 0.0001;
		}
		else
		{
			delta[cur_state][0] = sp[cur_state] * op[cur_state][obs[0]];
		}

		if (cur_state == (num_states - 1))
		{
			// reached final state, now find max prob path
			for (i = 0; i < num_states; i++)
			{
				if (delta[i][obs_len-1] > max_path_prob)
				{
					max_path_prob = delta[i][obs_len-1];
					max_prob_state = i;
				}
			}

			// update the path with this higher probability path
			path[obs_len] = max_prob_state;
			//printf("push=%d, cur_state=%d\n", max_state, cur_state);
		}

		return delta[cur_state][0];
	}

	// induction step
	for (i = 0; i < num_states; i++)
	{
	//	newPath.clear();
		prod = viterbi_forward(num_states, sp, tp, op, obs, obs_len-1, delta, i, path) * tp[i][cur_state];
		if (i == 0)
		{
			// init the max to first prod
			max_prod = prod;
			max_state = 0;
		}
		else
		{
			// update max if required
			if (prod > max_prod)
			{
				max_prod = prod;
				max_state = i;
			}
		}
	}

	// path.push_back(max_state);
	// only update the path if a higher probability path is found
	// path[obs_len-1] = max_state;
	
	if (op[cur_state][obs[obs_len-1]] == 0)
	{
		delta[cur_state][obs_len-1] = max_prod * 0.0001;
	}
	else
	{
		delta[cur_state][obs_len-1] = max_prod * op[cur_state][obs[obs_len-1]];
	}

	if (cur_state == (num_states - 1))
	{
		// reached final state, now find max prob path
		for (i = 0; i < num_states; i++)
		{
			if (delta[i][obs_len-1] > max_path_prob)
			{
				max_path_prob = delta[i][obs_len-1];
				max_prob_state = i;
			}
		}

		// update the path with this higher probability path
		path[obs_len] = max_prob_state;
		//printf("push=%d, cur_state=%d\n", max_state, cur_state);
	}

	return delta[cur_state][obs_len-1];
}

int viterbi(int num_states, int num_obs, float *sp, float ** tp, float **op, int *obs, int obs_len, IntVec_t &path)
{
	int i, j, max_state;
	float prod, max_prod;

	// allocate memory for forward DP table
	delta = (float **)malloc(num_states * sizeof(float *));
	for (i = 0; i < num_states; i++)
	{
		delta[i] = (float *)malloc (obs_len * sizeof(float));
	}

	// prepare DP table
	for (i = 0; i < num_states; i++)
	{
		for (j = 0; j < obs_len; j++)
		{
			delta[i][j] = -1.0;
		}
	}

	// perform forward DP step
	for (i = 0; i < num_states; i++)
	{
		prod = viterbi_forward(num_states, sp, tp, op, obs, obs_len, delta, i, path);
		
		if (i == 0)
		{
			// init the max to first prod
			max_prod = prod;
			max_state = 0;
		}
		else
		{
			// update max if required
			if (prod > max_prod)
			{
				max_prod = prod;
				max_state = i;
			}
		}

		//printf("-----\n");

		//viterbi_forward(num_states, sp, tp, op, obs, obs_len, delta, i, path);
	}
	
	//path.push_back(max_state);
//	path[obs_len] = max_state;

	//path.push_back(max_state);

	return 0;
}


int main()
{
	int i, j,k; 
	
		
	
	int obs[OBS_LEN_MAX] ;
	IntVec_t pathVec;
	int obs_len = 5;
        
        load_model_params();
	

	string sentence;
	string predicted_output;
   	ifstream fin("our_test_dataset.txt");
	ofstream fout ("ner_output.txt");

	if( !fin  ) {
        cout << "Can't open file\n ";
         return 0;
    	}
	
	int obs_index = 0;
	int sentence_length = 0;

   	while (getline( fin, sentence)) 
   	{
		 obs_index = 0;
		 memset(obs,0,sizeof(obs));
		 istringstream buf(sentence);
    		 istream_iterator<string> beg(buf);
		 istream_iterator<string> end;

    		 vector<string> tokens(beg, end);
		
		sentence_length = tokens.size();
		
		printf("The sentence is -%s\n",sentence.c_str());
    		for(k = 0;k<sentence_length;k++)
		{
		    if(obs_number_map[tokens.at(k)] != 0)
		    {
		    	obs[obs_index++] =  obs_number_map[tokens.at(k)]; 
		    }
		    else
		    {
			printf("unknown - %s --- %s",tokens.at(k).c_str(),observations.at(observations.size() -1).c_str());
			obs[obs_index++] =  observations.size() -1; 		
		    }
		 	
		    printf("%s-%d\t",tokens.at(k).c_str(),obs[obs_index-1]);
		}
		
				

	  	viterbi(num_states, num_obs, pi, a, b, obs, sentence_length, pathVec);

	  	printf("\n");
		for (i = 0; i < num_states; i++)
		{
			for (j = 0; j < obs_len; j++)
			{
				printf("%.5f ", delta[i][j]);
			}
			printf("\n");
		}
		printf("\n");

		printf("Path:\n");
		/*for (i = 0; i < pathVec.size(); i++)
		{
			printf("%s ", g_stateMap[pathVec[i]].c_str());
		}*/
		cout<<sentence<<"\n";
		predicted_output = "";
		for (i = 1; i <= sentence_length; i++)
		{
			printf("%s ", g_stateMap[pathVec[i]].c_str());
			predicted_output = tokens.at(i-1) + '\t'+g_stateMap[pathVec[i]];
			fout<<predicted_output;
			fout<<"\n";
		}

		printf("\n");

	}

	cout<<"States\n";

    for(int l = 0;l<num_states;l++)
    {
	cout<<states.at(l)<<"\n";

    }
	return 0;
}

