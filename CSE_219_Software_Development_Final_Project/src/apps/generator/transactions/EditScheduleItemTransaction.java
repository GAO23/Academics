/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package apps.generator.transactions;

import apps.generator.data.GeneratorData;
import apps.generator.data.ScheduleItem;
import apps.generator.workspace.dialogs.customAlert;
import libs.jTPS.jTPS_Transaction;

/**
 *
 * @author gaoxi
 */
public class EditScheduleItemTransaction implements jTPS_Transaction{
    private ScheduleItem original; 
    private ScheduleItem modified;
    private GeneratorData data;
    private ScheduleItem temp;
    
    public EditScheduleItemTransaction (ScheduleItem original, ScheduleItem modified, GeneratorData data){
        this.original = original;
        this.modified = modified;
        this.data = data;
        this.temp = original.clone();
        
    }
    

    @Override
    public void doTransaction() {
        if(!data.chechIfTimeIsInRange(modified.getUnformattedDate())){
            customAlert.showAlert("Updated date is not in range", "Date not in range error");
            return;
        }
        this.original.copyFrom(modified);
        data.refreshesScheduleTable();
    }

    @Override
    public void undoTransaction() {
        if(!data.chechIfTimeIsInRange(temp.getUnformattedDate())){
            customAlert.showAlert("redo date is not in range", "Date not in range error");
            return;
        }
       this.original.copyFrom(temp);
       data.refreshesScheduleTable();
    }
    
}
