package libs.DesktopJavaFramework.ui.foolproof;

import static libs.DesktopJavaFramework.AppPropertyType.*;
import libs.DesktopJavaFramework.AppTemplate;
import libs.DesktopJavaFramework.modules.AppGUIModule;
import javafx.scene.control.Button;
import libs.DesktopJavaFramework.AppPropertyType;
import libs.DesktopJavaFramework.AppTemplate;
import libs.DesktopJavaFramework.modules.AppGUIModule;
import libs.PropertiesManager.src.PropertiesManager;

/**
 *
 * @author McKillaGorilla
 */
public class FileFoolproofDesign implements FoolproofDesign {
    AppTemplate app;
    
    public FileFoolproofDesign(AppTemplate initApp) {
        app = initApp;
    }    

    @Override
    public void updateControls() {
        AppGUIModule gui = app.getGUIModule();
        enableIfInUse(AppPropertyType.HAS_NEW,      AppPropertyType.NEW_BUTTON,     true);
        enableIfInUse(AppPropertyType.HAS_LOAD,     AppPropertyType.LOAD_BUTTON,    true);
        enableIfInUse(AppPropertyType.HAS_CLOSE,    AppPropertyType.CLOSE_BUTTON,   app.getWorkspaceComponent().isActivated());
        enableIfInUse(AppPropertyType.HAS_SAVE,     AppPropertyType.SAVE_BUTTON,    !app.getFileModule().isSaved());
        //enableIfInUse(HAS_EXPORT,   EXPORT_BUTTON,  app.getWorkspaceComponent().isActivated());
        enableIfInUse(AppPropertyType.HAS_EXIT,     AppPropertyType.EXIT_BUTTON,    true);
    }
    
    private void enableIfInUse(Object controlInUseProperty, Object controlProperty, boolean enabled) {
        PropertiesManager props = PropertiesManager.getPropertiesManager();
        if (props.isTrue(controlInUseProperty)) {
            Button button = (Button)app.getGUIModule().getGUINode(controlProperty);
            button.setDisable(!enabled);
        }
    }
}