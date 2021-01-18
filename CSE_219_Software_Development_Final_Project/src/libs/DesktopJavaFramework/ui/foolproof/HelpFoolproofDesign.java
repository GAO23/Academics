package libs.DesktopJavaFramework.ui.foolproof;

import static libs.DesktopJavaFramework.AppPropertyType.LANGUAGE_BUTTON;
import libs.DesktopJavaFramework.AppTemplate;
import libs.DesktopJavaFramework.AppPropertyType;
import libs.DesktopJavaFramework.AppTemplate;

/**
 *
 * @author McKillaGorilla
 */
public class HelpFoolproofDesign  implements FoolproofDesign {
    AppTemplate app;
    
    public HelpFoolproofDesign(AppTemplate initApp) {
        app = initApp;
    }    

    @Override
    public void updateControls() {
        boolean appHasMoreThanOneLanguage = app.getLanguageModule().getLanguages().size() > 1;
        app.getGUIModule().getGUINode(AppPropertyType.LANGUAGE_BUTTON);
    }
}
