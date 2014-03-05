/*
 * malloc.c
 *
 *  Created on: Dec 31, 2013
 *      Author: Seokho
 */

#include "malloc.h"

void* mallocClean(uint64_t bytes)
{
	void* mem = malloc(bytes);
	memset(mem, 0, bytes);
	return mem;
}

void memset64( void * dest, uint64_t value, uintptr_t size )
{
  uintptr_t i;
  for( i = 0; i < (size & (~7)); i+=8 )
  {
    memcpy( ((char*)dest) + i, &value, 8 );
  }
  for( ; i < size; i++ )
  {
    ((char*)dest)[i] = ((char*)&value)[i&7];
  }
}
