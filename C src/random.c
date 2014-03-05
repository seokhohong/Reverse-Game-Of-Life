/*
 * random.c
 *
 *  Created on: Dec 29, 2013
 *      Author: Seokho
 */

#include <stdlib.h>

static unsigned long x=123456789, y=362436069, z=521288629;

unsigned long xorshf96()
{
	unsigned long t;
    x ^= x << 16;
    x ^= x >> 5;
    x ^= x << 1;

    t = x;
    x = y;
    y = z;
    z = t ^ x ^ y;

    return z;
}
