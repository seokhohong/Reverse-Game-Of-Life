/*
 * CuckooMap.h
 *
 *  Created on: Jan 4, 2014
 *      Author: Seokho
 */

#ifndef CUCKOOMAP_H_
#define CUCKOOMAP_H_

#include <stdlib.h>
#include "malloc.h"
#include "signatureMap.h"
#include "stdbool.h"
#include "random.h"
#include "inttypes.h"
#include "stdint.h"
#include "malloc.h"

#define ELEMS_PER_KEY 2
#define CUCKOO_NOT_FOUND -1
#define SAMPLE_CAP 1000000

typedef struct
{
	//Negative values mean the value being stored
	uint64_t* table1;
	uint64_t* table2;
	int size; //of table
} CuckooMap;

int findCuckooMap(CuckooMap* map, uint64_t key);
int findCuckooOptimized(CuckooMap* map, uint64_t key);
CuckooMap* getCuckooMap(uint64_t* slowMap, uint32_t* freqs, int bitsCollection);

uint32_t getHash1(uint64_t hash, int mod);
uint32_t getHash2(uint64_t hash, int mod);

/*
static inline bool exists1(CuckooMap* map, uint64_t key)
{
	return map->table1[map->hash1(key, map->size) * ELEMS_PER_KEY] == EXISTS;
}

static inline bool exists2(CuckooMap* map, uint64_t key)
{
	return map->table2[map->hash2(key, map->size) * ELEMS_PER_KEY] == EXISTS;
}
*/

void removeCuckooMap(CuckooMap* map, uint64_t key);

#endif /* CUCKOOMAP_H_ */
