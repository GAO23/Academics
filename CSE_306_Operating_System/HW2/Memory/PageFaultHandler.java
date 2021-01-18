package osp.Memory;
import java.util.*;
import osp.Hardware.*;
import osp.Threads.*;
import osp.Tasks.*;
import osp.FileSys.FileSys;
import osp.FileSys.OpenFile;
import osp.IFLModules.*;
import osp.Interrupts.*;
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
    The page fault handler is responsible for handling a page
    fault.  If a swap in or swap out operation is required, the page fault
    handler must request the operation.

    @OSPProject Memory
*/
public class PageFaultHandler extends IflPageFaultHandler
{
    /**
        This method handles a page fault. 

        It must check and return if the page is valid, 

        It must check if the page is already being brought in by some other
	thread, i.e., if the page's has already pagefaulted
	(for instance, using getValidatingThread()).
        If that is the case, the thread must be suspended on that page.
        
        If none of the above is true, a new frame must be chosen 
        and reserved until the swap in of the requested 
        page into this frame is complete. 

	Note that you have to make sure that the validating thread of
	a page is set correctly. To this end, you must set the page's
	validating thread using setValidatingThread() when a pagefault
	happens and you must set it back to null when the pagefault is over.

        If a swap-out is necessary (because the chosen frame is
        dirty), the victim page must be dissasociated 
        from the frame and marked invalid. After the swap-in, the 
        frame must be marked clean. The swap-ins and swap-outs 
        must are preformed using regular calls read() and write().

        The student implementation should define additional methods, e.g, 
        a method to search for an available frame.

	Note: multiple threads might be waiting for completion of the
	page fault. The thread that initiated the pagefault would be
	waiting on the IORBs that are tasked to bring the page in (and
	to free the frame during the swapout). However, while
	pagefault is in progress, other threads might request the same
	page. Those threads won't cause another pagefault, of course,
	but they would enqueue themselves on the page (a page is also
	an Event!), waiting for the completion of the original
	pagefault. It is thus important to call notifyThreads() on the
	page at the end -- regardless of whether the pagefault
	succeeded in bringing the page in or not.

        @param thread the thread that requested a page fault
        @param referenceType whether it is memory read or write
        @param page the memory page 

	@return SUCCESS is everything is fine; FAILURE if the thread
	dies while waiting for swap in or swap out or if the page is
	already in memory and no page fault was necessary (well, this
	shouldn't happen, but...). In addition, if there is no frame
	that can be allocated to satisfy the page fault, then it
	should return NotEnoughMemory

        @OSPProject Memory
    */

    public static int do_handlePageFault(ThreadCB thread, 
					 int referenceType,
					 PageTableEntry page)
    {

		// your code goes here

		if(page.isValid()) return FAILURE;
		FrameTableEntry choosenFrame = chooser();

		// shoundt happen
		if(choosenFrame == null) return NotEnoughMemory;

		Event faultingEvent = new SystemEvent("page fault");
		thread.suspend(faultingEvent);
		page.setValidatingThread(thread);
		choosenFrame.setReserved(thread.getTask());

		// swap in if a frame has a dirty page
		PageTableEntry oldPage = choosenFrame.getPage();
		if(oldPage != null){
			if(choosenFrame.isDirty()) {
				TaskCB oldTask = oldPage.getTask();
				oldTask.getSwapFile().write(oldPage.getID(), oldPage, thread);
			}

			if(thread.getStatus() == ThreadKill){
				notifyThread(page, faultingEvent);
				return FAILURE;
			}

			cleanFrame(choosenFrame, oldPage);
		}

		page.setFrame(choosenFrame);
		TaskCB oldTask = page.getTask();
		oldTask.getSwapFile().read(page.getID(), page, thread);
		if(thread.getStatus() == ThreadKill) {
			notifyThread(page, faultingEvent);
			return FAILURE;
		}

		choosenFrame.setPage(page);
		page.setValid(true);
		choosenFrame.setUnreserved(thread.getTask());
		page.setValidatingThread(null);
		notifyThread(page, faultingEvent);
		return SUCCESS;
    }

    // initialize the daemon
    static void init(){
    	System.err.println("initing the page handler\n");
    	// registers the daemons
		Daemon.create("cleaner daemon", cleanerFunction, 4000);
	}

	// this is our first hand, the sweeper
	private static DaemonInterface cleanerFunction = (ThreadCB thread)->{
		int size = MMU.getFrameTableSize();
		for(int i = 0; i < size; i++){
			FrameTableEntry frame = MMU.getFrame(i);
			frame.decrementUseCount();
		}
	};


	static FrameTableEntry chooser(){
    	// select a free frame first
		for(int i = 0; i < MMU.getFrameTableSize(); i++){
			FrameTableEntry candidateFrame = MMU.getFrame(i);
			if (candidateFrame.getLockCount() == 0 && !candidateFrame.isReserved() && candidateFrame.getPage() == null) return candidateFrame;
		}

		// if no free frame, select a clean frame
		for(int i = 0; i < MMU.getFrameTableSize(); i++){
			FrameTableEntry candidateFrame = MMU.getFrame(i);
			if (candidateFrame.getLockCount() == 0 && !candidateFrame.isReserved() && !candidateFrame.isDirty()) return candidateFrame;
		}

		// if no free frame, select a dirty frame with user count of 0
		for(int i = 0; i < MMU.getFrameTableSize(); i++){
			FrameTableEntry candidateFrame = MMU.getFrame(i);
			if (candidateFrame.getLockCount() == 0 && !candidateFrame.isReserved() && candidateFrame.getUseCount() == 0) return candidateFrame;
		}

		// if no dirty, we choose any frame at will
		for(int i = 0; i < MMU.getFrameTableSize(); i++){
			FrameTableEntry candidateFrame = MMU.getFrame(i);
			if (candidateFrame.getLockCount() == 0 && !candidateFrame.isReserved()) return candidateFrame;
		}

		return null;
	}






    /*
       Feel free to add methods/fields to improve the readability of your code
    */

    static void notifyThread(PageTableEntry page, Event faultingEvent){
		page.notifyThreads();
		faultingEvent.notifyThreads();
		ThreadCB.dispatch();
	}

	static void cleanFrame(FrameTableEntry frame, PageTableEntry page){
		frame.setDirty(false);
		frame.setReferenced(false);
		frame.setPage(null);
		page.setValid(false);
		page.setFrame(null);
	}


}