//
// Created by xgao on 11/28/19.
//

#include "../include/protocol.h"
#include <unistd.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>



int proto_recv_packet(int fd, BRS_PACKET_HEADER *hdr, void **payloadp){
    size_t header_len = sizeof(*hdr);
    int bytes_read = read(fd, hdr, header_len);
    if(bytes_read < 0){
        perror("read failed");
        return -1;
    }
    if(bytes_read != header_len){
        return -1;
    }
    size_t payload_length = ntohs(hdr->size);
    if(payload_length == 0){
        *payloadp = NULL;
        return 0;
    }

    *payloadp = malloc(payload_length);
    bytes_read = read(fd, *payloadp, payload_length);
    if(bytes_read < 0){
        perror("read failed");
        return -1;
    }
    if(bytes_read != payload_length){
        return -1;
    }
    return 0;
}

int proto_send_packet(int fd, BRS_PACKET_HEADER *hdr, void *payload){
    size_t header_len = sizeof(*hdr);
    size_t payload_length = ntohs(hdr->size);
    int bytes_written = write(fd, hdr, header_len);
    if(bytes_written < 0){
        perror("write failed");
        return -1;
    }
    if(bytes_written != header_len){
        return -1;
    }

    if(payload_length == 0){
        return 0;
    }

    bytes_written = write(fd, payload, payload_length);
    if(bytes_written < 0){
        perror("write failed");
        return -1;
    }
    if(bytes_written != payload_length){
        return -1;
    }
    return 0;
}
