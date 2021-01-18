/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package apps.generator.workspace;

import apps.generator.GeneratorApp;
import apps.generator.GeneratorProperty;
import apps.generator.files.GeneratorFiles;
import apps.generator.workspace.controllers.OfficerHoursController;
import apps.generator.workspace.controllers.ScheduleController;
import apps.generator.workspace.controllers.SiteController;
import apps.generator.workspace.dialogs.GeneratorTADialogs;
import apps.generator.workspace.foolproof.GeneratorFoolProofDesign;
import apps.generator.workspace.style.GeneratorStyle;
import libs.DesktopJavaFramework.AppTemplate;
import libs.DesktopJavaFramework.modules.AppFoolproofModule;
import libs.DesktopJavaFramework.components.AppWorkspaceComponent;
import libs.DesktopJavaFramework.modules.AppGUIModule;
import static libs.DesktopJavaFramework.modules.AppGUIModule.ENABLED;
import libs.DesktopJavaFramework.ui.AppNodesBuilder;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import libs.PropertiesManager.src.PropertiesManager;
import apps.generator.data.Lab;
import apps.generator.data.Lecture;
import apps.generator.data.MeetingItemEditOptions;
import apps.generator.data.Recitation;
import apps.generator.data.ScheduleItem;
import apps.generator.data.TeachingAssistantPrototype;
import apps.generator.data.TimeSlot;
import apps.generator.workspace.controllers.MeetingTimesController;
import apps.generator.workspace.controllers.SyllabusController;

import java.awt.*;
import java.io.File;
import java.util.Calendar;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.Pane;

/**
 *
 * @author xgao
 */
public class GeneratorWorkSpace extends AppWorkspaceComponent{
    private SiteController siteController;
    
     public GeneratorWorkSpace(GeneratorApp app) {
        super(app);

        // LAYOUT THE APP
        initLayout();

        // INIT THE EVENT HANDLERS
        initControllers();

        // 
        initFoolproofDesign();

        // INIT DIALOGS
        initDialogs();
    }

    private void initDialogs() {
        GeneratorTADialogs taDialog = new GeneratorTADialogs((GeneratorApp) app);
        app.getGUIModule().addDialog(GeneratorProperty.OH_TA_EDIT_DIALOG, taDialog);
    }

    // THIS HELPER METHOD INITIALIZES ALL THE CONTROLS IN THE WORKSPACE
    private void initLayout() {
        // FIRST LOAD THE FONT FAMILIES FOR THE COMBO BOX
        PropertiesManager props = PropertiesManager.getPropertiesManager();

       // THIS WILL BUILD ALL OF OUR JavaFX COMPONENTS FOR US
        AppNodesBuilder generatorBuilder = app.getGUIModule().getNodesBuilder();
        
        
        // init the tabPane
        TabPane tabContainer = generatorBuilder.buildTabPane(GeneratorProperty.GENERATOR_MAIN_TABPANE, null, GeneratorStyle.CLASS_GENERTAOR_MAIN_TABPANE, ENABLED);
        tabContainer.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
        this.buildSiteTab(tabContainer, generatorBuilder);
        this.buildSyllabusTab(tabContainer, generatorBuilder);
        this.buildMeetingTimesTab(tabContainer, generatorBuilder);
        this.buildOHTab(tabContainer, generatorBuilder);
        this.buildSchduleTab(tabContainer, generatorBuilder);
        // add tabPane as the workspace central pane 
        workspace = new BorderPane();

        // AND PUT EVERYTHING IN THE WORKSPACE
        ((BorderPane) workspace).setCenter(tabContainer);
    }
    
     private void buildSiteTab(TabPane tabContainer, AppNodesBuilder generatorBuilder){
          Tab siteTab = generatorBuilder.buildTab(GeneratorProperty.GENERATOR_SITE_TAB, tabContainer, GeneratorStyle.CLASS_OH_TAB, ENABLED);
          VBox siteTabBox = generatorBuilder.buildTabVBox(GeneratorProperty.GENERATOR_SITE_TAB_BOX, null, GeneratorStyle.CLASS_SITE_TAB_BOX, ENABLED);
          ScrollPane scroll = new ScrollPane();
          scroll.setFitToHeight(false);
          scroll.setFitToWidth(true);
          scroll.setContent(siteTabBox);
          siteTab.setContent(scroll);
          
          VBox siteBannerBox  = generatorBuilder.buildVBox(GeneratorProperty.GENERATOR_SITE_BANNER_HBOX, siteTabBox, GeneratorStyle.CLASS_SITE_VBOX, ENABLED);
          String exportDir =  AppTemplate.PATH_DATA + "export" + File.separator;
          generatorBuilder.buildLabel(GeneratorProperty.GENERATOR_SITE_BANNER_HEADER, siteBannerBox, GeneratorStyle.CLASS_SITE_HEADER_LABEL, ENABLED);
          HBox subjectBox  = generatorBuilder.buildHBox(GeneratorProperty.GENERATOR_SITE_SUBJECTHBOX, siteBannerBox, GeneratorStyle.GENERATOR_SITE_SUBHBOX, ENABLED);
          generatorBuilder.buildLabel(GeneratorProperty.GENERATOR_SITE_SUBJECT_LABEL, subjectBox, GeneratorStyle.CLASS_SITE_LABEL, ENABLED);
          ComboBox combo = generatorBuilder.buildComboBox(GeneratorProperty.GENERATOR_SITE_SUBJECT_COMBOBOX, GeneratorProperty.GENERATOR_SUBEJECT_OPTIONS, GeneratorProperty.GENERATOR_SUBEJECT_DEFAULT ,subjectBox , GeneratorStyle.CLASS_GENERATOR_SITE_COMBOBOX, ENABLED);
          combo.setEditable(true);
          combo.getSelectionModel().select(0);
          exportDir += combo.getValue() + "_";
          generatorBuilder.buildLabel(GeneratorProperty.GENERATOR_SITE_NUMBER_LABEL, subjectBox, GeneratorStyle.CLASS_SITE_LABEL, ENABLED);
          combo = generatorBuilder.buildComboBox(GeneratorProperty.GENERATOR_SITE_NUMBER_COMBOBOX, GeneratorProperty.GENERATOR_NUMBER_OPTIONS, GeneratorProperty.GENERATOR_NUMBER_DEFAULT ,subjectBox , GeneratorStyle.CLASS_GENERATOR_SITE_COMBOBOX, ENABLED);
          combo.setEditable(true);
          combo.getSelectionModel().select(0);
          exportDir += combo.getValue() + "_";
          HBox semesterBox  = generatorBuilder.buildHBox(GeneratorProperty.GENERATOR_SITE_SEMESTER_HBOX, siteBannerBox, GeneratorStyle.GENERATOR_SITE_SUBHBOX, ENABLED);
          generatorBuilder.buildLabel(GeneratorProperty.GENERATOR_SITE_SEMESTER_LABEL, semesterBox, GeneratorStyle.CLASS_SITE_LABEL, ENABLED);
          combo = generatorBuilder.buildComboBox(GeneratorProperty.GENERATOR_SITE_SEMESTER_COMBOBOX, GeneratorProperty.GENERATOR_SEMESTER_OPTIONS, GeneratorProperty.GENERATOR_SEMESTER_DEFAULT ,semesterBox , GeneratorStyle.CLASS_GENERATOR_SITE_COMBOBOX, ENABLED);
          combo.getSelectionModel().select(0);
          exportDir += combo.getValue() + "_";
          generatorBuilder.buildLabel(GeneratorProperty.GENERATOR_SITE_YEAR_LABEL, semesterBox, GeneratorStyle.CLASS_SITE_LABEL, ENABLED);
          combo = generatorBuilder.buildComboBox(GeneratorProperty.GENERATOR_SITE_YEAR_COMBOBOX, GeneratorProperty.GENERATOR_YEAR_OPTIONS, GeneratorProperty.GENERATOR_YEAR_DEFAULT ,semesterBox , GeneratorStyle.CLASS_GENERATOR_SITE_COMBOBOX, ENABLED);
          int year = Calendar.getInstance().get(Calendar.YEAR);
          combo.getItems().clear();
          combo.getItems().add(Integer.toString(year));
          combo.getItems().add(Integer.toString(year+1));
          combo.getSelectionModel().select(0);
          exportDir += combo.getValue()+File.separator+"CourseSite";
          HBox exportBox  = generatorBuilder.buildHBox(GeneratorProperty.GENERATOR_SITE_EXPORT_BOX, siteBannerBox, GeneratorStyle.GENERATOR_SITE_SUBHBOX, ENABLED);
          Label exLabel = generatorBuilder.buildLabel(GeneratorProperty.GENERATOR_SITE_EXPORT_LABEL, exportBox, GeneratorStyle.CLASS_SITE_EXPORT_LABEL, ENABLED);
          exLabel.setText(exportDir);
          
          HBox sitePageBox  = generatorBuilder.buildHBox(GeneratorProperty.GENERATOR_PAGE_HBOX, siteTabBox, GeneratorStyle.CLASS_SITE_PAGE_BOX, ENABLED);
          generatorBuilder.buildLabel(GeneratorProperty.GENERATOR_SITE_PAGE_HEADER, sitePageBox, GeneratorStyle.CLASS_SITE_HEADER_LABEL, ENABLED);
          CheckBox check = generatorBuilder.buildCheckBox(GeneratorProperty.GENERATOR_SITE_HOME_CHECKBOX, sitePageBox, GeneratorStyle.CLASS_SITE_CHECKBOX, ENABLED);
          check.setSelected(true);
          check = generatorBuilder.buildCheckBox(GeneratorProperty.GENERATOR_SITE_SYLLABUS_CHECKBOX, sitePageBox, GeneratorStyle.CLASS_SITE_CHECKBOX, ENABLED);
          check.setSelected(true);
          check = generatorBuilder.buildCheckBox(GeneratorProperty.GENERATOR_SITE_SCHEDULE_CHECKBOX, sitePageBox, GeneratorStyle.CLASS_SITE_CHECKBOX, ENABLED);
          check.setSelected(true);
          check = generatorBuilder.buildCheckBox(GeneratorProperty.GENERATOR_SITE_HOMEWORK_CHECKBOX, sitePageBox, GeneratorStyle.CLASS_SITE_CHECKBOX, ENABLED);
          check.setSelected(true);
          
           VBox siteStyleBox  = generatorBuilder.buildVBox(GeneratorProperty.GENERATOR_SITE_STYLE_HBOX, siteTabBox, GeneratorStyle.CLASS_SITE_VBOX, ENABLED);
           generatorBuilder.buildLabel(GeneratorProperty.GENERATOR_SITE_STYLE_HEADER, siteStyleBox, GeneratorStyle.CLASS_SITE_HEADER_LABEL, ENABLED);
           HBox faviconBox  = generatorBuilder.buildHBox(GeneratorProperty.GENERATOR_SITE_FAVICON_BOX, siteStyleBox, GeneratorStyle.GENERATOR_SITE_SUBHBOX, ENABLED);
           generatorBuilder.buildTextButton(GeneratorProperty.SITE_FAVICON_BUTTON, faviconBox, GeneratorStyle.CLASS_SITE_TEXT_BUTTON, ENABLED);
           generatorBuilder.buildIconButton(GeneratorProperty.SITE_FAVICON, faviconBox, GeneratorStyle.CLASS_SITE_IMAGE_ICON_BUTTON, ENABLED);
           HBox naviBarBox  = generatorBuilder.buildHBox(GeneratorProperty.GENERATOR_SITE_NAV_BOX, siteStyleBox, GeneratorStyle.GENERATOR_SITE_SUBHBOX, ENABLED);
           generatorBuilder.buildTextButton(GeneratorProperty.SITE_NAVBAR_BUTTON, naviBarBox, GeneratorStyle.CLASS_SITE_TEXT_BUTTON, ENABLED);
           generatorBuilder.buildIconButton(GeneratorProperty.SITE_NAVICON, naviBarBox, GeneratorStyle.CLASS_SITE_IMAGE_ICON_BUTTON, ENABLED);
           HBox leftFooterBox  = generatorBuilder.buildHBox(GeneratorProperty.GENERATOR_SITE_LEFT_FOOTER_BOX, siteStyleBox, GeneratorStyle.GENERATOR_SITE_SUBHBOX, ENABLED);
           generatorBuilder.buildTextButton(GeneratorProperty.SITE_LEFT_FOOTER_BUTTON, leftFooterBox, GeneratorStyle.CLASS_SITE_TEXT_BUTTON, ENABLED);
           generatorBuilder.buildIconButton(GeneratorProperty.SITE_LEFT_FOOTERCON, leftFooterBox, GeneratorStyle.CLASS_SITE_IMAGE_ICON_BUTTON, ENABLED);
           HBox rightFooterBox  = generatorBuilder.buildHBox(GeneratorProperty.GENERATOR_SITE_RIGHT_FOOTER_BOX, siteStyleBox, GeneratorStyle.GENERATOR_SITE_SUBHBOX, ENABLED);
           generatorBuilder.buildTextButton(GeneratorProperty.SITE_RIGHT_FOOTER_BUTTON, rightFooterBox, GeneratorStyle.CLASS_SITE_TEXT_BUTTON, ENABLED);
           generatorBuilder.buildIconButton(GeneratorProperty.SITE_RIGHT_FOOTERCON, rightFooterBox, GeneratorStyle.CLASS_SITE_IMAGE_ICON_BUTTON, ENABLED);
           HBox fontsBox  = generatorBuilder.buildHBox(GeneratorProperty.GENERATOR_SITE_FONTS_BOX, siteStyleBox, GeneratorStyle.GENERATOR_SITE_SUBHBOX, ENABLED);
           generatorBuilder.buildLabel(GeneratorProperty.GENERATOR_SITE_FONTCOLOR_SUBHEADER, fontsBox, GeneratorStyle.CLASS_SITE_LABEL, ENABLED);
           ComboBox cssCombo = generatorBuilder.buildComboBox(GeneratorProperty.GENERATOR_SITE_CSS_COMBOBOX, GeneratorProperty.GENERATOR_CSS_OPTIONS, GeneratorProperty.GENERATOR_CSS_DEFAULT ,fontsBox , GeneratorStyle.CLASS_GENERATOR_SITE_COMBOBOX, ENABLED);
           this.loadDynamicCSSCombo(cssCombo);
           HBox noteBox  = generatorBuilder.buildHBox(GeneratorProperty.GENERATOR_SITE_NOTE_BOX, siteStyleBox, GeneratorStyle.GENERATOR_SITE_SUBHBOX, ENABLED);
           generatorBuilder.buildLabel(GeneratorProperty.GENERATOR_SITE_NOTE_SUBHEADER, noteBox, GeneratorStyle.CLASS_SITE_LABEL, ENABLED);
           
           VBox siteInstructorBox  = generatorBuilder.buildVBox(GeneratorProperty.GENERATOR_SITE_INSTRUCTOR_HBOX, siteTabBox, GeneratorStyle.CLASS_SITE_VBOX, ENABLED);
           generatorBuilder.buildLabel(GeneratorProperty.GENERATOR_SITE_INSTRUCTOR_HEADER, siteInstructorBox, GeneratorStyle.CLASS_SITE_HEADER_LABEL, ENABLED);
           HBox nameBox  = generatorBuilder.buildHBox(GeneratorProperty.GENERATOR_SITE_NAME_BOX, siteInstructorBox, GeneratorStyle.GENERATOR_SITE_SUBHBOX, ENABLED);
           generatorBuilder.buildLabel(GeneratorProperty.GENERATOR_SITE_NAME_LABEL, nameBox, GeneratorStyle.CLASS_SITE_LABEL, ENABLED);
           generatorBuilder.buildTextField(GeneratorProperty.GENERATOR_SITE_NAME_TEXTFIELD, nameBox, GeneratorStyle.CLASS_SITE_TEXT_FIELD, ENABLED);
           generatorBuilder.buildLabel(GeneratorProperty.GENERATOR_SITE_ROOM_LABEL, nameBox, GeneratorStyle.CLASS_SITE_LABEL, ENABLED);
           generatorBuilder.buildTextField(GeneratorProperty.GENERATOR_SITE_ROOM_TEXTFIELD, nameBox, GeneratorStyle.CLASS_SITE_TEXT_FIELD, ENABLED);
           HBox emailBox  = generatorBuilder.buildHBox(GeneratorProperty.GENERATOR_SITE_EMAIL_BOX, siteInstructorBox, GeneratorStyle.GENERATOR_SITE_SUBHBOX, ENABLED);
           generatorBuilder.buildLabel(GeneratorProperty.GENERATOR_SITE_EMAIL_LABEL, emailBox, GeneratorStyle.CLASS_SITE_LABEL, ENABLED);
           generatorBuilder.buildTextField(GeneratorProperty.GENERATOR_SITE_EMAIL_TEXTFIELD, emailBox, GeneratorStyle.CLASS_SITE_TEXT_FIELD, ENABLED);
           generatorBuilder.buildLabel(GeneratorProperty.GENERATOR_SITE_HOME_PAGE_LABEL, emailBox, GeneratorStyle.CLASS_SITE_LABEL, ENABLED);
           generatorBuilder.buildTextField(GeneratorProperty.GENERATOR_SITE_HOME_PAGE_TEXTFIELD, emailBox, GeneratorStyle.CLASS_SITE_TEXT_FIELD, ENABLED);
           HBox expandBox  = generatorBuilder.buildHBox(GeneratorProperty.GENERATOR_SITE_EXPAND_BOX, siteInstructorBox, GeneratorStyle.GENERATOR_SITE_SUBHBOX, ENABLED);
           Button expand = generatorBuilder.buildTextButton(GeneratorProperty.GENERATOR_SITE_OFFICE_HOURS_EXPAND_BUTTON, expandBox, GeneratorStyle.CLASS_GENERATOR_EXPAND_BUTTON, ENABLED);
           expand.setText("+");
           generatorBuilder.buildLabel(GeneratorProperty.GENERATOR_SITE_OFFICE_HOURS_LABEL, expandBox, GeneratorStyle.CLASS_SITE_LABEL, ENABLED);
           HBox textAreaBox  = generatorBuilder.buildHBox(GeneratorProperty.GENERATOR_SITE_TEXT_AREA_BOX, null, GeneratorStyle.GENERATOR_SITE_SUBHBOX, ENABLED);
           generatorBuilder.buildTextArea(GeneratorProperty.GENERATOR_SITE_OFFICE_HOURS_TEXT_AREA, textAreaBox, GeneratorStyle.CLASS_GENERATOR_TEXTAREA, ENABLED);
           GeneratorFiles fileComponent = (GeneratorFiles) app.getFileComponent();
           fileComponent.loadComboTextFieldOption();
    }
     
     
     private void buildSyllabusTab(TabPane tabContainer, AppNodesBuilder generatorBuilder){
         Tab syllabusTab = generatorBuilder.buildTab(GeneratorProperty.GENERATOR_SYLLABUS_TAB, tabContainer, GeneratorStyle.CLASS_OH_TAB, ENABLED);
         VBox tabBox = generatorBuilder.buildTabVBox(GeneratorProperty.GENERATOR_SYLLABUS_TAB_BOX, null, GeneratorStyle.CLASS_SYLLABUS_TAB_BOX, ENABLED);
         ScrollPane scroll = new ScrollPane();
         scroll.setFitToHeight(false);
         scroll.setFitToWidth(true);
         scroll.setContent(tabBox);
         syllabusTab.setContent(scroll);
         
         
         VBox descriptionBox = generatorBuilder.buildVBox(GeneratorProperty.GENERATOR_SYLLABUS_DESCRIPTION_BOX, tabBox, GeneratorStyle.CLASS_SYLLABUS_SUB_VBOX, ENABLED);
         HBox descriptionExpandBox = generatorBuilder.buildHBox(GeneratorProperty.GENERATOR_SYLLABUS_DECSRIPTION_EXPAND_BOX, descriptionBox, GeneratorStyle.CLASS_SYLLABUS_EXPAND_HBOX, ENABLED);
         Button expand1 = generatorBuilder.buildTextButton(GeneratorProperty.GENERATOR_SYLLABUS_EXPAND_BUTTON_DESCRIPTION, descriptionExpandBox, GeneratorStyle.CLASS_GENERATOR_EXPAND_BUTTON, ENABLED);
         expand1.setText("-");
         generatorBuilder.buildLabel(GeneratorProperty.GENERATOR_SYLLABUS_DESCRIPTION_LABEL, descriptionExpandBox, GeneratorStyle.CLASS_SYLLABUS_EXPAND_LABEL, ENABLED);
         generatorBuilder.buildTextArea(GeneratorProperty.GENERATOR_SYLLABUS_AREA_DECSRCIPTION, descriptionBox, GeneratorStyle.CLASS_SYLLABUS_TEXT_AREA, ENABLED);
         
         VBox topicBox = generatorBuilder.buildVBox(GeneratorProperty.GENERATOR_SYLLABUS_TOPIC_BOX, tabBox, GeneratorStyle.CLASS_SYLLABUS_SUB_VBOX, ENABLED);
         HBox topicExpandBox = generatorBuilder.buildHBox(GeneratorProperty.GENERATOR_SYLLABUS_TOPIC_EXPAND_BOX, topicBox, GeneratorStyle.CLASS_SYLLABUS_EXPAND_HBOX, ENABLED);
         Button expand2 = generatorBuilder.buildTextButton(GeneratorProperty.GENERATOR_SYLLABUS_EXPAND_BUTTON_TOPIC, topicExpandBox, GeneratorStyle.CLASS_GENERATOR_EXPAND_BUTTON, ENABLED);
         expand2.setText("+");
         generatorBuilder.buildLabel(GeneratorProperty.GENERATOR_SYLLABUS_TOPIC_LABEL, topicExpandBox, GeneratorStyle.CLASS_SYLLABUS_EXPAND_LABEL, ENABLED);
         generatorBuilder.buildTextArea(GeneratorProperty.GENERATOR_SYLLABUS_AREA_TOPIC, null, GeneratorStyle.CLASS_SYLLABUS_TEXT_AREA, ENABLED);
         
         VBox perequistiteBox = generatorBuilder.buildVBox(GeneratorProperty.GENERATOR_SYLLABUS_PERE_BOX, tabBox, GeneratorStyle.CLASS_SYLLABUS_SUB_VBOX, ENABLED);
         HBox pereExpandBox = generatorBuilder.buildHBox(GeneratorProperty.GENERATOR_SYLLABUS_PERE_EXPAND_BOX, perequistiteBox, GeneratorStyle.CLASS_SYLLABUS_EXPAND_HBOX, ENABLED);
         Button expand3 = generatorBuilder.buildTextButton(GeneratorProperty.GENERATOR_SYLLABUS_EXPAND_BUTTON_PERE, pereExpandBox, GeneratorStyle.CLASS_GENERATOR_EXPAND_BUTTON, ENABLED);
         expand3.setText("+");
         generatorBuilder.buildLabel(GeneratorProperty.GENERATOR_SYLLABUS_PERE_LABEL, pereExpandBox, GeneratorStyle.CLASS_SYLLABUS_EXPAND_LABEL, ENABLED);
         generatorBuilder.buildTextArea(GeneratorProperty.GENERATOR_SYLLABUS_AREA_PERE, null, GeneratorStyle.CLASS_SYLLABUS_TEXT_AREA, ENABLED);
         
         VBox outBox = generatorBuilder.buildVBox(GeneratorProperty.GENERATOR_SYLLABUS_OUT_BOX, tabBox, GeneratorStyle.CLASS_SYLLABUS_SUB_VBOX, ENABLED);
         HBox outExpandBox = generatorBuilder.buildHBox(GeneratorProperty.GENERATOR_SYLLABUS_OUT_EXPAND_BOX, outBox, GeneratorStyle.CLASS_SYLLABUS_EXPAND_HBOX, ENABLED);
         Button expand4 = generatorBuilder.buildTextButton(GeneratorProperty.GENERATOR_SYLLABUS_EXPAND_BUTTON_OUT, outExpandBox, GeneratorStyle.CLASS_GENERATOR_EXPAND_BUTTON, ENABLED);
         expand4.setText("+");
         generatorBuilder.buildLabel(GeneratorProperty.GENERATOR_SYLLABUS_OUT_LABEL, outExpandBox, GeneratorStyle.CLASS_SYLLABUS_EXPAND_LABEL, ENABLED);
         generatorBuilder.buildTextArea(GeneratorProperty.GENERATOR_SYLLABUS_AREA_OUT, null, GeneratorStyle.CLASS_SYLLABUS_TEXT_AREA, ENABLED);
         
         VBox bookBox = generatorBuilder.buildVBox(GeneratorProperty.GENERATOR_SYLLABUS_BOOK_BOX, tabBox, GeneratorStyle.CLASS_SYLLABUS_SUB_VBOX, ENABLED);
         HBox bookExpandBox = generatorBuilder.buildHBox(GeneratorProperty.GENERATOR_SYLLABUS_BOOK_EXPAND_BOX, bookBox, GeneratorStyle.CLASS_SYLLABUS_EXPAND_HBOX, ENABLED);
         Button expand5 = generatorBuilder.buildTextButton(GeneratorProperty.GENERATOR_SYLLABUS_EXPAND_BUTTON_BOOK, bookExpandBox, GeneratorStyle.CLASS_GENERATOR_EXPAND_BUTTON, ENABLED);
         expand5.setText("+");
         generatorBuilder.buildLabel(GeneratorProperty.GENERATOR_SYLLABUS_BOOK_LABEL, bookExpandBox, GeneratorStyle.CLASS_SYLLABUS_EXPAND_LABEL, ENABLED);
         generatorBuilder.buildTextArea(GeneratorProperty.GENERATOR_SYLLABUS_AREA_BOOK, null, GeneratorStyle.CLASS_SYLLABUS_TEXT_AREA, ENABLED);
         
         VBox gradeBox = generatorBuilder.buildVBox(GeneratorProperty.GENERATOR_SYLLABUS_GRADE_BOX, tabBox, GeneratorStyle.CLASS_SYLLABUS_SUB_VBOX, ENABLED);
         HBox bookGradeBox = generatorBuilder.buildHBox(GeneratorProperty.GENERATOR_SYLLABUS_GRADE_EXPAND_BOX, gradeBox, GeneratorStyle.CLASS_SYLLABUS_EXPAND_HBOX, ENABLED);
         Button expand6 = generatorBuilder.buildTextButton(GeneratorProperty.GENERATOR_SYLLABUS_EXPAND_BUTTON_GRADE, bookGradeBox, GeneratorStyle.CLASS_GENERATOR_EXPAND_BUTTON, ENABLED);
         expand6.setText("+");
         generatorBuilder.buildLabel(GeneratorProperty.GENERATOR_SYLLABUS_GRADE_LABEL, bookGradeBox, GeneratorStyle.CLASS_SYLLABUS_EXPAND_LABEL, ENABLED);
         generatorBuilder.buildTextArea(GeneratorProperty.GENERATOR_SYLLABUS_AREA_GRADE, null, GeneratorStyle.CLASS_SYLLABUS_TEXT_AREA, ENABLED);
         
         VBox noteBox = generatorBuilder.buildVBox(GeneratorProperty.GENERATOR_SYLLABUS_NOTE_BOX, tabBox, GeneratorStyle.CLASS_SYLLABUS_SUB_VBOX, ENABLED);
         HBox noteExpandBox = generatorBuilder.buildHBox(GeneratorProperty.GENERATOR_SYLLABUS_NOTE_EXPAND_BOX, noteBox, GeneratorStyle.CLASS_SYLLABUS_EXPAND_HBOX, ENABLED);
         Button expand7 = generatorBuilder.buildTextButton(GeneratorProperty.GENERATOR_SYLLABUS_EXPAND_BUTTON_NOTE, noteExpandBox, GeneratorStyle.CLASS_GENERATOR_EXPAND_BUTTON, ENABLED);
         expand7.setText("+");
         generatorBuilder.buildLabel(GeneratorProperty.GENERATOR_SYLLABUS_NOTE_LABEL, noteExpandBox, GeneratorStyle.CLASS_SYLLABUS_EXPAND_LABEL, ENABLED);
         generatorBuilder.buildTextArea(GeneratorProperty.GENERATOR_SYLLABUS_AREA_NOTE, null, GeneratorStyle.CLASS_SYLLABUS_TEXT_AREA, ENABLED);
         
         VBox dishonestyBox = generatorBuilder.buildVBox(GeneratorProperty.GENERATOR_SYLLABUS_DISHONESTY_BOX, tabBox, GeneratorStyle.CLASS_SYLLABUS_SUB_VBOX, ENABLED);
         HBox dishonestyExpandBox = generatorBuilder.buildHBox(GeneratorProperty.GENERATOR_SYLLABUS_DISHONESTY_EXPAND_BOX, dishonestyBox, GeneratorStyle.CLASS_SYLLABUS_EXPAND_HBOX, ENABLED);
         Button expand8 = generatorBuilder.buildTextButton(GeneratorProperty.GENERATOR_SYLLABUS_EXPAND_BUTTON_DISHONESTY, dishonestyExpandBox, GeneratorStyle.CLASS_GENERATOR_EXPAND_BUTTON, ENABLED);
         expand8.setText("+");
         generatorBuilder.buildLabel(GeneratorProperty.GENERATOR_SYLLABUS_DISHONESTY_LABEL, dishonestyExpandBox, GeneratorStyle.CLASS_SYLLABUS_EXPAND_LABEL, ENABLED);
         generatorBuilder.buildTextArea(GeneratorProperty.GENERATOR_SYLLABUS_AREA_DISHONESTY, null, GeneratorStyle.CLASS_SYLLABUS_TEXT_AREA, ENABLED);
         
         VBox spBox = generatorBuilder.buildVBox(GeneratorProperty.GENERATOR_SYLLABUS_ASIS_BOX, tabBox, GeneratorStyle.CLASS_SYLLABUS_SUB_VBOX, ENABLED);
         HBox spExpandBox = generatorBuilder.buildHBox(GeneratorProperty.GENERATOR_SYLLABUS_ASIS_EXPAND_BOX, spBox, GeneratorStyle.CLASS_SYLLABUS_EXPAND_HBOX, ENABLED);
         Button expand9 = generatorBuilder.buildTextButton(GeneratorProperty.GENERATOR_SYLLABUS_EXPAND_BUTTON_ASIS, spExpandBox, GeneratorStyle.CLASS_GENERATOR_EXPAND_BUTTON, ENABLED);
         expand9.setText("+");
         generatorBuilder.buildLabel(GeneratorProperty.GENERATOR_SYLLABUS_ASIS_LABEL, spExpandBox, GeneratorStyle.CLASS_SYLLABUS_EXPAND_LABEL, ENABLED);
         generatorBuilder.buildTextArea(GeneratorProperty.GENERATOR_SYLLABUS_AREA_ASIS, null, GeneratorStyle.CLASS_SYLLABUS_TEXT_AREA, ENABLED);

         
     }
    
    
    
    
    
    
    private void buildOHTab(TabPane tabContainer, AppNodesBuilder generatorBuilder){
         
        Tab ohTab = generatorBuilder.buildTab(GeneratorProperty.GENERATOR_OH_TAB, tabContainer, GeneratorStyle.CLASS_OH_TAB, ENABLED);
         VBox ohTabVbox = generatorBuilder.buildTabVBox(GeneratorProperty.GENERATOR_OH_TAB_VBOX, null, GeneratorStyle.CLASS_OH_TAB_BOX_PANE, ENABLED);
         ScrollPane scroll = new ScrollPane();
         scroll.setFitToHeight(false);
         scroll.setFitToWidth(true);
         scroll.setContent(ohTabVbox);
         ohTab.setContent(scroll);
        
        VBox leftPane = generatorBuilder.buildVBox(GeneratorProperty.OH_LEFT_PANE, ohTabVbox, GeneratorStyle.CLASS_OH_PANE, ENABLED);
        HBox tasHeaderBox = generatorBuilder.buildHBox(GeneratorProperty.OH_TAS_HEADER_PANE, leftPane, GeneratorStyle.CLASS_OH_HEADER_HBOX, ENABLED);
        generatorBuilder.buildTextButton(GeneratorProperty.GENERATOR_OH_TA_EXPAND_BUTTON, tasHeaderBox, GeneratorStyle.CLASS_GENERATOR_EXPAND_BUTTON, ENABLED);
        generatorBuilder.buildLabel(GeneratorProperty.OH_TAS_HEADER_LABEL, tasHeaderBox, GeneratorStyle.CLASS_OH_HEADER_LABEL, ENABLED);
        HBox typeHeaderBox = generatorBuilder.buildHBox(GeneratorProperty.OH_GRAD_UNDERGRAD_TAS_PANE, tasHeaderBox, GeneratorStyle.CLASS_OH_RADIO_BOX, ENABLED);
        ToggleGroup tg = new ToggleGroup();
        generatorBuilder.buildRadioButton(GeneratorProperty.OH_ALL_RADIO_BUTTON, typeHeaderBox, GeneratorStyle.CLASS_OH_RADIO_BUTTON, ENABLED, tg, true);
        generatorBuilder.buildRadioButton(GeneratorProperty.OH_GRAD_RADIO_BUTTON, typeHeaderBox, GeneratorStyle.CLASS_OH_RADIO_BUTTON, ENABLED, tg, false);
        generatorBuilder.buildRadioButton(GeneratorProperty.OH_UNDERGRAD_RADIO_BUTTON, typeHeaderBox, GeneratorStyle.CLASS_OH_RADIO_BUTTON, ENABLED, tg, false);

        // MAKE THE TABLE AND SETUP THE DATA MODEL
        TableView<TeachingAssistantPrototype> taTable = generatorBuilder.buildTableView(GeneratorProperty.OH_TAS_TABLE_VIEW, leftPane, GeneratorStyle.CLASS_OH_TABLE_VIEW, ENABLED);
        taTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        TableColumn nameColumn = generatorBuilder.buildTableColumn(GeneratorProperty.OH_NAME_TABLE_COLUMN, taTable, GeneratorStyle.CLASS_OH_COLUMN);
        TableColumn emailColumn = generatorBuilder.buildTableColumn(GeneratorProperty.OH_EMAIL_TABLE_COLUMN, taTable, GeneratorStyle.CLASS_OH_COLUMN);
        TableColumn slotsColumn = generatorBuilder.buildTableColumn(GeneratorProperty.OH_SLOTS_TABLE_COLUMN, taTable, GeneratorStyle.CLASS_OH_CENTERED_COLUMN);
        TableColumn typeColumn = generatorBuilder.buildTableColumn(GeneratorProperty.OH_TYPE_TABLE_COLUMN, taTable, GeneratorStyle.CLASS_OH_COLUMN);
        nameColumn.setCellValueFactory(new PropertyValueFactory<String, String>("name"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<String, String>("email"));
        slotsColumn.setCellValueFactory(new PropertyValueFactory<String, String>("slots"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<String, String>("type"));
        nameColumn.prefWidthProperty().bind(taTable.widthProperty().multiply(1.0 / 5.0));
        emailColumn.prefWidthProperty().bind(taTable.widthProperty().multiply(2.0 / 5.0));
        slotsColumn.prefWidthProperty().bind(taTable.widthProperty().multiply(1.0 / 5.0));
        typeColumn.prefWidthProperty().bind(taTable.widthProperty().multiply(1.0 / 5.0));

        // ADD BOX FOR ADDING A TA
        HBox taBox = generatorBuilder.buildHBox(GeneratorProperty.OH_ADD_TA_PANE, leftPane, GeneratorStyle.CLASS_OH_ADD_TA_PANE, ENABLED);
        generatorBuilder.buildTextField(GeneratorProperty.OH_NAME_TEXT_FIELD, taBox, GeneratorStyle.CLASS_OH_TEXT_FIELD, ENABLED);
        generatorBuilder.buildTextField(GeneratorProperty.OH_EMAIL_TEXT_FIELD, taBox, GeneratorStyle.CLASS_OH_TEXT_FIELD, ENABLED);
        generatorBuilder.buildTextButton(GeneratorProperty.OH_ADD_TA_BUTTON, taBox, GeneratorStyle.CLASS_OH_BUTTON, !ENABLED);

        

        // INIT THE HEADER ON THE RIGHT
        VBox rightPane = generatorBuilder.buildVBox(GeneratorProperty.OH_RIGHT_PANE, ohTabVbox, GeneratorStyle.CLASS_OH_PANE, ENABLED);
        HBox officeHoursHeaderBox = generatorBuilder.buildHBox(GeneratorProperty.OH_OFFICE_HOURS_HEADER_PANE, rightPane, GeneratorStyle.CLASS_OH_HEADER_HBOX, ENABLED);
        generatorBuilder.buildLabel(GeneratorProperty.OH_OFFICE_HOURS_HEADER_LABEL, officeHoursHeaderBox, GeneratorStyle.CLASS_OH_HEADER_LABEL, ENABLED);
        generatorBuilder.buildLabel(GeneratorProperty.GENERATOR_OH_START_TIME_LABEL, officeHoursHeaderBox, GeneratorStyle.CLASS_OH_TIME_LABEL, ENABLED);
        generatorBuilder.buildComboBox(GeneratorProperty.GENERATOR_OH_START_TIME_COMBO, GeneratorProperty.GENERATOR_FIRST_COMBO_OPTION , GeneratorProperty.GENERATOR_OH_START_TIME_DEFAULT , officeHoursHeaderBox, GeneratorStyle.CLASS_OH_COMBOBOX, ENABLED);
        generatorBuilder.buildLabel(GeneratorProperty.GENERATOR_OH_END_TIME_LABEL, officeHoursHeaderBox, GeneratorStyle.CLASS_OH_TIME_LABEL, ENABLED);
        generatorBuilder.buildComboBox(GeneratorProperty.GENERATOR_OH_END_TIME_COMBO, GeneratorProperty.GENERATOR_SECOND_COMBO_OPTION , GeneratorProperty.GENERATOR_OH_END_TIME_DEFAULT, officeHoursHeaderBox, GeneratorStyle.CLASS_OH_COMBOBOX, ENABLED);

        // SETUP THE OFFICE HOURS TABLE
        TableView<TimeSlot> officeHoursTable = generatorBuilder.buildTableView(GeneratorProperty.OH_OFFICE_HOURS_TABLE_VIEW, rightPane, GeneratorStyle.CLASS_OH_OFFICE_HOURS_TABLE_VIEW, ENABLED);
        setupOfficeHoursColumn(GeneratorProperty.OH_START_TIME_TABLE_COLUMN, officeHoursTable, GeneratorStyle.CLASS_OH_TIME_COLUMN, "startTime");
        setupOfficeHoursColumn(GeneratorProperty.OH_END_TIME_TABLE_COLUMN, officeHoursTable, GeneratorStyle.CLASS_OH_TIME_COLUMN, "endTime");
        setupOfficeHoursColumn(GeneratorProperty.OH_MONDAY_TABLE_COLUMN, officeHoursTable, GeneratorStyle.CLASS_OH_DAY_OF_WEEK_COLUMN, "monday");
        setupOfficeHoursColumn(GeneratorProperty.OH_TUESDAY_TABLE_COLUMN, officeHoursTable, GeneratorStyle.CLASS_OH_DAY_OF_WEEK_COLUMN, "tuesday");
        setupOfficeHoursColumn(GeneratorProperty.OH_WEDNESDAY_TABLE_COLUMN, officeHoursTable, GeneratorStyle.CLASS_OH_DAY_OF_WEEK_COLUMN, "wednesday");
        setupOfficeHoursColumn(GeneratorProperty.OH_THURSDAY_TABLE_COLUMN, officeHoursTable, GeneratorStyle.CLASS_OH_DAY_OF_WEEK_COLUMN, "thursday");
        setupOfficeHoursColumn(GeneratorProperty.OH_FRIDAY_TABLE_COLUMN, officeHoursTable, GeneratorStyle.CLASS_OH_DAY_OF_WEEK_COLUMN, "friday");
        // MAKE SURE IT'S THE TABLE THAT ALWAYS GROWS IN THE LEFT PANE
        VBox.setVgrow(leftPane, Priority.ALWAYS);
        VBox.setVgrow(rightPane, Priority.ALWAYS);
        
    }
    
    private void buildMeetingTimesTab(TabPane tabContainer, AppNodesBuilder generatorBuilder){
         Tab meetingTab = generatorBuilder.buildTab(GeneratorProperty.GENERATOR_MEETING_TAB, tabContainer, GeneratorStyle.CLASS_OH_TAB, ENABLED);
         VBox tabBox = generatorBuilder.buildTabVBox(GeneratorProperty.GENERATOR_MEETING_TAB_BOX, null, GeneratorStyle.CLASS_SYLLABUS_TAB_BOX, ENABLED);
         ScrollPane scroll = new ScrollPane();
         scroll.setFitToHeight(false);
         scroll.setFitToWidth(true);
         scroll.setContent(tabBox);
         meetingTab.setContent(scroll);
         
         VBox lectureBox = generatorBuilder.buildVBox(GeneratorProperty.GENERATOR_MEETING_LECTURE_BOX, tabBox, GeneratorStyle.CLASS_MEETING_SUB_VBOX, ENABLED);
         HBox lectureHeader = generatorBuilder.buildHBox(GeneratorProperty.GENERATOR_MEETING_LECTURE_HEADER_BOX, lectureBox, GeneratorStyle.CLASS_MEETING_HEADER_HBOX, ENABLED);
         Button lecturePlus = generatorBuilder.buildTextButton(GeneratorProperty.GENERATOR_MEETING_LECTURE_PLUS_BUTTON, lectureHeader, GeneratorStyle.CLASS_GENERATOR_MINUS_BUTTON, ENABLED);
         lecturePlus.setText("+");
         Button lectureMinus = generatorBuilder.buildTextButton(GeneratorProperty.GENERATOR_MEETING_LECTURE_MINUS_BUTTON, lectureHeader, GeneratorStyle.CLASS_GENERATOR_MINUS_BUTTON, ENABLED);
         lectureMinus.setText("-");
         generatorBuilder.buildLabel(GeneratorProperty.GENERATOR_MEETING_LECTURE_LABEL, lectureHeader, GeneratorStyle.CLASS_MEETING_HEADER_LABEL, ENABLED);
         TableView<Lecture> lectureTable = generatorBuilder.buildTableView(GeneratorProperty.GENERATOR_LECTURE_TABLE_VIEW, lectureBox, GeneratorStyle.CLASS_GENERATOR_TABLE_VIEW, ENABLED);
         lectureTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
         lectureTable.setEditable(true);
         TableColumn secColumn = generatorBuilder.buildTableColumn(GeneratorProperty.GENERATOR_SEC_TABLE_COLUMN, lectureTable, GeneratorStyle.CLASS_GENERATOR_TABLE_COLUMN);
         TableColumn dayColumn = generatorBuilder.buildTableColumn(GeneratorProperty.GENERATOR_DAY_TABLE_COLUMN, lectureTable, GeneratorStyle.CLASS_GENERATOR_TABLE_COLUMN);
         TableColumn timeColumn = generatorBuilder.buildTableColumn(GeneratorProperty.GENERATOR_TIME_TABLE_COLUMN, lectureTable, GeneratorStyle.CLASS_GENERATOR_TABLE_COLUMN);
         TableColumn roomColumn = generatorBuilder.buildTableColumn(GeneratorProperty.GENERATOR_ROOM_TABLE_COLUMN, lectureTable, GeneratorStyle.CLASS_GENERATOR_TABLE_COLUMN);
         secColumn.setCellValueFactory(new PropertyValueFactory<String, String>("section"));
         secColumn.setCellFactory(TextFieldTableCell.forTableColumn());
         dayColumn.setCellValueFactory(new PropertyValueFactory<String, String>("day"));
         dayColumn.setCellFactory(TextFieldTableCell.forTableColumn());
         timeColumn.setCellValueFactory(new PropertyValueFactory<String, String>("time"));
         timeColumn.setCellFactory(TextFieldTableCell.forTableColumn());
         roomColumn.setCellValueFactory(new PropertyValueFactory<String, String>("room"));
         roomColumn.setCellFactory(TextFieldTableCell.forTableColumn());
         secColumn.prefWidthProperty().bind(lectureTable.widthProperty().multiply(1.0 / 4.0));
         dayColumn.prefWidthProperty().bind(lectureTable.widthProperty().multiply(1.0 / 4.0));
         timeColumn.prefWidthProperty().bind(lectureTable.widthProperty().multiply(1.0 / 4.0));
         roomColumn.prefWidthProperty().bind(lectureTable.widthProperty().multiply(1.0 / 4.0));
         this.setTableSortable(lectureTable, false);
         
         VBox recitationBox = generatorBuilder.buildVBox(GeneratorProperty.GENERATOR_MEETING_RECITATION_BOX, tabBox, GeneratorStyle.CLASS_MEETING_SUB_VBOX, ENABLED);
         HBox recitationHeader = generatorBuilder.buildHBox(GeneratorProperty.GENERATOR_MEETING_LECTURE_HEADER_BOX, recitationBox, GeneratorStyle.CLASS_MEETING_HEADER_HBOX, ENABLED);
         Button recitationPlus = generatorBuilder.buildTextButton(GeneratorProperty.GENERATOR_MEETING_RECITATION_PLUS_BUTTON, recitationHeader, GeneratorStyle.CLASS_GENERATOR_MINUS_BUTTON, ENABLED);
         recitationPlus.setText("+");
         Button recitationMinus = generatorBuilder.buildTextButton(GeneratorProperty.GENERATOR_MEETING_RECITATION_MINUS_BUTTON, recitationHeader, GeneratorStyle.CLASS_GENERATOR_MINUS_BUTTON, ENABLED);
         recitationMinus.setText("-");
         generatorBuilder.buildLabel(GeneratorProperty.GENERATOR_MEETING_RECITATION_LABEL, recitationHeader, GeneratorStyle.CLASS_MEETING_HEADER_LABEL, ENABLED);
         TableView<Recitation> recitationTable = generatorBuilder.buildTableView(GeneratorProperty.GENERATOR_RECITATION_TABLE_VIEW, recitationBox, GeneratorStyle.CLASS_GENERATOR_TABLE_VIEW, ENABLED);
         recitationTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
         recitationTable.setEditable(true);
         TableColumn secColumn1 = generatorBuilder.buildTableColumn(GeneratorProperty.GENERATOR_SEC1_TABLE_COLUMN, recitationTable, GeneratorStyle.CLASS_GENERATOR_TABLE_COLUMN);
         TableColumn dayColumn1 = generatorBuilder.buildTableColumn(GeneratorProperty.GENERATOR_DAY1_TABLE_COLUMN, recitationTable, GeneratorStyle.CLASS_GENERATOR_TABLE_COLUMN);
         TableColumn roomColumn1 = generatorBuilder.buildTableColumn(GeneratorProperty.GENERATOR_ROOM1_TABLE_COLUMN, recitationTable, GeneratorStyle.CLASS_GENERATOR_TABLE_COLUMN);
         TableColumn ta1Column = generatorBuilder.buildTableColumn(GeneratorProperty.GENERATOR_TA1_TABLE_COLUMN, recitationTable, GeneratorStyle.CLASS_GENERATOR_TABLE_COLUMN);
         TableColumn ta2Column = generatorBuilder.buildTableColumn(GeneratorProperty.GENERATOR_TA2_TABLE_COLUMN, recitationTable, GeneratorStyle.CLASS_GENERATOR_TABLE_COLUMN);
         secColumn1.setCellValueFactory(new PropertyValueFactory<String, String>("section"));
         secColumn1.setCellFactory(TextFieldTableCell.forTableColumn());
         dayColumn1.setCellValueFactory(new PropertyValueFactory<String, String>("day"));
         dayColumn1.setCellFactory(TextFieldTableCell.forTableColumn());
         roomColumn1.setCellValueFactory(new PropertyValueFactory<String, String>("room"));
         roomColumn1.setCellFactory(TextFieldTableCell.forTableColumn());
         ta1Column.setCellValueFactory(new PropertyValueFactory<String, String>("ta1"));
         ta1Column.setCellFactory(TextFieldTableCell.forTableColumn());
         ta2Column.setCellValueFactory(new PropertyValueFactory<String, String>("ta2"));
         ta2Column.setCellFactory(TextFieldTableCell.forTableColumn());
         secColumn1.prefWidthProperty().bind(lectureTable.widthProperty().multiply(1.0 / 5.0));
         dayColumn1.prefWidthProperty().bind(lectureTable.widthProperty().multiply(1.0 / 5.0));
         roomColumn1.prefWidthProperty().bind(lectureTable.widthProperty().multiply(1.0 / 5.0));
         ta1Column.prefWidthProperty().bind(lectureTable.widthProperty().multiply(1.0 / 5.0));
         ta2Column.prefWidthProperty().bind(lectureTable.widthProperty().multiply(1.0 / 5.0));
         this.setTableSortable(recitationTable, false);
         
         VBox labBox = generatorBuilder.buildVBox(GeneratorProperty.GENERATOR_MEETING_LAB_BOX, tabBox, GeneratorStyle.CLASS_MEETING_SUB_VBOX, ENABLED);
         HBox labHeader = generatorBuilder.buildHBox(GeneratorProperty.GENERATOR_MEETING_LAB_HEADER_BOX, labBox, GeneratorStyle.CLASS_MEETING_HEADER_HBOX, ENABLED);
         Button labPlus = generatorBuilder.buildTextButton(GeneratorProperty.GENERATOR_MEETING_LAB_PLUS_BUTTON, labHeader, GeneratorStyle.CLASS_GENERATOR_MINUS_BUTTON, ENABLED);
         labPlus.setText("+");
         Button labMinus = generatorBuilder.buildTextButton(GeneratorProperty.GENERATOR_MEETING_LAB_MINUS_BUTTON, labHeader, GeneratorStyle.CLASS_GENERATOR_MINUS_BUTTON, ENABLED);
         labMinus.setText("-");
         generatorBuilder.buildLabel(GeneratorProperty.GENERATOR_MEETING_LAB_LABEL, labHeader, GeneratorStyle.CLASS_MEETING_HEADER_LABEL, ENABLED);
         TableView<Lab> labTable = generatorBuilder.buildTableView(GeneratorProperty.GENERATOR_LAB_TABLE_VIEW, labBox, GeneratorStyle.CLASS_GENERATOR_TABLE_VIEW, ENABLED);
         labTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
         labTable.setEditable(true);
         TableColumn secColumn2 = generatorBuilder.buildTableColumn(GeneratorProperty.GENERATOR_SEC2_TABLE_COLUMN, labTable, GeneratorStyle.CLASS_GENERATOR_TABLE_COLUMN);
         TableColumn dayColumn2 = generatorBuilder.buildTableColumn(GeneratorProperty.GENERATOR_DAY2_TABLE_COLUMN, labTable, GeneratorStyle.CLASS_GENERATOR_TABLE_COLUMN);
         TableColumn roomColumn2 = generatorBuilder.buildTableColumn(GeneratorProperty.GENERATOR_ROOM2_TABLE_COLUMN, labTable, GeneratorStyle.CLASS_GENERATOR_TABLE_COLUMN);
         TableColumn ta1Column1 = generatorBuilder.buildTableColumn(GeneratorProperty.GENERATOR_TA1_TABLE_COLUMN1, labTable, GeneratorStyle.CLASS_GENERATOR_TABLE_COLUMN);
         TableColumn ta2Column1 = generatorBuilder.buildTableColumn(GeneratorProperty.GENERATOR_TA2_TABLE_COLUMN1, labTable, GeneratorStyle.CLASS_GENERATOR_TABLE_COLUMN);
         secColumn2.setCellValueFactory(new PropertyValueFactory<String, String>("section"));
         secColumn2.setCellFactory(TextFieldTableCell.forTableColumn());
         dayColumn2.setCellValueFactory(new PropertyValueFactory<String, String>("day"));
         dayColumn2.setCellFactory(TextFieldTableCell.forTableColumn());
         roomColumn2.setCellValueFactory(new PropertyValueFactory<String, String>("room"));
         roomColumn2.setCellFactory(TextFieldTableCell.forTableColumn());
         ta1Column1.setCellValueFactory(new PropertyValueFactory<String, String>("ta1"));
         ta1Column1.setCellFactory(TextFieldTableCell.forTableColumn());
         ta2Column1.setCellValueFactory(new PropertyValueFactory<String, String>("ta2"));
         ta2Column1.setCellFactory(TextFieldTableCell.forTableColumn());
         secColumn2.prefWidthProperty().bind(lectureTable.widthProperty().multiply(1.0 / 5.0));
         dayColumn2.prefWidthProperty().bind(lectureTable.widthProperty().multiply(1.0 / 5.0));
         ta1Column1.prefWidthProperty().bind(lectureTable.widthProperty().multiply(1.0 / 5.0));
         roomColumn2.prefWidthProperty().bind(lectureTable.widthProperty().multiply(1.0 / 5.0));;
         ta2Column1.prefWidthProperty().bind(lectureTable.widthProperty().multiply(1.0 / 5.0));;
         this.setTableSortable(recitationTable, false);
    }
    
    private void buildSchduleTab(TabPane tabContainer, AppNodesBuilder generatorBuilder){
         Tab sceduleTab = generatorBuilder.buildTab(GeneratorProperty.GENERATOR_SCHEDULE_TAB, tabContainer, GeneratorStyle.CLASS_OH_TAB, ENABLED);
         VBox tabBox = generatorBuilder.buildTabVBox(GeneratorProperty.GENERATOR_SCHEDULE_TAB_BOX, null, GeneratorStyle.CLASS_SCHEDULE_TAB_BOX, ENABLED);
         ScrollPane scroll = new ScrollPane();
         scroll.setFitToHeight(false);
         scroll.setFitToWidth(true);
         scroll.setContent(tabBox);
         sceduleTab.setContent(scroll);
         
         VBox calenBox = this.buildScheduleSubVbox(GeneratorProperty.GENERATOR_SCHEDULE_CALENDERBOX);
         generatorBuilder.buildLabel(GeneratorProperty.GENERATOR_SCHEDULE_CALENDER_HEADER, calenBox, GeneratorStyle.CLASS_SCHEDULE_HEADER_LABEL, ENABLED);
         HBox box1 = this.buildDateComboHBox(calenBox, GeneratorProperty.GENERATOR_SCHEDULE_STARTING_SUB_HBOX, GeneratorProperty.GENERATOR_SCHEDULE_STARTING_LABEL, GeneratorProperty.GENERATOR_SCHEDULE_STARTING_COMBO, GeneratorProperty.GENERATOR_SCHEDULE_STARTING_COMBO_DEFAULT);
         this.buildDateComboHBox(box1, GeneratorProperty.GENERATOR_SCHEDULE_ENDING_SUB_HBOX, GeneratorProperty.GENERATOR_SCHEDULE_ENDING_LABEL, GeneratorProperty.GENERATOR_SCHEDULE_ENDING_COMBO, GeneratorProperty.GENERATOR_SCHEDULE_ENDING_COMBO_DEFAULT);
         
         VBox itemBox = this.buildScheduleSubVbox(GeneratorProperty.GENERATOR_SCHEDULE_ITEM);
         HBox itemExpandHbox = generatorBuilder.buildHBox(GeneratorProperty.GENERATOR_SCHEDULE_ITEM_EXPAND_BOX, itemBox, GeneratorStyle.CLASS_SCHEDULE_INNER_SUB_HBOX, ENABLED);
         Button itemExpand = generatorBuilder.buildTextButton(GeneratorProperty.GENERATOR_SCHEDULE_ITEM_REMOVE_BUTTON, itemExpandHbox, GeneratorStyle.CLASS_GENERATOR_EXPAND_BUTTON, ENABLED);
         itemExpand.setText("-");
         generatorBuilder.buildLabel(GeneratorProperty.GENERATOR_SCHEDULE_ITEM_EXPAND_LABEL, itemExpandHbox, GeneratorStyle.CLASS_SCHEDULE_HEADER_LABEL, ENABLED);
         TableView<ScheduleItem> schduleItemTable = generatorBuilder.buildTableView(GeneratorProperty.GENERATOR_SCHEDULE_ITEM_TABLE, itemBox, GeneratorStyle.CLASS_GENERATOR_TABLE_VIEW, ENABLED);
         TableColumn typeColumn = generatorBuilder.buildTableColumn(GeneratorProperty.GENERATOR_SCHEDULE_TYPE_COLUMN, schduleItemTable, GeneratorStyle.CLASS_GENERATOR_TABLE_COLUMN);
         TableColumn dateColumn = generatorBuilder.buildTableColumn(GeneratorProperty.GENERATOR_SCHEDULE_DATE_COLUMN, schduleItemTable, GeneratorStyle.CLASS_GENERATOR_TABLE_COLUMN);
         TableColumn titleColumn = generatorBuilder.buildTableColumn(GeneratorProperty.GENERATOR_SCHEDULE_TITLE_COLUMN, schduleItemTable, GeneratorStyle.CLASS_GENERATOR_TABLE_COLUMN);
         TableColumn topicColumn = generatorBuilder.buildTableColumn(GeneratorProperty.GENERATOR_SCHEDULE_TOPIC_COLUMN, schduleItemTable, GeneratorStyle.CLASS_GENERATOR_TABLE_COLUMN);
         typeColumn.setCellValueFactory(new PropertyValueFactory<String, String>("type"));
         dateColumn.setCellValueFactory(new PropertyValueFactory<String, String>("date"));
         titleColumn.setCellValueFactory(new PropertyValueFactory<String, String>("title"));
         topicColumn.setCellValueFactory(new PropertyValueFactory<String, String>("topic"));
         typeColumn.prefWidthProperty().bind(schduleItemTable.widthProperty().multiply(1.0 / 4.0));
         dateColumn.prefWidthProperty().bind(schduleItemTable.widthProperty().multiply(1.0 / 4.0));
         titleColumn.prefWidthProperty().bind(schduleItemTable.widthProperty().multiply(1.0 / 4.0));
         topicColumn.prefWidthProperty().bind(schduleItemTable.widthProperty().multiply(1.0 / 4.0));
         this.setTableSortable(schduleItemTable, false);
         
         VBox addBox = this.buildScheduleSubVbox(GeneratorProperty.GENERATOR_SCHEDULE_ADDBOX);
         generatorBuilder.buildLabel(GeneratorProperty.GENERATOR_SCHEDULE_ADD_LABEL, addBox, GeneratorStyle.CLASS_SCHEDULE_HEADER_LABEL, ENABLED);
         this.buildStrongLabelComboHBox(addBox, GeneratorProperty.GENERATOR_SCHEDULE_TYPE_COMBO_HBOX, GeneratorProperty.GENERATOR_SCHEDULE_TYPE_LABEL, GeneratorProperty.GENERATOR_SCHEDULE_TYPE_COMBOBOX, GeneratorProperty.GENERATOR_SCHEDULE_TYPE_COMBO_OPTION, GeneratorProperty.GENERATOR_SCHEDULE_TYPE_COMBO_DEAFULT);
         this.buildDateComboHBox(addBox, GeneratorProperty.GENERATOR_SCHEDULE_DATE_COMBO_HBOX, GeneratorProperty.GENERATOR_SCHEDULE_DATE_LABEL, GeneratorProperty.GENERATOR_SCHEDULE_DATE_COMBOBOX, GeneratorProperty.GENERATOR_SCHEDULE_DATE_COMBO_DEAFULT);
         this.buildScheduleAddLabelTextFieldBox(GeneratorProperty.GENERATOR_SCHEDULE_TITLE_HBOX, GeneratorProperty.GENERATOR_SCHEDULE_TITLE_LABEL, GeneratorProperty.GENERATOR_SCHEDULE_TITLE_TEXTFIELD);
         this.buildScheduleAddLabelTextFieldBox(GeneratorProperty.GENERATOR_SCHEDULE_TOPIC_HBOX, GeneratorProperty.GENERATOR_SCHEDULE_TOPIC_LABEL, GeneratorProperty.GENERATOR_SCHEDULE_TOPIC_TEXTFIELD);
         this.buildScheduleAddLabelTextFieldBox(GeneratorProperty.GENERATOR_SCHEDULE_LINK_HBOX, GeneratorProperty.GENERATOR_SCHEDULE_LINK_LABEL, GeneratorProperty.GENERATOR_SCHEDULE_LINK_TEXTFIELD);
         HBox addButtonsHbox = generatorBuilder.buildHBox(GeneratorProperty.GENERATOR_SCHEDULE_ADD_BUTTON_HBOX, addBox, GeneratorStyle.CLASS_SCHEDULE_INNER_SUB_HBOX, ENABLED);
         generatorBuilder.buildTextButton(GeneratorProperty.GENERATOR_SCHEDULE_ADD_BUTTON, addButtonsHbox, GeneratorStyle.CLASS_GENERATOR_OTHER_BUTTON, ENABLED);
         generatorBuilder.buildTextButton(GeneratorProperty.GENERATOR_SCHEDULE_CLEAR_BUTTON, addButtonsHbox, GeneratorStyle.CLASS_GENERATOR_OTHER_BUTTON, ENABLED);
         
    }
    
    private HBox buildScheduleAddLabelTextFieldBox(Object nodeID, Object label, Object textField){
        AppNodesBuilder generatorBuilder = app.getGUIModule().getNodesBuilder();
        VBox parent = (VBox) app.getGUIModule().getGUINode(GeneratorProperty.GENERATOR_SCHEDULE_ADDBOX);
        HBox result = generatorBuilder.buildHBox(nodeID, parent, GeneratorStyle.CLASS_SCHEDULE_INNER_SUB_HBOX, ENABLED);
        generatorBuilder.buildLabel(label, result, GeneratorStyle.CLASS_SCHEDULE_HEADER_LABEL, ENABLED);
        generatorBuilder.buildTextField(textField, result, GeneratorStyle.CLASS_SCHEDULE_TEXT_FIELD, ENABLED);
        return result;
    }
    
    private VBox buildScheduleSubVbox(Object nodeID){
        AppNodesBuilder generatorBuilder = app.getGUIModule().getNodesBuilder();
        VBox box = (VBox) app.getGUIModule().getGUINode(GeneratorProperty.GENERATOR_SCHEDULE_TAB_BOX);
       return generatorBuilder.buildVBox(nodeID, box, GeneratorStyle.CLASS_SCHEDULE_SUB_VBOX, ENABLED);
    }
    
     private HBox buildStrongLabelComboHBox(Pane parent, Object toBuild, Object label, Object ComboBox, Object options, Object defaultOption){
        AppNodesBuilder generatorBuilder = app.getGUIModule().getNodesBuilder();
        HBox result = generatorBuilder.buildHBox(toBuild, parent, GeneratorStyle.CLASS_SCHEDULE_INNER_SUB_HBOX, ENABLED);
        generatorBuilder.buildLabel(label, result, GeneratorStyle.CLASS_SCHEDULE_HEADER_LABEL, ENABLED);
        generatorBuilder.buildComboBox(ComboBox, options, defaultOption, result, GeneratorStyle.CLASS_SCHEDULE_COMBOBOX, ENABLED);
        return result;
    }
    
    
    private HBox buildDateComboHBox(Pane parent, Object toBuild, Object label, Object ComboBox, Object defaultOption){
        AppNodesBuilder generatorBuilder = app.getGUIModule().getNodesBuilder();
        HBox result = generatorBuilder.buildHBox(toBuild, parent, GeneratorStyle.CLASS_SCHEDULE_INNER_SUB_HBOX, ENABLED);
        generatorBuilder.buildLabel(label, result, GeneratorStyle.CLASS_SCHEDULE_HEADER_LABEL, ENABLED);
        generatorBuilder.buildDateCombo(ComboBox, defaultOption, result, GeneratorStyle.CLASS_SCHEDULE_COMBOBOX, ENABLED);
        return result;
    }

    private void setupOfficeHoursColumn(Object columnId, TableView tableView, String styleClass, String columnDataProperty) {
        AppNodesBuilder builder = app.getGUIModule().getNodesBuilder();
        TableColumn<TeachingAssistantPrototype, String> column = builder.buildTableColumn(columnId, tableView, styleClass);
        column.setCellValueFactory(new PropertyValueFactory<TeachingAssistantPrototype, String>(columnDataProperty));
        column.prefWidthProperty().bind(tableView.widthProperty().multiply(1.0 / 7.0));
        column.setCellFactory(col -> {
            return new TableCell<TeachingAssistantPrototype, String>() {
                @Override
                protected void updateItem(String text, boolean empty) {
                    super.updateItem(text, empty);
                    if (text == null || empty) {
                        setText(null);
                        setStyle("");
                    } else {
                        // CHECK TO SEE IF text CONTAINS THE NAME OF
                        // THE CURRENTLY SELECTED TA
                        setText(text);
                        TableView<TeachingAssistantPrototype> tasTableView = (TableView) app.getGUIModule().getGUINode(GeneratorProperty.OH_TAS_TABLE_VIEW);
                        TeachingAssistantPrototype selectedTA = tasTableView.getSelectionModel().getSelectedItem();
                        if (selectedTA == null) {
                            setStyle("");
                        } else if (text.contains(selectedTA.getName())) {
                            setStyle("-fx-background-color: yellow");
                        } else {
                            setStyle("");
                        }
                    }
                }
            };
        });
    }
    
    private void setTableSortable(TableView table, boolean sortable){
        for (int i = 0; i < table.getColumns().size(); i++) {
            ((TableColumn) table.getColumns().get(i)).setSortable(sortable);
        }
    }
    
    public void updateExportLabel(){
        this.siteController.updateExportLabel();
    }

    public void initControllers() {
        OfficerHoursController controller = new OfficerHoursController((GeneratorApp) app);
        siteController = new SiteController((GeneratorApp) app);
        SyllabusController syllabusController = new SyllabusController((GeneratorApp) app);
        ScheduleController scheduleController = new ScheduleController((GeneratorApp) app);
        MeetingTimesController meetingController = new MeetingTimesController((GeneratorApp)app);
        AppGUIModule gui = app.getGUIModule();
        

        // FOOLPROOF DESIGN STUFF
        TextField nameTextField = ((TextField) gui.getGUINode(GeneratorProperty.OH_NAME_TEXT_FIELD));
        TextField emailTextField = ((TextField) gui.getGUINode(GeneratorProperty.OH_EMAIL_TEXT_FIELD));

        nameTextField.textProperty().addListener(e -> {
            controller.processTypeTA();
        });
        emailTextField.textProperty().addListener(e -> {
            controller.processTypeTA();
        });

        // FIRE THE ADD EVENT ACTION
        nameTextField.setOnAction(e -> {
            controller.processAddTA();
        });
        emailTextField.setOnAction(e -> {
            controller.processAddTA();
        });
        ((Button) gui.getGUINode(GeneratorProperty.OH_ADD_TA_BUTTON)).setOnAction(e -> {
            controller.processAddTA();
        });

        TableView officeHoursTableView = (TableView) gui.getGUINode(GeneratorProperty.OH_OFFICE_HOURS_TABLE_VIEW);
        officeHoursTableView.getSelectionModel().setCellSelectionEnabled(true);
        officeHoursTableView.setOnMouseClicked(e -> {
            controller.processToggleOfficeHours();
        });

        // DON'T LET ANYONE SORT THE TABLES
        TableView tasTableView = (TableView) gui.getGUINode(GeneratorProperty.OH_TAS_TABLE_VIEW);
        for (int i = 0; i < officeHoursTableView.getColumns().size(); i++) {
            ((TableColumn) officeHoursTableView.getColumns().get(i)).setSortable(false);
        }
        for (int i = 0; i < tasTableView.getColumns().size(); i++) {
            ((TableColumn) tasTableView.getColumns().get(i)).setSortable(false);
        }

        tasTableView.setOnMouseClicked(e -> {
            app.getFoolproofModule().updateAll();
            if (e.getClickCount() == 2) {
                controller.processEditTA();
            }
            controller.processSelectTA();
        });

        RadioButton allRadio = (RadioButton) gui.getGUINode(GeneratorProperty.OH_ALL_RADIO_BUTTON);
        allRadio.setOnAction(e -> {
            controller.processSelectAllTAs();
        });
        RadioButton gradRadio = (RadioButton) gui.getGUINode(GeneratorProperty.OH_GRAD_RADIO_BUTTON);
        gradRadio.setOnAction(e -> {
            controller.processSelectGradTAs();
        });
        RadioButton undergradRadio = (RadioButton) gui.getGUINode(GeneratorProperty.OH_UNDERGRAD_RADIO_BUTTON);
        undergradRadio.setOnAction(e -> {
            controller.processSelectUndergradTAs();
        });
        
        Button taOHExpand = (Button) gui.getGUINode(GeneratorProperty.GENERATOR_OH_TA_EXPAND_BUTTON);
        taOHExpand.setOnAction(e -> {
            controller.proccessTAEpxand();
        });
        
         Button siteOfficeHoursExpand = (Button) gui.getGUINode(GeneratorProperty.GENERATOR_SITE_OFFICE_HOURS_EXPAND_BUTTON);
         siteOfficeHoursExpand.setOnAction(e -> {
            siteController.proccessOfficeHoursEpxand();
        });
        
        ComboBox ohStartCombo = (ComboBox) gui.getGUINode(GeneratorProperty.GENERATOR_OH_START_TIME_COMBO);
        ohStartCombo.valueProperty().addListener(new customChangeListener(controller, ohStartCombo));
        
        ComboBox ohEndCombo = (ComboBox) gui.getGUINode(GeneratorProperty.GENERATOR_OH_END_TIME_COMBO);
        ohEndCombo.valueProperty().addListener(new customChangeListener(controller, ohEndCombo));
        
        Button se1 = (Button) gui.getGUINode(GeneratorProperty.GENERATOR_SYLLABUS_EXPAND_BUTTON_DESCRIPTION);
        Button se2 = (Button) gui.getGUINode(GeneratorProperty.GENERATOR_SYLLABUS_EXPAND_BUTTON_TOPIC);
        Button se3 = (Button) gui.getGUINode(GeneratorProperty.GENERATOR_SYLLABUS_EXPAND_BUTTON_PERE);
        Button se4 = (Button) gui.getGUINode(GeneratorProperty.GENERATOR_SYLLABUS_EXPAND_BUTTON_OUT);
        Button se5 = (Button) gui.getGUINode(GeneratorProperty.GENERATOR_SYLLABUS_EXPAND_BUTTON_BOOK);
        Button se6 = (Button) gui.getGUINode(GeneratorProperty.GENERATOR_SYLLABUS_EXPAND_BUTTON_GRADE);
        Button se7 = (Button) gui.getGUINode(GeneratorProperty.GENERATOR_SYLLABUS_EXPAND_BUTTON_NOTE);
        Button se8 = (Button) gui.getGUINode(GeneratorProperty.GENERATOR_SYLLABUS_EXPAND_BUTTON_DISHONESTY);
        Button se9 = (Button) gui.getGUINode(GeneratorProperty.GENERATOR_SYLLABUS_EXPAND_BUTTON_ASIS);
        se1.setOnAction(e -> {
            syllabusController.handleExpandButton1();
        });
        se2.setOnAction(e -> {
            syllabusController.handleExpandButton2();
        });
        se3.setOnAction(e -> {
            syllabusController.handleExpandButton3();
        });
        se4.setOnAction(e -> {
            syllabusController.handleExpandButton4();
        });
        se5.setOnAction(e -> {
            syllabusController.handleExpandButton5();
        });
        se6.setOnAction(e -> {
            syllabusController.handleExpandButton6();
        });
        se7.setOnAction(e -> {
            syllabusController.handleExpandButton7();
        });
        se8.setOnAction(e -> {
            syllabusController.handleExpandButton8();
        });
        se9.setOnAction(e -> {
            syllabusController.handleExpandButton9();
        });
        
        Button itemRemove = (Button) gui.getGUINode(GeneratorProperty.GENERATOR_SCHEDULE_ITEM_REMOVE_BUTTON);
        itemRemove.setOnAction(e -> {
            scheduleController.proccessRemove();
        });
        
         Button im1 = (Button) gui.getGUINode(GeneratorProperty.SITE_FAVICON);
         Button tx1 = (Button) gui.getGUINode(GeneratorProperty.SITE_FAVICON_BUTTON);
        tx1.setOnAction(e -> {
            siteController.proccessImageIcon(GeneratorProperty.SITE_FAVICON);
        });
        
         Button im2 = (Button) gui.getGUINode(GeneratorProperty.SITE_NAVICON);
         Button tx2 = (Button) gui.getGUINode(GeneratorProperty.SITE_NAVBAR_BUTTON);
        tx2.setOnAction(e -> {
           siteController.proccessImageIcon(GeneratorProperty.SITE_NAVICON);
        });
        
         Button im3 = (Button) gui.getGUINode(GeneratorProperty.SITE_LEFT_FOOTERCON);
         Button tx3 = (Button) gui.getGUINode(GeneratorProperty.SITE_LEFT_FOOTER_BUTTON);
        tx3.setOnAction(e -> {
            siteController.proccessImageIcon(GeneratorProperty.SITE_LEFT_FOOTERCON);
        });
        
        Button tx4 = (Button) gui.getGUINode(GeneratorProperty.SITE_RIGHT_FOOTER_BUTTON);
        Button im4 = (Button) gui.getGUINode(GeneratorProperty.SITE_RIGHT_FOOTERCON);
        tx4.setOnAction(e -> {
            siteController.proccessImageIcon(GeneratorProperty.SITE_RIGHT_FOOTERCON);
        });
        
        
          CheckBox check = (CheckBox) gui.getGUINode(GeneratorProperty.GENERATOR_SITE_HOME_CHECKBOX);
          check.setOnAction(e -> {
            siteController.proccessCheckClicked(GeneratorProperty.GENERATOR_SITE_HOME_CHECKBOX);
          });
          check = (CheckBox) gui.getGUINode(GeneratorProperty.GENERATOR_SITE_SYLLABUS_CHECKBOX);
          check.setOnAction(e -> {
            siteController.proccessCheckClicked(GeneratorProperty.GENERATOR_SITE_SYLLABUS_CHECKBOX);
          });
          check = (CheckBox) gui.getGUINode(GeneratorProperty.GENERATOR_SITE_SCHEDULE_CHECKBOX);
          check.setOnAction(e -> {
            siteController.proccessCheckClicked(GeneratorProperty.GENERATOR_SITE_SCHEDULE_CHECKBOX);
          });
          check = (CheckBox) gui.getGUINode(GeneratorProperty.GENERATOR_SITE_HOMEWORK_CHECKBOX);
          check.setOnAction(e -> {
            siteController.proccessCheckClicked(GeneratorProperty.GENERATOR_SITE_HOMEWORK_CHECKBOX);
          });
          
        ComboBox combo = (ComboBox) gui.getGUINode(GeneratorProperty.GENERATOR_SITE_SUBJECT_COMBOBOX);
        combo.getEditor().focusedProperty().addListener(new customTypeInComboTypeInListener(combo, GeneratorProperty.GENERATOR_SITE_SUBJECT_COMBOBOX, siteController));
        
        combo = (ComboBox) gui.getGUINode(GeneratorProperty.GENERATOR_SITE_NUMBER_COMBOBOX);
        combo.getEditor().focusedProperty().addListener(new customTypeInComboTypeInListener(combo, GeneratorProperty.GENERATOR_SITE_NUMBER_COMBOBOX, siteController));
        
        combo = (ComboBox) gui.getGUINode(GeneratorProperty.GENERATOR_SITE_SEMESTER_COMBOBOX);
        combo.valueProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue ov, Object oldValue, Object newValue) {
                siteController.proccessExportCombo(oldValue.toString(), newValue.toString(), GeneratorProperty.GENERATOR_SITE_SEMESTER_COMBOBOX, this);
            }
        });
        
        
        combo = (ComboBox) gui.getGUINode(GeneratorProperty.GENERATOR_SITE_YEAR_COMBOBOX);
        combo.valueProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue ov, Object oldValue, Object newValue) {
                siteController.proccessExportCombo(oldValue.toString(), newValue.toString(), GeneratorProperty.GENERATOR_SITE_YEAR_COMBOBOX, this);
            }
        });
        
       
        combo = (ComboBox) gui.getGUINode(GeneratorProperty.GENERATOR_SITE_CSS_COMBOBOX);
        combo.valueProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue ov, Object oldValue, Object newValue) {
                siteController.proccessGeneralComboSelect(oldValue.toString(), newValue.toString(), GeneratorProperty.GENERATOR_SITE_CSS_COMBOBOX, this);
            }
        });
        
        
        TextField textField = (TextField) app.getGUIModule().getGUINode(GeneratorProperty.GENERATOR_SITE_NAME_TEXTFIELD);
        textField.focusedProperty().addListener(new customTextFocusListener(textField, (TextEditController)siteController));
        
        textField = (TextField) app.getGUIModule().getGUINode(GeneratorProperty.GENERATOR_SITE_ROOM_TEXTFIELD);
        textField.focusedProperty().addListener(new customTextFocusListener(textField, (TextEditController)siteController));
        
        textField = (TextField) app.getGUIModule().getGUINode(GeneratorProperty.GENERATOR_SITE_EMAIL_TEXTFIELD);
        textField.focusedProperty().addListener(new customTextFocusListener(textField, (TextEditController)siteController));
        
        textField = (TextField) app.getGUIModule().getGUINode(GeneratorProperty.GENERATOR_SITE_HOME_PAGE_TEXTFIELD);
        textField.focusedProperty().addListener(new customTextFocusListener(textField, (TextEditController)siteController));
        
        TextArea area = (TextArea) gui.getGUINode(GeneratorProperty.GENERATOR_SITE_OFFICE_HOURS_TEXT_AREA);
        area.focusedProperty().addListener(new customTextFocusListener(area, (TextEditController)siteController));
        
        // syllabus tab starts here 
        area = (TextArea) gui.getGUINode(GeneratorProperty.GENERATOR_SYLLABUS_AREA_DECSRCIPTION);
        area.focusedProperty().addListener(new customTextFocusListener(area, (TextEditController)syllabusController));
        
        area = (TextArea) gui.getGUINode(GeneratorProperty.GENERATOR_SYLLABUS_AREA_TOPIC);
        area.focusedProperty().addListener(new customTextFocusListener(area, (TextEditController)syllabusController));
        
        area = (TextArea) gui.getGUINode(GeneratorProperty.GENERATOR_SYLLABUS_AREA_PERE);
        area.focusedProperty().addListener(new customTextFocusListener(area, (TextEditController)syllabusController));
        
        area = (TextArea) gui.getGUINode(GeneratorProperty.GENERATOR_SYLLABUS_AREA_OUT);
        area.focusedProperty().addListener(new customTextFocusListener(area, (TextEditController)syllabusController));
        
        area = (TextArea) gui.getGUINode(GeneratorProperty.GENERATOR_SYLLABUS_AREA_BOOK);
        area.focusedProperty().addListener(new customTextFocusListener(area, (TextEditController)syllabusController));
        
        area = (TextArea) gui.getGUINode(GeneratorProperty.GENERATOR_SYLLABUS_AREA_GRADE);
        area.focusedProperty().addListener(new customTextFocusListener(area, (TextEditController)syllabusController));
        
        area = (TextArea) gui.getGUINode(GeneratorProperty.GENERATOR_SYLLABUS_AREA_NOTE);
        area.focusedProperty().addListener(new customTextFocusListener(area, (TextEditController)syllabusController));
        
        area = (TextArea) gui.getGUINode(GeneratorProperty.GENERATOR_SYLLABUS_AREA_DISHONESTY);
        area.focusedProperty().addListener(new customTextFocusListener(area, (TextEditController)syllabusController));
        
        area = (TextArea) gui.getGUINode(GeneratorProperty.GENERATOR_SYLLABUS_AREA_ASIS);
        area.focusedProperty().addListener(new customTextFocusListener(area, (TextEditController)syllabusController));
                 
        
        
        // the schedule tab starts here 
        /*------------------------------------------------------------------------Need  to do redo and undo for these -------------------------------------------------------------------------------------------*/
       
    
        DatePicker startPicker = (DatePicker) gui.getGUINode(GeneratorProperty.GENERATOR_SCHEDULE_STARTING_COMBO);
        startPicker.valueProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue ov, Object oldDate, Object newDate) {
               scheduleController.proccessStartEndComboSelect(oldDate.toString(), newDate.toString(), this, startPicker);
            }
        });
        
        DatePicker endPicker = (DatePicker) gui.getGUINode(GeneratorProperty.GENERATOR_SCHEDULE_ENDING_COMBO);
        endPicker.valueProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue ov, Object oldDate, Object newDate) {
                scheduleController.proccessStartEndComboSelect(oldDate.toString(), newDate.toString(), this, endPicker);
            }
        });
        
        
        DatePicker datePicker = (DatePicker) gui.getGUINode(GeneratorProperty.GENERATOR_SCHEDULE_DATE_COMBOBOX);
        datePicker.valueProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue ov,  Object oldValue,  Object newValue) {
                if(oldValue.toString().equals(newValue.toString())){
                    return;
                }
               
                scheduleController.proccessScheduleDateCombo(oldValue.toString(), newValue.toString(), datePicker, this); 
            }
        });
        
         
        
        
        /*--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
        TableView scheduleItemTable = (TableView) gui.getGUINode(GeneratorProperty.GENERATOR_SCHEDULE_ITEM_TABLE);
        scheduleItemTable.setOnMouseClicked(e -> {
            scheduleController.resetScheduleAddButtonText();
        });
        
        
        Button button = (Button) gui.getGUINode(GeneratorProperty.GENERATOR_SCHEDULE_ADD_BUTTON);
        button.setOnAction(s -> scheduleController.proccessAddUpdateScheduleItem());
        
        button = (Button) gui.getGUINode(GeneratorProperty.GENERATOR_SCHEDULE_CLEAR_BUTTON);
        button.setOnAction(s -> scheduleController.proccessClearShecduleItem());
        
        
        combo = (ComboBox) gui.getGUINode(GeneratorProperty.GENERATOR_SCHEDULE_TYPE_COMBOBOX);
        combo.valueProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue obs, Object ov, Object nv) {
                scheduleController.proccessGeneralComboSelect(ov.toString(), nv.toString(), GeneratorProperty.GENERATOR_SCHEDULE_TYPE_COMBOBOX, this);
            }
        });
        
        textField = (TextField) app.getGUIModule().getGUINode(GeneratorProperty.GENERATOR_SCHEDULE_TITLE_TEXTFIELD);
        textField.focusedProperty().addListener(new customTextFocusListener(textField, (TextEditController)scheduleController));
        
        textField = (TextField) app.getGUIModule().getGUINode(GeneratorProperty.GENERATOR_SCHEDULE_TOPIC_TEXTFIELD);
        textField.focusedProperty().addListener(new customTextFocusListener(textField, (TextEditController)scheduleController));
        
        
        textField = (TextField) app.getGUIModule().getGUINode(GeneratorProperty.GENERATOR_SCHEDULE_LINK_TEXTFIELD);
        textField.focusedProperty().addListener(new customTextFocusListener(textField, (TextEditController)scheduleController));
        
        
        // meeting tab starts here
        Button meetingPlusButton = (Button) gui.getGUINode(GeneratorProperty.GENERATOR_MEETING_LECTURE_PLUS_BUTTON);
        meetingPlusButton.setOnAction(e-> meetingController.processAddItem(new Lecture(),  ((TableView) gui.getGUINode(GeneratorProperty.GENERATOR_LECTURE_TABLE_VIEW)).getItems()));
        
        meetingPlusButton = (Button) gui.getGUINode(GeneratorProperty.GENERATOR_MEETING_RECITATION_PLUS_BUTTON);
        meetingPlusButton.setOnAction(e-> meetingController.processAddItem(new Recitation(),  ((TableView) gui.getGUINode(GeneratorProperty.GENERATOR_RECITATION_TABLE_VIEW)).getItems()));
        
        meetingPlusButton = (Button) gui.getGUINode(GeneratorProperty.GENERATOR_MEETING_LAB_PLUS_BUTTON);
        meetingPlusButton.setOnAction(e-> meetingController.processAddItem(new Lab(),  ((TableView) gui.getGUINode(GeneratorProperty.GENERATOR_LAB_TABLE_VIEW)).getItems()));
        
        Button meetingMinusButton = (Button) gui.getGUINode(GeneratorProperty.GENERATOR_MEETING_LECTURE_MINUS_BUTTON);
        TableView lectureTable = (TableView) gui.getGUINode(GeneratorProperty.GENERATOR_LECTURE_TABLE_VIEW);
        meetingMinusButton.setOnAction(e-> {
                meetingController.processRemoveItem(lectureTable.getSelectionModel().getSelectedItem(),  ((TableView) gui.getGUINode(GeneratorProperty.GENERATOR_LECTURE_TABLE_VIEW)).getItems());
               lectureTable.getSelectionModel().clearSelection();
         });
        
        meetingMinusButton = (Button) gui.getGUINode(GeneratorProperty.GENERATOR_MEETING_RECITATION_MINUS_BUTTON);
        TableView recitationTable = (TableView) gui.getGUINode(GeneratorProperty.GENERATOR_RECITATION_TABLE_VIEW);
        meetingMinusButton.setOnAction(e-> {
                meetingController.processRemoveItem(recitationTable.getSelectionModel().getSelectedItem(),  ((TableView) gui.getGUINode(GeneratorProperty.GENERATOR_RECITATION_TABLE_VIEW)).getItems());
                recitationTable.getSelectionModel().clearSelection();
         });
        
        meetingMinusButton = (Button) gui.getGUINode(GeneratorProperty.GENERATOR_MEETING_LAB_MINUS_BUTTON);
        TableView labTable = (TableView) gui.getGUINode(GeneratorProperty.GENERATOR_LAB_TABLE_VIEW);
        meetingMinusButton.setOnAction(e-> {
                meetingController.processRemoveItem(labTable.getSelectionModel().getSelectedItem(),  ((TableView) gui.getGUINode(GeneratorProperty.GENERATOR_LAB_TABLE_VIEW)).getItems());
                labTable.getSelectionModel().clearSelection();
         });
        
        TableColumn column = (TableColumn) lectureTable.getColumns().get(0);
        column.setOnEditCommit(new EventHandler<CellEditEvent<Lecture, String>>() {
            @Override
            public void handle(CellEditEvent<Lecture, String> event) {
               Lecture lecture = event.getTableView().getItems().get(event.getTablePosition().getRow());
               String oldValue = event.getOldValue();
               String newValue = event.getNewValue();
               meetingController.proccessEditItem(oldValue, newValue, lecture, MeetingItemEditOptions.LECTURE_ITEM, MeetingItemEditOptions.EDIT_SECTION);
            }
        });
        
        column = (TableColumn) lectureTable.getColumns().get(1);
        column.setOnEditCommit(new EventHandler<CellEditEvent<Lecture, String>>() {
            @Override
            public void handle(CellEditEvent<Lecture, String> event) {
               Lecture lecture = event.getTableView().getItems().get(event.getTablePosition().getRow());
               String oldValue = event.getOldValue();
               String newValue = event.getNewValue();
               meetingController.proccessEditItem(oldValue, newValue, lecture, MeetingItemEditOptions.LECTURE_ITEM, MeetingItemEditOptions.EDIT_DAY);
            }
        });
        
        column = (TableColumn) lectureTable.getColumns().get(2);
        column.setOnEditCommit(new EventHandler<CellEditEvent<Lecture, String>>() {
            @Override
            public void handle(CellEditEvent<Lecture, String> event) {
               Lecture lecture = event.getTableView().getItems().get(event.getTablePosition().getRow());
               String oldValue = event.getOldValue();
               String newValue = event.getNewValue();
               meetingController.proccessEditItem(oldValue, newValue, lecture, MeetingItemEditOptions.LECTURE_ITEM, MeetingItemEditOptions.EDIT_TIME);
            }
        });
        
        column = (TableColumn) lectureTable.getColumns().get(3);
        column.setOnEditCommit(new EventHandler<CellEditEvent<Lecture, String>>() {
            @Override
            public void handle(CellEditEvent<Lecture, String> event) {
               Lecture lecture = event.getTableView().getItems().get(event.getTablePosition().getRow());
               String oldValue = event.getOldValue();
               String newValue = event.getNewValue();
               meetingController.proccessEditItem(oldValue, newValue, lecture, MeetingItemEditOptions.LECTURE_ITEM, MeetingItemEditOptions.EDIT_ROOM);
            }
        });
        
        column = (TableColumn) recitationTable.getColumns().get(0);
        column.setOnEditCommit(new EventHandler<CellEditEvent<Recitation, String>>() {
            @Override
            public void handle(CellEditEvent<Recitation, String> event) {
               Recitation recitation = event.getTableView().getItems().get(event.getTablePosition().getRow());
               String oldValue = event.getOldValue();
               String newValue = event.getNewValue();
               meetingController.proccessEditItem(oldValue, newValue, recitation, MeetingItemEditOptions.RECITATION_ITEM, MeetingItemEditOptions.EDIT_SECTION);
            }
        });
        
        column = (TableColumn) recitationTable.getColumns().get(1);
        column.setOnEditCommit(new EventHandler<CellEditEvent<Recitation, String>>() {
            @Override
            public void handle(CellEditEvent<Recitation, String> event) {
               Recitation recitation = event.getTableView().getItems().get(event.getTablePosition().getRow());
               String oldValue = event.getOldValue();
               String newValue = event.getNewValue();
               meetingController.proccessEditItem(oldValue, newValue, recitation, MeetingItemEditOptions.RECITATION_ITEM, MeetingItemEditOptions.EDIT_DAY);
            }
        });
        
        column = (TableColumn) recitationTable.getColumns().get(2);
        column.setOnEditCommit(new EventHandler<CellEditEvent<Recitation, String>>() {
            @Override
            public void handle(CellEditEvent<Recitation, String> event) {
               Recitation recitation = event.getTableView().getItems().get(event.getTablePosition().getRow());
               String oldValue = event.getOldValue();
               String newValue = event.getNewValue();
               meetingController.proccessEditItem(oldValue, newValue, recitation, MeetingItemEditOptions.RECITATION_ITEM, MeetingItemEditOptions.EDIT_ROOM);
            }
        });
        
        column = (TableColumn) recitationTable.getColumns().get(3);
        column.setOnEditCommit(new EventHandler<CellEditEvent<Recitation, String>>() {
            @Override
            public void handle(CellEditEvent<Recitation, String> event) {
               Recitation recitation = event.getTableView().getItems().get(event.getTablePosition().getRow());
               String oldValue = event.getOldValue();
               String newValue = event.getNewValue();
               meetingController.proccessEditItem(oldValue, newValue, recitation, MeetingItemEditOptions.RECITATION_ITEM, MeetingItemEditOptions.EDIT_TA1);
            }
        });
        
        column = (TableColumn) recitationTable.getColumns().get(4);
        column.setOnEditCommit(new EventHandler<CellEditEvent<Recitation, String>>() {
            @Override
            public void handle(CellEditEvent<Recitation, String> event) {
               Recitation recitation = event.getTableView().getItems().get(event.getTablePosition().getRow());
               String oldValue = event.getOldValue();
               String newValue = event.getNewValue();
               meetingController.proccessEditItem(oldValue, newValue, recitation, MeetingItemEditOptions.RECITATION_ITEM, MeetingItemEditOptions.EDIT_TA2);
            }
        });
        
        column = (TableColumn) labTable.getColumns().get(0);
        column.setOnEditCommit(new EventHandler<CellEditEvent<Lab, String>>() {
            @Override
            public void handle(CellEditEvent<Lab, String> event) {
               Lab lab = event.getTableView().getItems().get(event.getTablePosition().getRow());
               String oldValue = event.getOldValue();
               String newValue = event.getNewValue();
               meetingController.proccessEditItem(oldValue, newValue, lab, MeetingItemEditOptions.LAB_ITEM, MeetingItemEditOptions.EDIT_SECTION);
            }
        });
        
        column = (TableColumn) labTable.getColumns().get(1);
        column.setOnEditCommit(new EventHandler<CellEditEvent<Lab, String>>() {
            @Override
            public void handle(CellEditEvent<Lab, String> event) {
               Lab lab = event.getTableView().getItems().get(event.getTablePosition().getRow());
               String oldValue = event.getOldValue();
               String newValue = event.getNewValue();
               meetingController.proccessEditItem(oldValue, newValue, lab, MeetingItemEditOptions.LAB_ITEM, MeetingItemEditOptions.EDIT_DAY);
            }
        });
        
        column = (TableColumn) labTable.getColumns().get(2);
        column.setOnEditCommit(new EventHandler<CellEditEvent<Lab, String>>() {
            @Override
            public void handle(CellEditEvent<Lab, String> event) {
               Lab lab = event.getTableView().getItems().get(event.getTablePosition().getRow());
               String oldValue = event.getOldValue();
               String newValue = event.getNewValue();
               meetingController.proccessEditItem(oldValue, newValue, lab, MeetingItemEditOptions.LAB_ITEM, MeetingItemEditOptions.EDIT_ROOM);
            }
        });
        
        column = (TableColumn) labTable.getColumns().get(3);
        column.setOnEditCommit(new EventHandler<CellEditEvent<Lab, String>>() {
            @Override
            public void handle(CellEditEvent<Lab, String> event) {
               Lab lab = event.getTableView().getItems().get(event.getTablePosition().getRow());
               String oldValue = event.getOldValue();
               String newValue = event.getNewValue();
               meetingController.proccessEditItem(oldValue, newValue, lab, MeetingItemEditOptions.LAB_ITEM, MeetingItemEditOptions.EDIT_TA1);
            }
        });
        
        column = (TableColumn) labTable.getColumns().get(4);
        column.setOnEditCommit(new EventHandler<CellEditEvent<Lab, String>>() {
            @Override
            public void handle(CellEditEvent<Lab, String> event) {
               Lab lab = event.getTableView().getItems().get(event.getTablePosition().getRow());
               String oldValue = event.getOldValue();
               String newValue = event.getNewValue();
               meetingController.proccessEditItem(oldValue, newValue, lab, MeetingItemEditOptions.LAB_ITEM, MeetingItemEditOptions.EDIT_TA2);
            }
        });
        
        
    }
    
    public void loadDynamicCSSCombo(ComboBox combo){
         final String pathname = AppTemplate.PATH_DATA + "export" + File.separator + "CourseSite";
        File folder = new File(pathname);
        System.out.println(pathname);
        File[] listOfFiles = folder.listFiles();
        ObservableList items = combo.getItems();
        items.clear();
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile() && listOfFiles[i].getName().endsWith(".css")) {
                items.add(listOfFiles[i].getName());
            } 
        }
        combo.getSelectionModel().select(0);
    }
    
  

    public void initFoolproofDesign() {
        AppGUIModule gui = app.getGUIModule();
        AppFoolproofModule foolproofSettings = app.getFoolproofModule();
        foolproofSettings.registerModeSettings(GeneratorProperty.OH_FOOLPROOF_SETTINGS,
                new GeneratorFoolProofDesign((GeneratorApp) app));
    }

    @Override
    public void processWorkspaceKeyEvent(KeyEvent ke) {
        // WE AREN'T USING THIS FOR THIS APPLICATION
    }

    @Override
    public void showNewDialog() {
        // WE AREN'T USING THIS FOR THIS APPLICATION
    }
    
   

    
    
    /*----------------------------------------------------------------------------------------------------------------------------------- Inner classes and interfaces------------------------------------------------------------------------------------------------------------- */
    public  class customChangeListener implements ChangeListener{
        OfficerHoursController controller;
        ComboBox combo;
        customChangeListener(OfficerHoursController controller, ComboBox combo){
            this.controller = controller;
            this.combo = combo;
        }
        @Override
        public void changed(ObservableValue ov, Object oldValue, Object newValue) {
             controller.proccessCombo(oldValue.toString(), newValue.toString(), combo, this);
        }
        
    }
    
    public class customTypeInComboTypeInListener implements ChangeListener<Boolean>{
            private ComboBox attachedCombo;
            private String previousValue;
            private Object comboID;
            SiteController controller;
        
        
            customTypeInComboTypeInListener(ComboBox attachedCombo, Object comboID, SiteController controller){
                this.attachedCombo = attachedCombo;
                this.comboID = comboID;
                this.controller = controller;
            }

            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean oldBoolean, Boolean inFocused) {
                if(inFocused){
                    this.previousValue = this.attachedCombo.getValue().toString();
                }else{
                    String newValue = attachedCombo.getEditor().getText();
                    controller.proccessComboBoxTypeIn(this.previousValue, newValue, attachedCombo, comboID);
                }
            }
        
         }
    
    public class customTextFocusListener  implements ChangeListener<Boolean>{
            private TextInputControl control;
            private String previousValue;
            private TextEditController controller;
            
            public customTextFocusListener(TextInputControl control, TextEditController controller){
                this.control = control;
                this.controller = controller;
                this.previousValue = control.getText();
            }

        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldBoolean, Boolean inFocused) {
             if(inFocused){
                    this.previousValue = this.control.getText();
                }else{
                    String newValue = control.getText();
                    controller.processTextChange(this.previousValue, newValue, this.control);
                }
        }
            
    }
    
    
    public interface TextEditController{
            public void processTextChange(String oldValue, String newValue, TextInputControl control);
    }
    

   

        
    
   
    
}
