/*
 * print128.c
 *
 *  Created on: Feb 3, 2014
 *      Author: HONG
 */


#include "print128.h"
#include <inttypes.h>
#include <stdint.h>
#include <stdio.h>

#define P10_UINT64 10000000000000000000ULL   /* 19 zeroes */
#define E10_UINT64 19

#define STRINGIZER(x)   # x
#define TO_STRING(x)    STRINGIZER(x)

__uint128_t combine(uint64_t one, uint64_t two)
{
	return ((__uint128_t) one << 64) | two;
}

int print_u128(__uint128_t u128)
{
    int rc;
    if (u128 > UINT64_MAX)
    {
        __uint128_t leading  = u128 / P10_UINT64;
        uint64_t  trailing = u128 % P10_UINT64;
        rc = print_u128(leading);
        rc += printf("%." TO_STRING(E10_UINT64) PRIu64, trailing);
    }
    else
    {
        uint64_t u64 = u128;
        rc = printf("%" PRIu64, u64);
    }
    return rc;
}


int print_u128E(char* leadString, __uint128_t u128, char* postString)
{
    int rc;
    if (u128 > UINT64_MAX)
    {
        __uint128_t leading  = u128 / P10_UINT64;
        uint64_t  trailing = u128 % P10_UINT64;
        rc = print_u128E(leadString, leading, "");
        rc += printf("%." TO_STRING(E10_UINT64) PRIu64 "%s", trailing, postString);
    }
    else
    {
        uint64_t u64 = u128;
        rc = printf("%s%" PRIu64 "%s", leadString, u64, postString);
    }
    return rc;
}
