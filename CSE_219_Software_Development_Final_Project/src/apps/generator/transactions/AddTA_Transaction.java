/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package apps.generator.transactions;

import apps.generator.data.GeneratorData;
import apps.generator.data.TeachingAssistantPrototype;
import libs.jTPS.jTPS_Transaction;

/**
 *
 * @author xgao
 */
public class AddTA_Transaction implements jTPS_Transaction{
    
    GeneratorData data;
    TeachingAssistantPrototype ta;
    
    public AddTA_Transaction(GeneratorData initData, TeachingAssistantPrototype initTA) {
        data = initData;
        ta = initTA;
    }

    @Override
    public void doTransaction() {
        data.addTA(ta);        
    }

    @Override
    public void undoTransaction() {
        data.removeTA(ta);
    }
    
}
