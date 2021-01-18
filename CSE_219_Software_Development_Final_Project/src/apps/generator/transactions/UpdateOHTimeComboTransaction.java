/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package apps.generator.transactions;


import apps.generator.data.GeneratorData;
import apps.generator.workspace.GeneratorWorkSpace;
import apps.generator.workspace.controllers.OfficerHoursController;
import javafx.scene.control.ComboBox;
import libs.jTPS.jTPS_Transaction;

/**
 *
 * @author gaoxi
 */
public class UpdateOHTimeComboTransaction implements jTPS_Transaction{
    
    private final ComboBox combo;
    private final String oldValue;
    private final String newValue;
    private final OfficerHoursController controller;
    private final GeneratorWorkSpace.customChangeListener listner;
    private final GeneratorData data;
    
    public UpdateOHTimeComboTransaction(String oldValue, String newValue, ComboBox combo, OfficerHoursController controller, GeneratorWorkSpace.customChangeListener listner, GeneratorData data){
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.controller = controller;
        this.combo = combo;
        this.listner = listner;
        this.data =data;
    }
    
    
    
    
    @Override
    public void doTransaction() {
        combo.valueProperty().removeListener(this.listner);
        if(!combo.getValue().equals(newValue)){
            combo.getSelectionModel().select(newValue);
        }
        controller.updateCombo();
        data.refreshesTimeSlotTable();
        combo.valueProperty().addListener(this.listner);
    }

    @Override
    public void undoTransaction() {
        combo.valueProperty().removeListener(this.listner);
        combo.getSelectionModel().select(oldValue);
        controller.updateCombo();
        data.refreshesTimeSlotTable();
        combo.valueProperty().addListener(this.listner);
    }
    
}
