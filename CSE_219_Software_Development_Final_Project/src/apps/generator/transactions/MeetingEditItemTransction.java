/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package apps.generator.transactions;

import apps.generator.data.GeneratorData;
import apps.generator.data.Lab;
import apps.generator.data.Lecture;
import apps.generator.data.MeetingItemEditOptions;
import apps.generator.data.Recitation;
import libs.jTPS.jTPS_Transaction;

/**
 *
 * @author xgao
 */
public class MeetingEditItemTransction implements jTPS_Transaction{
    private final String oldValue, newValue;
    private final Object item;
    private final int transactionType;
    private final int itemType;
    private final GeneratorData data;
    
    
    public MeetingEditItemTransction(String oldValue, String newValue, Object item, int itemType, int transactionType, GeneratorData data){
            this.newValue = newValue;
            this.oldValue = oldValue;
            this.item = item;
            this.itemType = itemType;
            this.transactionType = transactionType;
            this.data = data;
    }

    @Override
    public void doTransaction() {
       switch(this.itemType){
           case MeetingItemEditOptions.LECTURE_ITEM: this.handleLectureEdit(false);
           break;
           case MeetingItemEditOptions.RECITATION_ITEM: this.handleRecitationEdit(false);
           break;
           case MeetingItemEditOptions.LAB_ITEM: this.handleLabEdit(false);
           break;
       }
       
    }

    @Override
    public void undoTransaction() {
       switch(this.itemType){
           case MeetingItemEditOptions.LECTURE_ITEM: this.handleLectureEdit(true);
           break;
           case MeetingItemEditOptions.RECITATION_ITEM: this.handleRecitationEdit(true);
           break;
           case MeetingItemEditOptions.LAB_ITEM: this.handleLabEdit(true);
           break;
       }
    }
    
    public void handleLectureEdit(boolean undo){
        Lecture lecture = (Lecture) this.item;
        switch(this.transactionType){
            case MeetingItemEditOptions.EDIT_SECTION: if(undo) lecture.setSection(oldValue);  else lecture.setSection(newValue);
                break;
                case MeetingItemEditOptions.EDIT_DAY: if(undo) lecture.setDay(oldValue); else lecture.setDay(newValue);
                break;
                case MeetingItemEditOptions.EDIT_TIME: if(undo) lecture.setTime(oldValue); else lecture.setTime(newValue);
                break;
                case MeetingItemEditOptions.EDIT_ROOM:  if(undo) lecture.setRoom(oldValue); else lecture.setRoom(newValue);
                break;
        }    
        data.refreshLectureTable();
                
    }
    
      public void handleRecitationEdit(boolean undo){
        Recitation recitation = (Recitation) this.item;
        switch(this.transactionType){
                case MeetingItemEditOptions.EDIT_SECTION: if(undo) recitation.setSection(oldValue); else recitation.setSection(newValue);
                break;
                case MeetingItemEditOptions.EDIT_DAY: if(undo) recitation.setDay(oldValue); else recitation.setDay(newValue);
                break;
                case MeetingItemEditOptions.EDIT_ROOM:  if(undo) recitation.setRoom(oldValue); else recitation.setRoom(newValue);
                break;
                case MeetingItemEditOptions.EDIT_TA1:  if(undo) recitation.setTa1(oldValue); else recitation.setTa1(newValue);
                break;
                case MeetingItemEditOptions.EDIT_TA2:  if(undo) recitation.setTa2(oldValue); else recitation.setTa2(newValue);
                break;
        }    
        data.refreshRecitationTable();
                
    }
      
      public void handleLabEdit(boolean undo){
        Lab lab = (Lab) this.item;
        switch(this.transactionType){
                case MeetingItemEditOptions.EDIT_SECTION: if(undo) lab.setSection(oldValue); else lab.setSection(newValue);
                break;
                case MeetingItemEditOptions.EDIT_DAY: if(undo) lab.setDay(oldValue); else lab.setDay(newValue);
                break;
                case MeetingItemEditOptions.EDIT_ROOM:  if(undo) lab.setRoom(oldValue); else lab.setRoom(newValue);
                break;
                case MeetingItemEditOptions.EDIT_TA1:  if(undo) lab.setTa1(oldValue); else lab.setTa1(newValue);
                break;
                case MeetingItemEditOptions.EDIT_TA2:  if(undo) lab.setTa2(oldValue); else lab.setTa2(newValue);
                break;
        }    
                data.refreshLabTable();
    }
    
    
    
    
}
