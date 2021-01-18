//
// Created by xgao on 3/18/20.
//

#include "../include/LKmalloc.h"
#include <stdlib.h>
#include <errno.h>
#include <string.h>
#include <stdio.h>
#include <time.h>

// using a doubly linked list because easy and the search time is a linear O(n) which is good enough for me.
AllocatedLists lists;

// report for storing the messages for logging out
Record* report;
// keeping track of static variables for report
int record_num = 0;
static int max_record = 100;




// prototypes
static int removeAllocatedApprox(void* ptr);
static char* getCurrentTime();
static void logRecords(Record* record);
static Variables* initNewFlag(u_int flag);
static void* getAllocated(void* ptr);
static AllocatedLists* insertNewAllocated(void* ptr, void* underPtr, void* overPtr, Variables* flag, u_int size);
static int removeAllocatedRegular(void* ptr);
static int removeAllocatedApprox(void* ptr);
static AllocatedLists* getRecycleAllocated(void* ptr);


/* return current time
 * This function is taken from a code snippet from
 * https://stackoverflow.com/questions/5141960/get-the-current-time-in-c
 * */
static char* getCurrentTime(){
    time_t rawTime;
    struct tm* timeInfo;
    time ( &rawTime );
    timeInfo = localtime ( &rawTime );
    return asctime (timeInfo);
}

// creating a new record
static void createRecord(
        uint record_type, char* filename, const char* fxname,
        int line_num, void* ptr_passed,char* retval,
        u_int size_or_flags, void* alloc_addr_returned, u_int record_mod){
            Record newRecord;
            newRecord.record_type = record_type;
            strcpy(newRecord.filename, filename);
            strcpy(newRecord.fxName, fxname);
            newRecord.line_num = line_num;
            strcpy(newRecord.timestamp, getCurrentTime());
            newRecord.ptr_passed = ptr_passed;
            strcpy(newRecord.retVal, retval);
            newRecord.size_or_flags = size_or_flags;
            newRecord.alloc_addr_returned = alloc_addr_returned;
            newRecord.record_mode = record_mod;
    logRecords(&newRecord);

        }

// print the logs, mode is only print if this flag exists
static void logRecords(Record* record){
    if(report == NULL)
        report = calloc(sizeof(Record), 100);

    // copy the content into the array
    // memcpy(&record[record_num], record, sizeof(*record));
    // better method
    report[record_num] = *record;


    // increment record num by one each time logged
    record_num += 1;

    // if storage on heap is not large enough
    if(record_num > max_record){
        // increment the storage size by another 100
        report = realloc(report, sizeof(Record) * (max_record + 100));
        max_record += 100;
    }
}



// allocate a new flag for memory object
static Variables* initNewFlag(u_int flag){
    Variables* retVal = calloc(1, sizeof(Variables));
    retVal->init = flag & LKM_INIT;
    retVal->over = flag & LKM_OVER;
    retVal->under = flag & LKM_UNDER;
    return retVal;
}

// find an allocated object from the linked list
static void* getAllocated(void* ptr){
    // if the ptr is NULL then it won't be in the doubly linked list
    if(ptr == NULL)
        return NULL;

    AllocatedLists* temList = &lists;
    Allocated* requestedAllocated = NULL;
    while(1){
        Allocated* element = temList->element;

        if(element->free == 1)
            goto SKIP;


        if(element->ptr == ptr)
            requestedAllocated = element;

        SKIP:
        if(temList->next == NULL)
            break;

        temList = temList->next;
    }

    return requestedAllocated;
}

// recyclyling allocated that are free if ptr matches
static AllocatedLists* getRecycleAllocated(void* ptr){
    // if the ptr is NULL then it won't be in the doubly linked list
    if(ptr == NULL)
        return NULL;

    AllocatedLists* temList = &lists;
    Allocated* requestedAllocated = NULL;

    // this means that the list is empty
    if(temList->element == NULL){
        return NULL;
    }

    while(1){
        Allocated* element = temList->element;

        if(element->ptr == ptr &&  element->free == 1){
            requestedAllocated = element;
            break;
        }

        void* startAddr = (element->lowerPtr) ? element->lowerPtr : element->ptr;
        void* endAddr = (element->upperPtr) ? element->upperPtr + 8 : element->ptr + element->size;

        if(element->free == 1 && ptr >= startAddr && ptr <= endAddr){
            requestedAllocated = element;
            break;
        }


        if(temList->next == NULL)
            break;

        temList = temList->next;
    }

    if(requestedAllocated != NULL){
        return temList;
    }else{
        return NULL;
    }
}

// allocate new memory object and insert it into a newly allocated node
static AllocatedLists* insertNewAllocated(void* ptr, void* underPtr, void* overPtr, Variables* flag, u_int size){
    Allocated* newAllocated;
    if(lists.element == NULL){
        newAllocated = calloc(sizeof(Allocated), 1);
        newAllocated->flags = flag;
        newAllocated->lowerPtr = underPtr;
        newAllocated->upperPtr = overPtr;
        newAllocated->ptr = ptr;
        newAllocated->size = size;
        lists.element = newAllocated;
        return &lists;
    }


    AllocatedLists* recycle = getRecycleAllocated(ptr);
    if(recycle == NULL){
        newAllocated = calloc(sizeof(Allocated), 1);
        newAllocated->flags = flag;
        newAllocated->lowerPtr = underPtr;
        newAllocated->upperPtr = overPtr;
        newAllocated->ptr = ptr;
        newAllocated->size = size;
        AllocatedLists* tempList = &lists;
        while(tempList->next != NULL){
            tempList = tempList->next;
        }
        tempList->next = calloc(1, sizeof(AllocatedLists));
        tempList->next->element = newAllocated;
        tempList->next->previous = tempList;
        tempList->next->next = NULL;
        return tempList->next;
    } else{
        newAllocated = recycle->element;
        newAllocated->flags = flag;
        newAllocated->lowerPtr = underPtr;
        newAllocated->upperPtr = overPtr;
        newAllocated->ptr = ptr;
        newAllocated->size = size;
        newAllocated->free = 0;
        return recycle;
    }

}

// removed and free a memory object at a given node regularly.
static int removeAllocatedRegular(void* ptr){
    AllocatedLists* tempList = &lists;
    Allocated* requestedAllocated = NULL;

    // this means the list is empty
    if(tempList->element == NULL)
        return NOT_FOUND;

    while(1){
        Allocated* element = tempList->element;

        if(element->free == 1 && ptr == element->ptr)
            return DOUBLE_FREE;

        if(element->free == 1)
            goto SKIP;

        if(element->ptr == ptr){
            requestedAllocated = element;
            break;
        }

        SKIP:
        if(tempList->next == NULL)
            break;

        tempList = tempList->next;
    }

    if(requestedAllocated == NULL)
        return NOT_FOUND;

    // free the requested allocated
    if(requestedAllocated->lowerPtr != NULL)
        free(requestedAllocated->lowerPtr);
    else
        free(requestedAllocated->ptr);

    // free the flag struct, these not needed anymore
    free(requestedAllocated->flags);
    requestedAllocated->flags = NULL;

    // set the free bits to true
    requestedAllocated->free = 1;

    return SUCCESS;
}

// dangerously free a memory object
static int removeAllocatedApprox(void* ptr){
    AllocatedLists* tempList = &lists;
    Allocated* requestAllocated = NULL;

    // this means the list is empty
    if(tempList->element == NULL)
        return NOT_FOUND;

    while(1){
        Allocated* element = tempList->element;

        void* startAddr = (element->lowerPtr) ? element->lowerPtr : element->ptr;
        void* endAddr = (element->upperPtr) ? element->upperPtr + 8 : element->ptr + element->size;

        if(element->free == 1 && (ptr >= startAddr && ptr <= endAddr))
            return DOUBLE_FREE;

        if(element->free == 1)
            goto SKIP;

        if(ptr >= startAddr && ptr <= endAddr){
            requestAllocated = element;
            break;
        }

        SKIP:
        if(tempList->next == NULL)
            break;

        tempList = tempList->next;
    }

    if(requestAllocated == NULL)
        return NOT_FOUND;

    // print if in guardian zone
    if((requestAllocated->lowerPtr != NULL) && (ptr <= (requestAllocated->lowerPtr + 8))){
        fprintf(stderr, "LKF_WARN: ptr is in the lower 8 bytes guardian zone!\n");
    }


    if((requestAllocated->upperPtr != NULL) && (ptr >= (requestAllocated->ptr + requestAllocated->size)))
        fprintf(stderr, "LKF_WARN: ptr is in the upper 8 bytes guardian zone!\n");

    // free the pointer
    if(requestAllocated->lowerPtr != NULL)
        free(requestAllocated->lowerPtr);
    else
        free(requestAllocated->ptr);

    // free the flags, not needed
    free(requestAllocated->flags);

    // flip the free bits
    requestAllocated->free = 1;

    return SUCCESS;
}

int lkmalloc(u_int size, void **ptr, u_int flags){
    int doubleMalloc = 0;

    // user mistake checking
    if(size <= 0){
        fprintf(stderr, "error: size is less than or equal to zero\n");
        return ENOMEM;
    }


    if((void*)ptr <= NULL){
        fprintf(stderr, "error: **ptr is null or equal to zero\n");
        return ENOMEM;
    }

    // check if the user is doing double malloc
    if(getAllocated(*ptr) != NULL){
        fprintf(stderr, "This ptr %p has a valid allocated memory on heap, you may be doing double malloc creating memory leak!\n", *ptr);
        doubleMalloc = 1;
    }

    // now begin checking the flags
    Variables* newFlag = initNewFlag(flags);

    // if no protection
    if(flags == LKM_REG){
        void* newPtr = malloc(size);
        insertNewAllocated(newPtr, NULL, NULL, newFlag, size);
        *ptr = newPtr;
        if(doubleMalloc){
            createRecord(MALLOC_RECORD,
                         "LKmalloc.c",
                         __FUNCTION__,
                         336,
                         ptr,
                         "0",
                         size,
                         *ptr,
                         LKR_SERIOUS
            );
            return SUCCESS;
        }else{
            // this means normal free
            createRecord(MALLOC_RECORD,
                         "LKmalloc.c",
                         __FUNCTION__,
                         349,
                         ptr,
                         "0",
                         size,
                         *ptr,
                         LKR_MATCH
            );

            return SUCCESS;
        }
    }

    // check if protections include under or over size
    u_int totalSize = size;
    if(newFlag->under) totalSize += 8;
    if(newFlag->over) totalSize += 8;
    void* entirePtr = malloc(totalSize);
    void* actualPtr = (newFlag->under) ?  entirePtr + 8 : entirePtr;
    void* upperPtr = (newFlag->over) ? actualPtr + size : NULL;

    // if upperPtr exists then overwrite memory with 0x5a
    if(upperPtr)
        memset(upperPtr, 0x5a, 8);

    // if the actual ptr is different then entire ptr is the under pointer and overwrite memory with 0x6b
    if(actualPtr != entirePtr)
        memset(entirePtr, 0x6b, 8);

    // zero out the buffer
    if(newFlag->init)
        memset(actualPtr, 0 , size);

    insertNewAllocated(actualPtr, (actualPtr == entirePtr) ? NULL : entirePtr, upperPtr, newFlag, size);
    *ptr = actualPtr;

    // if double malloc
    if(doubleMalloc){
        createRecord(MALLOC_RECORD,
                    "LKmalloc.c",
                     __FUNCTION__,
                    388,
                    ptr,
                    "0",
                    size,
                    *ptr,
                    LKR_SERIOUS
                );
        return SUCCESS;
    }else{
        // this means malloc with protection
        createRecord(MALLOC_RECORD,
                     "LKmalloc.c",
                     __FUNCTION__,
                     401,
                     ptr,
                     "0",
                     size,
                     *ptr,
                     LKR_MATCH
        );
        return SUCCESS;
    }


}

int lkfree(void **ptr, u_int flags){

    // user error checking
    if((void*)ptr <= NULL){
        fprintf(stderr, "error: **ptr is null or less than 0\n");
        return EINVAL;
    }

    // user error checking
    if(*ptr <= NULL){
        fprintf(stderr, "error: *ptr is null or less than 0\n");
        return EINVAL;
    }

    // parse the flags
    int appro = (flags & LKF_APPROX);
    int warn = (flags & LKF_WARN);
    int unknown = (flags & LKF_UNKNOWN);
    int error = (flags & LKF_ERROR);

    // remove regularly first
    int result = removeAllocatedRegular(*ptr);

    // if double free on regular
    if(result == DOUBLE_FREE){
        createRecord(FREE_RECORD,
                     "LKmalloc.c",
                     __FUNCTION__,
                     460,
                     ptr,
                     "EINVAL",
                     flags,
                     *ptr,
                     LKR_DOUBLE_FREE
        );
        fprintf(stderr, "error: This ptr %p is double free!\n", *ptr);
        if(error)
            exit(1);
        return EINVAL;
    }

    // regular free
    if(flags == LKF_REG){
        if (result == NOT_FOUND){
            createRecord(FREE_RECORD,
                         "LKmalloc.c",
                         __FUNCTION__,
                         444,
                         ptr,
                         "EINVAL",
                         flags,
                         *ptr,
                         LKR_ORPHAN_FREE
            );
            if(unknown)
                fprintf(stderr, "LKF_UNKNOWN: This ptr %p is not allocated\n", *ptr);
            if(error)
                exit(1);
            return EINVAL;
        }

        // this means free success
        createRecord(FREE_RECORD,
                     "LKmalloc.c",
                     __FUNCTION__,
                     476,
                     ptr,
                     "0",
                     flags,
                     *ptr,
                     LKR_MATCH
        );
        return SUCCESS;
    }

    // advanced free
    if(appro)
        result = removeAllocatedApprox(*ptr);

    // not a valid buffer
    if(result == NOT_FOUND){
        createRecord(FREE_RECORD,
                     "LKmalloc.c",
                     __FUNCTION__,
                     495,
                     ptr,
                     "EINVAL",
                     flags,
                     *ptr,
                     LKR_ORPHAN_FREE
        );
        if(unknown)
            fprintf(stderr, "LKF_UNKNOWN: this ptr %p is not part of any allocated buffer\n", *ptr);
        return EINVAL;
    }

    if(result == DOUBLE_FREE){
        fprintf(stderr, "error: This ptr %p is double free!\n", *ptr);
        createRecord(FREE_RECORD,
                     "LKmalloc.c",
                     __FUNCTION__,
                     511,
                     ptr,
                     "EINVAL",
                     flags,
                     *ptr,
                     LKR_DOUBLE_FREE
        );
        if(error)
            exit(1);
        return EINVAL;
    }

    fprintf(stderr, "warning: The ptr you are freeing is in the middle of a valid buffer! %p\n", *ptr);

    // this means free succeeded on APPROX
    if(warn)
        fprintf(stderr, "LKF_WARN: Freed ptr %p as LKF_APPROX\n",  *ptr);

    createRecord(FREE_RECORD,
                 "LKmalloc.c",
                 __FUNCTION__,
                 506,
                 ptr,
                 "0",
                 flags,
                 *ptr,
                 LKR_BAD_FREE
    );

    if(error)
        exit(-1);

    return SUCCESS;
}

int lkreport(int fd, u_int flags){
    FILE* file = fdopen(fd, "w");
    for(int i = 0; i <= record_num; i++){
        Record* record = &report[i];
        record->timestamp[strlen(record->timestamp) - 1] = '\0'; // remove the new line
        if(!(flags & record->record_mode))
            continue;
        fprintf(file, "%d,%s,%s,%d,%s,%p,%s,%x,%p\n",
                record->record_type,
                record->filename,
                record->fxName,
                record->line_num,
                record->timestamp,
                record->ptr_passed,
                record->retVal,
                record->size_or_flags,
                record->alloc_addr_returned
                );
    }
    fclose(file);
    return 0;
}