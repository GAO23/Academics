//
// Created by xgao on 4/8/20.
//

#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include <signal.h>
#include <sys/wait.h>
#include "../include/commons.h"
#include "../include/list.h"

extern JOBS_LINKED_LIST jobs;
extern int debug;

void insert_node(JOBS_LINKED_LIST* node){
    JOBS_LINKED_LIST* last_job = &jobs;
    while (last_job->next != NULL){
        last_job = last_job->next;
    }
    last_job->next = node;
    node->previous = last_job;
}

static void print_terminated(JOBS* job){
    char** argv = job->argv;
    int count = 0;
    char* arg = argv[count];
    fprintf(stderr, "ENDED: %s ", arg);
    count += 1;
    arg = argv[count];
    while(arg != NULL){
        if(debug) fprintf(stderr, "%s ", arg);
        count += 1;
        arg = argv[count];
    }
    fprintf(stderr, "\n");
    fprintf(stderr, "(ret=%d)\n", job->exit_code);
}

// very important function for changing all background job's status after a sigchild. These change jobs to terminated, running, continue, stop, as well as remove failed background jobs from the list
int change_job_status(siginfo_t* siginfo){
    JOBS_LINKED_LIST* current_job = &jobs;
    pid_t pid = siginfo->si_pid;
    if(current_job->next == NULL){
//        fprintf(stderr,"no such job(%d) found\n", pid);
        return -1;
    }

    int found = 0;

    while (current_job->next != NULL){
        if(current_job->next->element->pid == pid){
            found = true;
            break;
        }
        current_job = current_job->next;
    }


    if(!found) {
//        fprintf(stderr,"no such job(%d) found\n", pid);
        return -1;
    }

    if(siginfo->si_code == CLD_STOPPED){
        current_job->next->element->status = STOPPED;
        return 0;
    }

    if(siginfo->si_code == CLD_CONTINUED){
        current_job->next->element->status = RUNNING;
        return 0;
    }

    if((siginfo->si_code == CLD_EXITED) || (siginfo->si_code == CLD_KILLED)){
        if(siginfo->si_status == 69){
            if(debug) {
                current_job->next->element->exit_code = siginfo->si_status;
                print_terminated(current_job->next->element);
            }
            remove_node(current_job->next);
            return 0;
        }
        current_job->next->element->status = TERMINATED;
        current_job->next->element->exit_code = siginfo->si_status;
        if(debug) print_terminated(current_job->next->element);
        return 0;
    }

    return 0;
}



int remove_node(struct JOBS_LINKED_LIST* node){
    if(node->previous != NULL){
        node->previous->next = node->next;
    }

    if(node->next != NULL){
        node->next->previous = node->previous;
    }

    free(node->element->argv[0]);
    free(node->element->argv);
    free(node->element);

   free(node);
   return 0;
}

// use for getting the job number in "jobs"
int get_node_index(JOBS_LINKED_LIST* node){
    JOBS_LINKED_LIST* currentNode = &jobs;
    int count = 1;
    while (currentNode->next != NULL){
        if(currentNode->next == node) return count;
        currentNode = currentNode->next;
        count++;
    }
    return -1;
}

