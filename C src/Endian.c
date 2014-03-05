/*
 * Endian.c
 *
 *  Created on: Dec 31, 2013
 *      Author: Seokho
 */

#include "Endian.h"

bool isBigEndian()
{
    int test_var = 1;
    unsigned char* test_endian = (unsigned char*)&test_var;
    return test_endian[0] == 0;
}
