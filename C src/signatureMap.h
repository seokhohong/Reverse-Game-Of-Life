/*
 * signatureMap.h
 *
 *  Created on: Dec 29, 2013
 *      Author: Seokho
 */

#ifndef SIGNATUREMAP_H_
#define SIGNATUREMAP_H_

#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdbool.h>
#include <inttypes.h>
#include "Endian.h"

uint64_t* readSignatureMap(char* filename);
uint64_t getSignatureMapSize(uint64_t* map);
void getMapKey_Large(uint64_t* map, uint64_t* keySpace, int index, int keyLength);
void freeMap(uint64_t* map);

#endif /* SIGNATUREMAP_H_ */
