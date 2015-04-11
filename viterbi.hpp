#ifndef __VITERBI_HPP__
#define __VITERBI_HPP__

#define NUM_STATES 2
#define NUM_OBS 3
#define OBS_LEN_MAX 10

using namespace std;

const char *states[] = {
	"Healthy",
	"Fever"
};

const char *observations[] = {
	"normal",
	"cold",
	"dizzy"
};

float start_prob[NUM_STATES] = { 0.6, 0.4 };

float transition_prob[NUM_STATES][NUM_STATES] = {
	{0.7, 0.3},
	{0.4, 0.6}
};

float emission_prob[NUM_STATES][NUM_OBS] = {
	{0.5, 0.4, 0.1},
	{0.1, 0.3, 0.6}
};

#endif
