/*
 * CuckooMap.h
 *
 *  Created on: Jan 4, 2014
 *      Author: Seokho
 */

#ifndef CUCKOOMAPLARGE_H_
#define CUCKOOMAPLARGE_H_

#include <stdlib.h>
#include "malloc.h"
#include "signatureMap.h"
#include "stdbool.h"
#include "random.h"
#include "inttypes.h"
#include "stdint.h"
#include "malloc.h"
#include "Limits.h"
#include "CuckooMap.h"

#define CUCKOO_NOT_FOUND -1
#define SAMPLE_CAP 1000000

typedef struct
{
	//Negative values mean the value being stored
	uint64_t* table1;
	uint64_t* table2;
	int size; //of table
	int keyLength;
	int elemsPerKey;
} LargeCuckooMap;

int findCuckooMap_Large(LargeCuckooMap* map, uint64_t* key);
int findCuckooOptimized_Large(LargeCuckooMap* map, uint64_t* key);
LargeCuckooMap* getCuckooMap_Large(uint64_t* slowMap, uint32_t* freqs, int bitsCollection, int keyLength);

uint32_t getHash1_Large(LargeCuckooMap* map, uint64_t* hash);
uint32_t getHash2_Large(LargeCuckooMap* map, uint64_t* hash);

void removeCuckooMap_Large(LargeCuckooMap* map, uint64_t* key);

#endif /* CUCKOOMAP_H_ */
