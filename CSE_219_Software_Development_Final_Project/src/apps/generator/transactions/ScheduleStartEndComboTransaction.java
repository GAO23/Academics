/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package apps.generator.transactions;

import apps.generator.data.GeneratorData;
import apps.generator.workspace.controllers.ScheduleController;
import apps.generator.workspace.dialogs.customAlert;
import java.time.LocalDate;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.DatePicker;
import libs.jTPS.jTPS_Transaction;

/**
 *
 * @author gaoxi
 */
public class ScheduleStartEndComboTransaction implements jTPS_Transaction {

    private LocalDate oldDate;
    private LocalDate newDate;
    private GeneratorData data;
    private DatePicker picker;
    private boolean firstProcess = true;
    private final ChangeListener listener;
    private ScheduleController controller;
    
    public ScheduleStartEndComboTransaction(LocalDate oldDate, LocalDate newDate, DatePicker picker, GeneratorData data, ChangeListener listner, ScheduleController controller){
        this.oldDate = oldDate;
        this.newDate = newDate;
        this.data = data;
        this.picker = picker;
        this.listener = listner;
        this.controller = controller;
    }
    
    
    
    @Override
    public void doTransaction() {
        picker.valueProperty().removeListener(this.listener);
        if(!this.firstProcess){
            this.picker.setValue(newDate);
            if(!data.isStartEndValid()){
                this.picker.setValue(oldDate);
                customAlert.showAlert("Date will not be in range if undo", "Date isn't in range");
                picker.valueProperty().addListener(this.listener);
                return;
            }
        }else{
            this.firstProcess = false;
        }
        data.updateScheduleStartEndSelect();
        picker.valueProperty().addListener(this.listener);
        controller.resetScheduleAddButtonText();
    }

    @Override
    public void undoTransaction() {
       picker.valueProperty().removeListener(this.listener);
       this.picker.setValue(oldDate);
       if(!data.isStartEndValid()){
           customAlert.showAlert("Date will not be in range if undo", "Date isn't in range");
           this.picker.setValue(newDate);
           picker.valueProperty().addListener(this.listener);
           return;
       }
       
       data.updateScheduleStartEndSelect();
       picker.valueProperty().addListener(this.listener);
       this.controller.resetScheduleAddButtonText();
    }
    
    
    
}
