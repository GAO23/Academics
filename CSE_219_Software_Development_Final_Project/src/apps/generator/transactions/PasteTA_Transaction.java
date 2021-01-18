package apps.generator.transactions;

import apps.generator.GeneratorApp;
import apps.generator.data.GeneratorData;
import apps.generator.data.TeachingAssistantPrototype;
import libs.jTPS.jTPS_Transaction;

public class PasteTA_Transaction implements jTPS_Transaction {
    GeneratorApp app;
    TeachingAssistantPrototype taToPaste;

    public PasteTA_Transaction(  GeneratorApp initApp, 
                                 TeachingAssistantPrototype initTAToPaste) {
        app = initApp;
        taToPaste = initTAToPaste;
    }

    @Override
    public void doTransaction() {
        GeneratorData data = (GeneratorData)app.getDataComponent();
        data.addTA(taToPaste);
    }

    @Override
    public void undoTransaction() {
        GeneratorData data = (GeneratorData)app.getDataComponent();
        data.removeTA(taToPaste);
    }   
}