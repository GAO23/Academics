//
// Created by xgao on 12/6/19.
//

#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include <time.h>
#include <pthread.h>
#include "../include/protocol.h"
#include "../include/trader.h"
#include "../include/trader_exchage_struct.h"

static pthread_mutex_t trader_lock;
static const int NANO_SECONDS = 1000000000;

static TRADER* sentinel;
static void lock(char* msg);
static void unlock(char* msg);

extern EXCHANGE* exchange;

int trader_init(void){
    sentinel = calloc(1, sizeof(TRADER));
    if(sentinel == NULL)
        return -1;
    sentinel->login = -1;
    sentinel->fd = -1;
    return 0;
}


void trader_fini(void){
    lock("trader_fini");
    TRADER* current_trader = sentinel->next;
    while(current_trader != NULL){
        TRADER* temp = current_trader;
        current_trader = current_trader->next;
        free(temp->name);
        free(temp);
    }
    unlock("trader_fini");
}


TRADER *trader_login(int fd, char *name){
    lock("trader_login");
    TRADER* current_trader = sentinel->next;
    while(current_trader != NULL){
        if(strcmp(name, current_trader->name) == 0){
            if(current_trader->login == 1){
                unlock("trader_login");
                return NULL;
            }
            current_trader->login = 1;
            current_trader->fd = fd;
            unlock("trader_login");
            return current_trader;
        }
        current_trader = current_trader->next;
    }

    TRADER* new_trader = calloc(1, sizeof(TRADER));
    if(new_trader == NULL){
        unlock("trader_login");
        return NULL;
    }
    new_trader->previous = sentinel;
    new_trader->next = sentinel->next;
    if(sentinel->next != NULL) sentinel->next->previous = new_trader;
    sentinel->next = new_trader;
    int string_len = strlen(name);
    new_trader->name = malloc(string_len + 1);
    if(new_trader->name == NULL){
       unlock("trader_login");
        return NULL;
    }
    new_trader->name = strncpy(new_trader->name, name, string_len);
    new_trader->fd = fd;
    new_trader->login = 1;
    unlock("trader_login");
    return new_trader;
}

void trader_logout(TRADER *trader){
    lock("trader_logout");
    trader->login = 0;
    unlock("trader_logout");
}

// not gonna be using this
TRADER *trader_ref(TRADER *trader, char *why){
    lock("trader_ref");
    trader->login += 1;
    unlock("trader_ref");
    return trader;
}

// not gonna be using this
void trader_unref(TRADER *trader, char *why){
    lock("trader_unref");
    trader->login -= 1;
    unlock("trader_unref");
}

int trader_send_packet(TRADER *trader, BRS_PACKET_HEADER *pkt, void *data){
   return proto_send_packet(trader->fd, pkt, data);
}

int trader_broadcast_packet(BRS_PACKET_HEADER *pkt, void *data){
    lock("trader_broadcast_packet");
    TRADER* current_trader = sentinel->next;
    while(current_trader != NULL){
        if(current_trader->login == 0) {
            current_trader = current_trader->next;
            continue;
        }
        int result = trader_send_packet(current_trader, pkt, data);
        if(result) {
            unlock("trader_broadcast_packet");
            return -1;
        }
        current_trader = current_trader->next;
    }
    unlock("trader_broadcast_packet");
    return 0;
}

int trader_send_ack(TRADER *trader, BRS_STATUS_INFO *info){
    lock("trader_send_ack");
    BRS_PACKET_HEADER header;
    header.type = BRS_ACK_PKT;
    uint32_t seconds = (unsigned) time(NULL);
    header.timestamp_sec = htonl(seconds);
    header.timestamp_nsec = htonl(seconds * NANO_SECONDS);
    if(info == NULL){
        header.size = htons(0);
        int result = trader_send_packet(trader, &header, NULL); // no payload
        unlock("trader_send_ack");
        return result;
    }else{
        header.size = htons(sizeof(BRS_STATUS_INFO));
        BRS_STATUS_INFO payload;
        payload.inventory = htonl(trader->statusInfo.inventory);
        payload.balance = htonl(trader->statusInfo.balance);
        payload.ask = htonl(trader->statusInfo.ask);
        payload.bid = htonl(trader->statusInfo.bid);
        payload.last = htonl(trader->statusInfo.last);
        payload.orderid = htonl(exchange->trader->statusInfo.orderid);
        payload.quantity = htonl(trader->statusInfo.quantity);
        payload.last = htonl(exchange->trader->statusInfo.last);
        payload.bid = htonl(exchange->trader->statusInfo.bid);
        payload.ask = htonl(exchange->trader->statusInfo.ask);
        int result = trader_send_packet(trader, &header, &payload);
        unlock("trader_send_ack");
        return result;
    }

}

int trader_send_nack(TRADER *trader){
    lock("trader_send_nack");
    BRS_PACKET_HEADER header;
    header.type = BRS_NACK_PKT;
    uint32_t seconds = (unsigned) time(NULL);
    header.timestamp_sec = htonl(seconds);
    header.timestamp_nsec = htonl(seconds * NANO_SECONDS);
    header.size = htons(0);
    int result = trader_send_packet(trader, &header, NULL); // no payload
    unlock("trader_send_nack");
    return result;
}

void trader_increase_balance(TRADER *trader, funds_t amount){
    lock("trader_increase_balance");
    trader->statusInfo.balance += amount;
    unlock("trader_increase_balance");
}

int trader_decrease_balance(TRADER *trader, funds_t amount){
    lock("trader_decrease_balance");
    if(trader->statusInfo.balance < amount){
        unlock("trader_decrease_balance");
        return -1;
    }
    trader->statusInfo.balance -= amount;
    unlock("trader_decrease_balance");
    return 0;
}

void trader_increase_inventory(TRADER *trader, quantity_t quantity){
    lock("trader_increase_inventory");
    trader->statusInfo.inventory += quantity;
    unlock("trader_increase_inventory");
}

int trader_decrease_inventory(TRADER *trader, quantity_t quantity){
    lock("trader_decrease_inventory");
    if(trader->statusInfo.inventory < quantity){
        unlock("trader_decrease_inventory");
        return -1;
    }
    trader->statusInfo.inventory -= quantity;
    unlock("trader_decrease_inventory");
    return 0;
}

static void lock(char* msg){
//    fprintf(stderr, "locking: %s\n", msg);
    pthread_mutex_lock(&trader_lock);
}
static void unlock(char* msg){
//    fprintf(stderr, "unlocking: %s\n", msg);
    pthread_mutex_unlock(&trader_lock);
}
