/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package apps.generator.workspace.foolproof;

import apps.generator.GeneratorApp;
import apps.generator.GeneratorProperty;
import apps.generator.data.GeneratorData;
import apps.generator.workspace.style.GeneratorStyle;
import libs.DesktopJavaFramework.modules.AppGUIModule;
import libs.DesktopJavaFramework.ui.foolproof.FoolproofDesign;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

/**
 *
 * @author xgao
 */
public class GeneratorFoolProofDesign implements FoolproofDesign{
    
        GeneratorApp app;

    public GeneratorFoolProofDesign(GeneratorApp initApp) {
        app = initApp;
        
    }

    @Override
    public void updateControls() {
        updateAddTAFoolproofDesign();
        updateEditTAFoolproofDesign();
    }

    private void updateAddTAFoolproofDesign() {
        AppGUIModule gui = app.getGUIModule();
        
        // FOOLPROOF DESIGN STUFF FOR ADD TA BUTTON
        TextField nameTextField = ((TextField) gui.getGUINode(GeneratorProperty.OH_NAME_TEXT_FIELD));
        TextField emailTextField = ((TextField) gui.getGUINode(GeneratorProperty.OH_EMAIL_TEXT_FIELD));
        String name = nameTextField.getText();
        String email = emailTextField.getText();
        GeneratorData data = (GeneratorData) app.getDataComponent();
        Button addTAButton = (Button) gui.getGUINode(GeneratorProperty.OH_ADD_TA_BUTTON);

        // FIRST, IF NO TYPE IS SELECTED WE'LL JUST DISABLE
        // THE CONTROLS AND BE DONE WITH IT
        boolean isTypeSelected = data.isTATypeSelected();
        if (!isTypeSelected) {
            nameTextField.setDisable(true);
            emailTextField.setDisable(true);
            addTAButton.setDisable(true);
            return;
        } // A TYPE IS SELECTED SO WE'LL CONTINUE
        else {
            nameTextField.setDisable(false);
            emailTextField.setDisable(false);
            addTAButton.setDisable(false);
        }

        // NOW, IS THE USER-ENTERED DATA GOOD?
        boolean isLegalNewTA = data.isLegalNewTA(name, email);

        // ENABLE/DISABLE THE CONTROLS APPROPRIATELY
        addTAButton.setDisable(!isLegalNewTA);
        if (isLegalNewTA) {
            nameTextField.setOnAction(addTAButton.getOnAction());
            emailTextField.setOnAction(addTAButton.getOnAction());
        } else {
            nameTextField.setOnAction(null);
            emailTextField.setOnAction(null);
        }

        // UPDATE THE CONTROL TEXT DISPLAY APPROPRIATELY
        boolean isLegalNewName = data.isLegalNewName(name);
        boolean isLegalNewEmail = data.isLegalNewEmail(email);
        foolproofTextField(nameTextField, isLegalNewName);
        foolproofTextField(emailTextField, isLegalNewEmail);
    }
    
    private void updateEditTAFoolproofDesign() {
        
    }
    
    public void foolproofTextField(TextField textField, boolean hasLegalData) {
        if (hasLegalData) {
            textField.getStyleClass().remove(GeneratorStyle.CLASS_OH_TEXT_FIELD_ERROR);
            if (!textField.getStyleClass().contains(GeneratorStyle.CLASS_OH_TEXT_FIELD)) {
                textField.getStyleClass().add(GeneratorStyle.CLASS_OH_TEXT_FIELD);
            }
        } else {
            textField.getStyleClass().remove(GeneratorStyle.CLASS_OH_TEXT_FIELD);
            if (!textField.getStyleClass().contains(GeneratorStyle.CLASS_OH_TEXT_FIELD_ERROR)) {
                textField.getStyleClass().add(GeneratorStyle.CLASS_OH_TEXT_FIELD_ERROR);
            }
        }
    }
    
   
    
}
