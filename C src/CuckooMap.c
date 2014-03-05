/*
 * CuckooMap.c
 *
 *  Created on: Jan 4, 2014
 *      Author: Seokho
 */

#include "CuckooMap.h"

/** Reinvents the wheel, but given that the hashing was a huge bottleneck, I wanted full control over it*/

#define MAX_LOOP 100000
#define NONE (((uint64_t) 1 << 63) - 1) //Keys CANNOT occupy this bit
#define SPACE_MULTIPLIER 3.5

void set1(CuckooMap* map, uint64_t key, uint64_t value)
{
	map->table1[getHash1(key, map->size) * ELEMS_PER_KEY] = key;
	map->table1[getHash1(key, map->size) * ELEMS_PER_KEY + 1] = value;
}

void set2(CuckooMap* map, uint64_t key, uint64_t value)
{
	map->table2[getHash2(key, map->size) * ELEMS_PER_KEY] = key;
	map->table2[getHash2(key, map->size) * ELEMS_PER_KEY + 1] = value;
}

void clear1(CuckooMap* map, uint64_t key)
{
	map->table1[getHash1(key, map->size) * ELEMS_PER_KEY] = NONE;
	map->table1[getHash1(key, map->size) * ELEMS_PER_KEY + 1] = NONE;
}
void clear2(CuckooMap* map, uint64_t key)
{
	map->table2[getHash2(key, map->size) * ELEMS_PER_KEY] = NONE;
	map->table2[getHash2(key, map->size) * ELEMS_PER_KEY + 1] = NONE;
}

uint64_t getKey1(CuckooMap* map, uint64_t key)
{
	return map->table1[getHash1(key, map->size) * ELEMS_PER_KEY];
}
uint64_t getKey2(CuckooMap* map, uint64_t key)
{
	return map->table2[getHash2(key, map->size) * ELEMS_PER_KEY];
}
static inline uint64_t getValue1(CuckooMap* map, uint64_t key)
{
	return map->table1[getHash1(key, map->size) * ELEMS_PER_KEY + 1];
}
static inline uint64_t getValue2(CuckooMap* map, uint64_t key)
{
	return map->table2[getHash2(key, map->size) * ELEMS_PER_KEY + 1];
}

uint64_t hash1Mult = 392746233656539461L;
uint64_t hash2Mult = 318372663882574251L;

uint32_t getHash1(uint64_t hash, int mod)
{
	return ((hash * hash1Mult) % (mod - 5)); //sometimes this number had to be twiddled to fit everything into the hashmap
}

uint32_t getHash2(uint64_t hash, int mod)
{
	return ((hash * hash2Mult) % (mod - 1));
}

bool addRecursive(CuckooMap* map, uint64_t key, uint64_t value, int depth)
{
	if(depth > MAX_LOOP)
	{
		return false;
	}
	//if its empty in the first table, add it there
	if(getKey1(map, key) == NONE)
	{
		set1(map, key, value);
	}
	//if its empty in the second table, add it there
	else if(getKey2(map, key) == NONE)
	{
		set2(map, key, value);
	}
	//if both are occupied, randomly displace one and re-add the displaced one
	else if((xorshf96() & 1) == 0)
	{
		uint64_t pushedKey = getKey1(map, key);
		uint64_t pushedValue = getValue1(map, key);
		set1(map, key, value);
		return addRecursive(map, pushedKey, pushedValue, depth + 1);
	}
	else
	{
		uint64_t pushedKey = getKey2(map, key);
		uint64_t pushedValue = getValue2(map, key);
		set2(map, key, value);
		return addRecursive(map, pushedKey, pushedValue, depth + 1);
	}
	return true;
}

bool add(CuckooMap* map, uint64_t key, uint64_t value)
{
	return addRecursive(map, key, value, 0);
}

void clearTables(CuckooMap* map)
{
	memset64(map -> table1, NONE, map -> size * ELEMS_PER_KEY * sizeof(uint64_t));
	memset64(map -> table2, NONE, map -> size * ELEMS_PER_KEY * sizeof(uint64_t));
}

void addValues(CuckooMap* map, uint64_t* slowMap, uint32_t* freqs, int bitsCollection)
{
	puts("Building CuckooMap");
	fflush(stdout);
	int numAdded = 0;
	clearTables(map);
	for(int a = 0; a < getSignatureMapSize(slowMap); a ++)
	{
		if(freqs[a * (bitsCollection + 1)] >= SAMPLE_CAP)
		{
			continue;
		}
		numAdded ++;
		if(slowMap[a] == NONE)
		{
			printf("Key value conflicts with NONE");
			fflush(stdout);
			exit(1);
		}
		if(!add(map, slowMap[a], a))
		{
			printf("Failed to add all values to CuckooMap. Stopped at %d of %d. Retrying...\n", a, (int) getSignatureMapSize(slowMap));
			clearTables(map);
			hash1Mult += rand();
			hash2Mult += rand();
			fflush(stdout);
			a = 0;
			numAdded = 0;
		}
		//catch immediate insertion errors
		else if(findCuckooOptimized(map, slowMap[a]) != a)
		{
			puts("Error in CuckooMap");
			exit(1);
		}
	}
	//Reloop through the whole thing to make sure it's good
	for(int a = 0; a < getSignatureMapSize(slowMap); a ++)
	{
		if(freqs[a * (bitsCollection + 1)] >= SAMPLE_CAP)
		{
			continue;
		}
		if(findCuckooOptimized(map, slowMap[a]) != a)
		{
			puts("Error in CuckooMap");
			exit(1);
		}
	}
	printf("Inserted %d of %d elements into CuckooMap.\n", numAdded, (int) getSignatureMapSize(slowMap));
	fflush(stdout);
}

void removeCuckooMap(CuckooMap* map, uint64_t key)
{
	if(key == getKey1(map, key))
	{
		clear1(map, key);
	}
	if(key == getKey2(map, key))
	{
		clear2(map, key);
	}
	if(findCuckooOptimized(map, key) != CUCKOO_NOT_FOUND)
	{
		//assuming that the key was originally in this map, there's an error
		puts("Error with Removing");
		exit(1);
	}
}
//Super fast
int findCuckooOptimized(CuckooMap* map, uint64_t key)
{
	uint32_t hash1 = getHash1(key, map->size);
	uint32_t hash2 = getHash2(key, map->size);
	if(key == map->table1[hash1 * ELEMS_PER_KEY])
	{
		return map->table1[hash1 * ELEMS_PER_KEY + 1];
	}
	if(key == map->table2[hash2 * ELEMS_PER_KEY])
	{
		return map->table2[hash2 * ELEMS_PER_KEY + 1];
	}
	return CUCKOO_NOT_FOUND;
}

CuckooMap* getCuckooMap(uint64_t* slowMap, uint32_t* freqs, int bitsCollection)
{
	CuckooMap* map = mallocClean(sizeof(CuckooMap));
	int numElems = getSignatureMapSize(slowMap) * SPACE_MULTIPLIER;
	map->table1 = mallocClean(sizeof(uint64_t) * numElems * ELEMS_PER_KEY);
	map->table2 = mallocClean(sizeof(uint64_t) * numElems * ELEMS_PER_KEY);
	map->size = numElems;
	addValues(map, slowMap, freqs, bitsCollection);
	return map;
}
