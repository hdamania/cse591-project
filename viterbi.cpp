#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <iostream>
#include <string>
#include <map>
#include <vector>
#include <deque>
#include <fstream>

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
    states = read_from_file("output_random/states.txt");
    observations = read_from_file("output_random/observations.txt"); 
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
		// one extra for unknown obs
		b[i] = (float*)malloc(sizeof(float)*num_obs);
    }    
    
    
    fill_matrix("output_random/amatrix.csv",a,num_states,num_states);

    
    fill_matrix("output_random/bmatrix.csv",b,num_states,num_obs);

    pi = (float *)malloc(sizeof(float)* num_states);
    
	/*
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
	*/
    fill_matrix("output_random/startprob.txt",&pi,1,num_states);

    //for(i = 0 ; i< num_states;i++)
    //   cout<< pi[i]<<"\t";

    //cout<<"\n";

    for (i = 0; i < num_states; i++)
    {
	g_stateMap.insert(make_pair<int, string>(i, states.at(i)));
    }

    for (i = 0; i < num_obs; i++)
    {
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

int main(int argc, char *argv[])
{
	int i, j;

	std::map<std::string, int> obsMap;
	std::map<std::string, int>::iterator it;

	char *command, *token;
	int obs[OBS_LEN_MAX] = {16, 24, 25,17, 24,15};
	IntVec_t pathVec;
	int obs_len = 6;
    
	if (argc != 2)
	{
		cout << "Incorrect arguments!" << endl;
		cout << "Only pass the command in double-inverted commas." << endl;
		return 0;
	}

    load_model_params();
	
	// Create map of observations
	for (i = 0; i < observations.size(); i++)
	{
		obsMap.insert(std::make_pair<std::string, int>(observations[i], i));
	}

	for (i = 0, command = argv[1]; ; i++, command = NULL)
	{
		token = strtok(command, " ");
		if (NULL == token)
		{
			// end of command reached
			break;
		}

		it = obsMap.find(token);
		if (it != obsMap.end())
		{
			// found in obsMap, now insert its value in the obs array
			obs[i] = it->second;
		}
		else
		{
			// unknown token
			obs[i] = observations.size()-1;
		}
	}

	obs_len = i;
	
	viterbi(num_states, num_obs, pi, a, b, obs, obs_len, pathVec);

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
	for (i = 1; i <= obs_len; i++)
	{
		printf("%s ", g_stateMap[pathVec[i]].c_str());
	}

	printf("\n");

	return 0;
}

