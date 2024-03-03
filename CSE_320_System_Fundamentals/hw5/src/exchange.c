//
// Created by xgao on 12/7/19.
//

#include "../include/exchange.h"
#include "../include/trader_exchage_struct.h"
#include "../include/protocol.h"
#include <stdlib.h>
#include <pthread.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>

EXCHANGE* exchange;
pthread_mutex_t exchange_mutex;

static const int NANO_SECONDS = 1000000000;
static int orderID = 1;
static volatile int die = 0;
static pthread_t tid;

static void lock(char* msg);
static void unlock(char* msg);
static void construct_brs_packet(BRS_PACKET_HEADER* header, BRS_PACKET_TYPE type, uint16_t size);
static void remove_from_link_list(EXCHANGE* para);
static void insert_into_link_list(EXCHANGE* para);
static void make_transaction();
static void* thread_fun(void* arg);



static void construct_brs_packet(BRS_PACKET_HEADER* header, BRS_PACKET_TYPE type, uint16_t size){
    header->type = type;
    header->size = htons(size);
    uint32_t seconds = (unsigned) time(NULL);
    header->timestamp_sec = htonl(seconds);
    header->timestamp_nsec = htonl(seconds * NANO_SECONDS);
}

EXCHANGE *exchange_init(){
    exchange = calloc(1, sizeof(EXCHANGE));
    exchange->trader = calloc(1, sizeof(struct trader));
    pthread_create(&tid, NULL, thread_fun, NULL);
    return exchange;
}


void exchange_fini(EXCHANGE *xchg){
    die = 1;
    lock("exchange_fini");
    xchg = xchg->next;
    while(xchg != NULL){
        EXCHANGE* temp = xchg;
        free(temp);
        xchg = xchg->next;
    }
    free(exchange->trader);
    free(exchange);
    unlock("exchange_fini");
}

// not gonna be using this
void exchange_get_status(EXCHANGE *xchg, BRS_STATUS_INFO *infop){
    lock("exchange_fini");
    memcpy(infop, &exchange->exchangeInfo, sizeof(BRS_STATUS_INFO));
    unlock("exchange_fini");
}

int exchange_cancel(EXCHANGE *xchg, TRADER *trader, orderid_t order, quantity_t *quantity){
    lock("exchange_cancel");
    xchg = xchg->next;
    while(xchg != NULL){
        if(xchg->exchangeInfo.buyer == order || xchg->exchangeInfo.seller == order) break;
        xchg = xchg->next;
    }
    if(xchg == NULL) {
        unlock("exchange_cancel");
        return -1;
    }

    if(xchg->trader != trader){
        unlock("exchange_cancel");
    }

    BRS_PACKET_HEADER header;
    construct_brs_packet(&header, BRS_CANCELED_PKT, sizeof(BRS_ORDER_INFO));
    BRS_NOTIFY_INFO info;
    info.quantity = htonl(xchg->exchangeInfo.quantity);
    info.price = htonl(xchg->exchangeInfo.price);
    info.buyer = htonl(xchg->exchangeInfo.buyer);
    info.seller = htonl(xchg->exchangeInfo.seller);
    trader_broadcast_packet(&header, &info);
    remove_from_link_list(xchg);
    free(xchg);
    unlock("exchange_cancel");
    return 0;
}

orderid_t exchange_post_sell(EXCHANGE *xchg, TRADER *trader, quantity_t quantity, funds_t price){
    if(trader->statusInfo.inventory < quantity)
        return 0;

    lock("exchange_post_sell");
    EXCHANGE* new_exchange = calloc(1, sizeof(*new_exchange));
    new_exchange->exchangeType = TO_SELL;
    new_exchange->trader = trader;
    new_exchange->exchangeInfo.seller = orderID;
    new_exchange->exchangeInfo.quantity = quantity;
    new_exchange->exchangeInfo.price = price;
    insert_into_link_list(new_exchange);
    BRS_PACKET_HEADER header;
    construct_brs_packet(&header, BRS_POSTED_PKT, sizeof(BRS_NOTIFY_INFO));
    BRS_NOTIFY_INFO info;
    info.buyer = 0;
    info.seller = htonl(new_exchange->exchangeInfo.seller);
    info.price = htonl(new_exchange->exchangeInfo.price);
    info.quantity = htonl(new_exchange->exchangeInfo.quantity);
    trader_decrease_inventory(trader, quantity);
    trader_broadcast_packet(&header, &info);
    if((price < exchange->trader->statusInfo.ask) || (price == 0)){
        exchange->trader->statusInfo.ask = price;
    }
    exchange->trader->statusInfo.orderid = orderID;
    orderID += 1;
//    make_transaction();
    unlock("exchange_post_sell");
    return new_exchange->exchangeInfo.seller;
}

orderid_t exchange_post_buy(EXCHANGE *xchg, TRADER *trader, quantity_t quantity, funds_t price){
    if((quantity * price) > trader->statusInfo.balance)
        return 0;
    lock("exchange_post_buy");
    EXCHANGE* new_exchange = calloc(1, sizeof(*new_exchange));
    new_exchange->exchangeType = TO_BUY;
    new_exchange->trader = trader;
    new_exchange->exchangeInfo.buyer = orderID;
    new_exchange->exchangeInfo.price = price;
    new_exchange->exchangeInfo.quantity = quantity;
    insert_into_link_list(new_exchange);
    BRS_PACKET_HEADER header;
    construct_brs_packet(&header, BRS_POSTED_PKT, sizeof(BRS_NOTIFY_INFO));
    BRS_NOTIFY_INFO info;
    info.seller = 0;
    info.buyer = htonl(new_exchange->exchangeInfo.buyer);
    info.price = htonl(new_exchange->exchangeInfo.price);
    info.quantity = htonl(new_exchange->exchangeInfo.quantity);
    trader_decrease_balance(trader, quantity*price);
    trader_broadcast_packet(&header, &info);
    if(price > exchange->trader->statusInfo.bid){
        exchange->trader->statusInfo.bid = price;
    }
    exchange->trader->statusInfo.orderid = orderID;
    orderID += 1;
    unlock("exchange_post_buy");
//    make_transaction();
    return new_exchange->exchangeInfo.buyer;
}

static void remove_from_link_list(EXCHANGE* para){
    if(para->next != NULL) para->next->previous = para->previous;
    para->previous->next = para->next;
    free(para);
}

static void insert_into_link_list(EXCHANGE* para){
    para->next = exchange->next;
    para->previous = exchange;
    if(exchange->next != NULL) exchange->next->previous = para;
    exchange->next = para;
}

static void lock(char* msg){
//    fprintf(stderr, "locking: %s\n", msg);
    pthread_mutex_lock(&exchange_mutex);
}
static void unlock(char* msg){
//    fprintf(stderr, "unlocking: %s\n", msg);
    pthread_mutex_unlock(&exchange_mutex);
}

static void make_transaction(){
    pthread_mutex_lock(&exchange_mutex);
    for(EXCHANGE* sell_exchange = exchange->next; sell_exchange != NULL; sell_exchange = sell_exchange->next){
        if((sell_exchange->previous != exchange) && (sell_exchange->previous->exchangeInfo.quantity == 0)){
            remove_from_link_list(sell_exchange->previous);
        }
        if(sell_exchange->exchangeInfo.quantity == 0) continue;
        if(sell_exchange->exchangeType != TO_SELL) continue;
        EXCHANGE* highest_buy = NULL;
        for(EXCHANGE* buy_exchange = exchange->next; buy_exchange != NULL; buy_exchange = buy_exchange->next){
            if(buy_exchange->exchangeType != TO_BUY) continue;
            if(buy_exchange->exchangeInfo.quantity == 0) continue;
            if(highest_buy == NULL && (buy_exchange->exchangeInfo.price >= sell_exchange->exchangeInfo.price)){
                highest_buy = buy_exchange;
            }else if(highest_buy != NULL){
                if(buy_exchange->exchangeInfo.price >= highest_buy->exchangeInfo.price){
                    highest_buy = buy_exchange;
                }
            }
        }

        if(highest_buy != NULL){
            quantity_t actual_quantity = (sell_exchange->exchangeInfo.quantity >= highest_buy->exchangeInfo.quantity) ?
                    highest_buy->exchangeInfo.quantity : highest_buy->exchangeInfo.quantity - sell_exchange->exchangeInfo.quantity;

            // increases buyer inventory, gives seller money
            trader_increase_inventory(highest_buy->trader, actual_quantity);
            trader_increase_balance(sell_exchange->trader, (actual_quantity * highest_buy->exchangeInfo.price));
            highest_buy->exchangeInfo.quantity -= actual_quantity;
            sell_exchange->exchangeInfo.quantity -= actual_quantity;
            BRS_PACKET_HEADER header;
            BRS_NOTIFY_INFO info;
            info.quantity = htonl(actual_quantity);
            info.price = htonl(highest_buy->exchangeInfo.price);
            info.seller = htonl(sell_exchange->exchangeInfo.seller);
            info.buyer = htonl(highest_buy->exchangeInfo.buyer);
            construct_brs_packet(&header, BRS_BOUGHT_PKT, sizeof(BRS_NOTIFY_INFO));
            trader_send_packet(highest_buy->trader, &header, &info);
            construct_brs_packet(&header, BRS_SOLD_PKT, sizeof(BRS_NOTIFY_INFO));
            trader_send_packet(sell_exchange->trader, &header, &info);

            // broadcast trade to all
            BRS_STATUS_INFO* current_exchange_info = &exchange->trader->statusInfo;
            current_exchange_info->last = highest_buy->exchangeInfo.price; // set the last price
            construct_brs_packet(&header, BRS_TRADED_PKT, sizeof(BRS_NOTIFY_INFO));
            trader_broadcast_packet(&header, &info);
        }
    }
    pthread_mutex_unlock(&exchange_mutex);
}

static void* thread_fun(void* arg){
    pthread_detach(pthread_self());
    while(!die){
        sleep(1);
        make_transaction();
    }
    return NULL;
}