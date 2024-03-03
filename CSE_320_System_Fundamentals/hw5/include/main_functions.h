//
// Created by xgao on 11/28/19.
//

#ifndef HW5_MAIN_FUNCTIONS_H
#define HW5_MAIN_FUNCTIONS_H

#define USAGE_PROMPT "Usage: ./demo_server -p <port>\n"
extern int lis_sock_fd;

void sigup_handler(int signal);

typedef struct sockaddr_in Sock_Address;

void* client_thread_function(void* client_sockfd);

#endif //HW5_MAIN_FUNCTIONS_H
