
#include <stdlib.h>
#include <pthread.h>
#include <unistd.h>

#include "../include/client_registry.h"
#include "../include/exchange.h"
#include "../include/trader.h"
#include "../include/debug.h"
#include "../include/main_functions.h"
#include "../include/server.h"

extern EXCHANGE *exchange;
extern CLIENT_REGISTRY *client_registry;
int lis_sock_fd;

void sigup_handler(int signal){
    // Shutdown all client connections.
    // This will trigger the eventual termination of service threads.
    creg_shutdown_all(client_registry);

    debug("Waiting for service threads to terminate...");
    creg_wait_for_empty(client_registry);
    debug("All service threads terminated.");

    // Finalize modules.
    creg_fini(client_registry);
    exchange_fini(exchange);
    trader_fini();
    close(lis_sock_fd);
    debug("Bourse server terminating");
    exit(EXIT_SUCCESS);
}

void* client_thread_function(void* client_sockfd){
    pthread_detach(pthread_self());
    brs_client_service(client_sockfd);
    return NULL;
}