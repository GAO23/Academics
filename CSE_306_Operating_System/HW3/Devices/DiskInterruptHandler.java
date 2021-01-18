package osp.Devices;
import java.util.*;

import osp.Devices.Device;
import osp.IFLModules.*;
import osp.Hardware.*;
import osp.Interrupts.*;
import osp.Threads.*;
import osp.Utilities.*;
import osp.Tasks.*;
import osp.Memory.*;
import osp.FileSys.*;

/**
 * Xiangshuai Gao
 * 110930549
 *
 * I pledge my honor that all parts of this project were done by me individually,
 * 2
 * without collaboration with anyone, and without consulting any external sources
 * that provide full or partial solutions to a similar project.
 * I understand that breaking this pledge will result in an “F” for the entire
 * course.
 */



/**
    The disk interrupt handler.  When a disk I/O interrupt occurs,
    this class is called upon the handle the interrupt.

    @OSPProject Devices
*/
public class DiskInterruptHandler extends IflDiskInterruptHandler
{
    /** 
        Handles disk interrupts. 
        
        This method obtains the interrupt parameters from the 
        interrupt vector. The parameters are IORB that caused the 
        interrupt: (IORB)InterruptVector.getEvent(), 
        and thread that initiated the I/O operation: 
        InterruptVector.getThread().
        The IORB object contains references to the memory page 
        and open file object that participated in the I/O.
        
        The method must unlock the page, set its IORB field to null,
        and decrement the file's IORB count.
        
        The method must set the frame as dirty if it was memory write 
        (but not, if it was a swap-in, check whether the device was 
        SwapDevice)

        As the last thing, all threads that were waiting for this 
        event to finish, must be resumed.

        @OSPProject Devices 
    */
    public void do_handleInterrupt()
    {

        osp.Devices.IORB iorb = (osp.Devices.IORB) InterruptVector.getEvent();
        OpenFile file = iorb.getOpenFile();

        file.decrementIORBCount();

        // close the file
        if(file.closePending && file.getIORBCount() == 0) file.close();

        PageTableEntry page = iorb.getPage();
        page.unlock();

        ThreadCB thread = iorb.getThread();
        TaskCB task = thread.getTask();
        FrameTableEntry frame = page.getFrame();
        if(task.getStatus() != TaskTerm) {
            if(iorb.getDeviceID() != SwapDeviceID && thread.getStatus() != ThreadCB.ThreadKill) {
                frame.setReferenced(true);
                if(iorb.getIOType() == FileRead)frame.setDirty(true);
            }
            else {
                frame.setDirty(false);
            }
        }

        int deviceId = iorb.getDeviceID();
        hadnleFrame(task, frame, iorb, thread);

        if (task.getStatus() == GlobalVariables.TaskTerm && frame.isReserved()) frame.setUnreserved(task);

        iorb.notifyThreads();
        Device device = Device.get(deviceId);
        device.setBusy(false);
        osp.Devices.IORB newIorb = device.dequeueIORB();
        if(newIorb != null) device.startIO(newIorb);
        ThreadCB.dispatch();
    }


    /*
       Feel free to add methods/fields to improve the readability of your code
    */

    private void hadnleFrame(TaskCB task, FrameTableEntry frame, osp.Devices.IORB iorb, ThreadCB thread){
        if(task.getStatus() == TaskTerm){
            frame.setDirty(false);
            return;
        }

        if(iorb.getDeviceID() != SwapDeviceID && thread.getStatus() != ThreadCB.ThreadKill) {
                frame.setReferenced(true);
                if(iorb.getIOType() == FileRead)frame.setDirty(true);
        }
    }

}

/*
      Feel free to add local classes to improve the readability of your code
*/
