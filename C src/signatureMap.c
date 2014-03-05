/*
 * signatureMap.c
 *
 *  Created on: Dec 29, 2013
 *      Author: Seokho
 */

#include "signatureMap.h"
#include "Limits.h"

//Returns the array with the size prepended
uint64_t* readSignatureMap(char* filename)
{
	FILE* f = fopen(filename, "rb");
	fseek(f, 0L, SEEK_END);
	int tableBytes = ftell(f);
	fseek(f, 0L, SEEK_SET);
	uint64_t* table = malloc(tableBytes + sizeof(uint64_t));
	table[0] = tableBytes;
	memset(table + 1, 0, tableBytes);
	fread(table + 1, tableBytes, 1, f);
	int count = tableBytes / sizeof(uint64_t);

	if(!isBigEndian()) //I hate endian issues
	{
		for(int a = 0; a < count; a ++)
		{
			table[a + 1] = __builtin_bswap64(table[a + 1]);
		}
	}
	fclose(f);
	return (uint64_t*) table + 1;
}
//Index 0 for first key, 1 for second key, regardless of keylength
void getMapKey_Large(uint64_t* map, uint64_t* keySpace, int index, int keyLength)
{
	for(int b = 0; b < keyLength; b++)
	{
		//keySpace[b] = (map[index * keyLength + b] << ((keyLength - b - 1) * 64));
		keySpace[b] = map[index * keyLength + b];
	}
}
uint64_t getSignatureMapSize(uint64_t* map)
{
	return map[-1] / sizeof(uint64_t);
}

void freeMap(uint64_t* signatureMap)
{
	free(signatureMap - 1);
}
