/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package apps.generator.transactions;

import javafx.scene.control.TextInputControl;
import libs.jTPS.jTPS_Transaction;

/**
 *
 * @author xgao
 */
public class GeneralTextChangeTransaction implements jTPS_Transaction{
    
    private final TextInputControl control;
    private final String oldValue;
    private final String newValue;
    
    public GeneralTextChangeTransaction(String oldValue, String newValue, TextInputControl control){
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.control = control;
    }

    @Override
    public void doTransaction() {
        if(!this.control.getText().equals(newValue)){
            this.control.setText(newValue);
        }
    }

    @Override
    public void undoTransaction() {
       this.control.setText(oldValue);
    }
    
}
