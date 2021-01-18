#include <stdlib.h>
#include <errno.h>
#include <signal.h>
#include <string.h>
#include "../include/jobber.h"

/*
 * "Jobber" job spooler.
 */


int main(int argc, char *argv[])
{
//    freopen("/dev/null", "r", stdin);
    jobs_init();
    fflush(stderr);
    fflush(stdout);
    exit(EXIT_SUCCESS);
}

/*
 * Just a reminder: All non-main functions should
 * be in another file not named main.c
 */


