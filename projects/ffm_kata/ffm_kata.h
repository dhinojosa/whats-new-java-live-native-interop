#ifndef FFM_KATA_H
#define FFM_KATA_H

typedef struct {
    int x;
    int y;
} Point;

int add_ints(int a, int b);
double multiply_double(double a, double b);
int distance_squared(Point* p);
void copy_string(const char* src, char* dest, int length);
const char* greet();
int apply_callback(int (*callback)(int), int value);

#endif
