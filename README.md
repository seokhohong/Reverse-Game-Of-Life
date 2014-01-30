Reverse Game Of Life
====================
Dec 2013-Feb 2014: Entry into the Reverse Game Of Life competition on Kaggle.com 
http://www.kaggle.com/c/conway-s-reverse-game-of-life


What's currently uploaded is one file of my program, the underlying representation of the Game of Life grid, which reveals nothing specific about my methods but still presents a critical component exemplifying the complexities of implementing my solution. The rest will be uploaded after the conclusion of the competition on March 3, 2014, along with more details about my methods.


As data competitions generally go, an inevitable hurdle is time and space, and are perhaps the only major hurdles of this problem. Although about two-thirds of code is in Java, I've exported the calculation intensive portions to C for better performance (approximately 3x faster). The uploaded board.c shows the extent to which this code has been optimized, heavily exploiting bit operations to speed up both generation of boards and iteration through generations. The other parts of the program function similarly, capitalizing on lookup tables, bit manipulation, and fast hash tables (the main bottleneck in the computation is handled by the wonderfully fast cuckoo hashmap). Even calling rand() became a performance issue, which I subsequently replaced with an XOR-shift number generator. Of course, it's also multithreaded for maximum performance.


Premature optimization may be the root of all evil, but since this code has run for perhaps thousands of cpu-hours on my machine, I argue it is justified.


Note with regard to Kaggle rules: I am still abiding by the rules to not share code privately, as this is a public GitHub repository, theoretically accessible by all participants.


Current standing:
As of Jan. 30, 2014, I hold 1st place (under pseudoname Miranda) out of 96 teams with one month remaining in the competition.
