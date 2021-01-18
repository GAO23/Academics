/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package apps.generator.transactions;

import apps.generator.data.GeneratorData;
import apps.generator.data.TeachingAssistantPrototype;
import apps.generator.data.TimeSlot;
import apps.generator.data.TimeSlot.DayOfWeek;
import libs.jTPS.jTPS_Transaction;

/**
 *
 * @author xgao
 */
public class ToggleOfficeHours_Transaction implements jTPS_Transaction{
    
    GeneratorData data;
    TimeSlot timeSlot;
    DayOfWeek dow;
    TeachingAssistantPrototype ta;
    
    public ToggleOfficeHours_Transaction(   GeneratorData initData, 
                                            TimeSlot initTimeSlot,
                                            DayOfWeek initDOW,
                                            TeachingAssistantPrototype initTA) {
        data = initData;
        timeSlot = initTimeSlot;
        dow = initDOW;
        ta = initTA;
    }

    @Override
    public void doTransaction() {
        timeSlot.toggleTA(dow, ta);
    }

    @Override
    public void undoTransaction() {
        timeSlot.toggleTA(dow, ta);
    }
    
}
