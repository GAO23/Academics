//
// Created by xgao on 4/8/20.
//

#ifndef HW2_LIST_H
#define HW2_LIST_H

typedef struct JOBS {
    char** argv;
    int status;
    pid_t pid;
    int exit_code;
}JOBS;


typedef struct JOBS_LINKED_LIST{
    struct JOBS_LINKED_LIST* next;
    struct JOBS_LINKED_LIST* previous;
    JOBS* element;
}JOBS_LINKED_LIST;

int remove_node(struct JOBS_LINKED_LIST* node);
void insert_node(JOBS_LINKED_LIST* node);
int change_job_status(siginfo_t* siginfo);
int get_node_index(JOBS_LINKED_LIST* node);

#endif //HW2_LIST_H
