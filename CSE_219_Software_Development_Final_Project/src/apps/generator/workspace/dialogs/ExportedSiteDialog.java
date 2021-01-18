/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package apps.generator.workspace.dialogs;

import apps.generator.GeneratorApp;
import apps.generator.GeneratorProperty;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 *
 * @author xiangsgao
 */
public class ExportedSiteDialog extends Stage{
    
    private WebView webView;
    private String path;
    
    public ExportedSiteDialog(GeneratorApp app) throws MalformedURLException{
        
        webView = new WebView();
        Scene scene = new Scene(webView);
        this.setScene(scene);

        // MAKE IT MODAL
        this.initOwner(app.getGUIModule().getWindow());
        this.initModality(Modality.APPLICATION_MODAL);
        Label label = (Label) app.getGUIModule().getGUINode(GeneratorProperty.GENERATOR_SITE_EXPORT_LABEL);
        this.path = label.getText()+File.separator+"index.html";
        System.out.println(path);
        this.showWebDialog();
       
    }
    
    public void showWebDialog() throws MalformedURLException {
        
        WebEngine webEngine = webView.getEngine();
        webEngine.documentProperty().addListener(e->{
            // THE PAGE WILL LOAD ASYNCHRONOUSLY, SO MAKE
            // SURE TO GRAB THE TITLE FOR THE WINDOW
            // ONCE IT'S BEEN LOADED
            String title = webEngine.getTitle();
            this.setTitle(title);
        });
        URL pageURL = new File(path).toURI().toURL();
        String pagePath = pageURL.toExternalForm();
        webEngine.load(pagePath);        
        this.showAndWait();
    }
    
}
