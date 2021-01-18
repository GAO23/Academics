package osp.Threads;
import java.util.Vector;
import java.util.Enumeration;
import osp.Utilities.*;
import osp.IFLModules.*;
import osp.Tasks.*;
import osp.EventEngine.*;
import osp.Hardware.*;
import osp.Devices.*;
import osp.Memory.*;
import osp.Resources.*;

/**
   This class is responsible for actions related to threads, including
   creating, killing, dispatching, resuming, and suspending threads.

   @OSPProject Threads

   Xiangshuai Gao
   • student Id: 110930549
   • the following pledge:
   I pledge my honor that all parts of this project were done by me individually, without
   collaboration with anyone, and without consulting any external sources that provide
   full or partial solutions to a similar project.
   I understand that breaking this pledge will result in an “F” for the entire course.

*/
public class ThreadCB extends IflThreadCB
{
    public static final int PRIORITY_HIGH = 1;
    public static final int PRIORITY_MEDIUM = 4;
    public static final int PRIORITY_LOW = 8;
    public static final int clockTicks = 80;
    private static int invocation;
    private static ReadyQueue readyQueue;
    /**
       The thread constructor. Must call 

       	   super();

       as its first statement.

       @OSPProject Threads
    */
    public ThreadCB()
    {
        // your code goes here
        super();
    }

    /**
       This method will be called once at the beginning of the
       simulation. The student can set up static variables here.
       
       @OSPProject Threads
    */
    public static void init()
    {
        // your code goes here
        readyQueue = new ReadyQueue();
        invocation = 1;
    }

    /** 
        Sets up a new thread and adds it to the given task. 
        The method must set the ready status 
        and attempt to add thread to task. If the latter fails 
        because there are already too many threads in this task, 
        so does this method, otherwise, the thread is appended 
        to the ready queue and dispatch() is called.

	The priority of the thread can be set using the getPriority/setPriority
	methods. However, OSP itself doesn't care what the actual value of
	the priority is. These methods are just provided in case priority
	scheduling is required.

	@return thread or null

        @OSPProject Threads
    */
    static public ThreadCB do_create(TaskCB task)
    {
        // your code goes here

        if(task.getThreadCount() >= MaxThreadsPerTask){
            ThreadCB.dispatch();
            // do not create more threads until some threads finished to make more room
            return null;
        }

        ThreadCB newThread = new ThreadCB();
        newThread.setPriority(ThreadCB.PRIORITY_HIGH); // set the new thread priority to high
        newThread.setStatus(ThreadReady);
        newThread.setTask(task);

        // if thread failed to be added, do nothing
        if(task.addThread(newThread) != SUCCESS){
            ThreadCB.dispatch();
            return null;
        }

        // returns everything
        readyQueue.addToQueue(newThread);
        ThreadCB.dispatch();
        return newThread;
    }

    /** 
	Kills the specified thread. 

	The status must be set to ThreadKill, the thread must be
	removed from the task's list of threads and its pending IORBs
	must be purged from all device queues.
        
	If some thread was on the ready queue, it must removed, if the 
	thread was running, the processor becomes idle, and dispatch() 
	must be called to resume a waiting thread.
	
	@OSPProject Threads
    */

    // this means just to kill this thread, all its IOs, and free all its reosurces
    // why the bloody confusing desscription?
    public void do_kill()
    {
        // your code goes here

        switch(this.getStatus()){
            case ThreadReady:
                Object result = readyQueue.removeThread(this);
                if(result == null) {
                    throw new RuntimeException("extream bug in dispathch, thread not found in ready queque"); // crashes the program if ready thready is not found in ready queue, i mean how can this be?
                }
                this.setStatus(ThreadKill);
                break;
            case ThreadRunning:
                MMU.setPTBR(null);
                this.getTask().setCurrentThread(null);
                break;
            default:
                if(this.getStatus() >= ThreadWaiting){
                    this.setStatus(ThreadKill);
                    break;
                }
                // can be waiting or if extreame bug, can be already dead
                System.err.println("Extream bug in do_kill, status is " + this.getStatus());
                break;
        }

        this.setStatus(ThreadKill);
        this.getTask().removeThread(this);

        // close IOs
        for(int i = 0; i < Device.getTableSize(); i++){
            Device.get(i).cancelPendingIO(this);
        }

        ResourceCB.giveupResources(this);

        // kills the task if no more threads
        if(this.getTask().getThreadCount() == 0) this.getTask().kill();
        ThreadCB.dispatch();
    }

    /** Suspends the thread that is currenly on the processor on the 
        specified event. 

        Note that the thread being suspended doesn't need to be
        running. It can also be waiting for completion of a pagefault
        and be suspended on the IORB that is bringing the page in.
	
	Thread's status must be changed to ThreadWaiting or higher,
        the processor set to idle, the thread must be in the right
        waiting queue, and dispatch() must be called to give CPU
        control to some other thread.

	@param event - event on which to suspend this thread.

        @OSPProject Threads
    */
    public void do_suspend(Event event)
    {
        // your code goes here
        switch(this.getStatus()){
            case ThreadRunning:
                MMU.setPTBR(null);
                this.getTask().setCurrentThread(null);
                this.setStatus(ThreadWaiting);
                break;
            case ThreadWaiting:
                this.setStatus(this.getStatus() + 1);
                break;
            default:
                if(this.getStatus() > ThreadWaiting){
                    this.setStatus(this.getStatus() + 1);
                    break;
                }
                // can be dead or ready state which is exream bug
                System.err.println("Extream bug in do_suspend, thread status is " + this.getStatus());
                // do nothing
                break;
        }
        event.addThread(this);
        ThreadCB.readyQueue.removeThread(this);
        ThreadCB.dispatch();
    }

    /** Resumes the thread.
        
	Only a thread with the status ThreadWaiting or higher
	can be resumed.  The status must be set to ThreadReady or
	decremented, respectively.
	A ready thread should be placed on the ready queue.
	
	@OSPProject Threads
    */
    public void do_resume()
    {
        // your code goes here
        if(this.getStatus() < ThreadWaiting){
            System.err.println("Extream bug, thread is not in waiting state");
            ThreadCB.dispatch();
            return;
        }

        if(this.getStatus() > ThreadWaiting){
            this.setStatus(this.getStatus() - 1);
            ThreadCB.dispatch();
            return;
        }

        if(this.getStatus() == ThreadWaiting){
            this.setStatus(ThreadReady);
            readyQueue.addToQueue(this);
            ThreadCB.dispatch();
            return;
        }

    }

    /** 
        Selects a thread from the run queue and dispatches it. 

        If there is just one theread ready to run, reschedule the thread 
        currently on the processor.Fdo

        In addition to setting the correct thread status it must
        update the PTBR.
	
	@return SUCCESS or FAILURE

        @OSPProject Threads
    */
    public static int do_dispatch()
    {
        // your code goes here
        ThreadCB currentThread = null;
        try{
            currentThread = MMU.getPTBR().getTask().getCurrentThread(); // get the thread that is running
        }catch (NullPointerException e){/*ignore*/}

        if(currentThread != null){
            currentThread.getTask().setCurrentThread(null); // rest the currently running thread
            MMU.setPTBR(null); // clear the currently running tread task table
            currentThread.setStatus(ThreadReady);
            readyQueue.addToQueue(currentThread); // add the premmpted running thread back to the ready queque
        }

        // this means no thread in readyQueue
        if(readyQueue.allEmpty()){
            MMU.setPTBR(null);
            return FAILURE;
        }

        ThreadCB nextThread = readyQueue.getNextThread();
        MMU.setPTBR(nextThread.getTask().getPageTable());
        nextThread.setPriority(nextThread.getPriority() + 1); // increaments the priority of the thread so it adds to the right sub queue
        nextThread.getTask().setCurrentThread(nextThread);
        nextThread.setStatus(ThreadRunning);
        HTimer.set(ThreadCB.clockTicks);

        // increament the invocation
        ThreadCB.invocation = (ThreadCB.invocation == 6) ? 1 : ThreadCB.invocation + 1;
        return SUCCESS;
    }

    /**
       Called by OSP after printing an error message. The student can
       insert code here to print various tables and data structures in
       their state just after the error happened.  The body can be
       left empty, if this feature is not used.

       @OSPProject Threads
    */
    public static void atError()
    {
        // your code goes here

    }

    /** Called by OSP after printing a warning message. The student
        can insert code here to print various tables and data
        structures in their state just after the warning happened.
        The body can be left empty, if this feature is not used.
       
        @OSPProject Threads
     */
    public static void atWarning()
    {
        // your code goes here

    }


    /*
       Feel free to add methods/fields to improve the readability of your code
    */
    private static class ReadyQueue{
        private final GenericList Q1 = new GenericList();
        private final GenericList Q2 = new GenericList();
        private final GenericList Q3 = new GenericList();

        public final void addToQueue(ThreadCB thread){
           final int priority = thread.getPriority();
           if(thread.getStatus() != ThreadReady) throw new RuntimeException("Thread is in not ready state"); // crashed the porgram if thread is not in ready state
           if(priority < ThreadCB.PRIORITY_MEDIUM){
               Q1.append(thread);
               return;
           }
           if(priority < ThreadCB.PRIORITY_LOW){
               Q2.append(thread);
               return;
           }
           if(priority >= ThreadCB.PRIORITY_LOW){
               Q3.append(thread);
               return;
           }
           // crashed the program if priorities are not within 1-8 or above
           throw(new RuntimeException("No priorities found for thread"));
        }

        public final ThreadCB getNextThread(){

            do{
                if(ThreadCB.invocation <= 3){
                    if(!Q1.isEmpty()){
                        return ((ThreadCB) Q1.removeHead());
                    }
                    ThreadCB.invocation = 4; // if empty then the Q1 misses all its remaining turns
                }

                if(ThreadCB.invocation <= 5){
                    if(!Q2.isEmpty()){
                        return ((ThreadCB) Q2.removeHead());
                    }
                    ThreadCB.invocation = 6; // if empty then Q2 misses its remaining turns
                }

                if(ThreadCB.invocation == 6){
                    if(!Q3.isEmpty()){
                        return ((ThreadCB) Q3.removeHead());
                    }
                    ThreadCB.invocation = 1; // if empty, then Q1 misses its turn, goes to the next cycle
                }
            }while(!this.allEmpty());


            // here means all queues empty
            return null;
        }

        public final Object removeThread(ThreadCB thread){
            final int priority = thread.getPriority();
            if(priority < ThreadCB.PRIORITY_MEDIUM){
               return Q1.remove(thread);
            }

            if(priority < ThreadCB.PRIORITY_LOW){
                return Q2.remove(thread);
            }

            if(priority >= ThreadCB.PRIORITY_LOW){
                return Q3.remove(thread);
            }

            return null;
        }

        public final boolean allEmpty(){
            return (this.Q1.isEmpty() && this.Q2.isEmpty() && this.Q3.isEmpty());
        }
    }
}

/*
      Feel free to add local classes to improve the readability of your code
*/
