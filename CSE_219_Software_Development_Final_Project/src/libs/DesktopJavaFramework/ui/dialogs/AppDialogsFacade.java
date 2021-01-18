package libs.DesktopJavaFramework.ui.dialogs;

import libs.DesktopJavaFramework.AppPropertyType;
import static libs.DesktopJavaFramework.AppPropertyType.*;
import libs.DesktopJavaFramework.AppTemplate;
import libs.DesktopJavaFramework.modules.AppRecentWorkModule;
import libs.DesktopJavaFramework.modules.AppLanguageModule;
import libs.DesktopJavaFramework.modules.AppLanguageModule.LanguageException;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Optional;
import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import libs.DesktopJavaFramework.AppPropertyType;
import libs.DesktopJavaFramework.AppTemplate;
import libs.DesktopJavaFramework.modules.AppLanguageModule;
import libs.DesktopJavaFramework.modules.AppRecentWorkModule;
import libs.PropertiesManager.src.PropertiesManager;

/**
 * @author McKillaGorilla
 */
public class AppDialogsFacade {

    public static void showAboutDialog(AppTemplate app) {
        AppWebDialog dialog = new AppWebDialog(app);
        try {
            PropertiesManager props = PropertiesManager.getPropertiesManager();
            String webPath = props.getProperty(AppPropertyType.APP_PATH_WEB);
            String filePath = webPath + props.getProperty(AppPropertyType.APP_ABOUT_PAGE);
            dialog.showWebDialog(filePath);
        } catch (MalformedURLException murle) {
            AppDialogsFacade.showMessageDialog(app.getGUIModule().getWindow(), AppPropertyType.ABOUT_DIALOG_ERROR_TITLE, AppPropertyType.ABOUT_DIALOG_ERROR_CONTENT);
        }
    }

    public static void showExportDialog(AppTemplate app) throws IOException {
        AppWebDialog dialog = new AppWebDialog(app);
        PropertiesManager props = PropertiesManager.getPropertiesManager();
        String filePath = props.getProperty(AppPropertyType.APP_EXPORT_PAGE);
        dialog.showWebDialog(filePath);
         
    }

    public static void showHelpDialog(AppTemplate app) {
        AppWebDialog dialog = new AppWebDialog(app);
        try {
            PropertiesManager props = PropertiesManager.getPropertiesManager();
            String webPath = props.getProperty(AppPropertyType.APP_PATH_WEB);
            String filePath = webPath + props.getProperty(AppPropertyType.APP_HELP_PAGE);
            dialog.showWebDialog(filePath);
        } catch (MalformedURLException murle) {
            AppDialogsFacade.showMessageDialog(app.getGUIModule().getWindow(), AppPropertyType.HELP_DIALOG_ERROR_TITLE, AppPropertyType.HELP_DIALOG_ERROR_CONTENT);
        }
    }

    public static void showLanguageDialog(AppLanguageModule languageSettings) throws AppLanguageModule.LanguageException {
        PropertiesManager props = PropertiesManager.getPropertiesManager();
        String currentLanguage = languageSettings.getCurrentLanguage();
        ArrayList<String> languages = languageSettings.getLanguages();
        ChoiceDialog<String> languageDialog = new ChoiceDialog<>(currentLanguage, languages);
        String title = props.getProperty(AppPropertyType.LANGUAGE_DIALOG_TITLE);
        String headerText = props.getProperty(AppPropertyType.LANGUAGE_DIALOG_HEADER_TEXT);
        String contentText = props.getProperty(AppPropertyType.LANGUAGE_DIALOG_CONTENT_TEXT);
        languageDialog.setTitle(title);
        languageDialog.setHeaderText(headerText);
        languageDialog.setContentText(contentText);
        Optional<String> buttonSelected = languageDialog.showAndWait();
        if (buttonSelected.isPresent()) {
            String selectedLanguage = languageDialog.getSelectedItem();
            if (selectedLanguage != null) {
                languageSettings.setCurrentLanguage(selectedLanguage);
            }
        }
    }

    public static void showMessageDialog(Stage parent, Object titleProperty, Object contentTextProperty) {
        PropertiesManager props = PropertiesManager.getPropertiesManager();
        String title = props.getProperty(titleProperty);
        String contentText = props.getProperty(contentTextProperty);
        Alert messageDialog = new Alert(Alert.AlertType.INFORMATION);
        messageDialog.initOwner(parent);
        messageDialog.initModality(Modality.APPLICATION_MODAL);
        messageDialog.setTitle(title);
        messageDialog.setHeaderText("");
        Label label = new Label(contentText);
        label.setWrapText(true);
        messageDialog.getDialogPane().setContent(label);
        messageDialog.showAndWait();
    }

    public static File showOpenDialog(Stage window, AppPropertyType openTitleProp) {
        FileChooser fc = new FileChooser();
        PropertiesManager props = PropertiesManager.getPropertiesManager();
        String workFile = props.getProperty(AppPropertyType.PATH_WORK);
        fc.setInitialDirectory(new File(workFile));
        fc.setTitle(props.getProperty(AppPropertyType.LOAD_WORK_TITLE));
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter(props.getProperty(AppPropertyType.WORK_FILE_EXT_DESC), props.getProperty(AppPropertyType.WORK_FILE_EXT))
        );
        File selectedFile = fc.showOpenDialog(window);
        return selectedFile;
    }

    public static File showSaveDialog(Stage window, AppPropertyType saveTitleProp) {
        // PROMPT THE USER FOR A FILE NAME
        FileChooser fc = new FileChooser();
        PropertiesManager props = PropertiesManager.getPropertiesManager();
        String workFile = props.getProperty(AppPropertyType.PATH_WORK);
        fc.setInitialDirectory(new File(workFile));
        fc.setTitle(props.getProperty(saveTitleProp));
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter(props.getProperty(AppPropertyType.WORK_FILE_EXT_DESC), props.getProperty(AppPropertyType.WORK_FILE_EXT))
        );
        File selectedFile = fc.showSaveDialog(window);
        return selectedFile;
    }

    public static void showStackTraceDialog(Stage parent, Exception exception,
            Object appErrorTitleProperty,
            Object appErrorContentProperty) {
        // FIRST MAKE THE DIALOG
        Alert stackTraceDialog = new Alert(Alert.AlertType.ERROR);
        stackTraceDialog.initOwner(parent);
        stackTraceDialog.initModality(Modality.APPLICATION_MODAL);
        PropertiesManager props = PropertiesManager.getPropertiesManager();
        stackTraceDialog.setTitle(props.getProperty(appErrorTitleProperty));
        stackTraceDialog.setContentText(props.getProperty(appErrorContentProperty));

        // GET THE TEXT FOR THE STACK TRACE
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);
        String exceptionText = sw.toString();

        // AND PUT THE STACK TRACE IN A TEXT ARA
        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        stackTraceDialog.getDialogPane().setExpandableContent(textArea);

        // OPEN THE DIALOG
        stackTraceDialog.showAndWait();
    }

    public static void showTextInputDialog(Stage parent, 
            Object titleProperty, Object contentProperty, StringProperty nameProp) {
        PropertiesManager props = PropertiesManager.getPropertiesManager();
        String title = props.getProperty(titleProperty);
        String contentText = props.getProperty(contentProperty);
        TextInputDialog textDialog = new TextInputDialog();
        textDialog.initOwner(parent);
        textDialog.initModality(Modality.NONE);
        textDialog.setTitle(title);
        textDialog.setHeaderText(contentText);
        textDialog.setContentText(null);
        textDialog.setOnHidden(e-> {
            String result = textDialog.getResult();
            if (result != null) {
                System.out.println(result);
                nameProp.set(result);
            }
            else 
                nameProp.set("");
        });        
        Platform.runLater(()->textDialog.showAndWait());
    }

    public static String showWelcomeDialog(AppTemplate app) {
        // FIRST LOAD ALL THE RECENT WORK
        AppRecentWorkModule recentWork = app.getRecentWorkModule();
        recentWork.loadRecentWorkList();

        // OPEN THE DIALOG
        AppWelcomeDialog wd = new AppWelcomeDialog(app);
        wd.showAndWait();

        // AND RETURN THE USER SELECTION
        return wd.selectedWorkName;
    }

    public static ButtonType showYesNoCancelDialog(Stage parent, Object titleProperty, Object contentTextProperty) {
        PropertiesManager props = PropertiesManager.getPropertiesManager();
        String title = props.getProperty(titleProperty);
        String contentText = props.getProperty(contentTextProperty);
        Alert confirmationDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationDialog.initOwner(parent);
        confirmationDialog.initModality(Modality.APPLICATION_MODAL);
        confirmationDialog.getButtonTypes().clear();
        confirmationDialog.getButtonTypes().addAll(ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
        confirmationDialog.setTitle(title);
        confirmationDialog.setContentText(contentText);
        Optional<ButtonType> result = confirmationDialog.showAndWait();
        return result.get();
    }
}
