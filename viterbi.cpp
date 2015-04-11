#include <stdio.h>
#include <stdlib.h>
#include <iostream>
#include <string>
#include <map>
#include <vector>
#include <deque>
#include "viterbi.hpp"

using namespace std;

float pi[NUM_STATES];

float a[NUM_STATES][NUM_STATES];

float b[NUM_STATES][NUM_OBS];
	
float **delta;

typedef map<int, string> StringMap_t;
//typedef deque<int> IntVec_t;
typedef map<int, int> IntVec_t;

StringMap_t g_stateMap;
StringMap_t g_obsMap;

int prepare_hmm_model()
{
	int i, j;

	for (i = 0; i < NUM_STATES; i++)
	{
		pi[i] = start_prob[i];

		for (j = 0; j < NUM_STATES; j++)
		{
			a[i][j] = transition_prob[i][j];
		}

		for (j = 0; j < NUM_OBS; j++)
		{
			b[i][j] = emission_prob[i][j];
		}
	}

	for (i = 0; i < NUM_STATES; i++)
	{
		g_stateMap.insert(make_pair<int, string>(i, states[i]));
	}

	for (i = 0; i < NUM_OBS; i++)
	{
		g_obsMap.insert(make_pair<int, string>(i, observations[i]));
	}

	return 0;
}

float viterbi_forward(int num_states, float *sp, float tp[NUM_STATES][NUM_STATES], float op[NUM_STATES][NUM_OBS], int *obs, int obs_len, float **delta, int cur_state, IntVec_t &path)
{
	int i, max_state;
	float prod = 0.0, max_prod;

	printf("obs_len=%d, cur_state=%d\n", obs_len, cur_state);
	// return memoized state
	if (delta[cur_state][obs_len-1] > -0.5)
	{
		return delta[cur_state][obs_len-1];
	}

	// at t = 0
	if (obs_len == 1)
	{
		delta[cur_state][0] = sp[cur_state] * op[cur_state][obs[0]];
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

	//path.push_back(max_state);
	path[obs_len-1] = max_state;

	printf("push=%d, cur_state=%d\n", obs_len, cur_state);
	delta[cur_state][obs_len-1] = max_prod * op[cur_state][obs[obs_len-1]];

	return delta[cur_state][obs_len-1];
}

int viterbi(int num_states, int num_obs, float *sp, float tp[NUM_STATES][NUM_STATES], float op[NUM_STATES][NUM_OBS], int *obs, int obs_len, IntVec_t &path)
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
	for (i = 0; i < NUM_STATES; i++)
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

		printf("-----\n");

		//viterbi_forward(num_states, sp, tp, op, obs, obs_len, delta, i, path);
	}
	
	//path.push_back(max_state);
	path[obs_len] = max_state;

	//path.push_back(max_state);

	return 0;
}

int main()
{
	int i, j;
	int obs[OBS_LEN_MAX] = {0, 1, 2};
	IntVec_t pathVec;
	int obs_len = 3;

	prepare_hmm_model();

	viterbi(NUM_STATES, NUM_OBS, pi, a, b, obs, obs_len, pathVec);

	printf("\n");
	for (i = 0; i < NUM_STATES; i++)
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

