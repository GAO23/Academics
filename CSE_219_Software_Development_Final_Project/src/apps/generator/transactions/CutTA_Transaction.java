package apps.generator.transactions;

import apps.generator.GeneratorApp;
import apps.generator.data.GeneratorData;
import apps.generator.data.TeachingAssistantPrototype;
import apps.generator.data.TimeSlot;
import libs.jTPS.jTPS_Transaction;
import static libs.DesktopJavaFramework.AppPropertyType.APP_CLIPBOARD_FOOLPROOF_SETTINGS;

import java.util.ArrayList;
import java.util.HashMap;


public class CutTA_Transaction implements jTPS_Transaction {
    GeneratorApp app;
    TeachingAssistantPrototype taToCut;
    HashMap<TimeSlot, ArrayList<TimeSlot.DayOfWeek>> officeHours;

    public CutTA_Transaction(GeneratorApp initApp, 
            TeachingAssistantPrototype initTAToCut, 
            HashMap<TimeSlot, ArrayList<TimeSlot.DayOfWeek>> initOfficeHours) {
        app = initApp;
        taToCut = initTAToCut;
        officeHours = initOfficeHours;
    }

    @Override
    public void doTransaction() {
        GeneratorData data = (GeneratorData)app.getDataComponent();
        data.removeTA(taToCut, officeHours);
        app.getFoolproofModule().updateControls(APP_CLIPBOARD_FOOLPROOF_SETTINGS);
    }

    @Override
    public void undoTransaction() {
        GeneratorData data = (GeneratorData)app.getDataComponent();
        data.addTA(taToCut, officeHours);
        app.getFoolproofModule().updateControls(APP_CLIPBOARD_FOOLPROOF_SETTINGS);
    }   
}