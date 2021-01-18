/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package apps.generator.transactions;

import apps.generator.workspace.dialogs.customAlert;
import libs.DesktopJavaFramework.ui.AppNodesBuilder;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import libs.jTPS.jTPS_Transaction;

/**
 *
 * @author xgao
 */
public class SiteImageTransaction implements jTPS_Transaction{
    
    private final String oldImage;
    private final String newImage;
    private final AppNodesBuilder.imageButton button;
    
    public SiteImageTransaction(String oldImage, String newImage, AppNodesBuilder.imageButton button){
        this.oldImage = oldImage;
        this.newImage = newImage;
        this.button = button;
    }

    @Override
    public void doTransaction() {
       Image buttonImage = new Image(newImage);
       if (!buttonImage.isError()) {
            button.setGraphic(new ImageView(buttonImage));
            ((AppNodesBuilder.imageButton) button).setFilePath(newImage);
        } else{
           customAlert.showAlert("Can't load image, make sure the image file still exists", "Can't load Image'");
       }
    }

    @Override
    public void undoTransaction() {
        Image buttonImage = new Image(oldImage);
       if (!buttonImage.isError()) {
            button.setGraphic(new ImageView(oldImage));
            ((AppNodesBuilder.imageButton) button).setFilePath(oldImage);
        } else{
           customAlert.showAlert("Can't load image, make sure the image file still exists", "Can't load Image'");
       }
    }
    
}
