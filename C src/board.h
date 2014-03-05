/*
 * board.h
 *
 *  Created on: Dec 29, 2013
 *      Author: Seokho
 */

#ifndef BOARD_H_
#define BOARD_H_

#include <stdbool.h>
#include <stdint.h>
#include "random.h"
#include <string.h>
#include <stdlib.h>
#include <stdio.h>
#include <stdint.h>
#include "malloc.h"
#include "Limits.h"

#define ALIVE 1
#define BOARD_SIZE 20
#define PAD 12
#define BIG_DIM (PAD * 2 + BOARD_SIZE)
#define BOARD_BITS (BIG_DIM * sizeof(uint64_t))

uint64_t* newBoard();
uint64_t* getRandomBoard();
void getRandomBoardOptimized(uint64_t* board);
uint64_t* getCheckerBoard();
uint64_t* duplicate(uint64_t* board);
//void duplicateOptimized(uint64_t* board, uint64_t* dup);
uint64_t getBits4x4(uint64_t* board, int row, int col);
uint64_t getBits(uint64_t* board, int row, int col, int rowDim, int colDim);
uint64_t getBits5x5(uint64_t* board, int row, int col);
void getBits5x5Optimized(uint64_t* board, int row, int col, uint64_t* result);
uint64_t getBits7x7(uint64_t* board, int row, int col);
void getBitsLarge(uint64_t* board, uint64_t* bitsSpace, int row, int col, int rowDim, int colDim, int keyDivisions);
bool stepOptimized2(uint64_t* board);
int stepOptimized(uint64_t* board);
bool step(uint64_t* board);
char* print(uint64_t* board);
char* printEntire(uint64_t* board);
char* printSubboard(uint64_t bits, int dim);
void write(char* filename, char* string);
void writeDebugging(char* filename, uint64_t* board);
void buildNeighborTable();

static inline bool getBoardBit(uint64_t* board, int row, int col)
{
	return (board[PAD + row] & ((uint64_t) 1 << (PAD + col))) > 0;
}
static inline void setBoardBit(uint64_t* board, int row, int col, int value)
{
	board[PAD + row] |= ((uint64_t) value) << (PAD + col);
}
static inline void duplicateOptimized(uint64_t* board, uint64_t* dup)
{
	memcpy(dup, board, BOARD_BITS);
}

#endif /* BOARD_H_ */
