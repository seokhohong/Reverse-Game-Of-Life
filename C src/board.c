/*
 * board.c
 *
 *  Created on: Dec 29, 2013
 *      Author: Seokho
 */

#include "board.h"
#include <inttypes.h>
#include "Endian.h"
#include "Limits.h"

#define __STDC_FORMAT_MACROS

#define CLUSTER_ROWS 4
#define CLUSTER_COLS 6
#define CLUSTER_BITS 24

uint64_t* newBoard()
{
	return (uint64_t*) mallocClean(BOARD_BITS);
}

static inline bool getBoardBit1D(uint64_t* board, int index)
{
	int row = index / BOARD_SIZE;
	int col = index % BOARD_SIZE;
	return getBoardBit(board, row, col);
}

void getRandomBoardOptimized(uint64_t* board)
{
	double frequency = (xorshf96() % 99) + 1; //between 1 and 99
	for(int a = 0; a < BOARD_SIZE * BOARD_SIZE; a ++ )
	{
		int row = a / BOARD_SIZE;
		int col = a % BOARD_SIZE;
		if(xorshf96() % 100 < frequency)
		{
			setBoardBit(board, row, col, 1);
		}
		else
		{
			setBoardBit(board, row, col, 0);
		}
	}
}
uint64_t* getRandomBoard()
{
	uint64_t* board = newBoard();
	double frequency = (rand() % 99) + 1; //between 1 and 99
	for(int a = 0; a < BOARD_SIZE * BOARD_SIZE; a ++ )
	{
		int row = a / BOARD_SIZE;
		int col = a % BOARD_SIZE;
		if(xorshf96() % 100 < frequency)
		{
			setBoardBit(board, row, col, 1);
		}
		else
		{
			setBoardBit(board, row, col, 0);
		}
	}
	return board;
}
uint64_t* getCheckerBoard()
{
	uint64_t* board = newBoard();
	for(int a = 0; a < BOARD_SIZE * BOARD_SIZE; a ++ )
	{
		int row = a / BOARD_SIZE;
		int col = a % BOARD_SIZE;
		if((row % 2) + (col % 2) == 1)
		{
			setBoardBit(board, row, col, 1);
		}
		else
		{
			setBoardBit(board, row, col, 0);
		}
	}
	return board;
}

void clear(uint64_t* board)
{
	memset(board, 0, BOARD_BITS);
}

uint64_t* duplicate(uint64_t* board)
{
	uint64_t* dup = newBoard();
	memcpy(dup, board, BOARD_BITS);
	return dup;
}

int getNeighbors(uint64_t* board, int row, int col)
{
	int neighbors = 0;
	if(row > 0)
	{
		if(col > 0) neighbors += getBoardBit(board, row - 1, col - 1);
		if(col < BOARD_SIZE - 1) neighbors += getBoardBit(board, row - 1, col + 1);
		neighbors += getBoardBit(board, row - 1, col);
	}
	if(col > 0) neighbors += getBoardBit(board, row, col - 1);
	if(col < BOARD_SIZE - 1) neighbors += getBoardBit(board, row, col + 1);
	if(row < BOARD_SIZE - 1)
	{
		if(col > 0) neighbors += getBoardBit(board, row + 1, col - 1);
		if(col < BOARD_SIZE - 1) neighbors += getBoardBit(board, row + 1, col + 1);
		neighbors += getBoardBit(board, row + 1, col);
	}
	return neighbors;
}

int neighborTable[1 << 9]; //1 or 0
uint16_t clusterTable[1 << CLUSTER_BITS];

static inline unsigned short __builtin_bswap16(unsigned short a)
{
  return (a<<8)|(a>>8);
}

void buildClusterTable()
{
	FILE* f = fopen("clusterTable.txt", "rb");
	fread(clusterTable, (1 << CLUSTER_BITS) * sizeof(uint16_t), 1, f);
	if(!isBigEndian())
	{
		for(int a = 0; a < 1 << CLUSTER_BITS; a ++)
		{
			clusterTable[a] = __builtin_bswap16(clusterTable[a]);
		}
	}
	fclose(f);
}
/*
//4x2
void buildClusterTable()
{
	memset(clusterTable, 0, (1 << CLUSTER_BITS) * sizeof(uint16_t));
	for(int a = 0; a < (1 << CLUSTER_BITS); a++)
	{
		int bits[CLUSTER_BITS];
		for(int b = 0; b < CLUSTER_BITS; b++)
		{
			bits[b] = (a >> b) & 1;
		}
		for(int c = 1; c < CLUSTER_COLS - 1; c++)
		{
			for(int d = 1; d < CLUSTER_ROWS - 1; d++)
			{
				int neighbors = bits[(c - 1) * (CLUSTER_ROWS - 2) + d - 1]
				              + bits[c * (CLUSTER_ROWS - 2) + d - 1]
				              + bits[(c + 1) * (CLUSTER_ROWS - 2) + d - 1]
				              + bits[(c - 1) * (CLUSTER_ROWS - 2) + d]
				              + bits[(c + 1) * (CLUSTER_ROWS - 2) + d]
				              + bits[(c - 1) * (CLUSTER_ROWS - 2) + d + 1]
				              + bits[c * (CLUSTER_ROWS - 2) + d + 1]
				              + bits[(c + 1) * (CLUSTER_ROWS - 2) + d + 1];
				if((neighbors == 2 && bits[c * (CLUSTER_ROWS - 2) + d]) || neighbors == 3)
				{
					clusterTable[a] |= 1 << ((c - 1) * (CLUSTER_ROWS - 2) + (d - 1));
				}
			}
		}
	}
}
*/

void buildNeighborTable()
{
	for(int a = 0; a < (1 << 9); a++)
	{
		int neighbors = 0;
		neighborTable[a] = 0;
		bool centerAlive = false;
		for(int b = 0; b < 9; b++)
		{
			if((a >> b) & 1)
			{
				if(b == 4) centerAlive = true;
				else neighbors++;
			}
		}
		if(centerAlive && neighbors == 2)
		{
			neighborTable[a] = 1;
		}
		if(neighbors == 3)
		{
			neighborTable[a] = 1;
		}
	}
	buildClusterTable();
}

bool stepOptimized2(uint64_t* board)
{
	uint64_t nextBoard[BIG_DIM];
	memset(nextBoard, 0, BIG_DIM * sizeof(uint64_t));
	uint64_t aliveSignature = 0;
	for(int row = 0; row < BOARD_SIZE; row += CLUSTER_ROWS - 2)
	{
		for(int col = 0; col < BOARD_SIZE; col += CLUSTER_COLS - 2)
		{
			int samplingRow = row - 1;
			int samplingCol = col - 1;
			int colShift = (PAD + samplingCol);
			uint64_t bits = 0;
			uint64_t mask = ((1 << CLUSTER_COLS) - 1) << colShift;
			for(int a = 0; a < CLUSTER_ROWS; a ++)
			{
				bits |= (((uint64_t) (board[a + samplingRow + PAD] & mask)) >> colShift) << (a * CLUSTER_COLS);
			}
			uint64_t result = (uint64_t) clusterTable[bits];
			aliveSignature |= result;
			for(int subRow = 0; subRow < CLUSTER_ROWS - 2; subRow ++)
			{
				uint64_t resultMask = ((1 << (CLUSTER_COLS - 2)) - 1) << subRow * (CLUSTER_COLS - 2);
				nextBoard[PAD + row + subRow] |= (result & resultMask) >> (subRow * (CLUSTER_COLS - 2)) << (col + PAD);
			}
		}
	}
	memcpy(board, nextBoard, BOARD_BITS);
	return aliveSignature > 0;
}
//Returns number alive
int stepOptimized(uint64_t* board)
{
	uint64_t* nextBoard = newBoard();
	int numAlive = 0;
	for(int a = 0; a < BOARD_SIZE * BOARD_SIZE; a++)
	{
		int row = a / BOARD_SIZE - 1; //-1 offset of 3x3 square
		int col = a % BOARD_SIZE - 1;
		//Equivalent to getBits3x3 to get bits of neighbors
		uint64_t bits = 0;
		uint64_t mask = ((1 << 3) - 1) << (PAD + col);
		for(int a = 0; a < 3; a++)
		{
			bits |= (((uint64_t) (board[a + row + PAD] & mask)) >> (PAD + col)) << (a * 3);
		}
		//+1 offset for 3x3 squareness
		numAlive += neighborTable[(int) bits];
		setBoardBit(nextBoard, row + 1, col + 1, neighborTable[(int) bits]);
	}
	memcpy(board, nextBoard, BOARD_BITS);
	free(nextBoard);
	return numAlive;
}

/** Returns if the next step is alive */

bool step(uint64_t* board)
{
	uint64_t nextBoard[BIG_DIM];
	memset(nextBoard, 0, BIG_DIM * sizeof(uint64_t));
	bool isAlive = false;
	for(int a = 0; a < BOARD_SIZE * BOARD_SIZE; a++)
	{
		int row = a / BOARD_SIZE;
		int col = a % BOARD_SIZE;
		int neighbors = 0;
		neighbors += getBoardBit(board, row - 1, col - 1);
		neighbors += getBoardBit(board, row - 1, col + 1);
		neighbors += getBoardBit(board, row - 1, col);
		neighbors += getBoardBit(board, row, col - 1);
		neighbors += getBoardBit(board, row, col + 1);
		neighbors += getBoardBit(board, row + 1, col - 1);
		neighbors += getBoardBit(board, row + 1, col + 1);
		neighbors += getBoardBit(board, row + 1, col);
		switch(neighbors)
		{
			case 2:
			{
				if(getBoardBit(board, row, col))
				{
					setBoardBit(nextBoard, row, col, ALIVE);
					isAlive = true;
				}
				break;
			}
			case 3:
			{
				setBoardBit(nextBoard, row, col, ALIVE);
				isAlive = true;
			}
		}
	}
	memcpy(board, nextBoard, BOARD_BITS);
	//free(nextBoard);
	return isAlive;
}

/** Returns if the next step is alive */
/*
bool step(uint64_t* board)
{
	uint64_t* nextBoard = newBoard();
	bool isAlive = false;
	for(int a = 0; a < BOARD_SIZE * BOARD_SIZE; a++)
	{
		int row = a / BOARD_SIZE;
		int col = a % BOARD_SIZE;
		switch(getNeighbors(board, row, col))
		{
			case 2:
			{
				if(getBoardBit(board, row, col))
				{
					setBoardBit(nextBoard, row, col, ALIVE);
					isAlive = true;
				}
				break;
			}
			case 3:
			{
				setBoardBit(nextBoard, row, col, ALIVE);
				isAlive = true;
			}
		}
	}
	memcpy(board, nextBoard, BOARD_BITS);
	free(nextBoard);
	return isAlive;
}
*/
uint64_t getBits4x4(uint64_t* board, int row, int col)
{
	uint64_t bits = 0;
	uint64_t mask = ((1 << 4) - 1) << (PAD + col);
	for(int a = 0; a < 4; a++)
	{
		bits |= (((uint64_t) (board[a + row + PAD] & mask)) >> (PAD + col)) << (a * 4);
	}
	return bits;
}
/*
uint64_t getBits(uint64_t* board, int row, int col, int dim)
{
	uint64_t bits = 0;
	uint64_t mask = ((1 << dim) - 1) << (PAD + col);
	for(int a = 0; a < dim; a++)
	{
		bits |= (((uint64_t) (board[a + row + PAD] & mask)) >> (PAD + col)) << (a * dim);
	}
	return bits;
}
*/
uint64_t getBits(uint64_t* board, int row, int col, int rowDim, int colDim)
{
	uint64_t bits = 0;
	uint64_t mask = ((1 << colDim) - 1) << (PAD + col);
	for(int a = 0; a < rowDim; a++)
	{
		bits |= (((uint64_t) (board[a + row + PAD] & mask)) >> (PAD + col)) << (a * colDim);
	}
	return bits;
}
void getBitsLarge(uint64_t* board, uint64_t* bitsSpace, int row, int col, int rowDim, int colDim, int keyDivisions)
{
	for(int a = 0; a < keyDivisions; a++)
	{
		bitsSpace[a] = getBits(board, row + (rowDim / keyDivisions) * a, col, rowDim / keyDivisions, colDim);
	}
	/*
	if(rowDim == 12)
	{
		bitsSpace[0] = getBits(board, row, col, 5, colDim);
		bitsSpace[1] = getBits(board, row + 5, col, 5, colDim);
		bitsSpace[2] = getBits(board, row + 10, col, 5, colDim);
	}
	else if(rowDim == 10)
	{
		bitsSpace[0] = getBits(board, row, col, rowDim / 2, colDim);
		bitsSpace[1] = getBits(board, row + rowDim / 2, col, rowDim / 2, colDim);
	}
	else if(rowDim == 9)
	{
		bitsSpace[0] = getBits(board, row, col, 4, colDim);
		bitsSpace[1] = getBits(board, row + 4, col, 5, colDim);
	}
	*/
}


uint64_t getBits5x5(uint64_t* board, int row, int col)
{
	uint64_t bits = 0;
	uint64_t mask = ((1 << 5) - 1) << (PAD + col);
	for(int a = 0; a < 5; a++)
	{
		bits |= (((uint64_t) (board[a + row + PAD] & mask)) >> (PAD + col)) << (a * 5);
	}
	return bits;
}

void getBits5x5Optimized(uint64_t* board, int row, int col, uint64_t* result)
{
	*result = (((uint64_t) (board[0 + row + PAD] & ((1 << 5) - 1) << (PAD + col))) >> (PAD + col)) << (0 * 5)
			| (((uint64_t) (board[1 + row + PAD] & ((1 << 5) - 1) << (PAD + col))) >> (PAD + col)) << (1 * 5)
			| (((uint64_t) (board[2 + row + PAD] & ((1 << 5) - 1) << (PAD + col))) >> (PAD + col)) << (2 * 5)
			| (((uint64_t) (board[3 + row + PAD] & ((1 << 5) - 1) << (PAD + col))) >> (PAD + col)) << (3 * 5)
			| (((uint64_t) (board[4 + row + PAD] & ((1 << 5) - 1) << (PAD + col))) >> (PAD + col)) << (4 * 5);
}

uint64_t getBits7x7(uint64_t* board, int row, int col)
{
	uint64_t bits = 0;
	uint64_t mask = ((1 << 7) - 1) << (PAD + col);
	for(int a = 0; a < 7; a++)
	{
		bits |= (((uint64_t) (board[a + row + PAD] & mask)) >> (PAD + col)) << (a * 7);
	}
	return bits;
}

uint64_t* toPaddedBits(uint64_t* board)
{
	int bigDim = BOARD_SIZE + PAD * 2;
	uint64_t* rows = mallocClean(bigDim * sizeof(uint64_t));
	for(int a = 0; a < BOARD_BITS; a ++)
	{
		int row = a / BOARD_SIZE;
		int col = a % BOARD_SIZE;
		rows[PAD + row] |= board[a] << (PAD + col);
	}
	return rows;
}

char* print(uint64_t* board)
{
	char* str = mallocClean(BOARD_SIZE * (BOARD_SIZE + 1));
	for(int a = 0; a < BOARD_SIZE * BOARD_SIZE; a++)
	{
		if(a > 0 && a % BOARD_SIZE == 0)
		{
			strcat(str, "\n");
		}
		strcat(str, ((getBoardBit1D(board, a) == 1) ? "1" : "0"));
	}
	return str;
}

char* printEntire(uint64_t* board)
{
	char* str = mallocClean(BIG_DIM * (BIG_DIM + 1));
	for(int a = 0; a < BIG_DIM * BIG_DIM; a++)
	{
		if(a > 0 && a % BIG_DIM == 0)
		{
			strcat(str, "\n");
		}
		strcat(str, (((board[a / BIG_DIM] & ((uint64_t) 1 << (a % BIG_DIM))) != 0) ? "1" : "0"));
	}
	return str;
}

char* printSubboard(uint64_t board, int dim)
{
	char* str = mallocClean(dim * (dim + 1));
	for(int a = 0; a < dim * dim; a++)
	{
		if(a > 0 && a % dim == 0)
		{
			strcat(str, "\n");
		}
		strcat(str, (((board & ((uint64_t) 1 << a)) > 0) ? "1" : "0"));
	}
	return str;
}

void write(char* filename, char* string)
{
    FILE *fp = fopen(filename, "wb");
    if (fp != NULL)
    {
        fputs(string, fp);
        fclose(fp);
    }
}

void writeDebugging(char* filename, uint64_t* board)
{
	puts(print(board));
	fflush(stdout);
    FILE *fp = fopen(filename, "wb");
    if (fp != NULL)
    {
    	for(int turn = 0; turn < 5; turn++)
    	{
			for(int a = 0; a < BOARD_SIZE; a ++)
			{
				uint64_t data = __builtin_bswap64(board[a + PAD]);
				fwrite(&data, sizeof(uint64_t), 1, fp);
			}
			for(int a = -3; a < BOARD_SIZE; a ++)
			{
				for(int b = -3; b < BOARD_SIZE; b ++)
				{
					uint64_t data = __builtin_bswap64(getBits4x4(board, a, b));
					fwrite(&data, sizeof(uint64_t), 1, fp);
				}
			}
			stepOptimized2(board);
			puts("\n\n");
			puts(print(board));
			fflush(stdout);
    	}
        fclose(fp);
    }
}
