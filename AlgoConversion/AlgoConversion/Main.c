#include <stdlib.h>
#include <stdio.h>

/* Constants */
#define ROW 22
#define COL 17
#define MOVE_COST 10;                         // cost of FORWARD, BACKWARD movement
#define TURN_COST 20;                         // cost of RIGHT, LEFT movement. Cost more as no progress made

struct Coordinate {				
	int row;
	int col;
} Coordinates;

struct Cell {
	int isExplored;
	int isObstacle;
	int isWalked;
	int isStart;
	int isDest;
	int robotAtCell;
	char cellType;				// " " = unexplored, 0 = movable, 1 = not movable, X = robot pos , Y = goal
	double gCost;
} Cells;

struct Map {
	struct Cell cell[ROW][COL];	//Collection of cell[Row][Col]
} Maps;

/* Stack implementation */
struct StackNode
{
	struct Coordinate coords;
	struct StackNode* next;
} StackNodeItem;
struct StackNode* newNode(int row, int col)
{
	struct StackNode* stackNode = (struct StackNode*) malloc(sizeof(struct StackNode));
	stackNode->coords.row = row;
	stackNode->coords.col = col;
	stackNode->next = NULL;
	return stackNode;
}
int isEmpty(struct StackNode *root)
{
	return !root;
}
void push(struct StackNode** root, int row, int col)
{
	struct StackNode* stackNode = newNode(row, col);
	stackNode->next = *root;
	*root = stackNode;
	printf("%d %d pushed to stack\n", row, col);
}
struct Coordinate pop(struct StackNode** root)
{
	if (isEmpty(*root)) {
		struct Coordinate emptyCoords;
		emptyCoords.row = -1;
		emptyCoords.col = -1;

		return emptyCoords;
	}
	struct StackNode* temp = *root;
	*root = (*root)->next;
	struct Coordinate popped = temp->coords;
	free(temp);

	return popped;
}
struct Coordinate peek(struct StackNode* root)
{
	if (isEmpty(root)) {
		struct Coordinate emptyCoords;
		emptyCoords.row = -1;
		emptyCoords.col = -1;

		return emptyCoords;
	}
	return root->coords;
}

///* description of route between two nodes */
//struct route {
//	/* route has only one direction! */
//	int x; /* index of stop in array of all stops of src of this route */
//	int y; /* intex of stop in array of all stops od dst of this route */
//	double d;
//};
//
///* description of graph node to visit*/
//struct stop {
//	double col, row;
//	/* array of indexes of routes from this stop to neighbours in array of all routes */
//	int * n;
//	int n_len;
//	double f, g, h;
//	int from;
//};

/* Global variables*/
struct Map mapRobot;				//Map for robot to navigate with
struct StackNode* root = NULL; 		//Global stack
int toVisitCount = 0;				//Keep track of size of toVisit array
int visitedCount = 0;				//Keep track of size of visited array
struct Coordinate toVisit[84];		// max possible moves
struct Coordinate visited[84];		// max possible moves
struct Coordinate neighbours[4];	// NSEW of robot
struct Coordinate current;
char curDir = 'N';					// Current direction of robot (TTD: GET ACTUAL CURRENT ROBOT DIRECTION)


///* Check whether cell is valid */
//int isValid(int row, int col)
//{
//	/* Returns 1 if row and column in range */
//	if((row >= 0) && (row < ROW) &&	(col >= 0) && (col < COL))
//		return 1;
//	else
//		return 0;
//}
//
///* Check whether cell is blocked */
//int isUnBlocked(int grid[ROW][COL], int row, int col)
//{
//	/* Returns 1 if the cell is not blocked */
//	if (grid[row][col] == 1)
//		return 1;
//	else
//		return 0;
//}
//
///* Check whether destination is reached */
//int isDestination(int row, int col, struct Coordinate dest)
//{
//	if (row == dest.row && col == dest.col)
//		return 1;
//	else
//		return 0;
//}


/* Returns heuristic cost (h(n)) from a given Coordinate to a given [row, col] of maze */
int costH(struct Coordinate b, int row, int col) {
	// No. of moves = sum of the difference of the rows and col absolute values.
	int moveCost = (abs(col - b.col) + abs(row - b.row)) * MOVE_COST;

	if (moveCost == 0)
		return 0;

	// If b not in the same row or col, 1 turn will be needed.
	int turnCost = 0;
	if (col - b.col != 0 || row - b.row != 0) {
		turnCost = TURN_COST;
	}

	return moveCost + turnCost;
}

/* Returns Coordinate in toVisit with min(g(n) + h(n)) */
struct Coordinate minCostCoordinate(int row, int col) {
	int minCost = 999;
	struct Coordinate result;
	result.col = -1;
	result.row = -1;

	// If gCost < minCost, set minCost as gCost and return the coords of toVisit
	for (int i = toVisitCount - 1; i >= 0; i--) {
		int gCost = mapRobot.cell[toVisit[i].row][toVisit[i].col].gCost;
		int cost = gCost + costH(toVisit[i], row, col);
		if (cost < minCost) {
			minCost = cost;
			result.col = toVisit[i].row;
			result.col = toVisit[i].col;
		}
	}

	return result;
}

// Set startRow and startCol as robot's coordinates
// TTD: ROBOT NEEDS TO KEEP TRACK OF IT'S OWN COORDINATES
int fastestPath(int startRow, int startCol, int destRow, int destCol) {
	printf("Executing fastest path from %d %d to %d %d\n", startRow, startCol, destRow, destCol);

	current.row = startRow;
	current.col = startCol;

	printf("Initializing gCost of cells\n");
	for (int r = ROW - 1; r >= 0; r--) {
		for (int c = 0; c <= COL - 1; c++) {

			/* If cell is obstacle, max gCost, else set to 0*/
			if (mapRobot.cell[r][c].isObstacle) {		
				mapRobot.cell[r][c].gCost = 999;
			}
			else {
				mapRobot.cell[r][c].gCost = 0;
			}
		}
	}

	toVisit[0] = current;
	toVisitCount++;

	return 1;
}

/* Online reference */
//int fastestPath(int startRow, int startCol, int destRow, int destCol) {
//	printf("Executing fastest path from %d %d to %d %d\n", startRow, startCol, destRow, destCol);
//
//	int i, j, k, l, b, found;
//	int p_len = 0;
//	int * path = NULL;
//	int c_len = 0;
//	int * closed = NULL;
//	int o_len = 1;
//	int * open = (int*)calloc(o_len, sizeof(int));
//	double min, tempg;
//	int s;
//	int e;
//	int current;
//	int s_len = 0;
//	struct stop * stops = NULL;
//	int r_len = 0;
//	struct route * routes = NULL;
//
//	for (i = 1; i < ROW - 1; i++) {
//		for (j = 1; j < COL - 1; j++) {
//			if (!mapRobot.cell[i][j].isObstacle) {
//				++s_len;
//				stops = (struct stop *)realloc(stops, s_len * sizeof(struct stop));
//				int t = s_len - 1;
//				stops[t].col = j;
//				stops[t].row = i;
//				stops[t].from = -1;
//				stops[t].g = DBL_MAX;
//				stops[t].n_len = 0;
//				stops[t].n = NULL;
//				ind[i][j] = t;
//			}
//		}
//	}
//
//	/* index of start stop */
//	s = 0;
//	/* index of finish stop */
//	e = s_len - 1;
//
//	for (i = 0; i < s_len; i++) {
//		stops[i].h = sqrt(pow(stops[e].row - stops[i].row, 2) + pow(stops[e].col - stops[i].col, 2));
//	}
//
//	for (i = 1; i < map_size_rows - 1; i++) {
//		for (j = 1; j < map_size_cols - 1; j++) {
//			if (ind[i][j] >= 0) {
//				for (k = i - 1; k <= i + 1; k++) {
//					for (l = j - 1; l <= j + 1; l++) {
//						if ((k == i) and (l == j)) {
//							continue;
//						}
//						if (ind[k][l] >= 0) {
//							++r_len;
//							routes = (struct route *)realloc(routes, r_len * sizeof(struct route));
//							int t = r_len - 1;
//							routes[t].x = ind[i][j];
//							routes[t].y = ind[k][l];
//							routes[t].d = sqrt(pow(stops[routes[t].y].row - stops[routes[t].x].row, 2) + pow(stops[routes[t].y].col - stops[routes[t].x].col, 2));
//							++stops[routes[t].x].n_len;
//							stops[routes[t].x].n = (int*)realloc(stops[routes[t].x].n, stops[routes[t].x].n_len * sizeof(int));
//							stops[routes[t].x].n[stops[routes[t].x].n_len - 1] = t;
//						}
//					}
//				}
//			}
//		}
//	}
//
//	open[0] = s;
//	stops[s].g = 0;
//	stops[s].f = stops[s].g + stops[s].h;
//	found = 0;
//
//	while (o_len and not found) {
//		min = DBL_MAX;
//
//		for (i = 0; i < o_len; i++) {
//			if (stops[open[i]].f < min) {
//				current = open[i];
//				min = stops[open[i]].f;
//			}
//		}
//
//		if (current == e) {
//			found = 1;
//
//			++p_len;
//			path = (int*)realloc(path, p_len * sizeof(int));
//			path[p_len - 1] = current;
//			while (stops[current].from >= 0) {
//				current = stops[current].from;
//				++p_len;
//				path = (int*)realloc(path, p_len * sizeof(int));
//				path[p_len - 1] = current;
//			}
//		}
//
//		for (i = 0; i < o_len; i++) {
//			if (open[i] == current) {
//				if (i not_eq (o_len - 1)) {
//					for (j = i; j < (o_len - 1); j++) {
//						open[j] = open[j + 1];
//					}
//				}
//				--o_len;
//				open = (int*)realloc(open, o_len * sizeof(int));
//				break;
//			}
//		}
//
//		++c_len;
//		closed = (int*)realloc(closed, c_len * sizeof(int));
//		closed[c_len - 1] = current;
//
//		for (i = 0; i < stops[current].n_len; i++) {
//			b = 0;
//
//			for (j = 0; j < c_len; j++) {
//				if (routes[stops[current].n[i]].y == closed[j]) {
//					b = 1;
//				}
//			}
//
//			if (b) {
//				continue;
//			}
//
//			tempg = stops[current].g + routes[stops[current].n[i]].d;
//
//			b = 1;
//
//			if (o_len > 0) {
//				for (j = 0; j < o_len; j++) {
//					if (routes[stops[current].n[i]].y == open[j]) {
//						b = 0;
//					}
//				}
//			}
//
//			if (b or (tempg < stops[routes[stops[current].n[i]].y].g)) {
//				stops[routes[stops[current].n[i]].y].from = current;
//				stops[routes[stops[current].n[i]].y].g = tempg;
//				stops[routes[stops[current].n[i]].y].f = stops[routes[stops[current].n[i]].y].g + stops[routes[stops[current].n[i]].y].h;
//
//				if (b) {
//					++o_len;
//					open = (int*)realloc(open, o_len * sizeof(int));
//					open[o_len - 1] = routes[stops[current].n[i]].y;
//				}
//			}
//		}
//	}
//
//	for (i = 0; i < map_size_rows; i++) {
//		for (j = 0; j < map_size_cols; j++) {
//			if (map[i][j]) {
//				putchar(0xdb);
//			}
//			else {
//				b = 0;
//				for (k = 0; k < p_len; k++) {
//					if (ind[i][j] == path[k]) {
//						++b;
//					}
//				}
//				if (b) {
//					putchar('x');
//				}
//				else {
//					putchar('.');
//				}
//			}
//		}
//		putchar('\n');
//	}
//
//	if (not found) {
//		puts("IMPOSSIBLE");
//	}
//	else {
//		printf("path cost is %d:\n", p_len);
//		for (i = p_len - 1; i >= 0; i--) {
//			printf("(%1.0f, %1.0f)\n", stops[path[i]].col, stops[path[i]].row);
//		}
//	}
//
//	for (i = 0; i < s_len; ++i) {
//		free(stops[i].n);
//	}
//	free(stops);
//	free(routes);
//	free(path);
//	free(open);
//	free(closed);
//
//	return 0;
//
//}

/* Simulate an empty explored map */
void generateMap(int startRow, int startCol, int destRow, int destCol) {
	for (int r = ROW - 1; r >= 0; r--) {
		for (int c = 0; c <= COL - 1; c++) {
			if (c == 0 || c == 16 || r == 0 || r == 21) {		//set obstacles here
				mapRobot.cell[r][c].isObstacle = 1;
				mapRobot.cell[r][c].cellType = '1';
			}
			else {
				mapRobot.cell[r][c].isObstacle = 0;
				mapRobot.cell[r][c].cellType = '0';
			}

			if (r == startRow && c == startCol) {
				mapRobot.cell[startRow][startCol].isStart = 1;	//Mark where the robot is
				mapRobot.cell[startRow][startCol].robotAtCell = 1;
				mapRobot.cell[startRow][startCol].cellType = 'X';
			}
			if (r == destRow && c == destCol) {
				mapRobot.cell[destRow][destCol].isDest = 1;
				mapRobot.cell[destRow][destCol].cellType = 'Y';
			}

			printf("%c", mapRobot.cell[r][c].cellType);
		}
		printf("\n");
	}
}

void main(){

	generateMap(2, 2, 19, 14);
	fastestPath(2, 2, 19, 14);
}