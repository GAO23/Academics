package osp.Memory;

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
    The PageTable class represents the page table for a given task.
    A PageTable consists of an array of PageTableEntry objects.  This
    page table is of the non-inverted type.

    @OSPProject Memory
*/
import java.lang.Math;
import osp.Tasks.*;
import osp.Utilities.*;
import osp.IFLModules.*;
import osp.Hardware.*;

public class PageTable extends IflPageTable
{
    /** 
	The page table constructor. Must call
	
	    super(ownerTask)

	as its first statement.

	@OSPProject Memory
    */
    public PageTable(TaskCB ownerTask)
    {
        // your code goes here
        super(ownerTask);
        int totalPages = (int)Math.pow(2, MMU.getPageAddressBits());
        this.pages = new PageTableEntry[totalPages];
        for(int i = 0; i < totalPages; i++){
            // setting the index as the frame id
            this.pages[i] = new PageTableEntry(this, i);
        }
    }

    /**
       Frees up main memory occupied by the task.
       Then unreserves the freed pages, if necessary.

       @OSPProject Memory
    */
    public void do_deallocateMemory()
    {
        // your code goes here

        // get the task that this page table belongs to
        TaskCB associatedTask = this.getTask();

        // loop through the entire frame table to search for frames that hold the pages of this tasks
        int frameSizes = MMU.getFrameTableSize();
        for(int i = 0; i < frameSizes; i++){
            FrameTableEntry currentFrame = MMU.getFrame(i);
            PageTableEntry currentPage = currentFrame.getPage();

            if(currentPage == null) continue;

            if(currentPage.getTask() != associatedTask) continue;;

            currentFrame.setReferenced(false); // clear the reference bit
            currentFrame.setDirty(false); // clean the frame
            currentFrame.setPage(null); // clear the page of this frame

            if(currentFrame.getReserved() == associatedTask) currentFrame.setUnreserved(associatedTask);

        }

    }


    /*
       Feel free to add methods/fields to improve the readability of your code
    */

}

/*
      Feel free to add local classes to improve the readability of your code
*/
