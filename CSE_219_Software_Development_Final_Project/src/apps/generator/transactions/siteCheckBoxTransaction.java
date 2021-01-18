/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package apps.generator.transactions;

import apps.generator.workspace.controllers.SiteController;
import javafx.scene.control.CheckBox;
import libs.jTPS.jTPS_Transaction;

/**
 *
 * @author gaoxi
 */
public class siteCheckBoxTransaction implements jTPS_Transaction{
    private final CheckBox check;
    private final SiteController controller;
    private boolean proccessBefore = false;
    
    public siteCheckBoxTransaction(CheckBox check, SiteController controller){
        this.check = check;
        this.controller = controller;
    }

    @Override
    public void doTransaction() {
        if(!this.proccessBefore){
            this.proccessBefore = true;
            controller.validateCheckClicked(check);
            return;
        }
       check.setSelected(!check.isSelected());
       controller.validateCheckClicked(check);
    }

    @Override
    public void undoTransaction() {
       check.setSelected(!check.isSelected());
       controller.validateCheckClicked(check);
    }
    
}
