/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package apps.generator.transactions;

import apps.generator.workspace.controllers.SiteController;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.ComboBox;
import libs.jTPS.jTPS_Transaction;

/**
 *
 * @author gaoxi
 */
public class updateSiteExportComboTransaction implements jTPS_Transaction{
    private final ComboBox combo;
    private final String oldValue;
    private final String newValue;
    private final SiteController controller;
    private final ChangeListener listner;
    
    public updateSiteExportComboTransaction(String oldValue, String newValue, ComboBox combo, SiteController controller, ChangeListener listner){
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.controller = controller;
        this.combo = combo;
        this.listner = listner;
    }

    @Override
    public void doTransaction() {
        if(!combo.getValue().equals(newValue)){
            combo.valueProperty().removeListener(this.listner);
            combo.getSelectionModel().select(newValue);
            combo.valueProperty().addListener(this.listner);
        }
        controller.updateExportLabel();
    }

    @Override
    public void undoTransaction() {
        combo.valueProperty().removeListener(this.listner);
        combo.getSelectionModel().select(oldValue);
        controller.updateExportLabel();
        combo.valueProperty().addListener(this.listner);
    }
    
    
    
    
    
    
}
