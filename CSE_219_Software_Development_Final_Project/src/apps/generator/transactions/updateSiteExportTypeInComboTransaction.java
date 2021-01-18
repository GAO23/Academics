/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package apps.generator.transactions;

import apps.generator.workspace.controllers.SiteController;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import libs.jTPS.jTPS_Transaction;

/**
 *
 * @author gaoxi
 */
public class updateSiteExportTypeInComboTransaction implements jTPS_Transaction{
    private final  String oldValue;
    private final  String newValue;
    private final  SiteController controller;
    private final ComboBox combo;
    private final Object comboID;
    
    public updateSiteExportTypeInComboTransaction(String oldValue, String newValue, Object comboID, ComboBox combo,SiteController controller){
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.controller = controller;
        this.combo = combo;
        this.comboID = comboID;
        
    }

    @Override
    public void doTransaction() {
        ObservableList<String> items = combo.getItems();
        this.handleComboBoxTypeIn();
    }

    @Override
    public void undoTransaction() {
        this.undoComboBoxTypeIn();
    }
    
    
    public void handleComboBoxTypeIn(){
          ObservableList<String> items = combo.getItems();
          if(!this.newValue.equals("") && !items.contains(newValue)){
            items.add(newValue);
          }
          combo.setValue(newValue);
          controller.updateExportLabel();
          controller.handleSaveComboTextFieldOptions(comboID);
      }
    
    public void redoComboBoxTypeIn(){
          ObservableList<String> items = combo.getItems();
          items.add(newValue);
          combo.setValue(newValue);
          controller.updateExportLabel();
          controller.handleSaveComboTextFieldOptions(comboID);
      }
    
    public void undoComboBoxTypeIn(){
          ObservableList<String> items = combo.getItems();
          if(!items.contains(oldValue)){
              items.add(oldValue);
          }
          combo.setValue(oldValue);
          items.remove(newValue);
          controller.updateExportLabel();
          controller.handleSaveComboTextFieldOptions(comboID);
      }
    
    
}
