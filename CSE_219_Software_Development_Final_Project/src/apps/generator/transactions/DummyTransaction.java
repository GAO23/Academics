/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package apps.generator.transactions;

import libs.jTPS.jTPS_Transaction;

/**
 *
 * @author gaoxi
 */
public class DummyTransaction implements jTPS_Transaction{

    @Override
    public void doTransaction() {
        System.out.println("This is a dummy transaction so redo and undo do not work");
    }

    @Override
    public void undoTransaction() {
        System.out.println("This is a dummy transaction so redo and undo do not work");
    }
    
}
