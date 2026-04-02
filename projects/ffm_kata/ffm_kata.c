// ffm_kata.c
#include <stdio.h>
#include <string.h>

int add_ints(int a, int b) {
    return a + b;
}

double multiply_double(double a, double b) {
    return a * b;
}

typedef struct {
    int x;
    int y;
} Point;

int distance_squared(Point* p) {
    return p->x * p->x + p->y * p->y;
}

void copy_string(const char *src, char *dest, int maxLen) {
    strncpy(dest, src, maxLen);
}

typedef int (*Callback)(int);
int apply_callback(Callback cb, int value) {
    return cb(value);
}

const char* greet() {
    return "Hello from C!";
}
