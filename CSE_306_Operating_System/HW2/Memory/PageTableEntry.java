package osp.Memory;

import osp.Hardware.*;
import osp.Tasks.*;
import osp.Threads.*;
import osp.Devices.*;
import osp.Utilities.*;
import osp.IFLModules.*;

/**
 * • Xiangshuai Gao
 * • 110930549
 * I pledge my honor that all parts of this project were done by me individually,
 * without collaboration with anyone, and without consulting any external sources
 * that provide full or partial solutions to a similar project.
 * I understand that breaking this pledge will result in an “F” for the entire
 * course.
 */


/**
   The PageTableEntry object contains information about a specific virtual
   page in memory, including the page frame in which it resides.
   
   @OSPProject Memory

*/

public class PageTableEntry extends IflPageTableEntry
{
    /**
       The constructor. Must call

       	   super(ownerPageTable,pageNumber);
	   
       as its first statement.

       @OSPProject Memory
    */
    public PageTableEntry(PageTable ownerPageTable, int pageNumber)
    {
        // your code goes here
        super(ownerPageTable, pageNumber);

    }

    /**
       This method increases the lock count on the page by one. 

	The method must FIRST increment lockCount, THEN  
	check if the page is valid, and if it is not and no 
	page validation event is present for the page, start page fault 
	by calling PageFaultHandler.handlePageFault().

	@return SUCCESS or FAILURE
	FAILURE happens when the pagefault due to locking fails or the 
	that created the IORB thread gets killed.

	@OSPProject Memory
     */
    public int do_lock(IORB iorb)
    {

        // your code goes here
        boolean valid = this.isValid();
        ThreadCB faultingThread = this.getValidatingThread();
        ThreadCB ioThread = iorb.getThread();

        if(!valid){
            if(faultingThread == null){
                PageFaultHandler.handlePageFault(ioThread, MemoryLock, this);
            }else if(faultingThread != ioThread){
                ioThread.suspend(this);
                if(ioThread.getStatus() == ThreadKill) {
                    return FAILURE;
                }
            }
        }

        this.getFrame().incrementLockCount();
        return SUCCESS;
    }

    /** This method decreases the lock count on the page by one. 

	This method must decrement lockCount, but not below zero.

	@OSPProject Memory
    */
    public void do_unlock()
    {
        // your code goes here
       int lockCount = this.getFrame().getLockCount();
       if(lockCount <= 0){
           System.err.println("do unlock has 0 or less lock count");
           return;
       }
       this.getFrame().decrementLockCount();
    }


    /*
       Feel free to add methods/fields to improve the readability of your code
    */

}

/*
      Feel free to add local classes to improve the readability of your code
*/
