/*
 * LargeCuckooMap.c
 *
 *
 * My implementation of the awesome Cuckoo hashmap.
 *
 *
 *  Created on: Jan 4, 2014
 *      Author: Seokho
 */

#include "LargeCuckooMap.h"
#include "stdbool.h"
#include "Limits.h"
#include "print128.h"

#define MAX_LOOP 5000
#define NONE_LARGE (((uint64_t) 1 << 63) - 1) //Keys CANNOT occupy this bit
#define SPACE_MULTIPLIER 4

void set1_Large(LargeCuckooMap* map, uint64_t* key, uint64_t value)
{
	for(int a = 0; a < map->keyLength; a++)
	{
		map->table1[getHash1_Large(map, key) * map->elemsPerKey + a] = key[a];
	}
	map->table1[getHash1_Large(map, key) * map->elemsPerKey + map->keyLength] = value;
}

void set2_Large(LargeCuckooMap* map, uint64_t* key, uint64_t value)
{
	for(int a = 0; a < map->keyLength; a++)
	{
		map->table2[getHash2_Large(map, key) * map->elemsPerKey + a] = key[a];
	}
	map->table2[getHash2_Large(map, key) * map->elemsPerKey + map->keyLength] = value;
}

void clear1_Large(LargeCuckooMap* map, uint64_t* key)
{
	set1_Large(map, key, NONE_LARGE);
}
void clear2_Large(LargeCuckooMap* map, uint64_t* key)
{
	set2_Large(map, key, NONE_LARGE);
}

uint64_t* getKey1_Large(LargeCuckooMap* map, uint64_t* key)
{
	return map->table1 + getHash1_Large(map, key) * map->elemsPerKey;
}
uint64_t* getKey2_Large(LargeCuckooMap* map, uint64_t* key)
{
	return map->table2 + getHash2_Large(map, key) * map->elemsPerKey;
}
static inline uint64_t getValue1_Large(LargeCuckooMap* map, uint64_t* key)
{
	return map->table1[getHash1_Large(map, key) * map->elemsPerKey + map->keyLength];
}
static inline uint64_t getValue2_Large(LargeCuckooMap* map, uint64_t* key)
{
	return map->table2[getHash2_Large(map, key) * map->elemsPerKey + map->keyLength];
}
//Very basic hash: needs to be fast
__uint128_t hash1Mult_Large = ((__uint128_t) 11832546345696851205UL << 64) + (uint64_t) 16384545419507531801UL;
__uint128_t hash2Mult_Large = ((__uint128_t) 16416102846353452488UL << 64) + (uint64_t) 16315805464367464543UL;

uint32_t getHash1_Large(LargeCuckooMap* map, uint64_t* hash)
{
	__uint128_t hash128 = 0;
	if(map -> keyLength == 2)
	{
		hash128 = ((__uint128_t) hash[0] << 64) | hash[1];
	}
	else if(map -> keyLength == 3)
	{
		hash128 = ((__uint128_t) hash[0] << 64) | ((__uint128_t) hash[1] << 50) | hash[2]; //pretty gimmicky.
	}
	return (uint32_t) ((hash128 * hash1Mult_Large) % (map->size - 5));
}

uint32_t getHash2_Large(LargeCuckooMap* map, uint64_t* hash)
{
	__uint128_t hash128 = 0;
	if(map -> keyLength == 2)
	{
		hash128 = ((__uint128_t) hash[0] << 64) | hash[1];
	}
	else if(map -> keyLength == 3)
	{
		hash128 = ((__uint128_t) hash[2] << 56) | ((__uint128_t) hash[1] << 33) | hash[0] + hash[1];
	}
	return (uint32_t) ((hash128 * hash2Mult_Large) % (map->size - 2));
}

bool addRecursive_Large(LargeCuckooMap* map, uint64_t* key, uint64_t value, int depth)
{
	if(depth > MAX_LOOP)
	{
		return false;
	}
	//if just one is NONE_LARGE, then we can say that all of them are
	if(getKey1_Large(map, key)[0] == NONE_LARGE)
	{
		set1_Large(map, key, value);
	}
	else if(getKey2_Large(map, key)[0] == NONE_LARGE)
	{
		set2_Large(map, key, value);
	}
	else if((rand() & 1) == 0)
	{
		uint64_t pushedKey[map->keyLength];
		for(int a = 0; a < map -> keyLength; a ++)
		{
			pushedKey[a] = getKey1_Large(map, key)[a];
		}
		uint64_t pushedValue = getValue1_Large(map, key);
		set1_Large(map, key, value);
		return addRecursive_Large(map, pushedKey, pushedValue, depth + 1);
	}
	else
	{
		uint64_t pushedKey[map->keyLength];
		for(int a = 0; a < map -> keyLength; a ++)
		{
			pushedKey[a] = getKey2_Large(map, key)[a];
		}
		uint64_t pushedValue = getValue2_Large(map, key);
		set2_Large(map, key, value);
		return addRecursive_Large(map, pushedKey, pushedValue, depth + 1);
	}
	return true;
}

bool add_Large(LargeCuckooMap* map, uint64_t* key, uint64_t value)
{
	return addRecursive_Large(map, key, value, 0);
}

void clearTables_Large(LargeCuckooMap* map)
{
	for(int a = 0; a < map->size * map->elemsPerKey; a ++)
	{
		map->table1[a] = NONE_LARGE;
		map->table2[a] = NONE_LARGE;
	}
}

//Fills the map
void addValues_Large(LargeCuckooMap* map, uint64_t* slowMap, uint32_t* freqs, int bitsPerSample)
{
	puts("Building LargeCuckooMap");
	fflush(stdout);
	int numAdded = 0;
	clearTables_Large(map);
	uint64_t key[map->keyLength];
	for(int a = 0; a < getSignatureMapSize(slowMap) / map->keyLength; a ++)
	{
		getMapKey_Large(slowMap, key, a, map->keyLength);
		if(freqs[a * (bitsPerSample + 1)] >= SAMPLE_CAP)
		{
			continue;
		}
		numAdded ++;
		if(!add_Large(map, key, a))
		{
			printf("Failed to add all values to LargeCuckooMap. Stopped at %d of %d. Retrying...\n", a, (int) (getSignatureMapSize(slowMap) / map->keyLength));
			clearTables_Large(map);
			hash1Mult_Large += rand();
			hash2Mult_Large += rand();
			fflush(stdout);
			a = 0;
			numAdded = 0;
			exit(1);
		}
		else if(findCuckooOptimized_Large(map, key) != a)
		{
			puts("Error in LargeCuckooMap");
			exit(1);
		}
	}
	for(int a = 0; a < getSignatureMapSize(slowMap) / map->keyLength; a ++)
	{
		if(freqs[a * (bitsPerSample + 1)] >= SAMPLE_CAP)
		{
			continue;
		}
		getMapKey_Large(slowMap, key, a, map->keyLength);
		if(findCuckooOptimized_Large(map, key) != a)
		{
			puts("Error in LargeCuckooMap");
			exit(1);
		}
	}
	printf("Inserted %d of %d elements into LargeCuckooMap.\n", numAdded, (int) (getSignatureMapSize(slowMap) / map->keyLength));
	fflush(stdout);
}

static inline bool keys1Equal(LargeCuckooMap* map, uint64_t* key)
{
	bool equal = true;
	for(int a = 0; a < map->keyLength; a ++)
	{
		if(key[a] != getKey1_Large(map, key)[a])
		{
			equal = false;
		}
	}
	return equal;
}

static inline bool keys2Equal(LargeCuckooMap* map, uint64_t* key)
{
	bool equal = true;
	for(int a = 0; a < map->keyLength; a ++)
	{
		if(key[a] != getKey2_Large(map, key)[a])
		{
			equal = false;
		}
	}
	return equal;
}

void removeCuckooMap_Large(LargeCuckooMap* map, uint64_t* key)
{
	if(keys1Equal(map, key))
	{
		clear1_Large(map, key);
	}
	if(keys2Equal(map, key))
	{
		clear2_Large(map, key);
	}
	if(findCuckooOptimized_Large(map, key) != CUCKOO_NOT_FOUND)
	{
		puts("Error with Removing");
		exit(1);
	}
}

int findCuckooOptimized_Large(LargeCuckooMap* map, uint64_t* key)
{
	uint32_t hash1 = getHash1_Large(map, key);
	uint32_t hash2 = getHash2_Large(map, key);
	if(keys1Equal(map, key))
	{
		return map->table1[hash1 * map->elemsPerKey + map->keyLength];
	}
	if(keys2Equal(map, key))
	{
		return map->table2[hash2 * map->elemsPerKey + map->keyLength];
	}
	return CUCKOO_NOT_FOUND;
}

LargeCuckooMap* getCuckooMap_Large(uint64_t* slowMap, uint32_t* freqs, int bitsPerSample, int keyLength)
{
	LargeCuckooMap* map = mallocClean(sizeof(LargeCuckooMap));
	int numKeys = getSignatureMapSize(slowMap) * SPACE_MULTIPLIER / keyLength;
	map->keyLength = keyLength;
	map->elemsPerKey = keyLength + 1;
	map->table1 = mallocClean(sizeof(uint64_t) * numKeys * map->elemsPerKey);
	map->table2 = mallocClean(sizeof(uint64_t) * numKeys * map->elemsPerKey);
	map->size = numKeys;
	addValues_Large(map, slowMap, freqs, bitsPerSample);
	return map;
}
