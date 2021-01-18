//
// Created by xgao on 4/8/20.
//

#include <stdio.h>
#include <signal.h>
#include <sys/wait.h>
#include <string.h>
#include <stdlib.h>
#include "../include/parser.h"
#include "../include/commons.h"
#include "../include/execute.h"


JOBS_LINKED_LIST jobs;
int unreapChild = false;
FILE* null;
pid_t fgPid = -1;

int debug = false;

void parse_args(int argc, char** argv, char** envp);
void child_handler();
void reap_children(void);
void sigintHandler();
void sigstopHandler();


int main (int argc, char** argv, char** envp){




    // for debugging purposes and to make WError shut up about unused variables
    null = fopen("/dev/null", "w");
    parse_args(argc, argv, envp);

    // set up the handler for SIGCHILD
    struct sigaction newSigChildAction, oldSigChildAction; // old action may not be needed
    newSigChildAction.sa_handler = child_handler;
    sigemptyset(&newSigChildAction.sa_mask);
    newSigChildAction.sa_flags = 0; // used for behavior modifying
    sigaction (SIGCHLD, &newSigChildAction, &oldSigChildAction);

    // set up the handler for sigint
    struct sigaction newSigIntAction, oldSigIntAction; // old action may not be needed
    newSigIntAction.sa_handler = sigintHandler;
    sigemptyset(&newSigIntAction.sa_mask);
    newSigIntAction.sa_flags = 0; // used for behavior modifying
    sigaction (SIGINT, &newSigIntAction, &oldSigIntAction);

    // set up the handler for sigstop
    struct sigaction newSigStopAction, oldSigStopAction; // old action may not be needed
    newSigStopAction.sa_handler = sigstopHandler;
    sigemptyset(&newSigStopAction.sa_mask);
    newSigStopAction.sa_flags = 0; // used for behavior modifying
    sigaction (SIGTSTP, &newSigStopAction, &oldSigStopAction);

    int status;
    char* line = NULL;
    char** arguments = NULL;
    do{
        printf("smash> ");
        line = read_input();
        if(unreapChild) {
            printf("\n");
            reap_children();
        }
        arguments = parse_commands(line);
        status = execute(arguments, line);
    }while(status == CONTINUE);
    return 0;
}

void parse_args(int argc, char** argv, char** envp){

    if(argc == 1){
        return;
    }

    if(argc == 2){
        char* arg = argv[1];
        if(strcmp(arg, "-d") == 0) {
            debug = true;
        }else{
            execute_bash(arg);
        }
    }

    if(argc == 3){
        char* arg = argv[1];
        if(strcmp(arg, "-d") != 0){
            fprintf(stderr, "Usage: smash [-d] [filename]\n");
            exit(1);
        }
        execute_bash(argv[2]);
    }

    if(argc > 3){
        fprintf(stderr, "Usage: smash [-d] [filename]\n");
        return;
    }

    // to make WError shut up about unused varibles. Code here won't be reach and will do nothing anyways.
    int count = 0;
    while (envp[count] != NULL){
        fprintf(null, "%s\n", envp[count]);
        count++;
    }
}

// setting a flag to advoid reentrancy issues
void child_handler(){
    unreapChild = true;
}

// crl_c handler
void sigintHandler(){
    if(fgPid == -1) execute_exit();
    kill(fgPid, SIGKILL);
}

// crl_z handler
void sigstopHandler(){
    if(fgPid == -1)
        return;

    kill(fgPid, SIGSTOP);
}

// reaping exited children for background jobs. Foreground reaping is handled in their respectively functions in execute.c
void reap_children(void){
    siginfo_t siginfo;
    do{
        int result = waitid(P_ALL, 0, &siginfo, WEXITED | WNOHANG | WSTOPPED | WCONTINUED);
        if(result < 0) {
            break;
        }

        if(change_job_status(&siginfo) < 0)
            break;

        memset(&siginfo, 0 , sizeof(siginfo_t));
    }while(1);
    unreapChild = false;
}


