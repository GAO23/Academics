/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package apps.generator.workspace.controllers;

import apps.generator.GeneratorApp;
import apps.generator.GeneratorProperty;
import apps.generator.files.GeneratorFiles;
import apps.generator.transactions.GeneralTextChangeTransaction;
import apps.generator.transactions.generalComboBoxSelectTransaction;
import apps.generator.transactions.updateSiteExportComboTransaction;
import apps.generator.transactions.updateSiteExportTypeInComboTransaction;
import apps.generator.workspace.dialogs.customAlert;
import apps.generator.workspace.dialogs.imageChooserDialog;
import apps.generator.transactions.SiteImageTransaction;
import libs.DesktopJavaFramework.modules.AppGUIModule;
import libs.DesktopJavaFramework.ui.AppNodesBuilder;
import apps.generator.transactions.siteCheckBoxTransaction;
import apps.generator.workspace.GeneratorWorkSpace;

import java.io.File;
import java.io.IOException;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 *
 * @author xgao
 */
public class SiteController implements GeneratorWorkSpace.TextEditController{
    
     private GeneratorApp app;
     

    public SiteController(GeneratorApp generatorApp) {
        this.app = generatorApp;
    }
    
    
     public void proccessOfficeHoursEpxand() {
        HBox textAreaBox = (HBox) app.getGUIModule().getGUINode(GeneratorProperty.GENERATOR_SITE_TEXT_AREA_BOX);
        Button expandButton = (Button) app.getGUIModule().getGUINode(GeneratorProperty.GENERATOR_SITE_OFFICE_HOURS_EXPAND_BUTTON);
        VBox instructorBox = (VBox) app.getGUIModule().getGUINode(GeneratorProperty.GENERATOR_SITE_INSTRUCTOR_HBOX);
        if(expandButton.getText().equals("-")){
            instructorBox.getChildren().remove(textAreaBox);
            expandButton.setText("+");
        }else{
            instructorBox.getChildren().add(textAreaBox);
            expandButton.setText("-");
        }
    }
     
    
     
     public void proccessCheckClicked(Object checkID){
         CheckBox check = (CheckBox) app.getGUIModule().getGUINode(checkID);
         siteCheckBoxTransaction transaction = new  siteCheckBoxTransaction(check, this);
         app.processTransaction(transaction);
     }
     
     public void validateCheckClicked(CheckBox check){
        try{
            validateCheckBoxes();
        }catch(Exception e){
            check.setSelected(true);
            customAlert.showAlert(e.getMessage(), "Invalid check boxes");
        }
     }
     
     
      private void validateCheckBoxes()throws Exception{
        AppGUIModule gui = app.getGUIModule();
        CheckBox check1 = (CheckBox) gui.getGUINode(GeneratorProperty.GENERATOR_SITE_HOME_CHECKBOX);
        CheckBox check2 = (CheckBox) gui.getGUINode(GeneratorProperty.GENERATOR_SITE_SYLLABUS_CHECKBOX);
        CheckBox check3 = (CheckBox) gui.getGUINode(GeneratorProperty.GENERATOR_SITE_SCHEDULE_CHECKBOX);
        CheckBox check4 = (CheckBox) gui.getGUINode(GeneratorProperty.GENERATOR_SITE_HOMEWORK_CHECKBOX);
        if(!check1.isSelected() && !check2.isSelected() && !check3.isSelected() && !check4.isSelected()){
            throw new Exception("One of the check box must be selected");
        }
    }
      
      public void proccessComboBoxTypeIn(String oldValue, String newValue, ComboBox combo, Object comboID){
          
          if( newValue.equals(oldValue)){
            return;
          }
          updateSiteExportTypeInComboTransaction transaction = new updateSiteExportTypeInComboTransaction(oldValue, newValue, comboID, combo, this);
          app.processTransaction(transaction);
      }
      
      
      public void proccessExportCombo(String oldValue, String newValue, Object ComboID, ChangeListener listner){
         ComboBox combo = (ComboBox) app.getGUIModule().getGUINode(ComboID);
         updateSiteExportComboTransaction transaction = new updateSiteExportComboTransaction( oldValue,  newValue, combo, this, listner);
         app.processTransaction(transaction);
      }
      
      
      public void proccessImageIcon(Object buttonID){
          AppNodesBuilder.imageButton button = (AppNodesBuilder.imageButton) app.getGUIModule().getGUINode(buttonID);
          String oldImage = button.getFilePath();
          File newImageFile = imageChooserDialog.showOpenDialog(new Stage());
          if(newImageFile == null){
              return;
          }
          String newImage = newImageFile.toURI().toString();
          app.processTransaction(new SiteImageTransaction(oldImage, newImage, button));
      }
      
      
      public void handleSaveComboTextFieldOptions(Object comboID){
          GeneratorFiles fileModule  = (GeneratorFiles) app.getFileComponent();
         try {
             fileModule.saveComboTextFieldOption(comboID);
         } catch (IOException ex) {
             ex.printStackTrace();
         }
      }
      
      public void updateExportLabel(){
          GeneratorWorkSpace workSpace = (GeneratorWorkSpace) app.getWorkspaceComponent();
          String labelText = System.getProperty("user.dir") + File.separator + "export" + File.separator;
          ComboBox combo = (ComboBox) app.getGUIModule().getGUINode(GeneratorProperty.GENERATOR_SITE_SUBJECT_COMBOBOX);
          labelText += combo.getValue() + "_";
          combo = (ComboBox) app.getGUIModule().getGUINode(GeneratorProperty.GENERATOR_SITE_NUMBER_COMBOBOX);
          labelText += combo.getValue() + "_";
          combo = (ComboBox) app.getGUIModule().getGUINode(GeneratorProperty.GENERATOR_SITE_SEMESTER_COMBOBOX);
          labelText += combo.getValue() + "_";
          combo = (ComboBox) app.getGUIModule().getGUINode(GeneratorProperty.GENERATOR_SITE_YEAR_COMBOBOX);
          labelText += combo.getValue()+File.separator+"CourseSite" ;
          Label label =  (Label) app.getGUIModule().getGUINode(GeneratorProperty.GENERATOR_SITE_EXPORT_LABEL);
          label.setText(labelText);
      }
      
      public void proccessGeneralComboSelect(String oldValue, String newValue, Object comboID, ChangeListener listener){
            ComboBox combo = (ComboBox) app.getGUIModule().getGUINode(comboID);
            app.processTransaction(new generalComboBoxSelectTransaction(oldValue, newValue, combo, listener));
      }

    @Override
    public void processTextChange(String oldValue, String newValue, TextInputControl control) {
        app.processTransaction(new GeneralTextChangeTransaction(oldValue, newValue, control));
    }
     
     
}
