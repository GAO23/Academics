package libs.DesktopJavaFramework.ui.foolproof;

import static libs.DesktopJavaFramework.AppPropertyType.*;
import libs.DesktopJavaFramework.AppTemplate;
import libs.DesktopJavaFramework.components.AppClipboardComponent;
import libs.DesktopJavaFramework.modules.AppGUIModule;
import javafx.scene.control.Button;
import libs.DesktopJavaFramework.AppPropertyType;
import libs.DesktopJavaFramework.AppTemplate;
import libs.DesktopJavaFramework.components.AppClipboardComponent;
import libs.DesktopJavaFramework.modules.AppGUIModule;

/**
 *
 * @author McKillaGorilla
 */
public class ClipboardFoolproofDesign implements FoolproofDesign {
    AppTemplate app;
    
    public ClipboardFoolproofDesign(AppTemplate initApp) {
        app = initApp;
    }    

    @Override
    public void updateControls() {
        AppClipboardComponent clipboard = app.getClipboardComponent();
        AppGUIModule gui = app.getGUIModule();
        Button cutButton = (Button)gui.getGUINode(AppPropertyType.CUT_BUTTON);
        cutButton.setDisable(!clipboard.hasSomethingToCut());
        Button copyButton = (Button)gui.getGUINode(AppPropertyType.COPY_BUTTON);
        copyButton.setDisable(!clipboard.hasSomethingToCopy());
        Button pasteButton = (Button)gui.getGUINode(AppPropertyType.PASTE_BUTTON);
        pasteButton.setDisable(!clipboard.hasSomethingToPaste());
    }
}