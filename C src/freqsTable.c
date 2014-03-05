/*
 * freqsTable.c
 *
 *  Created on: Dec 29, 2013
 *      Author: Seokho
 */

#include "freqsTable.h"
#include <fcntl.h>

//Works up to 4gb
uint32_t* getFreqsTable(uint64_t* slowMap, int dim, int keyLength)
{
	int slowMapLength =  (*(slowMap - 1) / sizeof(uint64_t) / keyLength);
	int numEntriesPer = dim * dim + 1;
	uint32_t size = (slowMapLength * numEntriesPer + 1) * sizeof(uint32_t);
	uint32_t* table = mallocClean(size);
	table[0] = size;
	return table + 1;
}

uint64_t getFileSize_64(const char* filename)
{
	 int fh = _open( filename, _O_BINARY );

	 uint64_t n = _lseeki64(fh, 0, SEEK_END);

	 /* Close file */
	 _close(fh);

	 return n;
}

uint32_t* readFreqsTable(char* filename)
{
	FILE* f = fopen(filename, "rb");
	uint64_t tableBytes = getFileSize_64(filename);
	uint32_t* table = mallocClean(tableBytes + sizeof(uint32_t));
	table[0] = tableBytes;
	fread(table + 1, tableBytes, 1, f);
	if(!isBigEndian())
	{
		for(int a = 0; a < tableBytes / sizeof(uint32_t); a ++)
		{
			table[a + 1] = __builtin_bswap32(table[a + 1]);
		}
	}
	fclose(f);
	return (uint32_t*) table + 1;
}

void writeFreqsTable(char* filename, uint32_t* table)
{
	FILE* f = fopen(filename, "wb");
	if(!isBigEndian())
	{
		for(int a = 0; a < table[-1] / sizeof(uint32_t); a ++)
		{
			table[a] = __builtin_bswap32(table[a]);
		}
	}
	fwrite(table, table[-1], 1, f);
	//switch back
	if(!isBigEndian())
	{
		for(int a = 0; a < table[-1] / sizeof(uint32_t); a ++)
		{
			table[a] = __builtin_bswap32(table[a]);
		}
	}
	fclose(f);
}
