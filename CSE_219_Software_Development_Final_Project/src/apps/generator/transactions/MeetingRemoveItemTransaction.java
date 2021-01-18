/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package apps.generator.transactions;

import apps.generator.data.GeneratorData;
import javafx.collections.ObservableList;
import libs.jTPS.jTPS_Transaction;

/**
 *
 * @author xgao
 */
public class MeetingRemoveItemTransaction implements jTPS_Transaction{
    private Object item;
    private ObservableList itemList;
    private GeneratorData data;
    private int index;
    
    public MeetingRemoveItemTransaction(Object item, ObservableList itemList, GeneratorData data){
            this.item = item;
            this.itemList = itemList;
            this.data = data;
            this.index = itemList.indexOf(item);
    }

    @Override
    public void doTransaction() {
       itemList.remove(item);
       data.refreshesAllMeetingTables();
    }

    @Override
    public void undoTransaction() {
        this.addToIndex();
        data.refreshesAllMeetingTables();
    }
    
    public void addToIndex(){
        if(itemList.size()>index){
            itemList.add(index, item);
        }else{
            itemList.add(item);
        }
    }
    
    

    
}
