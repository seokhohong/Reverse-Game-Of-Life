/*
 * print128.h
 *
 *  Created on: Feb 3, 2014
 *      Author: HONG
 */

#ifndef PRINT128_H_
#define PRINT128_H_

#include "Limits.h"
#include <inttypes.h>

__uint128_t combine(uint64_t one, uint64_t two);
int print_u128(__uint128_t u128);
int print_u128E(char* leading, __uint128_t u128, char* trailing);

#endif /* PRINT128_H_ */
