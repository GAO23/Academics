/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package apps.generator.workspace.controllers;

import apps.generator.GeneratorApp;
import apps.generator.GeneratorProperty;
import apps.generator.transactions.GeneralTextChangeTransaction;
import apps.generator.workspace.GeneratorWorkSpace;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.VBox;

/**
 *
 * @author xgao
 */
public class SyllabusController implements GeneratorWorkSpace.TextEditController {
    GeneratorApp app;

    public SyllabusController(GeneratorApp generatorApp) {
        this.app = generatorApp;
    }
    
    
    public void handleExpandButton1(){
        VBox vb1 = (VBox) app.getGUIModule().getGUINode(GeneratorProperty.GENERATOR_SYLLABUS_DESCRIPTION_BOX);
        Button eb1 = (Button) app.getGUIModule().getGUINode(GeneratorProperty.GENERATOR_SYLLABUS_EXPAND_BUTTON_DESCRIPTION);
        TextArea ta1 = (TextArea) app.getGUIModule().getGUINode(GeneratorProperty.GENERATOR_SYLLABUS_AREA_DECSRCIPTION);
        if(eb1.getText().equals("-")){
            vb1.getChildren().remove(ta1);
            eb1.setText("+");
        }else{
            vb1.getChildren().add(ta1);
            eb1.setText("-");
        }
    }
        
    public void handleExpandButton2(){
         VBox vb2 = (VBox) app.getGUIModule().getGUINode(GeneratorProperty.GENERATOR_SYLLABUS_TOPIC_BOX);
        Button eb2 = (Button) app.getGUIModule().getGUINode(GeneratorProperty.GENERATOR_SYLLABUS_EXPAND_BUTTON_TOPIC);
        TextArea ta2 = (TextArea) app.getGUIModule().getGUINode(GeneratorProperty.GENERATOR_SYLLABUS_AREA_TOPIC);
        if(eb2.getText().equals("-")){
            vb2.getChildren().remove(ta2);
            eb2.setText("+");
        }else{
            vb2.getChildren().add(ta2);
            eb2.setText("-");
        }
    }
        
         public void handleExpandButton3(){
         VBox vb3 = (VBox) app.getGUIModule().getGUINode(GeneratorProperty.GENERATOR_SYLLABUS_PERE_BOX);
        Button eb3 = (Button) app.getGUIModule().getGUINode(GeneratorProperty.GENERATOR_SYLLABUS_EXPAND_BUTTON_PERE);
        TextArea ta3 = (TextArea) app.getGUIModule().getGUINode(GeneratorProperty.GENERATOR_SYLLABUS_AREA_PERE);
        if(eb3.getText().equals("-")){
            vb3.getChildren().remove(ta3);
            eb3.setText("+");
        }else{
            vb3.getChildren().add(ta3);
            eb3.setText("-");
        }
         }
        
          public void handleExpandButton4(){
        VBox vb4 = (VBox) app.getGUIModule().getGUINode(GeneratorProperty.GENERATOR_SYLLABUS_OUT_BOX);
        Button eb4 = (Button) app.getGUIModule().getGUINode(GeneratorProperty.GENERATOR_SYLLABUS_EXPAND_BUTTON_OUT);
        TextArea ta4 = (TextArea) app.getGUIModule().getGUINode(GeneratorProperty.GENERATOR_SYLLABUS_AREA_OUT);
        if(eb4.getText().equals("-")){
            vb4.getChildren().remove(ta4);
            eb4.setText("+");
        }else{
            vb4.getChildren().add(ta4);
            eb4.setText("-");
        }
          }
        
         public void handleExpandButton5(){
        VBox vb5 = (VBox) app.getGUIModule().getGUINode(GeneratorProperty.GENERATOR_SYLLABUS_BOOK_BOX);
        Button eb5 = (Button) app.getGUIModule().getGUINode(GeneratorProperty.GENERATOR_SYLLABUS_EXPAND_BUTTON_BOOK);
        TextArea ta5 = (TextArea) app.getGUIModule().getGUINode(GeneratorProperty.GENERATOR_SYLLABUS_AREA_BOOK);
        if(eb5.getText().equals("-")){
            vb5.getChildren().remove(ta5);
            eb5.setText("+");
        }else{
            vb5.getChildren().add(ta5);
            eb5.setText("-");
        }
         }
        
         public void handleExpandButton6(){
        VBox vb6 = (VBox) app.getGUIModule().getGUINode(GeneratorProperty.GENERATOR_SYLLABUS_GRADE_BOX);
        Button eb6 = (Button) app.getGUIModule().getGUINode(GeneratorProperty.GENERATOR_SYLLABUS_EXPAND_BUTTON_GRADE);
        TextArea ta6 = (TextArea) app.getGUIModule().getGUINode(GeneratorProperty.GENERATOR_SYLLABUS_AREA_GRADE);
        if(eb6.getText().equals("-")){
            vb6.getChildren().remove(ta6);
            eb6.setText("+");
        }else{
            vb6.getChildren().add(ta6);
            eb6.setText("-");
        }
         }
        
         public void handleExpandButton7(){
        VBox vb7 = (VBox) app.getGUIModule().getGUINode(GeneratorProperty.GENERATOR_SYLLABUS_NOTE_BOX);
        Button eb7 = (Button) app.getGUIModule().getGUINode(GeneratorProperty.GENERATOR_SYLLABUS_EXPAND_BUTTON_NOTE);
        TextArea ta7 = (TextArea) app.getGUIModule().getGUINode(GeneratorProperty.GENERATOR_SYLLABUS_AREA_NOTE);
        if(eb7.getText().equals("-")){
            vb7.getChildren().remove(ta7);
            eb7.setText("+");
        }else{
            vb7.getChildren().add(ta7);
            eb7.setText("-");
        }
         }
         
          public void handleExpandButton8(){
        VBox vb8 = (VBox) app.getGUIModule().getGUINode(GeneratorProperty.GENERATOR_SYLLABUS_DISHONESTY_BOX);
        Button eb8 = (Button) app.getGUIModule().getGUINode(GeneratorProperty.GENERATOR_SYLLABUS_EXPAND_BUTTON_DISHONESTY);
        TextArea ta8 = (TextArea) app.getGUIModule().getGUINode(GeneratorProperty.GENERATOR_SYLLABUS_AREA_DISHONESTY);
        if(eb8.getText().equals("-")){
            vb8.getChildren().remove(ta8);
            eb8.setText("+");
        }else{
            vb8.getChildren().add(ta8);
            eb8.setText("-");
        }
          }
        
           public void handleExpandButton9(){
         VBox vb9 = (VBox) app.getGUIModule().getGUINode(GeneratorProperty.GENERATOR_SYLLABUS_ASIS_BOX);
        Button eb9 = (Button) app.getGUIModule().getGUINode(GeneratorProperty.GENERATOR_SYLLABUS_EXPAND_BUTTON_ASIS);
        TextArea ta9 = (TextArea) app.getGUIModule().getGUINode(GeneratorProperty.GENERATOR_SYLLABUS_AREA_ASIS);
        if(eb9.getText().equals("-")){
            vb9.getChildren().remove(ta9);
            eb9.setText("+");
        }else{
            vb9.getChildren().add(ta9);
            eb9.setText("-");
        }
        
    }

    @Override
    public void processTextChange(String oldValue, String newValue, TextInputControl control) {
        app.processTransaction(new GeneralTextChangeTransaction(oldValue, newValue, control));
    }
    
}
