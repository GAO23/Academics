/*
 * Job manager for "jobber".
 */

#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <stdio.h>
#include <sys/types.h>
#include <signal.h>
#include <fcntl.h>
#include <wait.h>
#include <errno.h>
#include "../include/jobber.h"
#include "../include/task.h"



typedef struct {
    int jobId;
    pid_t gpid;
} Runner;

typedef struct {
    JOB_STATUS status;
    TASK* task; // need to be free
    char* job_cmd; // need to be free
    int exit_status;
    Runner* runner;
    int jobId;
} Job;

static Runner* runners[MAX_RUNNERS];


static Job* jobTable[MAX_JOBS];
static sigset_t mask_all, mask_sigchld;
static int enable = 0;
static volatile sig_atomic_t sigchl_flag = 0;

static const char* HELP_PROMPT = "jobber> help\n"
                          "Available commands:\n"
                          "help (0 args) Print this help message\n"
                          "quit (0 args) Quit the program\n"
                          "enable (0 args) Allow jobs to start\n"
                          "disable (0 args) Prevent jobs from starting\n"
                          "spool (1 args) Spool a new job\n"
                          "pause (1 args) Pause a running job\n"
                          "resume (1 args) Resume a paused job\n"
                          "cancel (1 args) Cancel an unfinished job\n"
                          "expunge (1 args) Expunge a finished job\n"
                          "status (1 args) Print the status of a job\n"
                          "jobs (0 args) Print the status of all jobs";

static const char* HELP = "help";
static const char* QUIT = "quit";
static const char* STATUS = "status";
static const char* JOBS = "jobs";
static const char* ENABLE = "enable";
static const char* DISABLE = "disable";
static const char* SPOOL = "spool";
static const char* PAUSE = "pause";
static const char* RESUME = "resume";
static const char* CANCEL = "cancel";
static const char* EXPUNGE = "expunge";


static Job* newJobPtr = NULL;


static int read_line_sig_handler(void);
static int handle_user_command(TASK* task, char* cmd);
static int start_jobs(void);
static int fork_command_list(COMMAND_LIST* commandList, char* input, char* output);
static int count_args(COMMAND* command);
static int reap_all_children(void);
static void sigchld_handler(int signal);
static Runner* get_avaible_runner(void);
static Job* get_avaible_job(void);
static int fork_job(PIPELINE_LIST* currentPL, Job* job);
static int get_user_input();


static int read_line_sig_handler(void){
    if(sigchl_flag){
        int olderrno = errno;
        sigset_t previous;
        sigprocmask(SIG_BLOCK, &mask_all, &previous);
        reap_all_children();
        sigchl_flag = 0;
        sigprocmask(SIG_SETMASK, &previous, NULL);
        errno = olderrno;
        start_jobs();
    }
    return 0;
}
static int count_args(COMMAND* command){
    int retval = 0;
    WORD_LIST* wordList = command->words->rest;
    while(wordList != NULL){
        wordList = wordList->rest;
        retval += 1;
    }
    return retval;
}


static int get_user_input(){
    while(1){
        char* input = sf_readline("jobber> ");
        if(input == NULL) {
            jobs_fini();
            return 0;
        }
        size_t input_length = strlen(input);
        if(input_length == 0) continue;
        char* temp = malloc(input_length);
        memcpy(temp, input, input_length);
        TASK* task = parse_task(&input);
        int result = handle_user_command(task, temp);
        free(temp);
        if(result == -1) continue;
        if(result == 1) return 0;
        start_jobs();
        fflush(stdout);
    }
}


static int handle_user_command(TASK* task, char* cmd){
    char* first_cmd = task->pipelines->first->commands->first->words->first;
    if(strcmp(first_cmd, HELP) == 0){
        printf("%s\n", HELP_PROMPT);
        return 0;
    }

    if(strcmp(first_cmd, QUIT) == 0){
        jobs_fini();
        return 1;
    }

    if(strcmp(first_cmd, ENABLE) == 0){
        jobs_set_enabled(1);
        return 0;
    }

    if(strcmp(first_cmd, DISABLE) == 0){
        jobs_set_enabled(0);
        return 0;
    }

    if(strcmp(first_cmd, SPOOL) == 0){
        Job* job = get_avaible_job();
        if(job == NULL){
            printf("%s \n", "Error: spool");
            return 0;
        }
        size_t cmdLength = strlen(cmd);
        if(cmd[6] == '\'' && cmd[cmdLength - 1] == '\''){
            cmd[cmdLength - 1] = '\0';
            cmd += 7;
            cmdLength -= 7;
        }else{
            int args_count = 0;
            args_count = count_args(task->pipelines->first->commands->first);
            printf("Wrong number of args (given: %d, required: 1) for command \'spool\'\n", args_count);
            return -1;
        }
        job->task = task;
        job->job_cmd = malloc(cmdLength);
        memcpy(job->job_cmd, cmd, cmdLength);
        job->status = NEW;
        newJobPtr = job;
        job_create(job->job_cmd);
        newJobPtr = NULL;
        return 0;
    }

    if(strcmp(first_cmd, PAUSE) == 0){
        if(task->pipelines->first->commands->first->words->rest == NULL){
            int args_count = count_args(task->pipelines->first->commands->first);
            printf("Wrong number of args (given: %d, required: 1) for command \'pause\'\n", args_count);
            return 0;
        }
        char* stringInt = task->pipelines->first->commands->first->words->rest->first;
        int jobid = (int) strtol(stringInt, NULL, 10);
        if(job_pause(jobid) < 0){
            printf("%s\n", "Error: pause");
        }
        return 0;
    }

    if(strcmp(first_cmd, RESUME) == 0){
        if(task->pipelines->first->commands->first->words->rest == NULL){
            int args_count = count_args(task->pipelines->first->commands->first);
            printf("Wrong number of args (given: %d, required: 1) for command \'resume\'\n", args_count);
            return 0;
        }
        char* stringInt = task->pipelines->first->commands->first->words->rest->first;
        int jobid = (int) strtol(stringInt, NULL, 10);
        if(job_resume(jobid) < 0){
            printf("%s\n", "Error: resume");
        }
        return 0;
    }

    if(strcmp(first_cmd, CANCEL) == 0){
        if(task->pipelines->first->commands->first->words->rest == NULL){
            int args_count = count_args(task->pipelines->first->commands->first);
            printf("Wrong number of args (given: %d, required: 1) for command \'cancel\'\n", args_count);
            return 0;
        }
        char* stringInt = task->pipelines->first->commands->first->words->rest->first;
        int jobid = (int) strtol(stringInt, NULL, 10);
        if(job_cancel(jobid) < 0){
            printf("%s\n", "Error: cancel");
        }
        return 0;
    }

    if(strcmp(first_cmd, EXPUNGE) == 0){
        if(task->pipelines->first->commands->first->words->rest == NULL){
            int args_count = count_args(task->pipelines->first->commands->first);
            printf("Wrong number of args (given: %d, required: 1) for command \'expunge\'\n", args_count);
            return 0;
        }
        char* stringInt = task->pipelines->first->commands->first->words->rest->first;
        int jobid = (int) strtol(stringInt, NULL, 10);
        if(job_expunge(jobid) < 0){
            printf("%s\n", "Error: expunge");
        }
        return 0;
    }

    if(strcmp(first_cmd, JOBS) == 0){
        for(int i = 0; i < MAX_JOBS; i++){
            if(jobTable[i]->status == -1) continue;
            Job* job = jobTable[i];
            printf("job %d [%s]: %s\n", i, job_status_names[job->status], job->job_cmd);
        }
        return 0;
    }

    if(strcmp(first_cmd, STATUS) == 0) {
        if(task->pipelines->first->commands->first->words->rest == NULL){
            int args_count = count_args(task->pipelines->first->commands->first);
            printf("Wrong number of args (given: %d, required: 1) for command \'expunge\'\n", args_count);
            return 0;
        }
        char* stringInt = task->pipelines->first->commands->first->words->rest->first;
        int jobid = (int) strtol(stringInt, NULL, 10);
        if(jobid < 0 || jobid > 7) return 0;
        if(jobTable[jobid]->status == -1) return 0;
        Job* job = jobTable[jobid];
        printf("job %d [%s]: %s\n", jobid, job_status_names[job->status], job->job_cmd);
        return 0;
    }

    printf("Unrecognized command: %s\n", cmd);
    return -1;
}


int jobs_init(void) {
    for(int i = 0; i < MAX_JOBS; i++){
        jobTable[i] = malloc(sizeof(Job));
        memset(jobTable[i], 0, sizeof(Job));
        jobTable[i]->status = -1;
    }
    for(int i = 0; i < MAX_RUNNERS; i++){
        runners[i] = malloc(sizeof(Runner));
        memset(runners[i], 0, sizeof(Runner));
        runners[i]->jobId = -1;
    }
    sf_set_readline_signal_hook(read_line_sig_handler);
    sigfillset(&mask_all);
    sigemptyset(&mask_sigchld);
    sigaddset(&mask_sigchld, SIGCHLD);
    signal(SIGCHLD, sigchld_handler);
    get_user_input();
    return 0;
}

static void sigchld_handler(int signal){
    sigset_t previous;
    sigprocmask(SIG_BLOCK, &mask_all, &previous);
    sigchl_flag = 1;
    sigprocmask(SIG_SETMASK, &previous, NULL);
}

void jobs_fini(void) {
    for(int i = 0; i < MAX_JOBS; i++){
        Job* job = jobTable[i];
        if(job->status == -1) continue;
        job_cancel(i);
    }
    reap_all_children();
    for(int i = 0; i < MAX_JOBS; i++){
        Job* job = jobTable[i];
        if(job->status == -1) continue;
        job_expunge(i);
    }
}

int jobs_set_enabled(int val) {
    int retval = enable;
    enable = (val) ? 1 : 0;
    return retval;
}

int jobs_get_enabled() {
    return enable;
}

static Job* get_avaible_job(void){
    for(int i = 0; i < MAX_JOBS; i++){
        Job* job = jobTable[i];
        if(job->status == -1) {
            job->jobId = i;
            return job;
        }
    }
    return NULL;
}

int job_create(char *command){
    Job* job = newJobPtr;
    if(job == NULL){
//        fprintf(stderr, "%s \n", "new job created is null for some reason");
        return -1;
    }
    printf("TASK: %s\n", command);
    sf_job_create(job->jobId);
    job->status = WAITING;
    sf_job_status_change(job->jobId, NEW, WAITING);
    return 0;
}

int job_expunge(int jobid) {
    if(jobid < 0 || jobid > 7){
        return -1;
    }

    Job* job = jobTable[jobid];
    if(job->status == COMPLETED || job->status == ABORTED || job->status == CANCELED){
        free_task(job->task);
        free(job->job_cmd);
        memset(job, 0, sizeof(Job));
        job->status = -1;
        sf_job_expunge(jobid);
        return 0;
    }
    return -1;
}

int job_cancel(int jobid) {
    if(jobid < 0 || jobid > 7){
        return -1;
    }
    sigset_t previous;
    Job* job = jobTable[jobid];
    Runner* runner;

    sigprocmask(SIG_BLOCK, &mask_all, &previous);
    if(job->status == RUNNING || job->status == PAUSED){
        runner = job->runner;
        if(runner == NULL){
            sigprocmask(SIG_SETMASK, &previous, NULL);
//            fprintf(stderr, "%s\n", "Cancel error, a running or paused job has NULL runner");
            return -1;
        }
        killpg(runner->gpid, SIGKILL);
        job->runner = NULL;
        job->status = CANCELED;
        sigprocmask(SIG_SETMASK, &previous, NULL);
        return 0;
    }
    else if(job->status == WAITING){
        job->status = ABORTED;
        sf_job_status_change(jobid, WAITING, ABORTED);
        sigprocmask(SIG_SETMASK, &previous, NULL);
        return 0;
    }
    sigprocmask(SIG_SETMASK, &previous, NULL);
    return -1;
}

int job_pause(int jobid) {
    if(jobid < 0 || jobid > 7){
        return -1;
    }
    sigset_t previous;
    Job* job = jobTable[jobid];
    Runner* runner;
    if(job->status != RUNNING) return -1;

    sigprocmask(SIG_BLOCK, &mask_all, &previous);
    runner = job->runner;
    if(runner == NULL) {
//        fprintf(stderr, "%s\n", "pause error, a running job has NULL runner");
        sigprocmask(SIG_SETMASK, &previous, NULL);
        return -1;
    }
    killpg(runner->gpid, SIGSTOP);
    job->status = PAUSED;
    sf_job_status_change(jobid, RUNNING, PAUSED);
    sf_job_pause(runner->jobId, runner->gpid);
    sigprocmask(SIG_SETMASK, &previous, NULL);

    return 0;
}

int job_resume(int jobid) {
    if(jobid < 0 || jobid > 7){
        return -1;
    }
    sigset_t previous;
    Job* job = jobTable[jobid];
    Runner* runner;
    if(job->status != PAUSED) return -1;

    sigprocmask(SIG_BLOCK, &mask_all, &previous);
    runner = job->runner;
    if(runner == NULL) {
//        fprintf(stderr, "%s\n", "resume error, a paused job has NULL runner");
        sigprocmask(SIG_SETMASK, &previous, NULL);
        return -1;
    }
    killpg(runner->gpid, SIGCONT);
    job->status = RUNNING;
    sf_job_status_change(jobid, PAUSED, RUNNING);
    sf_job_resume(runner->jobId, runner->gpid);
    sigprocmask(SIG_SETMASK, &previous, NULL);

    return 0;
}

int job_get_pgid(int jobid) {
    if(jobid < 0 || jobid > 7){
        return -1;
    }
    sigset_t previous;
    sigprocmask(SIG_BLOCK, &mask_all, &previous);
    Job* job = jobTable[jobid];
    Runner* runner;
    pid_t retval = -1;
    runner = job->runner;
    if(runner != NULL) retval = runner->gpid;
    sigprocmask(SIG_SETMASK, &previous, NULL);
    return retval;
}

JOB_STATUS job_get_status(int jobid) {
    if(jobid < 0 || jobid > 7){
        return -1;
    }
    Job* job = jobTable[jobid];
    if(job->status == -1) return -1;
    return job->status;
}

int job_get_result(int jobid) {
    if(jobid < 0 || jobid > 7){
        return -1;
    }
    Job* job = jobTable[jobid];
    if(job->status != COMPLETED) return -1;
    return job->exit_status;
}

int job_was_canceled(int jobid) {
    if(jobid < 0 || jobid > 7){
        return 0;
    }
    if(jobTable[jobid]->status == ABORTED || jobTable[jobid]->status == CANCELED){
        return 1;
    }
    return 0;
}

char *job_get_taskspec(int jobid) {
    if(jobid < 0 || jobid > 7){
        return NULL;
    }

    Job* job = jobTable[jobid];
    if(job->status == -1) return NULL;

    return job->job_cmd;
}

static Runner* get_avaible_runner(void){
    Runner* avaibleRunner = NULL;
    for(int i = 0; i < MAX_RUNNERS; i++){
        Runner* runner = runners[i];
        if(runner->jobId != -1) continue;
        avaibleRunner = runners[i];
        break;
    }
    return avaibleRunner;
}

static int start_jobs(void){
    if(!enable) return -1;
    sigset_t previous;
    sigprocmask(SIG_BLOCK, &mask_all, &previous);
    for(int i = 0; i < MAX_JOBS; i++){
        Job* job = jobTable[i];
        Runner* avaibleRunner = get_avaible_runner();
        if(job->status != WAITING) continue;
        if(avaibleRunner == NULL){
//            fprintf(stderr, "No more runner for jobid %d \n", i);
            sigprocmask(SIG_SETMASK, &previous, NULL);
            return -1;
        }
        TASK* task = job->task;
        PIPELINE_LIST* currentPL = task->pipelines;
        avaibleRunner->jobId = i;
        job->runner = avaibleRunner;
        fork_job(currentPL, job);
    }
    sigprocmask(SIG_SETMASK, &previous, NULL); // restores the signal that was blocked when forking
    return 0;
}

static int fork_job(PIPELINE_LIST* currentPL, Job* job){
    pid_t childPid = -1;
    if((childPid = fork()) < 0){
//        perror("can't fork pipelines");
        return -1;
    }

    if(childPid == 0){
        sigprocmask(SIG_UNBLOCK, &mask_all, NULL);
        setpgid(0, 0);
        COMMAND_LIST* currentCL =  currentPL->first->commands;
//        int count = 0;
        while(1){
            if(fork_command_list(currentCL, currentPL->first->input_path, currentPL->first->output_path) == -1){
//                fprintf(stderr, "%s \n", "failed to fork command list");
                killpg(getpgrp(), SIGKILL);
                abort();
            }
//            count += 1;
            int status = 0;
            pid_t childPid = -1;
            while ((childPid = waitpid(-1, &status, 0)) > 0) {
//            fprintf(stderr, "reaped the grandchild %d\n", childPid);
                if(!WIFEXITED(status)){
//                    fprintf(stderr, "%s \n", "one of the command terminated abnormally, aborting the job");
                    killpg(getpgrp(), SIGKILL);
                    abort();
                }
            }
            currentPL = currentPL->rest;
            if(currentPL == NULL) break;
            currentCL = currentPL->first->commands;
        }

//        fprintf(stderr, "child forked %d times\n", count);

        exit(0);
    }

    job->runner->gpid = childPid;
    sf_job_start(job->runner->jobId, childPid);
    JOB_STATUS old_status = job->status;
    job->status = RUNNING;
    sf_job_status_change(job->runner->jobId, old_status, RUNNING);
    return 0;
}

static int fork_command_list(COMMAND_LIST* commandList, char* input, char* output){
    pid_t childPid = -1;
    int pfd[2];
    int oldpfd[2];
    if(pipe(oldpfd) < 0){
//        perror("pipe oldpfd error");
        return  -1;
    }
    int pipe_result = -1;
    int commandListLength = 1;
    while(commandList != NULL){
        WORD_LIST* wordList = commandList->first->words;
        char* argv[100];
        int argvIndex = 0;
        memset(&argv, 0, sizeof(argv));
        while(wordList != NULL){
            if(strcmp(wordList->first, SPOOL) == 0){
              wordList = wordList->rest;
              continue;
            }
            int lastIndex = strlen(wordList->first) - 1;

            if(*(wordList->first) == '\''){
                argv[argvIndex] = wordList->first + 1;
            }

            else if(wordList->first[lastIndex] == '\''){
                wordList->first[lastIndex] = '\0';
                argv[argvIndex] = wordList->first;
            }

            else{
                argv[argvIndex] = wordList->first;
            }

            argvIndex += 1;
            wordList = wordList->rest;
        }

        if(commandList->rest != NULL){
            if((pipe_result = pipe(pfd)) < 0){
//              perror("pipe pfd error");
            }
        }


        if((childPid = fork()) < 0)
            return -1;

        if(childPid == 0){
            // unblocks child since child process als inherits the block
            sigprocmask(SIG_UNBLOCK, &mask_sigchld, NULL);
            if(commandList->rest != NULL){
                close(pfd[0]);
                if(dup2(pfd[1], STDOUT_FILENO) < 0){
//                    perror("dup error for pdf[1]");
                }
                close(pfd[1]);
            }

            if(commandListLength > 1) {
                close(oldpfd[1]);
                if(dup2(oldpfd[0], STDIN_FILENO) < 0) {
//                    perror("dup2 stdin error");
                }
                close(oldpfd[0]);
//                char buffer[200];
//                read(STDIN_FILENO, buffer, 200);
//                fprintf(stderr, "piping into the stdin are %s\n", buffer);
            }

            if(commandListLength == 1 && input != NULL){
                char temp[strlen(input)];
                int i = 0;
                while(*input != '\0' && *input != '\''){
                    temp[i] = *input;
                    i += 1;
                    input += 1;
                }
                temp[i] = 0;
                int in = open(temp, O_RDONLY, 0600);
                if(in < 0){
//                    perror("open error");
                }
                if(dup2(in, STDIN_FILENO) < 0){
//                    perror("Can't dup stdin");
                }
                close(in);
//                char buffer[200];
//                read(STDIN_FILENO, buffer, 200);
//                fprintf(stderr, "debug: %s\n", buffer);
            }

            if(commandList->rest == NULL && output != NULL){
                char temp[strlen(output)];
                int i = 0;
                while(*output != '\0' && *output != '\''){
                    temp[i] = *output;
                    i += 1;
                    output += 1;
                }
                temp[i] = 0;
                int out = open(temp, O_RDWR|O_CREAT, 0600);
                if(out < 0){
//                    perror("Can't open the output file");
                }
                if(dup2(out, STDOUT_FILENO) < 0){
//                    perror("can't redirect to std out");
                }
                close(out);
            }

            char* filePath = argv[0];
            filePath += strlen(filePath) -1;
            if(*filePath == '\''){
                *filePath = 0;
            }
//            for(int i = 0; argv[i] != NULL; i++){
//                fprintf(stderr, "Debug %d: %s\n", i, argv[i]);
//            }

//            fprintf(stderr, "parent pid %d, child gpid %d, child pid %d\n", getppid(), getpgrp(), getpid());
            // just in case
            execvp(argv[0], argv);
//            perror("execvp failed");
            abort();
        }

        if(!(pipe_result < 0) && (commandList->rest != NULL)){
            if(dup2(pfd[0], oldpfd[0]) < 0){
//                perror("dup error for oldpfd[0]");
            }
            if(dup2(pfd[1], oldpfd[1]) < 0){
//                perror("dup error for oldpfd[1]");
            }
            close(pfd[0]);
            close(pfd[1]);
        }

        commandList = commandList->rest;
        commandListLength += 1;
        pipe_result = -1;
    }
    close(oldpfd[0]);
    close(oldpfd[1]);
    close(pfd[0]);
    close(pfd[1]);
//    dup2(backup, STDIN_FILENO);
    return 0;
}


static int reap_all_children(void){
    for(int i = 0; i < MAX_RUNNERS; i++){
        Runner* runner = runners[i];
        if(runner->jobId == -1) continue;
        siginfo_t sigType;
        memset(&sigType, 0, sizeof(sigType));
//        fprintf(stderr, "waiting for pgid %d\n", runner[i].gpid);
        int result = waitid(P_PGID, runner->gpid, &sigType, WEXITED);
        if(sigType.si_pid == 0) continue;
        if(result == 0){
            jobTable[runner->jobId]->exit_status = sigType.si_status;
            jobTable[runner->jobId]->runner = NULL;
            sf_job_end(runner->jobId, runner->gpid, sigType.si_status);
            Job* job = jobTable[runner->jobId];
            job->status = (job->status == CANCELED) ? CANCELED : COMPLETED;
            sf_job_status_change(runner->jobId, RUNNING, job->status);
            memset(runner, 0, sizeof(Runner));
            job->runner = NULL;
            runner->jobId = -1;
        }
    }
    return 0;
}

