/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package apps.generator.workspace.dialogs;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/**
 *
 * @author gaoxi
 */
public class customAlert {
    
    public static void showAlert(String errorMessage, String errorTitle){
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Alert");
        alert.setHeaderText(errorTitle);
        alert.setContentText(errorMessage);

        alert.showAndWait();
    }
    
}
