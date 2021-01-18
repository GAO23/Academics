#include <stdlib.h>
#include <getopt.h>
#include <signal.h>
#include <string.h>
#include <pthread.h>
#include <unistd.h>

#include "../include/client_registry.h"
#include "../include/exchange.h"
#include "../include/trader.h"
#include "../include/debug.h"
#include "../include/main_functions.h"



extern EXCHANGE *exchange;
extern CLIENT_REGISTRY *client_registry;

static void terminate(int status);


/*
 * "Bourse" exchange server.
 *
 * Usage: bourse <port>
 */
int main(int argc, char* argv[]){
    // Option processing should be performed here.
    // Option '-p <port>' is required in order to specify the port number
    // on which the server should listen.

    char option;
    int port_num = -1;
    while((option = getopt(argc, argv, "p:")) != -1){ // : after a letter means this option requires an additional argument, port # in this case
        switch (option){
            case 'p':
                port_num = strtol(optarg, NULL, 10);
                break;
            default:
                fprintf(stderr, "%s", USAGE_PROMPT);
                exit(EXIT_FAILURE);
        }
    }

    if(port_num == -1) {
        fprintf(stderr, "%s", USAGE_PROMPT);
        exit(EXIT_FAILURE);
    }
    // Perform required initializations of the client_registry,
    // maze, and player modules.
    client_registry = creg_init();
    exchange = exchange_init();
    trader_init();

    // TODO: Set up the server socket and enter a loop to accept connections
    // on this socket.  For each connection, a thread should be started to
    // run function brs_client_service().  In addition, you should install
    // a SIGHUP handler, so that receipt of SIGHUP will perform a clean
    // shutdown of the server.

    struct sigaction action;
    memset(&action, 0, sizeof(struct sigaction));
    action.sa_handler = sigup_handler; // just need to set this simple field
    if(sigaction(SIGHUP, &action, NULL) == -1){
        perror("sigaction failed, must be a conspiracy");
        exit(EXIT_FAILURE);
    }

    Sock_Address server_addr;
    if((lis_sock_fd = socket(AF_INET, SOCK_STREAM, 0)) < 0){
        perror("socket error, must be the space aliens");
        exit(EXIT_FAILURE);
    }
    memset(&server_addr, 0, sizeof(server_addr));
    server_addr.sin_family = AF_INET;
    server_addr.sin_port = htons(port_num);
    server_addr.sin_addr.s_addr = INADDR_ANY;
    if(bind(lis_sock_fd, (struct sockaddr*) &server_addr, sizeof(server_addr)) < 0){
        perror("bind error");
        exit(EXIT_FAILURE);
    }

    if(listen(lis_sock_fd, 10) < 0){
        perror("Listening error, must be Godzilla");
        exit(EXIT_FAILURE);
    }
    debug("listening on port 3000");
    pthread_t tid;
    Sock_Address client_addr;
    socklen_t size = sizeof(client_addr);
    while(1) {
        int *client_sockfd = calloc(1, sizeof(*client_sockfd));
        *client_sockfd = accept(lis_sock_fd, (struct sockaddr *) &client_addr, &size);
        if (*client_sockfd < 0) {
            perror("accept failed, must be the vampire ninjas");
            exit(EXIT_FAILURE);
        }
        // spung off the server to a thread
        pthread_create(&tid, NULL, client_thread_function, client_sockfd);
    }
}

/*
 * Function called to cleanly shut down the server.
 */
static void terminate(int status) {
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

    debug("Bourse server terminating");
    close(lis_sock_fd);
    exit(status);
}

