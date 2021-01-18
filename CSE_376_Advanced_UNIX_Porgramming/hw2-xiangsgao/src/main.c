//
// Created by xgao on 3/18/20.
//

#include <stdlib.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <stdio.h>
#include "../include/LKmalloc.h"

char* recordFilePath = "./report";
int reportFd = -1;

void onExitCallBack (int status, void* arg){
    lkreport(reportFd, LKR_SERIOUS | LKR_DOUBLE_FREE | LKR_MATCH | LKR_BAD_FREE | LKR_ORPHAN_FREE);
    printf("Driver test completed, see report file for the logs of records\n");
}

int main(int argc, char** argv){
    // for debugging in gdb
//    AllocatedLists* debuggingList = &lists;
    // set up the report file
    reportFd = creat(recordFilePath, S_IRWXU);

    // registering a hook on function exit
    typedef void (*FunctionPtr) (int, void*);
    FunctionPtr functionPtr = &onExitCallBack;
    on_exit(functionPtr, NULL);

    int* noProtectionPtr = NULL;
    int* protectionPtr = NULL;

    printf("Driver test starting, logging all records\n");

    // testing malloc

    printf("start the driver test cases for lkmalloc without protection\n");
    lkmalloc(sizeof(int), (void**)&noProtectionPtr, LKM_REG);

    printf("start the driver test cases for lkmalloc with double malloc and no protection\n");
    lkmalloc(sizeof(int), (void**)&noProtectionPtr, LKM_REG);

    printf("start the driver test cases for lkmalloc with all protection enable\n");
    lkmalloc(sizeof(int), (void**)&protectionPtr, LKM_INIT | LKM_OVER | LKM_UNDER);

    printf("start the driver test cases for lkmalloc with all protection enable and double malloc\n");
    lkmalloc(sizeof(int), (void**)&protectionPtr, LKM_INIT | LKM_OVER | LKM_UNDER);

    // testing free

    printf("start the driver t"
           "est for regularly freeing ptr without any malloc protection\n");
    lkfree((void**)&noProtectionPtr,  LKF_REG);

    printf("start the driver test for double freeing regularly ptr without any malloc protection\n");
    lkfree((void**)&noProtectionPtr, LKF_REG);

    printf("start the driver test for freeing approx ptr with all malloc protection enable\n");
    lkfree((void**)&protectionPtr, LKF_APPROX | LKF_WARN | LKF_UNKNOWN | LKR_BAD_FREE);

    printf("start the driver test for double freeing approx ptr with all malloc protection enable\n");
    lkfree((void**)&protectionPtr, LKF_APPROX | LKF_WARN | LKF_UNKNOWN | LKR_BAD_FREE);

    // testing free in the middle or guardian zones of the buffer

    printf("start the driver test for freeing approx ptr with all malloc protection enable and in the lower guardian zone\n");
    lkmalloc(sizeof(int), (void**)&protectionPtr, LKM_INIT | LKM_OVER | LKM_UNDER);
    protectionPtr -= 2;
    lkfree((void**)&protectionPtr, LKF_APPROX | LKF_UNKNOWN | LKF_WARN);

    printf("start the driver test for double freeing approx ptr with all malloc protection enable and in the lower guardian zone\n");
    lkfree((void**)&protectionPtr, LKF_APPROX | LKF_UNKNOWN | LKF_WARN);

    printf("start the driver test for freeing approx ptr with all malloc protection enable and in the upper guardian zone\n");
    lkmalloc(sizeof(int), (void**)&protectionPtr, LKM_INIT | LKM_OVER | LKM_UNDER);
    protectionPtr = (int*)(((void*) protectionPtr) + 4 + 8);
    lkfree((void**)&protectionPtr, LKF_APPROX | LKF_UNKNOWN | LKF_WARN);

    printf("start the driver test for double freeing approx ptr with all malloc protection enable and in the upper guardian zone\n");
    lkfree((void**)&protectionPtr, LKF_APPROX | LKF_UNKNOWN | LKF_WARN);

    printf("start the driver test for double freeing approx ptr with all malloc protection enable and in the middle of the buffer zone\n");
    lkmalloc(sizeof(int), (void**)&protectionPtr, LKM_INIT | LKM_OVER | LKM_UNDER);
    protectionPtr = (int*)(((void*) protectionPtr) + 2);
    lkfree((void**)&protectionPtr, LKF_APPROX | LKF_UNKNOWN | LKF_WARN);

    // testing freeing ptr that is not allocated or otherwise bad
    int* ptr = (int*)0x234235;
    printf("start the driver test for freeing none existent ptr\n");
    lkfree((void**)&ptr, LKF_APPROX | LKF_UNKNOWN | LKF_WARN);
    printf("start the driver test for freeing NULL ptr\n");
    ptr = NULL;
    lkfree((void**)&ptr, LKF_APPROX | LKF_UNKNOWN | LKF_WARN);

    return 0;
}
