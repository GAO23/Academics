/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package apps.generator.transactions;

import javafx.beans.value.ChangeListener;
import javafx.scene.control.ComboBox;
import libs.jTPS.jTPS_Transaction;

/**
 *
 * @author xgao
 */
public class generalComboBoxSelectTransaction implements jTPS_Transaction{
    
    private final String oldValue;
    private final String newValue;
    private final ComboBox combo;
    private final ChangeListener listener;
    
    
   public  generalComboBoxSelectTransaction(String oldValue, String newValue, ComboBox combo, ChangeListener listener){
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.combo = combo;
        this.listener = listener;
   }

    @Override
    public void doTransaction() {
        if(!combo.getValue().toString().equals(newValue)){
            combo.valueProperty().removeListener(listener);
            combo.setValue(newValue);
            combo.valueProperty().addListener(listener);
        }
    }

    @Override
    public void undoTransaction() {
        combo.valueProperty().removeListener(listener);
       combo.setValue(oldValue);
       combo.valueProperty().addListener(listener);
    }
    
    
    
}
