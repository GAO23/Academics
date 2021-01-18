package apps.generator.transactions;


import apps.generator.GeneratorApp;
import apps.generator.GeneratorProperty;
import apps.generator.workspace.controllers.ScheduleController;
import libs.DesktopJavaFramework.modules.AppGUIModule;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import libs.jTPS.jTPS_Transaction;
import libs.PropertiesManager.src.PropertiesManager;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author xgao
 */
public class ClearScheduleTransaction implements jTPS_Transaction {
    
    private String oldTitleText;
    private String oldTopicText;
    private String oldLinkText;
    private TextField title;
    private TextField topic;
    private TextField link;
    private int selectedIndex; 
    private TableView scheduleTable;
    private GeneratorApp app;
    private ScheduleController controller;
    
    
   public ClearScheduleTransaction(GeneratorApp app, ScheduleController controller){
        AppGUIModule gui = app.getGUIModule();
        PropertiesManager manager = PropertiesManager.getPropertiesManager();
        Button addButton = (Button) gui.getGUINode(GeneratorProperty.GENERATOR_SCHEDULE_ADD_BUTTON);
        addButton.setText(manager.getProperty(GeneratorProperty.GENERATOR_SCHEDULE_ADD_BUTTON.toString()+"_TEXT"));
        this.title = (TextField) app.getGUIModule().getGUINode(GeneratorProperty.GENERATOR_SCHEDULE_TITLE_TEXTFIELD);
        this.topic = (TextField) app.getGUIModule().getGUINode(GeneratorProperty.GENERATOR_SCHEDULE_TOPIC_TEXTFIELD);
        this.link = (TextField) app.getGUIModule().getGUINode(GeneratorProperty.GENERATOR_SCHEDULE_LINK_TEXTFIELD);
        this.oldTitleText = title.getText();
        this.oldTopicText = topic.getText();
        this.oldLinkText = link.getText();
        this.scheduleTable = (TableView) gui.getGUINode(GeneratorProperty.GENERATOR_SCHEDULE_ITEM_TABLE);
        this.selectedIndex = scheduleTable.getSelectionModel().getSelectedIndex();
        this.app = app;
        this.controller = controller;
   }

    @Override
    public void doTransaction() {
         title.setText("");
        topic.setText("");
        link.setText("");
        scheduleTable.getSelectionModel().clearSelection();
        scheduleTable.getSelectionModel().focus(-1);
    }

    @Override
    public void undoTransaction() {
        AppGUIModule gui = app.getGUIModule();
        PropertiesManager manager = PropertiesManager.getPropertiesManager();
        Button addButton = (Button) gui.getGUINode(GeneratorProperty.GENERATOR_SCHEDULE_ADD_BUTTON);
        title.setText(oldTitleText);
        topic.setText(oldTopicText);
        link.setText(oldLinkText);
        this.scheduleTable.getSelectionModel().select(selectedIndex);
        this.scheduleTable.getSelectionModel().focus(selectedIndex);
        this.controller.resetScheduleAddButtonText();
        
    }
    

    
}
