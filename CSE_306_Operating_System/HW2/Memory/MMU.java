package osp.Memory;

import java.util.*;
import osp.IFLModules.*;
import osp.Threads.*;
import osp.Tasks.*;
import osp.Utilities.*;
import osp.Hardware.*;
import osp.Interrupts.*;

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
    The MMU class contains the student code that performs the work of
    handling a memory reference.  It is responsible for calling the
    interrupt handler if a page fault is required.

    @OSPProject Memory
*/
public class MMU extends IflMMU
{
    /** 
        This method is called once before the simulation starts. 
	Can be used to initialize the frame table and other static variables.

        @OSPProject Memory
    */




    public static void init()
    {
        // your code goes here
        for(int i = 0; i < MMU.getFrameTableSize(); i++){
            FrameTableEntry frame = new FrameTableEntry(i);
            MMU.setFrame(i, frame);
        }

        PageFaultHandler.init();
    }

    /**
       This method handlies memory references. The method must 
       calculate, which memory page contains the memoryAddress,
       determine, whether the page is valid, start page fault 
       by making an interrupt if the page is invalid, finally, 
       if the page is still valid, i.e., not swapped out by another 
       thread while this thread was suspended, set its frame
       as referenced and then set it as dirty if necessary.
       (After pagefault, the thread will be placed on the ready queue, 
       and it is possible that some other thread will take away the frame.)
       
       @param memoryAddress A virtual memory address
       @param referenceType The type of memory reference to perform 
       @param thread that does the memory access
       (e.g., MemoryRead or MemoryWrite).
       @return The referenced page.

       @OSPProject Memory
    */
    static public PageTableEntry do_refer(int memoryAddress, int referenceType, ThreadCB thread)
    {
        // your code goes here
        int pageSize = (int) Math.pow(2, MMU.getVirtualAddressBits() - MMU.getPageAddressBits());
        PageTableEntry pageTableEntry =  MMU.getPTBR().pages[memoryAddress / pageSize];

        // takes care of valid frame
        if(pageTableEntry.isValid()){
            cleanse(pageTableEntry.getFrame(), referenceType);
            return pageTableEntry;
        }

        // rest are invalid frames
        ThreadCB faultingThread = pageTableEntry.getValidatingThread();
        // if the page faulting thread is not null
        if(faultingThread != null){
            thread.suspend(pageTableEntry);
            if(thread.getStatus() != ThreadKill){
                cleanse(pageTableEntry.getFrame(), referenceType);
            }
        }else{ // if null
            InterruptVector.setPage(pageTableEntry);
            InterruptVector.setReferenceType(referenceType);
            InterruptVector.setThread(thread);
            CPU.interrupt(PageFault);
            if(thread.getStatus() != ThreadKill){
                cleanse(pageTableEntry.getFrame(), referenceType);
            }
        }

        // finally return
        return pageTableEntry;

    }

    static void cleanse(FrameTableEntry frame, int referenceType){
        frame.setReferenced(true);
        if (referenceType == MemoryWrite) {
            frame.setDirty(true);
        }
        // increament use count
        frame.increamentUseCount();
    }

    /** Called by OSP after printing an error message. The student can
	insert code here to print various tables and data structures
	in their state just after the error happened.  The body can be
	left empty, if this feature is not used.
     
	@OSPProject Memory
     */
    public static void atError()
    {
        // your code goes here

    }

    /** Called by OSP after printing a warning message. The student
	can insert code here to print various tables and data
	structures in their state just after the warning happened.
	The body can be left empty, if this feature is not used.
     
      @OSPProject Memory
     */
    public static void atWarning()
    {
        // your code goes here

    }


    /*
       Feel free to add methods/fields to improve the readability of your code
    */



}

/*
      Feel free to add local classes to improve the readability of your code
*/
