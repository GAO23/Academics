//
// Created by xgao on 4/6/20.
//

#include <stdlib.h>
#include <string.h>
#include <stdio.h>

typedef unsigned char byte;
#define true 1

void* get_key(char *random_string){
    void *retVal;
    size_t random_string_len;
    uint count;

    retVal = calloc(0x100,1);
    random_string_len = strlen(random_string);
    count = 0;
    while (count < random_string_len) {
        *(byte *)(count + (int)retVal) = random_string[count] ^ 0x1c;
        count = count + 1;
    }
    return retVal;
}

void print_wrong_number(void){
    puts("Wrong number");
    exit(1);
}

int FUN_00011251(void)

{
    size_t sVar1;
    int iVar2;
    int in_ECX;
    char local_66;
    char local_65;
    char local_64;
    int local_63;
    char local_62 [64];
    char local_22 [10];
    int local_18;
    int local_14;
    uint local_10;
    int local_c;

    local_c = in_ECX;
    printf("Enter the right number: ");
    local_18 = scanf("%27s",local_62);
    if (local_18 != 1) {
        print_wrong_number();
    }
    if (local_62[0] != '0') {
        print_wrong_number();
    }
    if (local_62[1] != '6') {
        print_wrong_number();
    }
    fflush(stdin);
    memset(local_22,0,10);
    local_63 = 0;
    local_10 = 0;
    local_14 = 0;
    while( true ) {
        sVar1 = strlen(local_22);
        if (8 < sVar1) break;
        sVar1 = strlen(local_62);
        if (sVar1 <= local_10) break;
        local_66 = local_62[local_10];
        local_65 = local_62[local_10 + 1];
        local_64 = local_62[local_10 + 2];
        iVar2 = atoi(&local_66);
        local_22[local_14] = (char)iVar2;
        local_22[local_14] = local_22[local_14] + '\x03';
        local_10 = local_10 + 3;
        local_14 = local_14 + 1;
    }
    local_22[local_14] = '\0';
    iVar2 = strcmp(local_22,"CSE363ESC");
    if (iVar2 == 0) {
        printf("Your number corresponds to %s, well done!\n",local_22);
    }
    else {
        print_wrong_number();
    }
    return 0;
}


int main(int argc,char **argv){
    void* key = key = get_key("\x7foy/*/szzoy\x7f.,.,");
    printf("passwd for key is %s\n", key);

    FUN_00011251();
}