//
// Created by xgao on 11/30/19.
//
#include <string.h>
#include <stdlib.h>
#include <time.h>
#include <stdio.h>
#include "../include/exchange.h"
#include "../include/server.h"
#include "../include/protocol.h"
#include "../include/trader.h"
#include "../include/trader_exchage_struct.h"


static const int NANO_SECONDS = 1000000000;
static void send_nack_without_trader(int fd, BRS_PACKET_HEADER* hdr);
extern EXCHANGE *exchange;
void *brs_client_service(void *arg){
    int fd = *((int *) arg);
    free(arg);
    creg_register(client_registry, fd);
    BRS_PACKET_HEADER hdr;
    void** payload = malloc(sizeof(*payload));
    *payload = NULL;
    TRADER* trader = NULL;
    BRS_STATUS_INFO* info = NULL; // not gonna be using this, only doing this  because we can't modify header
    while(proto_recv_packet(fd, &hdr, payload) != -1){
        BRS_PACKET_TYPE type = hdr.type;
        switch (type) {
            case BRS_LOGIN_PKT:{
                if(trader != NULL){
                    send_nack_without_trader(fd, &hdr);
                    break;
                }
                int length = ntohs(hdr.size);
                char name[length + 1];
                strncpy(name, *payload, length);
                name[length] = '\0';
                if ((trader = trader_login(fd, (char *) (*payload))) != NULL) {
                    trader_send_ack(trader, NULL);
                    info = &trader->statusInfo;
                } else {
                    send_nack_without_trader(fd, &hdr);
                }
                break;
            }

            case BRS_STATUS_PKT:
                if(trader == NULL) break;
                trader_send_ack(trader, info);
                break;

            case BRS_DEPOSIT_PKT: {
                if (trader == NULL) break;
                BRS_FUNDS_INFO *funds = (BRS_FUNDS_INFO *) (*payload);
                trader_increase_balance(trader, ntohl(funds->amount));
                trader_send_ack(trader, info);
                break;
            }

            case BRS_WITHDRAW_PKT:{
                if(trader == NULL) break;
                BRS_FUNDS_INFO *funds = (BRS_FUNDS_INFO *) (*payload);
                if(trader_decrease_balance(trader, ntohl(funds->amount)) < 0){
                    trader_send_nack(trader);
                }else{
                    trader_send_ack(trader, info);
                }
                break;
            }


            case BRS_ESCROW_PKT: {
                if (trader == NULL) break;
                BRS_ESCROW_INFO* escrowInfo = (BRS_ESCROW_INFO*)(*payload);
                trader_increase_inventory(trader, ntohl(escrowInfo->quantity));
                trader_send_ack(trader, info);
                break;
            }

            case BRS_RELEASE_PKT: {
                if (trader == NULL) break;
                BRS_ESCROW_INFO* escrowInfo = (BRS_ESCROW_INFO*)(*payload);
                if (trader_decrease_inventory(trader, ntohl(escrowInfo->quantity)) < 0) {
                    trader_send_nack(trader);
                } else {
                    trader_send_ack(trader, info);
                }
                break;
            }

            case BRS_BUY_PKT:{
                if(trader == NULL) break;
                BRS_ORDER_INFO* brsOrderInfo = (BRS_ORDER_INFO*)(*payload);
                quantity_t quantity =  ntohl(brsOrderInfo->quantity);
                funds_t funds = ntohl(brsOrderInfo->price);
                if(exchange_post_buy(exchange, trader, quantity, funds) == 0){
                    trader_send_nack(trader);
                }else{
                    trader_send_ack(trader, info);
                }
                break;
            }


            case BRS_SELL_PKT :{
                if(trader == NULL) break;
                BRS_ORDER_INFO* brsOrderInfo = (BRS_ORDER_INFO*)(*payload);
                quantity_t quantity =  ntohl(brsOrderInfo->quantity);
                funds_t funds = ntohl(brsOrderInfo->price);
                if(exchange_post_sell(exchange, trader, quantity, funds) == 0){
                    trader_send_nack(trader);
                }else{
                    trader_send_ack(trader, info);
                }
                break;
            }

            case BRS_CANCEL_PKT:{
                if(trader == NULL) break;
                BRS_CANCEL_INFO* brsCancelInfo = (BRS_CANCEL_INFO*)(*payload);
                orderid_t orderid =  ntohl(brsCancelInfo->order);
                if(exchange_cancel(exchange, trader, orderid, NULL) == 0){ // not using quantity
                    trader_send_nack(trader);
                }else{
                    trader_send_ack(trader, info);
                }
                break;
            }

            default:
                fprintf(stderr, "%s\n", "unrecognized packet");
                break;
        }

        memset(&hdr, 0, sizeof(BRS_PACKET_HEADER));
        if (*payload != NULL) {
            free(*payload);
            *payload = NULL;
        }
        exchange->trader->statusInfo.orderid = 0;
    }
    creg_unregister(client_registry, fd);
    if(trader != NULL) trader_logout(trader);
    return NULL;
}

static void send_nack_without_trader(int fd, BRS_PACKET_HEADER* hdr){
    hdr->type = BRS_NACK_PKT;
    hdr->size = htons(0);
    uint32_t seconds = (unsigned) time(NULL);
    hdr->timestamp_sec = htonl(seconds);
    hdr->timestamp_nsec = htonl(seconds * NANO_SECONDS);
    proto_send_packet(fd, hdr, NULL); // no payload
}

