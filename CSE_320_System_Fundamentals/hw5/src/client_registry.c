//
// Created by xgao on 11/29/19.
//

#include <pthread.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/socket.h>
#include <stdio.h>
#include "../include/client_registry.h"

static pthread_mutex_t registry_lock;
static volatile int registry_num = 0;
static pthread_mutex_t empty_lock;
static void lock(char* msg);
static void unlock(char* msg);
static void shut_lock();
static void shut_unlock();

struct client_registry{
    int fd;
    CLIENT_REGISTRY* next;
    CLIENT_REGISTRY* previous;
};

CLIENT_REGISTRY *client_registry;

CLIENT_REGISTRY* creg_init(){
    client_registry = calloc(1, sizeof(*client_registry));
    client_registry->fd = -1;
    return client_registry;
}

void creg_fini(CLIENT_REGISTRY *cr){
    // kinda redundant since the only registry left to free after all threads are finished will be the root registry in the linked list.
    lock("creg_fini");
    CLIENT_REGISTRY* current_registry = cr;
    while(current_registry != NULL){
        CLIENT_REGISTRY* temp = current_registry;
        current_registry = current_registry->next;
        free(temp);
    }
    unlock("creg_fini");
}

int creg_register(CLIENT_REGISTRY *cr, int fd){
    lock("creg_register");
    CLIENT_REGISTRY* new_registry = calloc(1, sizeof(*client_registry));
    new_registry->previous = cr;
    new_registry->next = cr->next;
    if(cr->next != NULL) client_registry->next->previous = new_registry;
    cr->next = new_registry;
    new_registry->fd = fd;
    registry_num += 1;
    unlock("creg_register");
    return 0;
}

int creg_unregister(CLIENT_REGISTRY *cr, int fd){
    lock("creg_unregister");
    cr = cr->next;
    while(cr != NULL){
        if(cr->fd == fd) break;
        cr = cr->next;
    }

    if(cr->previous != NULL){
        cr->previous->next = cr->next;
    }
    if(cr->next != NULL){
        cr->next->previous = cr->previous;
    }
    close(cr->fd);
    free(cr);
    registry_num -= 1;
    if(registry_num == 0) shut_unlock();
    unlock("creg_unregister");
    return 0;
}

void creg_wait_for_empty(CLIENT_REGISTRY *cr){
    shut_lock();
    shut_unlock();
}

void creg_shutdown_all(CLIENT_REGISTRY *cr){
    sleep(1); // ensure that the last register gets call first
    lock("creg_shutdown_all");
    CLIENT_REGISTRY* current_registry = client_registry->next;
    if(registry_num != 0) shut_lock();
    while(current_registry != NULL){
        shutdown(current_registry->fd, SHUT_RD);
        current_registry = current_registry->next;
    }
    unlock("creg_shutdown_all");
}

static void lock(char* msg){
//    fprintf(stderr, "locking: %s\n", msg);
    pthread_mutex_lock(&registry_lock);
}
static void unlock(char* msg){
//    fprintf(stderr, "unlocking: %s\n", msg);
    pthread_mutex_unlock(&registry_lock);
}

static void shut_lock(){
//    fprintf(stderr, "locking: %s\n", "empty");
    pthread_mutex_lock(&empty_lock);
}

static void shut_unlock(){
//    fprintf(stderr, "unlocking: %s\n", "empty");
    pthread_mutex_unlock(&empty_lock);
}
