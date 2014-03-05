/*
 * malloc.h
 *
 *  Created on: Dec 31, 2013
 *      Author: Seokho
 */

#ifndef MALLOC_H_
#define MALLOC_H_

#include <stdint.h>
#include <string.h>
#include <stdlib.h>

void* mallocClean(uint64_t bytes);
void memset64( void * dest, uint64_t value, uintptr_t size );

#endif /* MALLOC_H_ */
