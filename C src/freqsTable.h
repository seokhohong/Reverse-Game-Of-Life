/*
 * freqsTable.h
 *
 *  Created on: Dec 29, 2013
 *      Author: Seokho
 */

#ifndef FREQSTABLE32_H_
#define FREQSTABLE32_H_

#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdbool.h>
#include "Endian.h"
#include "malloc.h"

uint32_t* getFreqsTable(uint64_t* slowMap, int dim, int keyLength);
uint32_t* readFreqsTable(char* filename);
void writeFreqsTable(char* filename, uint32_t* table);

#endif /* FREQSTABLE_H_ */
