package osp.Devices;

import org.omg.IOP.IOR;
import osp.Devices.IORB;
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
    This class stores all pertinent information about a device in
    the device table.  This class should be sub-classed by all
    device classes, such as the Disk class.

    @OSPProject Devices
*/

import osp.IFLModules.*;
import osp.Threads.*;
import osp.Hardware.*;
import osp.Memory.*;
import osp.FileSys.*;
import osp.Utilities.GenericList;

import java.util.*;

public class Device extends IflDevice
{
    /**
        This constructor initializes a device with the provided parameters.
	As a first statement it must have the following:

	    super(id,numberOfBlocks);

	@param numberOfBlocks -- number of blocks on device

        @OSPProject Devices
    */

    QueueManager queueManager = new QueueManager();

    public Device(int id, int numberOfBlocks)
    {
        // your code goes here
        super(id, numberOfBlocks);
        iorbQueue = new GenericList();
    }

    /**
       This method is called once at the beginning of the
       simulation. Can be used to initialize static variables.

       @OSPProject Devices
    */
    public static void init()
    {
        // your code goes here
    }

    /**
       Enqueues the IORB to the IORB queue for this device
       according to some kind of scheduling algorithm.
       
       This method must lock the page (which may trigger a page fault),
       check the device's state and call startIO() if the 
       device is idle, otherwise append the IORB to the IORB queue.

       @return SUCCESS or FAILURE.
       FAILURE is returned if the IORB wasn't enqueued 
       (for instance, locking the page fails or thread is killed).
       SUCCESS is returned if the IORB is fine and either the page was 
       valid and device started on the IORB immediately or the IORB
       was successfully enqueued (possibly after causing pagefault pagefault)
       
       @OSPProject Devices
    */
    public int do_enqueueIORB(osp.Devices.IORB iorb)
    {
        // your code goes here
        OpenFile file = iorb.getOpenFile();
        PageTableEntry page = iorb.getPage();

        int result = page.lock(iorb);
        if(result == FAILURE) return FAILURE;
        file.incrementIORBCount();
        this.setCyclinder(iorb);

        ThreadCB thread = iorb.getThread();
        if(thread.getStatus() == ThreadKill) return FAILURE;

        if(this.isBusy()){
            queueManager.enqueue(iorb);
            ((GenericList) this.iorbQueue).append(iorb);
            return SUCCESS;
        }

        this.startIO(iorb);
        return SUCCESS;
    }

    /**
       Selects an IORB (according to some scheduling strategy)
       and dequeues it from the IORB queue.

       @OSPProject Devices
    */
    public IORB do_dequeueIORB()
    {
//         your code goes here

        IORB iorb = queueManager.deQueue();
        ((GenericList) this.iorbQueue).remove(iorb);
        return iorb;
    }

    /**
        Remove all IORBs that belong to the given ThreadCB from 
	this device's IORB queue

        The method is called when the thread dies and the I/O 
        operations it requested are no longer necessary. The memory 
        page used by the IORB must be unlocked and the IORB count for 
	the IORB's file must be decremented.

	@param thread thread whose I/O is being canceled

        @OSPProject Devices
    */
    public void do_cancelPendingIO(ThreadCB thread){
        // your code goes here

        IORB iorb = this.getIORB(thread);
        if(iorb == null) return;
        PageTableEntry page = iorb.getPage();
        page.unlock();
        OpenFile file = iorb.getOpenFile();
        file.decrementIORBCount();
        if (file.closePending && file.getIORBCount() == 0) file.close();
        ((GenericList)this.iorbQueue).remove(iorb);
        queueManager.cancel(iorb);
    }

    /** Called by OSP after printing an error message. The student can
	insert code here to print various tables and data structures
	in their state just after the error happened.  The body can be
	left empty, if this feature is not used.
	
	@OSPProject Devices
     */
    public static void atError()
    {
        // your code goes here
//        System.out.println(queueManager.toString());

    }

    /** Called by OSP after printing a warning message. The student
	can insert code here to print various tables and data
	structures in their state just after the warning happened.
	The body can be left empty, if this feature is not used.
	
	@OSPProject Devices
     */
    public static void atWarning()
    {
        // your code goes here
//        System.out.println(queueManager.toString());

    }


    /*
       Feel free to add methods/fields to improve the readability of your code
    */

    private IORB getIORB(ThreadCB thread){
        for (int i = this.iorbQueue.length() - 1; i >= 0; i--) {
            IORB iorb = (IORB) ((GenericList) this.iorbQueue).getAt(i);
            if (iorb.getThread().equals(thread)) {
                return iorb;
            }
        }
        return null;
    }

    private void setCyclinder(osp.Devices.IORB iorb){
        int blockNum = iorb.getBlockNumber();
        int totalBytes = ((Disk) this).getSectorsPerTrack() * ((Disk) this).getBytesPerSector();
        int addrLength = (int) Math.pow(2, MMU.getVirtualAddressBits() - MMU.getPageAddressBits());
        int blocksInTrack = totalBytes / addrLength;
        iorb.setCylinder(blockNum / (blocksInTrack * ((Disk) this).getPlatters()));
    }

    // we do 5 queues, should be enough
    private static class QueueManager {
        private int currentUnlockedQueue = 0;
        HashMap<Integer, ArrayList<osp.Devices.IORB>> map = new HashMap<>();
        private boolean[] lockedQueues = new boolean[5];

        public QueueManager(){
            for(int i = 0; i < 5; i++){
                lockedQueues[i] = false;
                map.put(i, new ArrayList<osp.Devices.IORB>());
            }
        }

        // insert according to the distance to the next track
        public void enqueue(osp.Devices.IORB iorb){
            if(lockedQueues[currentUnlockedQueue] == true) throw new RuntimeException("Severed Bug: currentUnlockedQueue is lock!\n");
            List<osp.Devices.IORB> list = map.get(currentUnlockedQueue);
            if(list.isEmpty()){
                list.add(iorb);
                return;
            }
            for(int i = 0; i < list.size(); i++){
                int currentPosition = list.get(i).getCylinder();
                int iorbPosition = iorb.getCylinder();
                if(iorbPosition < currentPosition){
                    list.add(i, iorb);
                    return;
                }
            }
            list.add(iorb);
        }

        public void cancel(IORB iorb){
            for (Map.Entry<Integer, ArrayList<osp.Devices.IORB>> entry : map.entrySet()){
                List<IORB> list = entry.getValue();
                int key = entry.getKey();
                if(list.remove(iorb)){
                    if(list.isEmpty()) lockedQueues[key] = false;
                    return;
                }
            }
        }

        private void lockedCurrentQueue(){
            this.lockedQueues[currentUnlockedQueue] = true;
            currentUnlockedQueue = (currentUnlockedQueue == 4) ? 0 : currentUnlockedQueue + 1;
        }

        public osp.Devices.IORB deQueue(){
            List<osp.Devices.IORB> list = null;
            int i = 0;
            for(; i < 5; i++){
                if(lockedQueues[i]){
                    list = map.get(i);
                    break;
                }
            }

            // get the next closest track
            if(list != null ){
                osp.Devices.IORB iorb = list.remove(0);
                if(list.isEmpty())lockedQueues[i] = false;
                return iorb;
            }

            list = map.get(currentUnlockedQueue);
            if(list.isEmpty()) {
//                System.err.println("No valid IORB in dequeue\n");
                return null;
            }

            int old_current = currentUnlockedQueue;
            this.lockedCurrentQueue();
            osp.Devices.IORB iorb = list.remove(0);
            if(list.isEmpty())lockedQueues[old_current] = false;
            return iorb;
        }

        @Override
        public String toString() {
            String retval = "";
            for (Map.Entry<Integer, ArrayList<osp.Devices.IORB>> entry : map.entrySet()){
                int key = entry.getKey();
                List<IORB> list = entry.getValue();
                retval += "\n-----------------------------Now printing queue " + key + "-----------------------------\n";
                retval += "queue locked = " + lockedQueues[key] + "\n";
                for(IORB element: list){
                    retval += element.toString() + "\n";
                }
            }
            return retval;
        }

    }

}

/*
      Feel free to add local classes to improve the readability of your code
*/
