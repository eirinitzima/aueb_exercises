#include "p3220083-p3220201-p3220219-pizza.h"

// Mutexes and condition variables declaration and initialization
pthread_mutex_t tel_mutex = PTHREAD_MUTEX_INITIALIZER;
pthread_mutex_t cook_mutex = PTHREAD_MUTEX_INITIALIZER;
pthread_mutex_t oven_mutex = PTHREAD_MUTEX_INITIALIZER;
pthread_mutex_t deliverer_mutex = PTHREAD_MUTEX_INITIALIZER;
pthread_mutex_t print_mutex = PTHREAD_MUTEX_INITIALIZER;

pthread_cond_t tel_cond = PTHREAD_COND_INITIALIZER;
pthread_cond_t cook_cond = PTHREAD_COND_INITIALIZER;
pthread_cond_t oven_cond = PTHREAD_COND_INITIALIZER;
pthread_cond_t deliverer_cond = PTHREAD_COND_INITIALIZER;

//Global scope viariables declaration and initialization
int available_tel = Ntel;
int available_cook = Ncook;
int available_oven = Noven;
int available_deliverer = Ndeliverer;
int pizza_prices[3] = {Pm,Pp,Ps}; //[margarita, pepperoni, special]
unsigned int seed;
int total_revenue = 0;
int total_pizza_sales[3] = { 0, 0, 0 };// [margarita, pepperoni, special]
int successful_orders = 0;
int failed_orders = 0;

//Arrays declaration for storing time spans for each customer-The memory for them is allocated at the main function 
struct timespec *order_time;
int *service_time;
int *cooling_time;


void* order_handler(void* arg) {
    
    int order_num = *(int*)arg;
    int rctel;
    int rcscreen;
    int rccook;
    int rcoven = -1;
    int rcdelivery;
   
    struct timespec finished_order_time;
    struct timespec delivered_time;

    //Allocate the size of an order struct and its array field-one for every customer-
    Order *order=(Order*)malloc(sizeof(Order));
    order->pizza_types = (int*)calloc(3, sizeof(int));

    //Only the fisrt customer calls at starting point 0, all the others wait for a random time span   
    if (order_num == 1) {
        clock_gettime(CLOCK_REALTIME, &order_time[order_num - 1]);
    }
    else {
        sleep(rand_r(&seed) % Torderhigh + Torderlow);
        clock_gettime(CLOCK_REALTIME, &order_time[order_num - 1]);
    }


    // Customer order placement
    rctel = pthread_mutex_lock(&tel_mutex);

    if (rctel != 0) {
        printf("ERROR: return code from pthread_mutex_lock() is non-zero\n");
        pthread_exit(&rctel);
    }
    //all the other threads wait for an available telephone
    while (available_tel == 0) {
        rctel = pthread_cond_wait(&tel_cond, &tel_mutex);
    }


    //Unique initialization for each order

    order->id = order_num;// unique order number
    order->num_pizzas = rand_r(&seed) % Norderhigh + Norderlow;// number of pizzas
    
    //initialization of the type of pizzas
    for (int j = 0; j < order->num_pizzas; j++) {
        int pizza_type = rand_r(&seed) % 100;
        if (pizza_type < Pm) {//Margarita
            order->pizza_types[0]++;
        }
        else if (pizza_type < Pm + Pp) {//Peperoni
            order->pizza_types[1]++;
        }
        else {//Special
            order->pizza_types[2]++;
        }
    }

    order->wait_time = order_time[order->id - 1].tv_sec - order_time[0].tv_sec;

    available_tel--;

    //Calculate the payment time and whether the payment was sucessful or not
    pthread_mutex_unlock(&tel_mutex);

    int payment_time = rand_r(&seed) % (Tpaymenthigh + Tpaymentlow);
    sleep(payment_time);
    int payment_success = rand_r(&seed) % 100 +1 > Pfail;

    pthread_mutex_lock(&tel_mutex);

    if (payment_success) {
        //Lock Screen for printing
        rcscreen= pthread_mutex_lock(&print_mutex);
        printf("Order of %d pizzas has successfully been registered! Order ID <%d>\n", order->num_pizzas,order->id);
        rcscreen = pthread_mutex_unlock(&print_mutex);

        //Update the stats
        successful_orders++;
        total_pizza_sales[0] += order->pizza_types[0];
        total_pizza_sales[1] += order->pizza_types[1];
        total_pizza_sales[2] += order->pizza_types[2];
        total_revenue += (order->pizza_types[0] * pizza_prices[0] + order->pizza_types[1] * pizza_prices[1] + order->pizza_types[2] * pizza_prices[2]);
        available_tel++;
    }
    else {
        //Lock Screen for printing
        rcscreen = pthread_mutex_lock(&print_mutex);
        printf("The payment for the order with ID %d could not be completed! The order is canceled\n", order->id);
        rcscreen = pthread_mutex_unlock(&print_mutex);

        //Update the stats
        failed_orders++;
        service_time[order->id] = -1;
        cooling_time[order->id] = -1;
        available_tel++;

        rctel = pthread_cond_signal(&tel_cond);
        rctel = pthread_mutex_unlock(&tel_mutex);
        //Terminate the current thread, no further code will be executed
        pthread_exit(NULL);
    }

    rctel = pthread_cond_signal(&tel_cond);
    rctel = pthread_mutex_unlock(&tel_mutex);
    
    // Order preparation

    rccook= pthread_mutex_lock(&cook_mutex);
    if (rccook != 0) {
        printf("ERROR: return code from pthread_mutex_lock() is non-zero\n");
        pthread_exit(&rccook);
    }
    //all the other threads wait for an available cook
    while (available_cook == 0) {
        pthread_cond_wait(&cook_cond, &cook_mutex);
    }
    available_cook--;

    rccook=pthread_mutex_unlock(&cook_mutex);
    //wait for the preperation time of each pizza, the pizzas of the same order are prepeared simultaneously
    sleep(Tprep * order->num_pizzas);

    rccook=pthread_mutex_lock(&cook_mutex);

    //Waiting for the oven to be available
    while (pthread_mutex_trylock(&oven_mutex) != 0) {}

    rccook = pthread_mutex_unlock(&cook_mutex);

    //Baking Proccess

    //the pizzas are baked simultaneously so we need to have enough ovens to fulfill one order
    while (available_oven < order->num_pizzas) {
        rcoven = pthread_cond_wait(&oven_cond, &oven_mutex);
    }

    available_oven -= order->num_pizzas;

    rccook = pthread_mutex_lock(&cook_mutex);

    available_cook++;

    rccook = pthread_cond_signal(&cook_cond);
    rccook = pthread_mutex_unlock(&cook_mutex);

    rcoven = pthread_mutex_unlock(&oven_mutex);
    //wait for the bake time span only once because the baking proccess of all pizzas is being done at once
    sleep(Tbake);
    clock_gettime(CLOCK_REALTIME, &finished_order_time);
    
    //Lock Screen for printing
    rcscreen = pthread_mutex_lock(&print_mutex);

    printf("Order with ID <%d> got prepared in %ld minutes!\n", order->id, (finished_order_time.tv_sec - order_time[order->id - 1].tv_sec));

    rcscreen = pthread_mutex_unlock(&print_mutex);

    rcoven = pthread_mutex_lock(&oven_mutex);

    available_oven += order->num_pizzas;

    rcoven = pthread_cond_signal(&oven_cond);
    rcoven = pthread_mutex_unlock(&oven_mutex);

    // Delivery 

    rcdelivery = pthread_mutex_lock(&deliverer_mutex);
    if (rcdelivery != 0) {
        printf("ERROR: return code from pthread_mutex_lock() is non - zero\n");
        pthread_exit(&rcdelivery);
    }
    //all the other threads wait for an available deliverer
    while (available_deliverer == 0) {
        rcdelivery = pthread_cond_wait(&deliverer_cond, &deliverer_mutex);
    }

    available_deliverer--;
    rcdelivery = pthread_mutex_unlock(&deliverer_mutex);

    //wait for all the pizzas to be packed 
    sleep(Tpack*order->num_pizzas);

    int deliveryTime = rand_r(&seed) % Tdelhigh + Tdellow;

    sleep(deliveryTime);
    clock_gettime(CLOCK_REALTIME, &delivered_time);


    // Lock-screen for printing information

    rcscreen = pthread_mutex_lock(&print_mutex);

    //Update the stats of current  customer
    service_time[order->id - 1] = delivered_time.tv_sec - order_time[order->id - 1].tv_sec;
    cooling_time[order->id - 1] = delivered_time.tv_sec - finished_order_time.tv_sec;
    printf("Order with ID <%d> arrived to destination in %d minutes!\n", order->id, service_time[order->id - 1]);

    rcscreen = pthread_mutex_unlock(&print_mutex);

    sleep(deliveryTime);

    rcdelivery = pthread_mutex_lock(&deliverer_mutex);

    available_deliverer++;

    rcdelivery = pthread_cond_signal(&deliverer_cond);
    rcdelivery = pthread_mutex_unlock(&deliverer_mutex);

    //free the memory allocated by thr order pointer 
    free(order->pizza_types);
    free(order);
    
    //Terminate the thread, the proccess has been finished
    pthread_exit(NULL);
}



int main(int argc, char* argv[]) {

    //check whether the correct amount of arguments is being passed
    if (argc != 3) {
        printf("The program takes only 2 arguments, the number of customers and the seed!!!");
        exit(- 1);
    }

    int Ncust = atoi(argv[1]);

    //check whether the number of customers is a positive non-zero value
    if (Ncust < 0) {
        printf("The number of customers must be a positive number");
        exit(-1);
    }
    
    seed = atoi(argv[2]);
   
    pthread_t *threads;

    //Arrays memory allocation
    threads = malloc(Ncust * sizeof(pthread_t));

    order_time = malloc(Ncust * sizeof(struct timespec));

    service_time = malloc(Ncust * sizeof(int));

    cooling_time = malloc(Ncust * sizeof(int));
    //if any of the previous arrays is equal to Null there is not enough memory in the system to store the data and the program is terminated
    if ((threads == NULL) || (order_time == NULL) || (service_time == NULL) || (cooling_time == NULL)) {
        printf("NOT ENOUGH MEMORY!\n");
        return -1;
    }

    int rc;
    int countArray[Ncust];
    int threadCount;

    rc=pthread_mutex_lock(&print_mutex);

    printf("This is AUEB's pizza store!\n");

    rc=pthread_mutex_unlock(&print_mutex);

    //Threads targeted at the order_handler function
    for (threadCount = 0; threadCount < Ncust; threadCount++) {

        countArray[threadCount] = threadCount + 1;
        rc = pthread_create(&threads[threadCount], NULL, order_handler, &countArray[threadCount]);

        if (rc != 0) {
            printf("ERROR: return code from pthread_create() is %d\n", rc);
            exit(-1);
        }

    }

    // pthread_join so the MAIN thread waits for all the secondary threads to finish

    for (threadCount = 0; threadCount < Ncust; threadCount++) {
        rc = pthread_join(threads[threadCount], NULL);

        if (rc != 0) {
            printf("ERROR: return code from pthread_join() is %d\n", rc);
            exit(-1);
        }
    }
    
    
    int max_service_time = service_time[0];
    int max_cooling_time = cooling_time[0];

    int service_time_sum = 0;
    int cooling_time_sum = 0;

    for (int i = 0; i < Ncust; i++) {
        if (service_time[i] > max_service_time) {//find the max service time
            max_service_time = service_time[i];
        }

        if (service_time[i] != -1) {//sum all the service times, if cooling time==-1 <==> this order was not successful
            service_time_sum += service_time[i];
        }
        if (cooling_time[i] > max_cooling_time) {//find the max pizza cooling time
            max_cooling_time = cooling_time[i];
        }

        if (cooling_time[i] != -1) {// sum all the cooling times, if cooling time==-1 <==> this order was not successful
            cooling_time_sum += cooling_time[i];
        }

        
    }

    double average_cooling_time = (double)cooling_time_sum / (double)successful_orders;
    double average_service_time = (double)service_time_sum / (double)successful_orders;
   
    
    // Print final statistics
    rc = pthread_mutex_lock(&print_mutex);

    printf("Final Statistics!!\n");
    printf("Total revenue: %d euros\n", total_revenue);
    printf("Total margarita pizzas sold: %d\n", total_pizza_sales[0]);
    printf("Total pepperoni pizzas sold: %d\n", total_pizza_sales[1]);
    printf("Total special pizzas sold: %d\n", total_pizza_sales[2]);
    printf("Total successful orders: %d\n", successful_orders);
    printf("Total failed orders: %d\n", failed_orders);

  
    printf("Average service time: %f minutes\n", average_service_time);
    printf("Max service time: %d minutes\n", max_service_time);
    printf("Average cooling time: %f minutes\n", average_cooling_time);
    printf("Max cooling time: %d minutes\n", max_cooling_time);
    printf("Thank you for choosing AUEB's pizza store!!");

    rc = pthread_mutex_unlock(&print_mutex);


    //Free all the memory allocated by the arrays
    free(threads);
    free(order_time);
    free(service_time);
    free(cooling_time);

    // Destroy mutexes and condition variables
    pthread_mutex_destroy(&tel_mutex);
    pthread_cond_destroy(&tel_cond);

    pthread_mutex_destroy(&cook_mutex);
    pthread_cond_destroy(&cook_cond);

    pthread_mutex_destroy(&oven_mutex);
    pthread_cond_destroy(&oven_cond);

    pthread_mutex_destroy(&deliverer_mutex);
    pthread_cond_destroy(&deliverer_cond);

    pthread_mutex_destroy(&print_mutex);

    return 0;
}