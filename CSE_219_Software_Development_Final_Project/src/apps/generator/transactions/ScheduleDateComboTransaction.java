/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package apps.generator.transactions;

import java.time.LocalDate;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.DatePicker;
import libs.jTPS.jTPS_Transaction;

/**
 *
 * @author xgao
 */
public class ScheduleDateComboTransaction implements jTPS_Transaction{
    
    private LocalDate oldDate;
    private LocalDate newDate;
    private DatePicker dateCombo;
    private ChangeListener listener;
    
    public ScheduleDateComboTransaction(LocalDate oldDate, LocalDate newDate, DatePicker dateCombo, ChangeListener listener){
        this.oldDate = oldDate;
        this.newDate = newDate;
        this.dateCombo = dateCombo;
        this.listener = listener;
        
    }

    @Override
    public void doTransaction() {
        if(dateCombo.getValue() != newDate){
            dateCombo.valueProperty().removeListener(listener);
            dateCombo.setValue(newDate);
            dateCombo.valueProperty().addListener(listener);
        }
    }

    @Override
    public void undoTransaction() {
         dateCombo.valueProperty().removeListener(listener);
        dateCombo.setValue(oldDate);
        dateCombo.valueProperty().addListener(listener);
    }
    
}
