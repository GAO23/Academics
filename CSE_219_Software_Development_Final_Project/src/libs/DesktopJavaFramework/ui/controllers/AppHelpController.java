package libs.DesktopJavaFramework.ui.controllers;

import libs.DesktopJavaFramework.ui.dialogs.AppDialogsFacade;
import libs.DesktopJavaFramework.AppTemplate;
import libs.DesktopJavaFramework.modules.AppLanguageModule;
import libs.DesktopJavaFramework.modules.AppLanguageModule.LanguageException;
import libs.DesktopJavaFramework.AppTemplate;
import libs.DesktopJavaFramework.modules.AppLanguageModule;

public class AppHelpController {
    private AppTemplate app;
    
    public AppHelpController(AppTemplate initApp) {
        app = initApp;
    }  
    
    public void processHelpRequest() {
        AppDialogsFacade.showHelpDialog(app);
    }
    
    public void processLanguageRequest() {
        try {
            AppLanguageModule languageSettings = app.getLanguageModule();
            AppDialogsFacade.showLanguageDialog(languageSettings);
        }
        catch(AppLanguageModule.LanguageException le) {
            System.out.println("Error Loading Language into UI");
        }
    }  
    
    public void processAboutRequest() {
        AppDialogsFacade.showAboutDialog(app);
    }    
}
