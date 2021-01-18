/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package apps.generator.workspace.controllers;

import apps.generator.GeneratorApp;
import apps.generator.GeneratorProperty;
import apps.generator.data.GeneratorData;
import apps.generator.transactions.MeetingRemoveItemTransaction;
import apps.generator.workspace.dialogs.customAlert;
import apps.generator.transactions.MeetingEditItemTransction;
import apps.generator.transactions.MeetingAddItemTransaction;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

/**
 *
 * @author xgao
 */
public class MeetingTimesController {
    
    GeneratorApp app;
    
    public MeetingTimesController(GeneratorApp app){
        this.app = app;
    }
    
    public void handleLectureExpand(){
         Button expandButton = (Button) app.getGUIModule().getGUINode(GeneratorProperty.GENERATOR_MEETING_LECTURE_PLUS_BUTTON);
         VBox container = (VBox) app.getGUIModule().getGUINode(GeneratorProperty.GENERATOR_MEETING_LECTURE_BOX);
         TableView table = (TableView) app.getGUIModule().getGUINode(GeneratorProperty.GENERATOR_LECTURE_TABLE_VIEW);
         if(expandButton.getText().equals("-")){
            container.getChildren().remove(table);
            expandButton.setText("+");
        }else{
            container.getChildren().add(table);
            expandButton.setText("-");
        }
        
    }
    
    public void handleRecitationExpand(){
         Button expandButton = (Button) app.getGUIModule().getGUINode(GeneratorProperty.GENERATOR_MEETING_RECITATION_PLUS_BUTTON);
         VBox container = (VBox) app.getGUIModule().getGUINode(GeneratorProperty.GENERATOR_MEETING_RECITATION_BOX);
         TableView table = (TableView) app.getGUIModule().getGUINode(GeneratorProperty.GENERATOR_RECITATION_TABLE_VIEW);
         if(expandButton.getText().equals("-")){
            container.getChildren().remove(table);
            expandButton.setText("+");
        }else{
            container.getChildren().add(table);
            expandButton.setText("-");
        }
        
    }
    
    public void handleLabExpand(){
         Button expandButton = (Button) app.getGUIModule().getGUINode(GeneratorProperty.GENERATOR_MEETING_LAB_PLUS_BUTTON);
         VBox container = (VBox) app.getGUIModule().getGUINode(GeneratorProperty.GENERATOR_MEETING_LAB_BOX);
         TableView table = (TableView) app.getGUIModule().getGUINode(GeneratorProperty.GENERATOR_LAB_TABLE_VIEW);
         if(expandButton.getText().equals("-")){
            container.getChildren().remove(table);
            expandButton.setText("+");
        }else{
            container.getChildren().add(table);
            expandButton.setText("-");
        }
        
    }
    
    public void processAddItem(Object item, ObservableList itemList){
         GeneratorData data = (GeneratorData) app.getDataComponent();
        app.processTransaction(new MeetingAddItemTransaction(item, itemList, data));
    }
    
    public void processRemoveItem(Object item, ObservableList itemList){
        if(item==null){
            customAlert.showAlert("You need to selecet an item first", "No item selected");
            return;
        }
         GeneratorData data = (GeneratorData) app.getDataComponent();
        app.processTransaction(new MeetingRemoveItemTransaction(item, itemList, data));
    }
    
    public void proccessEditItem(String oldValue, String newValue, Object item, int itemType, int transactionType){
        GeneratorData data = (GeneratorData) app.getDataComponent();
        app.processTransaction(new MeetingEditItemTransction(oldValue, newValue, item, itemType, transactionType, data));
    }
    
    
    
    
    
}
