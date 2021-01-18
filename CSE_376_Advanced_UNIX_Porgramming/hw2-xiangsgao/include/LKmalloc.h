//
// Created by xgao on 3/18/20.
//

#ifndef HW2_LKMALLOC_H
#define HW2_LKMALLOC_H

#define LKM_REG 0x0
#define LKM_INIT 0x1
#define LKM_OVER 0x2
#define LKM_UNDER 0x4

#define LKF_REG 0x0
#define LKF_APPROX 0x1
#define LKF_WARN 0x2
#define LKF_UNKNOWN 0x4
#define LKF_ERROR 0x8

#define LKR_NONE 0x0
#define LKR_SERIOUS 0x1
#define LKR_MATCH 0x2
#define LKR_BAD_FREE 0x4
#define LKR_ORPHAN_FREE 0x8
#define LKR_DOUBLE_FREE 0x10
#define MALLOC_RECORD 0x0
#define FREE_RECORD 0x1

#define NOT_FOUND -1
#define DOUBLE_FREE -2
#define SUCCESS 0

typedef unsigned int u_int;

typedef struct {
    unsigned int init;
    unsigned int over;
    unsigned int under;
} Variables;

typedef struct{
    void* upperPtr;
    void* lowerPtr;
    void* ptr;
    Variables* flags;
    u_int size;
    unsigned int free;
} Allocated;

typedef struct AllocatedLists{
    Allocated* element;
    struct AllocatedLists* next;
    struct AllocatedLists* previous;
} AllocatedLists;

typedef struct{
    int record_type;
    char filename[32];
    char fxName[64];
    int line_num;
    char timestamp[128];
    void* ptr_passed;
    char retVal[32];
    u_int size_or_flags;
    void* alloc_addr_returned;
    u_int record_mode;
} Record;

extern AllocatedLists lists;
extern Record* report;
extern int record_num;

int lkmalloc(u_int size, void **ptr, u_int flags);
int lkfree(void **ptr, u_int flags);
int lkreport(int fd, u_int flags);


#endif //HW2_LKMALLOC_H
