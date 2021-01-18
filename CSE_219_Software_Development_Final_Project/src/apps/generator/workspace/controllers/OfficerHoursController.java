/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package apps.generator.workspace.controllers;

import apps.generator.GeneratorApp;
import apps.generator.GeneratorProperty;
import apps.generator.data.GeneratorData;
import apps.generator.data.TAType;
import apps.generator.data.TeachingAssistantPrototype;
import apps.generator.data.TimeSlot;
import apps.generator.transactions.ToggleOfficeHours_Transaction;
import apps.generator.transactions.UpdateOHTimeComboTransaction;
import apps.generator.workspace.dialogs.GeneratorTADialogs;
import apps.generator.workspace.dialogs.customAlert;
import libs.DesktopJavaFramework.modules.AppGUIModule;
import libs.DesktopJavaFramework.ui.dialogs.AppDialogsFacade;
import apps.generator.transactions.AddTA_Transaction;
import javafx.collections.ObservableList;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import apps.generator.transactions.EditTA_Transaction;
import apps.generator.workspace.GeneratorWorkSpace;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 *
 * @author xgao
 */
public class OfficerHoursController {
       GeneratorApp app;

    public OfficerHoursController(GeneratorApp initApp) {
        app = initApp;
    }

    public void processAddTA() {
        AppGUIModule gui = app.getGUIModule();
        TextField nameTF = (TextField) gui.getGUINode(GeneratorProperty.OH_NAME_TEXT_FIELD);
        String name = nameTF.getText();
        TextField emailTF = (TextField) gui.getGUINode(GeneratorProperty.OH_EMAIL_TEXT_FIELD);
        String email = emailTF.getText();
        GeneratorData data = (GeneratorData) app.getDataComponent();
        TAType type = data.getSelectedType();
        if (data.isLegalNewTA(name, email)) {
            TeachingAssistantPrototype ta = new TeachingAssistantPrototype(name.trim(), email.trim(), type);
            AddTA_Transaction addTATransaction = new AddTA_Transaction(data, ta);
            app.processTransaction(addTATransaction);

            // NOW CLEAR THE TEXT FIELDS
            nameTF.setText("");
            emailTF.setText("");
            nameTF.requestFocus();
        }
        app.getFoolproofModule().updateControls(GeneratorProperty.OH_FOOLPROOF_SETTINGS);
    }

    public void processVerifyTA() {

    }

    public void processToggleOfficeHours() {
        AppGUIModule gui = app.getGUIModule();
        TableView<TimeSlot> officeHoursTableView = (TableView) gui.getGUINode(GeneratorProperty.OH_OFFICE_HOURS_TABLE_VIEW);
        ObservableList<TablePosition> selectedCells = officeHoursTableView.getSelectionModel().getSelectedCells();
        if (selectedCells.size() > 0) {
            TablePosition cell = selectedCells.get(0);
            int cellColumnNumber = cell.getColumn();
            GeneratorData data = (GeneratorData)app.getDataComponent();
            if (data.isDayOfWeekColumn(cellColumnNumber)) {
                TimeSlot.DayOfWeek dow = data.getColumnDayOfWeek(cellColumnNumber);
                TableView<TeachingAssistantPrototype> taTableView = (TableView)gui.getGUINode(GeneratorProperty.OH_TAS_TABLE_VIEW);
                TeachingAssistantPrototype ta = taTableView.getSelectionModel().getSelectedItem();
                if (ta != null) {
                    TimeSlot timeSlot = officeHoursTableView.getSelectionModel().getSelectedItem();
                    ToggleOfficeHours_Transaction transaction = new ToggleOfficeHours_Transaction(data, timeSlot, dow, ta);
                    app.processTransaction(transaction);
                }
                else {
                    Stage window = app.getGUIModule().getWindow();
                    AppDialogsFacade.showMessageDialog(window, GeneratorProperty.OH_NO_TA_SELECTED_TITLE, GeneratorProperty.OH_NO_TA_SELECTED_CONTENT);
                }
            }
            int row = cell.getRow();
            cell.getTableView().refresh();
        }
    }

    public void processTypeTA() {
        app.getFoolproofModule().updateControls(GeneratorProperty.OH_FOOLPROOF_SETTINGS);
    }

    public void processEditTA() {
        GeneratorData data = (GeneratorData)app.getDataComponent();
        if (data.isTASelected()) {
            TeachingAssistantPrototype taToEdit = data.getSelectedTA();
            GeneratorTADialogs taDialog = (GeneratorTADialogs)app.getGUIModule().getDialog(GeneratorProperty.OH_TA_EDIT_DIALOG);
            taDialog.showEditDialog(taToEdit);
            TeachingAssistantPrototype editTA = taDialog.getEditTA();
            if (editTA != null) {
                EditTA_Transaction transaction = new EditTA_Transaction(taToEdit, editTA.getName(), editTA.getEmail(), editTA.getType());
                app.processTransaction(transaction);
            }
        }
    }

    public void processSelectAllTAs() {
        GeneratorData data = (GeneratorData)app.getDataComponent();
        data.selectTAs(TAType.All);
    }

    public void processSelectGradTAs() {
        GeneratorData data = (GeneratorData)app.getDataComponent();
        data.selectTAs(TAType.Graduate);
    }

    public void processSelectUndergradTAs() {
        GeneratorData data = (GeneratorData)app.getDataComponent();
        data.selectTAs(TAType.Undergraduate);
    }
     public void proccessTAEpxand() {
        VBox leftPane = (VBox) app.getGUIModule().getGUINode(GeneratorProperty.OH_LEFT_PANE);
        Button expandButton = (Button) app.getGUIModule().getGUINode(GeneratorProperty.GENERATOR_OH_TA_EXPAND_BUTTON);
        TableView taTable = (TableView) app.getGUIModule().getGUINode(GeneratorProperty.OH_TAS_TABLE_VIEW);
        HBox addBox = (HBox) app.getGUIModule().getGUINode(GeneratorProperty.OH_ADD_TA_PANE);
        if(expandButton.getText().equals("-")){
            leftPane.getChildren().remove(taTable);
            leftPane.getChildren().remove(addBox);
            expandButton.setText("+");
        }else{
            leftPane.getChildren().add(taTable);
            leftPane.getChildren().add(addBox);
            expandButton.setText("-");
        }
    }
    

    public void processSelectTA() {
        AppGUIModule gui = app.getGUIModule();
        TableView<TimeSlot> officeHoursTableView = (TableView) gui.getGUINode(GeneratorProperty.OH_OFFICE_HOURS_TABLE_VIEW);
        officeHoursTableView.refresh();
    }
    
    public void proccessCombo(String oldValue, String newValue, ComboBox combo, GeneratorWorkSpace.customChangeListener listner){
        UpdateOHTimeComboTransaction transaction = new UpdateOHTimeComboTransaction(oldValue, newValue, combo, this, listner,(GeneratorData) app.getDataComponent() );
         AppGUIModule gui = app.getGUIModule();
        GeneratorData data = (GeneratorData)app.getDataComponent();
        ComboBox start = (ComboBox) gui.getGUINode(GeneratorProperty.GENERATOR_OH_START_TIME_COMBO);
        ComboBox end = (ComboBox) gui.getGUINode(GeneratorProperty.GENERATOR_OH_END_TIME_COMBO);
        String startTime = start.getValue().toString();
        String endTime = end.getValue().toString();
        if (!data.earlierThan(startTime, endTime)){
            this.showAlert();
            Platform.runLater((new Runnable() {
                @Override
                public void run() {
                    combo.valueProperty().removeListener(listner);
                    combo.setValue(oldValue);
                    combo.valueProperty().addListener(listner);
                }
            }));
            return;
        }
        app.processTransaction(transaction);
        
    }
    
    
    public void updateCombo() {
        AppGUIModule gui = app.getGUIModule();
        GeneratorData data = (GeneratorData)app.getDataComponent();
        ComboBox start = (ComboBox) gui.getGUINode(GeneratorProperty.GENERATOR_OH_START_TIME_COMBO);
        ComboBox end = (ComboBox) gui.getGUINode(GeneratorProperty.GENERATOR_OH_END_TIME_COMBO);
        String startTime = start.getValue().toString();
        String endTime = end.getValue().toString();
        if (!data.earlierThan(startTime, endTime)){
            customAlert.showAlert("Undo reverses the combo box to an invalid time range, table will remain unchanged", "undo results in invalid tiem range");
            return;
        }
        
        data.updateToCombo(startTime, endTime);
        
    }
    
    
 
    
    private void showAlert(){
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Alert");
        alert.setHeaderText("Alert");
        alert.setContentText("Time range is invalid, please seletect a differet time");
        alert.showAndWait();
    }
}
