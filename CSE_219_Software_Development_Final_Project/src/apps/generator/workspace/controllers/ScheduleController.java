/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package apps.generator.workspace.controllers;

import apps.generator.GeneratorApp;
import apps.generator.GeneratorProperty;
import apps.generator.data.GeneratorData;
import apps.generator.data.ScheduleItem;
import apps.generator.workspace.dialogs.customAlert;
import apps.generator.transactions.ClearScheduleTransaction;
import libs.DesktopJavaFramework.modules.AppGUIModule;
import apps.generator.transactions.AddScheduleItemTransaction;
import apps.generator.transactions.EditScheduleItemTransaction;
import apps.generator.transactions.GeneralTextChangeTransaction;
import apps.generator.transactions.ScheduleDateComboTransaction;
import apps.generator.transactions.ScheduleStartEndComboTransaction;
import apps.generator.transactions.generalComboBoxSelectTransaction;
import apps.generator.transactions.removeScheduleTransaction;
import apps.generator.workspace.GeneratorWorkSpace;

import java.time.LocalDate;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import libs.PropertiesManager.src.PropertiesManager;

/**
 *
 * @author xgao
 */
public class ScheduleController implements GeneratorWorkSpace.TextEditController{
    GeneratorApp app;
    
    public ScheduleController(GeneratorApp app){
        this.app = app;
    }
    
    
    public void proccessRemove(){
         GeneratorData data = (GeneratorData) app.getDataComponent();
         TableView table = (TableView) app.getGUIModule().getGUINode(GeneratorProperty.GENERATOR_SCHEDULE_ITEM_TABLE);
         Object item = table.getSelectionModel().getSelectedItem();
         if(item == null){
             customAlert.showAlert("You must select an item first", "No Item selected");
         }
         app.processTransaction(new removeScheduleTransaction((ScheduleItem)item, data, this));
    }

    @Override
    public void processTextChange(String oldValue, String newValue, TextInputControl control) {
        app.processTransaction(new GeneralTextChangeTransaction(oldValue, newValue, control));
    }
    
    public void proccessGeneralComboSelect(String oldValue, String newValue, Object comboID, ChangeListener listener){
            ComboBox combo = (ComboBox) app.getGUIModule().getGUINode(comboID);
            app.processTransaction(new generalComboBoxSelectTransaction(oldValue, newValue, combo, listener));
      }
    
    public void proccessScheduleDateCombo(String oldDate, String newDate, DatePicker dateCombo, ChangeListener listener){
        LocalDate old = this.formateDateToLocalDate(oldDate);
        LocalDate modfied = this.formateDateToLocalDate(newDate);
        app.processTransaction(new ScheduleDateComboTransaction(old, modfied, dateCombo,  listener));
    }
    
    public LocalDate formateDateToLocalDate(String date){
        LocalDate localDate = LocalDate.parse(date);
        return localDate;
    }
    
    public void proccessAddUpdateScheduleItem(){
         AppGUIModule gui = app.getGUIModule();
         GeneratorData data = (GeneratorData) app.getDataComponent();
         ComboBox combo = (ComboBox) gui.getGUINode(GeneratorProperty.GENERATOR_SCHEDULE_TYPE_COMBOBOX);
         DatePicker dateCombo = (DatePicker) gui.getGUINode(GeneratorProperty.GENERATOR_SCHEDULE_DATE_COMBOBOX);
         TextField title = (TextField) app.getGUIModule().getGUINode(GeneratorProperty.GENERATOR_SCHEDULE_TITLE_TEXTFIELD);
         TextField topic = (TextField) app.getGUIModule().getGUINode(GeneratorProperty.GENERATOR_SCHEDULE_TOPIC_TEXTFIELD);
         TextField link = (TextField) app.getGUIModule().getGUINode(GeneratorProperty.GENERATOR_SCHEDULE_LINK_TEXTFIELD);
         ScheduleItem item = new ScheduleItem(combo.getValue().toString(), dateCombo.getValue().toString(), title.getText(), topic.getText(), link.getText());
         
        PropertiesManager manager = PropertiesManager.getPropertiesManager();
        Button addButton = (Button) gui.getGUINode(GeneratorProperty.GENERATOR_SCHEDULE_ADD_BUTTON);
        
        if(addButton.getText().equals(manager.getProperty(GeneratorProperty.GENERATOR_SCHEDULE_ADD_BUTTON.toString()+"_TEXT"))){
            this.handleAdd(data, item);
        }
        else{
            if(!data.chechIfTimeIsInRange(item.getUnformattedDate())){
                 customAlert.showAlert("Updated date is not in range", "Date not in range error");
                 return;
             }
            this.handleUpdate(item, data, gui);
        }
         
         this.resetScheduleAddButtonText();
    }
    
    private void handleAdd(GeneratorData data, ScheduleItem item){
        if(!data.chechIfTimeIsInRange(item.getUnformattedDate())){
             customAlert.showAlert("The choosen time is outside the selected date range", "Invalid date");
             return;
         }
        app.processTransaction(new AddScheduleItemTransaction(item, data, this));
    }
    
    private void handleUpdate(ScheduleItem modified, GeneratorData data, AppGUIModule gui){
        TableView table = (TableView) gui.getGUINode(GeneratorProperty.GENERATOR_SCHEDULE_ITEM_TABLE);
        ScheduleItem original = (ScheduleItem) table.getSelectionModel().getSelectedItem();
        
        if(original.sameAs(modified)){
            customAlert.showAlert("None of the entry is modfied", "No changes detetced");
            return;
        }
        app.processTransaction(new EditScheduleItemTransaction(original, modified, data));
    }
    
    
    public void proccessClearShecduleItem(){
        app.processTransaction(new ClearScheduleTransaction(app, this));
    }
    
     public void resetScheduleAddButtonText(){
        PropertiesManager manager = PropertiesManager.getPropertiesManager();
        Button addButton = (Button) app.getGUIModule().getGUINode(GeneratorProperty.GENERATOR_SCHEDULE_ADD_BUTTON);
        TableView table = (TableView) app.getGUIModule().getGUINode(GeneratorProperty.GENERATOR_SCHEDULE_ITEM_TABLE);
         Object item = table.getSelectionModel().getSelectedItem();
         if(item == null){
             addButton.setText(manager.getProperty(GeneratorProperty.GENERATOR_SCHEDULE_ADD_BUTTON.toString()+"_TEXT"));
         }else{
             addButton.setText(manager.getProperty(GeneratorProperty.GENERATOR_SCHEDULE_UPDATE_BUTTON.toString()+"_TEXT"));
         }
        
    }
     
     public void proccessStartEndComboSelect(String before, String after, ChangeListener listener, DatePicker picker){
         GeneratorData data = (GeneratorData) app.getDataComponent();
         LocalDate oldDate = this.formateDateToLocalDate(before);
         LocalDate newDate = this.formateDateToLocalDate(after);
         if(!data.isStartEndValid()){
             customAlert.showAlert("The End date must be later or at the same time as starting date", "Invalid start/end selection");
             Platform.runLater((new Runnable() {
                @Override
                public void run() {
                    picker.valueProperty().removeListener(listener);
                    picker.setValue(oldDate);
                    picker.valueProperty().addListener(listener);
                }
            }));
             return;
         }
         
         
         app.processTransaction(new ScheduleStartEndComboTransaction(oldDate, newDate, picker, data, listener, this));
     }
     
     public void handleRemoveCleanUp(){
          TableView table = (TableView) app.getGUIModule().getGUINode(GeneratorProperty.GENERATOR_SCHEDULE_ITEM_TABLE);
          table.getSelectionModel().clearSelection();
          table.getSelectionModel().focus(-1);
          this.resetScheduleAddButtonText();
     }
    
    
    
    
    
    
}
