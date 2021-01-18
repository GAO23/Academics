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
    The FrameTableEntry class contains information about a specific page
    frame of memory.

    @OSPProject Memory
*/
import osp.Tasks.*;
import osp.Interrupts.*;
import osp.Utilities.*;
import osp.IFLModules.IflFrameTableEntry;

public class FrameTableEntry extends IflFrameTableEntry
{
    /**
       The frame constructor. Must have

       	   super(frameID)
	   
       as its first statement.

       @OSPProject Memory
    */

    private int useCount = 0;

    public FrameTableEntry(int frameID)
    {
        super(frameID);

    }

    public void decrementUseCount(){
        if(useCount == 0) return;
        useCount -= 1;
    }

    public void increamentUseCount(){
        if(useCount == 2) return;
        useCount += 1;
    }

    public int getUseCount() {
        return useCount;
    }

    /*
       Feel free to add methods/fields to improve the readability of your code
    */

}

/*
      Feel free to add local classes to improve the readability of your code
*/
