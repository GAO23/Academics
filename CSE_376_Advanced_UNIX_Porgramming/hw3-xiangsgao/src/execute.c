//
// Created by xgao on 4/8/20.
//

#include <stdlib.h>
#include <sys/types.h>
#include <fcntl.h>
#include <sys/stat.h>
#include <unistd.h>
#include <stdio.h>
#include <sys/wait.h>
#include <string.h>
#include "../include/list.h"
#include "../include/commons.h"

extern FILE* null;
extern JOBS_LINKED_LIST jobs;
extern pid_t fgPid;
extern int debug;

static JOBS_LINKED_LIST* create_new_background_job(char** argv);
static void check_for_redirect(char** argv);


static int execute_normal(char** argv){
    pid_t pid;
    pid = fork();
    if(pid < 0){
        perror("fork failed in execute_normal: ");
        exit(1);
    }

    if(pid == 0){
        // closing the stdin
//        int nullfd = fileno(null);
//        dup2(nullfd, STDIN_FILENO);
//        fclose(null);
        // signals blocking so it doesnt conflict
//        sigset_t blockedSignal;
//        sigemptyset(&blockedSignal);
//        sigaddset(&blockedSignal, SIGINT);
//        sigaddset(&blockedSignal, SIGTSTP);
//        sigprocmask(SIG_BLOCK, &blockedSignal, NULL);
        check_for_redirect(argv);
        if((execvp(argv[0], argv)) < 0){
            perror("failed to execute: ");
        }
        exit(69);
    }
    fgPid = pid;
    siginfo_t siginfo;
    do{
       if(waitid(P_PID, pid, &siginfo, WEXITED | WNOHANG | WSTOPPED) < 0){
           perror("waitid error in execute_normal: ");
           printf("%d\n", pid);
           break;
       }
       if((siginfo.si_code == CLD_EXITED) || (siginfo.si_code == CLD_KILLED)){
           if(debug){
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
               fprintf(stderr, "(ret=%d)\n", siginfo.si_status);
           }
           break;
       }

       if(siginfo.si_code == CLD_STOPPED){
           JOBS_LINKED_LIST* newNode = create_new_background_job(argv);
           JOBS* job = newNode->element;
           job->pid = pid;
           job->status = STOPPED;
           printf("\nProccess stopped\n");
           break;
       }
    }while(1);
    fgPid = -1;
    return EXECUTED_SUCCESSFULLY;
}

static int execute_cd(char** argv){
    if(argv[1] == NULL){
        char* homeDir = getenv("HOME");
        if(homeDir == NULL){
            fprintf(stderr, "Failed to get home dir in execute_cd\n");
        }else{
            if(chdir(homeDir) < 0){
                perror("Failed to changed directory: ");
            }
        }
    }else{
        if(chdir(argv[1]) < 0){
            perror("Failed to changed directory: ");
        }
    }
    return EXECUTED_SUCCESSFULLY;
}

void execute_exit(void){
    // kill all background jobs
    JOBS_LINKED_LIST* job = &jobs;
    while(job->next != NULL){
        kill(job->next->element->pid, SIGKILL);
        job = job->next;
    }
    exit(0);
}

static int execute_kill(char** argv){
    if(argv[1] != NULL){
        if(strcmp(argv[1], "-N") == 0){
            if(argv[2] == NULL){
                fprintf(stderr, "kill -N needs a a job number as third argument\n");
                return EXECUTED_SUCCESSFULLY;
            }
            int jobNum = strtol(argv[2], NULL, 10);

            JOBS_LINKED_LIST* node = jobs.next;
            for(int i = 1; i < jobNum; i++){
                node = node->next;
                if(node == NULL) break;
            }

            if(node == NULL){
                fprintf(stderr, "No such jobs\n");
                return EXECUTED_SUCCESSFULLY;
            }

            if(node->element->status == TERMINATED){
                fprintf(stderr, "job already exited\n");
                return EXECUTED_SUCCESSFULLY;
            }

            pid_t jobPid = node->element->pid;
            kill(jobPid, SIGKILL);
            printf("job %s killed\n", argv[2]);
            return EXECUTED_SUCCESSFULLY;
        }
    }

    // else just execute the kill found in the system environment path.
    return execute_normal(argv);
}

// if a job is to be run in the back ground then log it in a linked list
static JOBS_LINKED_LIST* create_new_background_job(char** argv){
    JOBS* newJob = calloc(1, sizeof(*newJob));
    newJob->argv = argv;
    JOBS_LINKED_LIST* newNode = calloc(1, sizeof(*newNode));
    newNode->element = newJob;
    insert_node(newNode);
    return newNode;
}

static int execute_fg(char** argv){
    if(argv[1] == NULL){
        fprintf(stderr, "fg requires a node number, check for it using jobs\n");
        return EXECUTED_SUCCESSFULLY;
    }

    int jobNum = strtol(argv[1], NULL, 10);

    JOBS_LINKED_LIST* node = jobs.next;
    for(int i = 1; i < jobNum; i++){
        node = node->next;
        if(node == NULL) break;
    }

    if(node == NULL){
        fprintf(stderr, "No such jobs\n");
        return EXECUTED_SUCCESSFULLY;
    }

    if(node->element->status == TERMINATED){
        fprintf(stderr, "job already exited\n");
        return EXECUTED_SUCCESSFULLY;
    }

    pid_t jobPid = node->element->pid;
    // if this child is stopped then have it continue executing
    if(node->element->status == STOPPED) kill(jobPid, SIGCONT);
    fgPid = node->element->pid;
    printf("Running node %s in fg\n", argv[1]);
    siginfo_t siginfo;
    do{
        if(waitid(P_PID, node->element->pid, &siginfo, WEXITED | WNOHANG | WSTOPPED) < 0){
            perror("waitid error in execute_normal: ");
            printf("%d\n", node->element->pid);
            break;
        }
        if((siginfo.si_code == CLD_EXITED) || (siginfo.si_code == CLD_KILLED)){
            change_job_status(&siginfo);
            printf("\nProccess exited with exit code %d\n",siginfo.si_status);
            break;
        }

        if(siginfo.si_code == CLD_STOPPED){
            change_job_status(&siginfo);
            printf("\nProccess stopped\n");
            break;
        }
    }while(1);
    fgPid = -1;

    return EXECUTED_SUCCESSFULLY;
}

// check for >, < , 2>
static void check_for_redirect(char** argv){
    int count = 1;
    char* last_arg = argv[count];
    while(last_arg != NULL){
        count += 1;
        last_arg = argv[count];
    }
    last_arg = argv[count - 1];
    if(last_arg[0] == '>'){
        last_arg += 1;
        if(*last_arg == '\0') return;
        int newstdout = creat(last_arg, S_IRWXG | S_IRWXU);
        dup2(newstdout, STDOUT_FILENO);
        close(newstdout);
        argv[count - 1] = NULL;
        return;
    }


    last_arg = argv[count - 1];
    if(last_arg[0] == '<'){
        last_arg += 1;
        if(*last_arg == '\0') return;
        int newstdin = open(last_arg, O_RDONLY);
        dup2(newstdin, STDIN_FILENO);
        close(newstdin);
        argv[count - 1] = NULL;
        return;
    }

    last_arg = argv[count - 1];
    if(last_arg[0] == '2'){
        last_arg += 1;
        if(*last_arg == '\0') return;
        if(*last_arg != '>') return;;
        last_arg +=1;
        if(*last_arg == '\0') return;
        int newstdout = creat(last_arg, S_IRWXU | S_IRWXG);
        dup2(newstdout, STDERR_FILENO);
        close(newstdout);
        argv[count - 1] = NULL;
        return;
    }

}

static int execute_background(JOBS* job){
    job->status = RUNNING;
    pid_t pid;
    pid = fork();

    if(pid < 0){
        perror("fork failed in execute_background: ");
        exit(1);
    }

    if(pid == 0){
        int count = 1;
        // closing the stdin
//        int nullfd = fileno(null);
//        dup2(nullfd, STDIN_FILENO);
//        fclose(null);
        // signals blocking so it doesnt conflict
//        sigset_t blockedSignal;
//        sigemptyset(&blockedSignal);
//        sigaddset(&blockedSignal, SIGINT);
//        sigaddset(&blockedSignal, SIGTSTP);
//        sigprocmask(SIG_BLOCK, &blockedSignal, NULL);


        while(true){
            if(strcmp(job->argv[count], "&") == 0)break;
            count++;
        }
        check_for_redirect(job->argv);
        job->argv[count] = NULL;
        if((execvp(job->argv[0], job->argv)) < 0){
            perror("failed to execute: ");
        }
        exit(69);
    }

    job->pid = pid;
    printf("%s is running\n", job->argv[0]);
    return EXECUTED_SUCCESSFULLY;
}

// running jobs command
static int execute_jobs(void){
    JOBS_LINKED_LIST* currentJob = &jobs;
    while(currentJob->next != NULL){
        char buffer[256];
        strcpy(buffer, "Unknown");
        JOBS* job = currentJob->next->element;
        if(job->status == RUNNING) strcpy(buffer, "Running");
        if(job->status == TERMINATED) sprintf(buffer, "terminated with exit code %d", job->exit_code);
        if(job->status == STOPPED) sprintf(buffer, "Stopped");
        printf("Job: %d, Job pid: %d, Program: %s, Status: %s\n", get_node_index(currentJob->next), job->pid, job->argv[0], buffer);
        currentJob = currentJob->next;
    }
    return EXECUTED_SUCCESSFULLY;
}

static int execute_bg(char** argv){
    if(argv[1] == NULL){
        fprintf(stderr, "fg requires a job number, check for it using jobs\n");
        return EXECUTED_SUCCESSFULLY;
    }

    int jobNum = strtol(argv[1], NULL, 10);

    JOBS_LINKED_LIST* job = jobs.next;
    for(int i = 1; i < jobNum; i++){
        job = job->next;
        if(job == NULL) break;
    }

    if(job == NULL){
        fprintf(stderr, "No such jobs\n");
        return EXECUTED_SUCCESSFULLY;
    }

    if(job->element->status == TERMINATED){
        fprintf(stderr, "job already exited\n");
        return EXECUTED_SUCCESSFULLY;
    }

    pid_t jobPid = job->element->pid;
    // if this child is stopped then have it continue executing
    if(job->element->status == STOPPED) kill(jobPid, SIGCONT);
    printf("Running job %s in bg\n", argv[1]);
    return EXECUTED_SUCCESSFULLY;
}

// check if a job has &
static int isBackground(char** argv){
    int count = 1;
    char* arg = argv[count];
    while (arg != NULL){
        if(strcmp(arg, "&") == 0)return true;
        count++;
        arg = argv[count];
    }
    return false;
}

// turns all $ into strings of their enviorment values
static void evaulte_enviorment_varibles(char** argv){
    int count = 0;
    char* arg = argv[count];
    while(arg != NULL){
        if(arg[0] == '$'){
            if(arg[1] == '\0') continue;
            arg += 1;
            argv[count] = getenv(arg);
        }
        count += 1;
        arg = argv[count];
    }
}

// if a bash file is provided in none interactive mode
void execute_bash(char* file){
    int pid = fork();

    if(pid < 0){
        perror("fork failed in execute_bash: ");
        exit(69);
    }

    char* args[2];
    args[0] = file;
    args[1] = NULL;

    if(pid == 0){
        execvp(file, args);
        perror("execvp failed in execute_bash\n");
        exit(69);
    }

    int status;
    wait(&status);
    printf("Script exited with status code %d\n", status);
    exit(0);
}

// master execute for all commands in interactive mode
int execute(char** argv, char* line) {
    if(debug) fprintf(stderr, "RUNNING: %s\n", line);

    if (argv[0] == NULL){
        if(debug) fprintf(stderr, "ENDED: %s\n", line);
        if(debug) fprintf(stderr, "(ret=No cmd so N/A)\n");
        return CONTINUE;
    }


    evaulte_enviorment_varibles(argv);

    if (strcmp(argv[0], "cd") == 0) {
        execute_cd(argv);
        return CONTINUE;
    }

    if (strcmp(argv[0], "exit") == 0) {
        execute_exit();
        return WTF;
    }

    if (strcmp(argv[0], "jobs") == 0) {
        execute_jobs();
        if(debug) fprintf(stderr, "ENDED: %s\n", line);
        if(debug) fprintf(stderr, "(ret=none, no fork used)\n");
        return CONTINUE;
    }

    if (strcmp(argv[0], "fg") == 0) {
        execute_fg(argv);
        if(debug) fprintf(stderr, "ENDED: %s\n", line);
        if(debug) fprintf(stderr, "(ret=none, no fork used)\n");
        return CONTINUE;
    }

    if (strcmp(argv[0], "bg") == 0) {
        execute_bg(argv);
        if(debug) fprintf(stderr, "ENDED: %s\n", line);
        if(debug) fprintf(stderr, "(ret=none, no fork used)\n");
        return CONTINUE;
    }

    if (strcmp(argv[0], "kill") == 0) {
        execute_kill(argv);
        return CONTINUE;
    }

    if (isBackground(argv)) {
        JOBS_LINKED_LIST *newNode = create_new_background_job(argv);
        execute_background(newNode->element);
        return CONTINUE;
    }

    execute_normal(argv);
    return CONTINUE;
}