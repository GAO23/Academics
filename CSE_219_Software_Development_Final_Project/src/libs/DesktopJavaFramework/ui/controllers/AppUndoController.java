package libs.DesktopJavaFramework.ui.controllers;

import libs.DesktopJavaFramework.AppTemplate;
import libs.DesktopJavaFramework.AppTemplate;
import libs.jTPS.jTPS;

public class AppUndoController {
    private AppTemplate app;
    
    public AppUndoController(AppTemplate initApp) {
        app = initApp;
    }
    
    public void processUndoRequest() {
        jTPS tps = app.getTPS();
        tps.undoTransaction();
        app.getFoolproofModule().updateAll();
    }
    
    public void processRedoRequest() {
        jTPS tps = app.getTPS();
        tps.doTransaction();
        app.getFoolproofModule().updateAll();
    }    
}
