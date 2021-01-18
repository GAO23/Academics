//
// Created by xgao on 12/7/19.
//

#ifndef HW5_TRADER_EXCHAGE_STRUCT_H
#define HW5_TRADER_EXCHAGE_STRUCT_H
#include "../include/trader.h"
#include "../include/exchange.h"

struct trader {
    struct brs_status_info statusInfo;
    volatile int login;
    int fd;
    char* name; // need to be free
    TRADER* next;
    TRADER* previous;
};

typedef enum exchange_type{
    UNDEFINED,
    TO_SELL,
    TO_BUY
} EXCHANGE_TYPE;

struct exchange {
    EXCHANGE_TYPE exchangeType;
    TRADER* trader;
    BRS_NOTIFY_INFO exchangeInfo;
    EXCHANGE* next;
    EXCHANGE* previous;
};

#endif //HW5_TRADER_EXCHAGE_STRUCT_H
