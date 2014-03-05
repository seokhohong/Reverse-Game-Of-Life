package game;

/**
 * 
 *  Reverse Game Of Life - Miranda
 * 
 *  (packages are not well organized)
 *  .apps: Utility classes generally with its own task
 *  .data: The very non-specific package contains most of the code, composing of what is necessary to manipulate information to generate predictions
 *  .game: Somewhat of a miscellaneous package
 *  .tuning: For fine tuning the parameters for Solver
 *  .utils: Self-explanatory
 *  
 *  C code:
 *  Essentially one streamlined program, run from BoardGenerator.c, designed to sample boards
 *  
 *  
 *  Obviously missing are lots of very large data files, which would be impractical to move around,
 *  and aren't even possible for humans to read anyway (binary arrays)
 * 
 *  
 *  
 *  General procedures:
 *  
 *  CountUniqueBoard.java produces a BitsMap which finds all the unique subboards that are present in the training/testing sets that 
 *  need sampling information.
 *  
 *  BoardGenerator.c uses the BitsMap to sample the designated subboards
 *  
 *  TuneParameters.java optimizes the parameters for processing the raw probability data
 *  
 *  Solver.java finalizes the processing of information and outputs results
 *
 **/

public class GameOfLife 
{
	public static final String LOCAL_DIR = "C:\\Life\\";
	public static final int WIDTH = 20;
	public static final int HEIGHT = 20;
}
