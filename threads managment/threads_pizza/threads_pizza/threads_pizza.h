#include <pthread.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <time.h>

//Constants
#define Ntel 2
#define Ncook 2
#define Noven 10
#define Ndeliverer 10

#define Torderlow 1
#define Torderhigh 5
#define Norderlow 1
#define Norderhigh 5

#define Pm 35
#define Pp 25
#define Ps 40

#define Tpaymentlow 1
#define Tpaymenthigh 3
#define Pfail 5

#define Cm 10
#define Cp 11
#define Cs 12

#define Tprep 1
#define Tbake 10
#define Tpack 1
#define Tdellow 5
#define Tdelhigh 15

//Struct's use is to simulate an Order placed by a customer
typedef struct Order {
    int id;
    int num_pizzas;
    int* pizza_types;
    int wait_time;
} Order;