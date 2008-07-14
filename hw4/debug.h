#ifndef DEBUG_H
#define DEBUG_H

#include <stdio.h>

#define dprint(expr) printf("**DEBUG: " #expr " = %d\n", expr);
#define dsprint(expr) printf("**DEBUG: " #expr " = %s\n", expr);

#define derror  printf("Internal error in %s: file %s, line %d\n", __func__, __FILE__, __LINE__);

#endif /* DEBUG_H */
