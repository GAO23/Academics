/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package apps.generator.workspace.dialogs;

import java.io.File;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import libs.DesktopJavaFramework.AppPropertyType;
import libs.PropertiesManager.src.PropertiesManager;

/**
 *
 * @author xiangsgao
 */
public class imageChooserDialog {
    
     public static File showOpenDialog(Stage window) {
         PropertiesManager props = PropertiesManager.getPropertiesManager();
         String pathIcon = props.getProperty(AppPropertyType.APP_PATH_ICONS);
        FileChooser fc = new FileChooser();
        fc.setInitialDirectory(new File(pathIcon));
        fc.setTitle(props.getProperty("CHOOSE_AN_IMAGE_TEXT"));
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter(".png", "*.png")
        );
        File selectedFile = fc.showOpenDialog(window);
        return selectedFile;
    }
    
}
