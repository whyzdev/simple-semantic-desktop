#ifndef HW03_H
#define HW03_H

/* Useful constants */
#define LINESIZE 100

#define GRAVITY 9.81

// this is the X value
#define PLATEAU_WIDTH   100.0
// this is the Y value
#define PLATEAU_HEIGHT  100.0
// this is the square size
#define METERS_PER_SQUARE 5.0


#define BOOMER_MOVE_SPEED   2.5     /* meters per turn   */
#define BOOMER_MAX_SHOT     40.0    /* meters per second */
#define BOOMER_BLAST        20.0    /* radius in meters  */

#define ZOOMER_MOVE_SPEED   5.0     /* meters per turn   */
#define ZOOMER_MAX_SHOT     15.0    /* meters per second */
#define ZOOMER_BLAST        1.0     /* radius in meters  */

#define DENNIS_MOVE_SPEED   1.0     /* meters per turn   */
#define DENNIS_MAX_SHOT     30.0    /* meters per second */
#define DENNIS_BLAST        5.0     /* radius in meters  */

#define CHOMPER_MOVE_SPEED  2.5     /* meters per turn   */

#ifndef TRUE
#define TRUE 1
#endif   
#ifndef FALSE
#define FALSE 0
#endif

/* starting information */

#define BOOMER_BEARING      45.0
#define BOOMER_X            10.0
#define BOOMER_Y            10.0
#define BOOMER_ALIVE        TRUE

#define ZOOMER_BEARING      300.0
#define ZOOMER_X            90.0
#define ZOOMER_Y            10.0
#define ZOOMER_ALIVE        TRUE

#define DENNIS_BEARING      120.0
#define DENNIS_X            10.0
#define DENNIS_Y            90.0
#define DENNIS_ALIVE        TRUE

#define CHOMPER1_BEARING    0.0
#define CHOMPER1_X          30.0
#define CHOMPER1_Y          30.0
#define CHOMPER1_ALIVE      TRUE

#define CHOMPER2_BEARING    0.0
#define CHOMPER2_X          80.0
#define CHOMPER2_Y          30.0
#define CHOMPER2_ALIVE      TRUE

#define CHOMPER3_BEARING    0.0
#define CHOMPER3_X          30.0
#define CHOMPER3_Y          80.0
#define CHOMPER3_ALIVE      TRUE


#endif

