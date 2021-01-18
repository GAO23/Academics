package libs.DesktopJavaFramework.ui.foolproof;

import static libs.DesktopJavaFramework.AppPropertyType.REDO_BUTTON;
import static libs.DesktopJavaFramework.AppPropertyType.UNDO_BUTTON;
import libs.DesktopJavaFramework.AppTemplate;
import libs.DesktopJavaFramework.modules.AppGUIModule;
import javafx.scene.control.Button;
import libs.jTPS.jTPS;

/**
 *
 * @author McKillaGorilla
 */
public class UndoFoolproofDesign implements FoolproofDesign {
    AppTemplate app;
    
    public UndoFoolproofDesign(AppTemplate initApp) {
        app = initApp;
    }    

    @Override
    public void updateControls() {
        jTPS tps = app.getTPS();
        AppGUIModule gui = app.getGUIModule();
        Button undoButton = (Button)gui.getGUINode(UNDO_BUTTON);
        undoButton.setDisable(!tps.hasTransactionToUndo());
        Button redoButton = (Button)gui.getGUINode(REDO_BUTTON);
        redoButton.setDisable(!tps.hasTransactionToRedo());
    }
}