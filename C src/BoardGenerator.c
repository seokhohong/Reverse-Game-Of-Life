/*
 ============================================================================
 Name        : BoardGenerator.c
 ============================================================================
 */

/**
 *
 *	Generates random boards and samples them as necessary, building a frequency table
 *
 *
 **/

#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <string.h>
#include <stdbool.h>
#include <windows.h>
#include <inttypes.h>
#include <stdint.h>
#include <time.h>

#include "board.h"
#include "signatureMap.h"
#include "freqsTable.h"
#include "Endian.h"
#include "CuckooMap.h"
#include "LargeCuckooMap.h"
#include "Limits.h"
#include "print128.h"

//Dimensionality of the grid extracted from board
#define SAMPLE_DIM 10
//Dimensionality of the tally space
#define COLLECTION_DIM 10
#define DELTA 1
#define NUM_THREADS 5
#define READ true

#define SAMPLE_BITS (SAMPLE_DIM * SAMPLE_DIM)
#define COLLECTION_BITS (COLLECTION_DIM * COLLECTION_DIM)
#define KEY_LENGTH (SAMPLE_BITS / 65 + 1) 			//this 65 is kinda stupid
#define SAMPLE_CAP 1000000
#define PRE_STEP 5
#define MARK_INTERVAL 100000
#define SAVE_INTERVAL 20000000
#define BATCH_SIZE 1

char* mapIdString;
char* freqsIdString;

uint32_t* freqsTable;
CuckooMap* cuckooMap;
LargeCuckooMap* cuckooMapLarge;

HANDLE addedMutex;
HANDLE removalMutex;
uint64_t lastMarkIter = 0;
uint64_t lastSaveIter = 0;
uint64_t iter = 0;
int totalAdded = 0;
clock_t start;
clock_t diff;

//Add what we see in startBits to the frequency table at the specified index (indicated by signatureMap)
//Adds 1 to the correct subindex in the frequency table if the corresponding bit in startBits is set to 1
//Locking is far too expensive, and since this is stochastic sampling, the race condition is irrelevant
static inline void addFreqsTable(uint64_t startBits, int tableIndex)
{
	for(int a = 0; a < SAMPLE_BITS; a ++)
	{
		freqsTable[tableIndex * (SAMPLE_BITS + 1) + a + 1] += (startBits >> a) & 1;
	}
	freqsTable[tableIndex * (SAMPLE_BITS + 1)] ++;
}

static inline void addFreqsTable_Large(uint64_t* startBits, int tableIndex)
{
	for(int a = 0; a < SAMPLE_BITS; a++)
	{
		freqsTable[tableIndex * (SAMPLE_BITS + 1) + a + 1] += (startBits[a / (SAMPLE_BITS / KEY_LENGTH)] >> (a % (SAMPLE_BITS / KEY_LENGTH))) & 1;
	}
	freqsTable[tableIndex * (SAMPLE_BITS + 1)] ++;
}
//For processing dimensions > 8
int addBoardLarge(uint64_t* startBoard, uint64_t* board)
{
	int numAdded = 0;
	uint64_t bits[KEY_LENGTH];
	uint64_t startBits[KEY_LENGTH];
	for(int a = - SAMPLE_DIM + 1; a < BOARD_SIZE; a++)
	{
		for(int b = - SAMPLE_DIM + 1; b < BOARD_SIZE; b++)
		{
			getBitsLarge(board, bits, a, b, SAMPLE_DIM, SAMPLE_DIM, KEY_LENGTH);
			int tableIndex = findCuckooOptimized_Large(cuckooMapLarge, bits);
			if(tableIndex != CUCKOO_NOT_FOUND)
			{
				//if we have enough samples
				if(freqsTable[tableIndex * (SAMPLE_BITS + 1)] < SAMPLE_CAP)
				{
					int rowOffset = a + (SAMPLE_DIM - COLLECTION_DIM) / 2;
					int colOffset = b + (SAMPLE_DIM - COLLECTION_DIM) / 2;
					getBitsLarge(startBoard, startBits, rowOffset, colOffset, COLLECTION_DIM, COLLECTION_DIM, KEY_LENGTH);
					addFreqsTable_Large(startBits, tableIndex);
					numAdded++;
				}
				else
				{
					//remove them from hashmap
					WaitForSingleObject(removalMutex, INFINITE);
					if(findCuckooOptimized_Large(cuckooMapLarge, bits) != CUCKOO_NOT_FOUND)
					{
						print_u128E("Removed ", combine(bits[0], bits[1]), "\n");
						fflush(stdout);
						removeCuckooMap_Large(cuckooMapLarge, bits);
					}
					ReleaseMutex(removalMutex);
				}
			}
		}
	}
	return numAdded;
}

int addBoardSmall(uint64_t* startBoard, uint64_t* board)
{
	int numAdded = 0;
	uint64_t startBits;
	uint64_t bits;
	for(int a = - COLLECTION_DIM + 1; a < BOARD_SIZE; a++)
	{
		for(int b = - COLLECTION_DIM + 1; b < BOARD_SIZE; b++)
		{
			//Equivalent to: bits = getBits(board, a, b);
			bits = getBits(board, a, b, SAMPLE_DIM, SAMPLE_DIM);
			int tableIndex = findCuckooOptimized(cuckooMap, bits);
			if(tableIndex != CUCKOO_NOT_FOUND)
			{
				//numElements
				if(freqsTable[tableIndex * (SAMPLE_BITS + 1)] < SAMPLE_CAP)
				{
					startBits = getBits(startBoard, a, b, COLLECTION_DIM, COLLECTION_DIM);
					addFreqsTable(startBits, tableIndex);
					numAdded++;
				}
				else
				{
					WaitForSingleObject(removalMutex, INFINITE);
					if(findCuckooOptimized(cuckooMap, bits) != CUCKOO_NOT_FOUND)
					{
						printf("Removed %"PRIu64"\n", bits);
						fflush(stdout);
						removeCuckooMap(cuckooMap, bits);
					}
					ReleaseMutex(removalMutex);
				}
			}
		}
	}
	return numAdded;
}

int addBoard(uint64_t* startBoard, uint64_t* board)
{
	if(SAMPLE_DIM <= 8)
	{
		return addBoardSmall(startBoard, board);
	}
	else
	{
		return addBoardLarge(startBoard, board);
	}
}
//Necessary to avoid the exact same sequence each run, not perfect, but decent
void reseed()
{
	for(int a = 0; a < rand() % 100; a++)
	{
		xorshf96();
	}
}

//Either displays progress message or saves data
void checkMilestone()
{
	if(iter > 0)
	{
		if(iter - lastMarkIter > MARK_INTERVAL) //just some console information about progress made on the sampling
		{
			lastMarkIter = iter;
			diff = clock() - start;
			int msec = diff * 1000 / CLOCKS_PER_SEC;
			printf("%d samples added from %"PRIu64" boards in %d milliseconds\n", totalAdded, (uint64_t) (iter / MARK_INTERVAL) * MARK_INTERVAL, msec);
			fflush(stdout);
			totalAdded = 0;
			start = clock(), diff;
			reseed();
		}
		if(iter - lastSaveIter > SAVE_INTERVAL) //saves the data periodically
		{
			lastSaveIter = iter;
			printf("Saving...\n");
			char filename[100];
			strcpy(filename, "C:\\Life\\");
			strcat(filename, freqsIdString);
			strcat(filename, "tableCI.txt");
			writeFreqsTable(filename, freqsTable);
			printf("Complete\n");
			fflush(stdout);
		}
	}
}

void makeBoards()
{
	uint64_t board[BIG_DIM];
	uint64_t duplicatedStartBoard[BIG_DIM];
	bool alive = 0;
	while(true)
	{
		int extraAdded = 0;
		for(int b = 0; b < BATCH_SIZE; b ++) //used to check the impact of locking addedMutex... BATCH_SIZE > 1 seems to impact the quality of data
		{
			getRandomBoardOptimized(board);
			for(int a = 0; a < PRE_STEP; a++)
			{
				alive = stepOptimized2(board);
				if(!alive) break;
			}
			if(!alive) continue;
			duplicateOptimized(board, duplicatedStartBoard);
			for(int a = 0; a < DELTA; a++)
			{
				alive = stepOptimized2(board);
			}
			if(!alive) continue;
			extraAdded += addBoard(duplicatedStartBoard, board);
		}
		WaitForSingleObject(addedMutex, INFINITE);
		totalAdded += extraAdded;
		iter += BATCH_SIZE;
		checkMilestone();
		ReleaseMutex(addedMutex);
	}
}

//who cares if I use globals...
void initGlobals()
{
	addedMutex = CreateMutex(NULL, FALSE, NULL);
	removalMutex = CreateMutex(NULL, FALSE, NULL);

	mapIdString = mallocClean(sizeof(char*) * 100);
	sprintf(mapIdString, "%d%d%d", SAMPLE_DIM, SAMPLE_DIM, DELTA);
	freqsIdString = mallocClean(sizeof(char*) * 100);
	if(SAMPLE_DIM != COLLECTION_DIM)
	{
		sprintf(freqsIdString, "%d%d%d%d%d", SAMPLE_DIM, SAMPLE_DIM, COLLECTION_DIM, COLLECTION_DIM, DELTA);
	}
	else
	{
		sprintf(freqsIdString, "%d%d%d", SAMPLE_DIM, SAMPLE_DIM, DELTA);
	}

	char* signatureMapFilename = mallocClean(100);
	strcpy(signatureMapFilename, "C:\\Life\\");
	strcat(signatureMapFilename, mapIdString);
	strcat(signatureMapFilename, "mapBinary.txt");
	uint64_t* signatureMap = readSignatureMap(signatureMapFilename);

	char* freqsFilename = mallocClean(100);
	strcpy(freqsFilename, "C:\\Life\\");
	strcat(freqsFilename, freqsIdString);
	strcat(freqsFilename, "tableCI.txt");

	if(READ)
	{
		freqsTable = readFreqsTable(freqsFilename);
	}
	else
	{
		freqsTable = getFreqsTable(signatureMap, COLLECTION_DIM, KEY_LENGTH);
	}
	if(SAMPLE_BITS <= 64)
	{
		cuckooMap = getCuckooMap(signatureMap, freqsTable, SAMPLE_BITS);
	}
	else
	{
		cuckooMapLarge = getCuckooMap_Large(signatureMap, freqsTable, SAMPLE_BITS, KEY_LENGTH);
	}

	freeMap(signatureMap);
}

void testBoard()
{
	xorshf96();
	xorshf96();
	xorshf96();
	xorshf96();
	writeDebugging("debugBoard.txt", getRandomBoard());
	exit(0);
}

void bitMode()
{
	if(sizeof(int*) == 8)
	{
		puts("64bit Mode");
	}
	else if(sizeof(int*) == 4)
	{
		puts("32bit Mode");
	}
	fflush(stdout);
}

DWORD WINAPI makeBoardsThread(void* data)
{
  makeBoards();
  return 0;
}
int main(void)
{
	/*
	uint64_t board[BIG_DIM];
	getRandomBoardOptimized(board);
	puts(print(board));
	//printf("%"PRIu64"\n", getBits(board, 0, 0, 8, 8));
	uint64_t bits[2];
	getBitsLarge(board, bits, 0, 0, SAMPLE_DIM, SAMPLE_DIM);
	printf("%"PRIu64"  %"PRIu64"\n", bits[0], bits[1]);
	*/
	srand(time(NULL));
	reseed();
	buildNeighborTable(); //static initializer for board.c
	bitMode();
	//test();
	//testBoard();
	initGlobals();
	start = clock();
	for(int a = 0; a < NUM_THREADS - 1; a++)
	{
		CreateThread(NULL, 0, makeBoardsThread, NULL, 0, NULL);
	}
	HANDLE aThread = CreateThread(NULL, 0, makeBoardsThread, NULL, 0, NULL);
	WaitForSingleObject(
	            aThread,    // handle to mutex
	            INFINITE);

	return EXIT_SUCCESS;
}

