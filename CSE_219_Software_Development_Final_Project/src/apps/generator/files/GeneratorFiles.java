/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package apps.generator.files;

import static libs.DesktopJavaFramework.AppPropertyType.EXPORT_BUTTON;

import apps.generator.GeneratorApp;
import apps.generator.GeneratorProperty;
import libs.DesktopJavaFramework.AppTemplate;
import libs.DesktopJavaFramework.components.AppDataComponent;
import libs.DesktopJavaFramework.components.AppFileComponent;
import libs.DesktopJavaFramework.modules.AppGUIModule;
import libs.DesktopJavaFramework.ui.AppNodesBuilder;
import apps.generator.data.GeneratorData;
import apps.generator.data.Lab;
import apps.generator.data.Lecture;
import apps.generator.data.Recitation;
import apps.generator.data.ScheduleItem;
import apps.generator.data.TAType;
import apps.generator.data.TeachingAssistantPrototype;
import apps.generator.data.TimeSlot;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;
import javax.json.stream.JsonGenerator;

import apps.generator.workspace.GeneratorWorkSpace;
import apps.generator.workspace.dialogs.ExportedSiteDialog;
import apps.generator.workspace.dialogs.customAlert;
import java.io.File;
import java.time.LocalDate;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javax.json.JsonObjectBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author xgao
 */
public class GeneratorFiles implements AppFileComponent{
    
     // THIS IS THE APP ITSELF
    GeneratorApp app;
    private boolean saved = false;
    
    // THESE ARE USED FOR IDENTIFYING JSON TYPES
    static final String JSON_OH_DATA = "office_hour_tab";
    static final String JSON_GRAD_TAS = "grad_tas";
    static final String JSON_UNDERGRAD_TAS = "undergrad_tas";
    static final String JSON_NAME = "name";
    static final String JSON_EMAIL = "email";
    static final String JSON_TYPE = "type";
    static final String JSON_OFFICE_HOURS = "officeHours";
    static final String JSON_START_HOUR = "startHour";
    static final String JSON_END_HOUR = "endHour";
    static final String JSON_START_TIME = "time";
    static final String JSON_DAY_OF_WEEK = "day";
    static final String JSON_MONDAY = "monday";
    static final String JSON_TUESDAY = "tuesday";
    static final String JSON_WEDNESDAY = "wednesday";
    static final String JSON_THURSDAY = "thursday";
    static final String JSON_FRIDAY = "friday";
    static final String JSON_OH_START_DEFAULT = "oh_start_combo";
    static final String JSON_OH_END_DEFAULT = "oh_end_combo";
    
    // for site tab json titles 
    static final String JSON_SITE_DATA = "site_tab";
    static final String JSON_SUBJECT_DEFAULT = "subject";
    static final String JSON_NUMBER_DEFAULT = "number";   
    static final String JSON_SEMESRER_DEAFULT = "semester";
    static final String JSON_YEAR_DEAFULT = "year";
    static final String JSON_EXPORT_LABEL = "export_label";
    static final String JSON_HOME_CHECK = "home_checked"; 
    static final String JSON_SYLLABUS_CHECK = "syllabus_checked"; 
    static final String JSON_SCHEDULE_CHECK = "schedule_check"; 
    static final String JSON_HW_CHECK = "hw_checked";
    static final String JSON_FAVICON_IMAGE = "favicon"; 
    static final String JSON_NAVCON_IMAGE = "navbar"; 
    static final String JSON_LEFTFOOT_IMAGE = "bottom_left"; 
    static final String JSON_RIGHTFOOT_IMAGE = "bottom_right"; 
    static final String JSON_CSS_DEFAULT = "css_deafult"; 
    static final String JSON_INSTRUCTOR_NAME = "name"; 
    static final String JSON_INSTRUCTOR_EMAIL = "email"; 
    static final String JSON_INSTRUCTOR_ROOM = "room"; 
    static final String JSON_INSTRUCTOR_HOMEPAGE = "link"; 
    static final String JSON_SITE_OFFICE_HOURS = "hours";
    
    // for the syllabus tab
    static final String JSON_SYLLABUS_DATA = "syllabus_tab";
    static final String JSON_DESCRIPTION = "description";
    static final String JSON_TOPIC = "topics";
    static final String JSON_PEREQUISITE = "prerequisites";
    static final String JSON_OUTCOME = "outcomes";
    static final String JSON_TEXTBOOK = "textbooks";
    static final String JSON_GRADED_COMPONENET = "gradedComponents";
    static final String JSON_GRADING = "gradingNote";
    static final String JSON_ACADEMIC_DISHONESTY = "academicDishonesty";
    static final String JSON_ASISTANCE = "specialAssistance";
    
    // for the meetings tab
    static final String JSON_MEETING_DATA = "meeting_tab";
    static final String JSON_LECTURE_ARRAY = "lectures";
    static final String JSON_LECTURE_SECTION = "section";
    static final String JSON_LECTURE_DAY = "days";
    static final String JSON_LECTURE_TIME = "time";
    static final String JSON_LECTURE_ROOM = "room";
    static final String JSON_RECITATION_ARRAY = "recitations";
    static final String JSON_RECITATION_SECTION = "section";
    static final String JSON_RECITATION_DAYS_AND_TIME = "day_time";
    static final String JSON_RECITATION_ROOM = "location";
    static final String JSON_RECITATION_TA1 = "ta_1";
    static final String JSON_RECITATION_TA2 = "ta_2";
    static final String JSON_LAB_ARRAY = "labs";
    static final String JSON_LAB_SECTION = "lab_section";
    static final String JSON_LAB_DAYS_AND_TIME = "lab_days_and_time";
    static final String JSON_LAB_ROOM = "lab_room";
    static final String JSON_LAB_TA1 = "lab_ta1";
    static final String JSON_LAB_TA2 = "lab_ta2";
   
    
    // for the schedule tab
    static final String JSON_SCHEDULE_DATA = "schedule_tab";
    static final String JSON_MONDAY_DEFAULT = "starting_monday";
    static final String JSON_FRIDAY_DEFAULT = "ending_friday";
    static final String JSON_SCHEDULE_ITEMS_ARRAY = "schedule_items";
    static final String JSON_SCHEDULE_ITEMS_TYPE = "type";
    static final String JSON_SCHEDULE_ITEMS_DATE = "date";
    static final String JSON_SCHEDULE_ITEMS_TITLE = "title";
    static final String JSON_SCHEDULE_ITEMS_TOPIC = "topic";
    static final String JSON_SCHEDULE_ITEMS_LINK = "link";
    static final String JSON_TYPE_DEFAULT = "type_default";
    static final String JSON_DATE_DEFAULT = "date_default";
    static final String JSON_TITLE = "title";
    static final String JSON_SCHEDULE_TOPIC = "schedule_topic";
    static final String JSON_LINK = "link";
    
    
    

    public GeneratorFiles(GeneratorApp initApp) {
        app = initApp;
    }

    @Override
    public void loadData(AppDataComponent data, String filePath) throws IOException {        
        // LOAD THE JSON FILE WITH ALL THE DATA
       JsonObject json = loadJSONFile(filePath);
       this.loadSiteTab(json.getJsonObject(JSON_SITE_DATA));
       this.loadSyllabusTab(json.getJsonObject(JSON_SYLLABUS_DATA));
       this.loadOfficeHoursTab( data, filePath, json.getJsonObject(JSON_OH_DATA));
       ((GeneratorData) data).updateTAs();
       this.loadMeetingTab(json.getJsonObject(JSON_MEETING_DATA));
       this.loadScheduleTab(json.getJsonObject(JSON_SCHEDULE_DATA));
       GeneratorWorkSpace workspace = (GeneratorWorkSpace)app.getWorkspaceComponent();
       workspace.updateExportLabel();
       app.clearAllTransaction();
        
    }
    
    private void loadSiteTab(JsonObject siteJson){
        AppGUIModule gui = app.getGUIModule();
         ComboBox combo = (ComboBox) gui.getGUINode(GeneratorProperty.GENERATOR_SITE_SUBJECT_COMBOBOX);
         this.loadComboDeafult(siteJson.getString(JSON_SUBJECT_DEFAULT), combo);
         combo = (ComboBox) gui.getGUINode(GeneratorProperty.GENERATOR_SITE_NUMBER_COMBOBOX);
         this.loadComboDeafult(siteJson.getString(JSON_NUMBER_DEFAULT), combo);
         combo = (ComboBox) gui.getGUINode(GeneratorProperty.GENERATOR_SITE_SEMESTER_COMBOBOX);
         this.loadComboDeafult(siteJson.getString(JSON_SEMESRER_DEAFULT), combo);
         combo = (ComboBox) gui.getGUINode(GeneratorProperty.GENERATOR_SITE_YEAR_COMBOBOX);
         this.loadComboDeafult(siteJson.getString(JSON_YEAR_DEAFULT), combo);
         Label label = (Label) gui.getGUINode(GeneratorProperty.GENERATOR_SITE_EXPORT_LABEL);
         label.setText(siteJson.getString(JSON_EXPORT_LABEL));
         CheckBox check = (CheckBox) gui.getGUINode(GeneratorProperty.GENERATOR_SITE_HOME_CHECKBOX);
         check.setSelected(Boolean.parseBoolean(siteJson.getString(JSON_HOME_CHECK)));
         check = (CheckBox) gui.getGUINode(GeneratorProperty.GENERATOR_SITE_SYLLABUS_CHECKBOX);
         check.setSelected(Boolean.parseBoolean(siteJson.getString(JSON_SYLLABUS_CHECK)));
         check = (CheckBox) gui.getGUINode(GeneratorProperty.GENERATOR_SITE_SCHEDULE_CHECKBOX);
          check.setSelected(Boolean.parseBoolean(siteJson.getString(JSON_SCHEDULE_CHECK)));
         check = (CheckBox) gui.getGUINode(GeneratorProperty.GENERATOR_SITE_HOMEWORK_CHECKBOX);
         check.setSelected(Boolean.parseBoolean(siteJson.getString(JSON_HW_CHECK)));
         AppNodesBuilder.imageButton imageButton = (AppNodesBuilder.imageButton) gui.getGUINode(GeneratorProperty.SITE_FAVICON);
         this.loadButtonImage(siteJson.getString(JSON_FAVICON_IMAGE), imageButton);
         imageButton = (AppNodesBuilder.imageButton) gui.getGUINode(GeneratorProperty.SITE_NAVICON);
         this.loadButtonImage(siteJson.getString(JSON_NAVCON_IMAGE), imageButton);
         imageButton = (AppNodesBuilder.imageButton) gui.getGUINode(GeneratorProperty.SITE_LEFT_FOOTERCON);
         this.loadButtonImage(siteJson.getString(JSON_LEFTFOOT_IMAGE), imageButton);
         imageButton = (AppNodesBuilder.imageButton) gui.getGUINode(GeneratorProperty.SITE_RIGHT_FOOTERCON);
          this.loadButtonImage(siteJson.getString(JSON_RIGHTFOOT_IMAGE), imageButton);
         combo = (ComboBox) gui.getGUINode(GeneratorProperty.GENERATOR_SITE_CSS_COMBOBOX);
         if(combo.getItems().contains(siteJson.getString(JSON_CSS_DEFAULT))) this.loadComboDeafult(siteJson.getString(JSON_CSS_DEFAULT), combo);
         TextArea area = (TextArea) gui.getGUINode(GeneratorProperty.GENERATOR_SITE_OFFICE_HOURS_TEXT_AREA);
         TextField textField = (TextField) gui.getGUINode(GeneratorProperty.GENERATOR_SITE_NAME_TEXTFIELD);
         textField.setText(siteJson.getString(JSON_INSTRUCTOR_NAME));
         textField = (TextField) gui.getGUINode(GeneratorProperty.GENERATOR_SITE_ROOM_TEXTFIELD);
         textField.setText(siteJson.getString(JSON_INSTRUCTOR_ROOM));
         textField = (TextField) gui.getGUINode(GeneratorProperty.GENERATOR_SITE_EMAIL_TEXTFIELD);
         textField.setText(siteJson.getString(JSON_INSTRUCTOR_EMAIL));
         textField = (TextField) gui.getGUINode(GeneratorProperty.GENERATOR_SITE_HOME_PAGE_TEXTFIELD);
         textField.setText(siteJson.getString(JSON_INSTRUCTOR_HOMEPAGE));
         area.setText(siteJson.getString(JSON_SITE_OFFICE_HOURS));
        
        
    }
    
    
    private void loadSyllabusTab(JsonObject syllabusJson){
         AppGUIModule gui = app.getGUIModule();
         TextArea area = (TextArea) gui.getGUINode(GeneratorProperty.GENERATOR_SYLLABUS_AREA_DECSRCIPTION);
         area.setText(syllabusJson.getString(JSON_DESCRIPTION));
         area = (TextArea) gui.getGUINode(GeneratorProperty.GENERATOR_SYLLABUS_AREA_TOPIC);
         area.setText(syllabusJson.getString(JSON_TOPIC ));
         area = (TextArea) gui.getGUINode(GeneratorProperty.GENERATOR_SYLLABUS_AREA_PERE);
         area.setText(syllabusJson.getString(JSON_PEREQUISITE));
         area = (TextArea) gui.getGUINode(GeneratorProperty.GENERATOR_SYLLABUS_AREA_OUT);
         area.setText(syllabusJson.getString(JSON_OUTCOME));
         area = (TextArea) gui.getGUINode(GeneratorProperty.GENERATOR_SYLLABUS_AREA_BOOK);
         area.setText(syllabusJson.getString(JSON_TEXTBOOK));
         area = (TextArea) gui.getGUINode(GeneratorProperty.GENERATOR_SYLLABUS_AREA_GRADE);
         area.setText(syllabusJson.getString(JSON_GRADED_COMPONENET));
         area = (TextArea) gui.getGUINode(GeneratorProperty.GENERATOR_SYLLABUS_AREA_NOTE);
         area.setText(syllabusJson.getString(JSON_GRADING));
         area = (TextArea) gui.getGUINode(GeneratorProperty.GENERATOR_SYLLABUS_AREA_DISHONESTY);
         area.setText(syllabusJson.getString(JSON_ACADEMIC_DISHONESTY));
         area = (TextArea) gui.getGUINode(GeneratorProperty.GENERATOR_SYLLABUS_AREA_ASIS);
         area.setText(syllabusJson.getString(JSON_ASISTANCE));
    }
    
    private void loadOfficeHoursTab(AppDataComponent data, String filePath, JsonObject json) {
                  // CLEAR THE OLD DATA OUT
	GeneratorData dataManager = (GeneratorData)data;
                   dataManager.reset();

	

	// LOAD THE START AND END HOURS
	String startHour = json.getString(JSON_START_HOUR);
        String endHour = json.getString(JSON_END_HOUR);
        dataManager.initHours(startHour, endHour);
        
        // LOAD ALL THE GRAD TAs
        loadTAs(dataManager, json, JSON_GRAD_TAS);
        loadTAs(dataManager, json, JSON_UNDERGRAD_TAS);
        

        // AND THEN ALL THE OFFICE HOURS
        JsonArray jsonOfficeHoursArray = json.getJsonArray(JSON_OFFICE_HOURS);
        for (int i = 0; i < jsonOfficeHoursArray.size(); i++) {
            JsonObject jsonOfficeHours = jsonOfficeHoursArray.getJsonObject(i);
            String startTime = jsonOfficeHours.getString(JSON_START_TIME);
            TimeSlot.DayOfWeek dow = TimeSlot.DayOfWeek.valueOf(jsonOfficeHours.getString(JSON_DAY_OF_WEEK));
            String name = jsonOfficeHours.getString(JSON_NAME);
            TeachingAssistantPrototype ta = dataManager.getTAWithName(name);
            TimeSlot timeSlot = dataManager.getTimeSlot(startTime);
            timeSlot.toggleTA(dow, ta);
        }
        dataManager.backUpOfficeHours();
        ComboBox start = (ComboBox)app.getGUIModule().getGUINode(GeneratorProperty.GENERATOR_OH_START_TIME_COMBO);
        ComboBox end = (ComboBox) app.getGUIModule().getGUINode(GeneratorProperty.GENERATOR_OH_END_TIME_COMBO);
        this.loadComboDeafult(json.getString(JSON_OH_START_DEFAULT), start);
        this.loadComboDeafult(json.getString(JSON_OH_END_DEFAULT), end);
        dataManager.updateToCombo(json.getString(JSON_OH_START_DEFAULT), json.getString(JSON_OH_END_DEFAULT));
    }
    
    
    
    private void loadTAs(GeneratorData data, JsonObject json, String tas) {
        JsonArray jsonTAArray = json.getJsonArray(tas);
        for (int i = 0; i < jsonTAArray.size(); i++) {
            JsonObject jsonTA = jsonTAArray.getJsonObject(i);
            String name = jsonTA.getString(JSON_NAME);
            String email = jsonTA.getString(JSON_EMAIL);
            TAType type = TAType.valueOf(jsonTA.getString(JSON_TYPE));
            TeachingAssistantPrototype ta = new TeachingAssistantPrototype(name, email, type);
            data.addTA(ta);
        }     
    }
      
    // HELPER METHOD FOR LOADING DATA FROM A JSON FORMAT
    private JsonObject loadJSONFile(String jsonFilePath) throws IOException {
        InputStream is = new FileInputStream(jsonFilePath);
        JsonReader jsonReader = Json.createReader(is);
        JsonObject json = jsonReader.readObject();
        jsonReader.close();
        is.close();
        return json;
    }
    
    private void loadScheduleTab(JsonObject scheduleJson){
         AppGUIModule gui = app.getGUIModule();
         DatePicker dateCombo = (DatePicker) gui.getGUINode(GeneratorProperty.GENERATOR_SCHEDULE_STARTING_COMBO);
         this.loadDateComboDeafult(scheduleJson.getString(JSON_MONDAY_DEFAULT) , dateCombo);
         dateCombo = (DatePicker) gui.getGUINode(GeneratorProperty.GENERATOR_SCHEDULE_ENDING_COMBO);
         this.loadDateComboDeafult(scheduleJson.getString(JSON_FRIDAY_DEFAULT) , dateCombo);
         
         
          JsonArray scheduleItemsArray = scheduleJson.getJsonArray(JSON_SCHEDULE_ITEMS_ARRAY);
         GeneratorData data = (GeneratorData) app.getDataComponent();
         for (int i = 0; i < scheduleItemsArray.size(); i++) {
            JsonObject schduleItemJson = scheduleItemsArray.getJsonObject(i);
            String type = schduleItemJson.getString(JSON_SCHEDULE_ITEMS_TYPE);
            String date = schduleItemJson.getString(JSON_SCHEDULE_ITEMS_DATE);
            String title = schduleItemJson.getString(JSON_SCHEDULE_ITEMS_TITLE);
            String topic =  schduleItemJson.getString(JSON_SCHEDULE_ITEMS_TOPIC);
            String link =  schduleItemJson.getString(JSON_SCHEDULE_ITEMS_LINK);
            ScheduleItem item = new ScheduleItem(type, date, title, topic, link);
            data.addSchedule(item);
        }     
        
         
         ComboBox combo = (ComboBox) gui.getGUINode(GeneratorProperty.GENERATOR_SCHEDULE_TYPE_COMBOBOX);
         this.loadComboDeafult(scheduleJson.getString(JSON_TYPE_DEFAULT)  , combo);
         dateCombo = (DatePicker) gui.getGUINode(GeneratorProperty.GENERATOR_SCHEDULE_DATE_COMBOBOX);
         this.loadDateComboDeafult(scheduleJson.getString(JSON_DATE_DEFAULT), dateCombo);
         TextField textField = (TextField) gui.getGUINode(GeneratorProperty.GENERATOR_SCHEDULE_TITLE_TEXTFIELD);
         textField.setText(scheduleJson.getString(JSON_TITLE));
         textField = (TextField) gui.getGUINode(GeneratorProperty.GENERATOR_SCHEDULE_TOPIC_TEXTFIELD);
         textField.setText(scheduleJson.getString(JSON_SCHEDULE_TOPIC));
         textField = (TextField) gui.getGUINode(GeneratorProperty.GENERATOR_SCHEDULE_LINK_TEXTFIELD);
         textField.setText(scheduleJson.getString(JSON_LINK));
         data.updateScheduleStartEndSelect();
    }

    @Override
    public void saveData(AppDataComponent data, String filePath) throws IOException {
	// GET THE DATA
	GeneratorData dataManager = (GeneratorData)data;

	
        
	// THEN PUT IT ALL TOGETHER IN A JsonObject
        JsonObject dataManagerJSO  = Json.createObjectBuilder()
		.add(JSON_OH_DATA, this.getOHJson())
                .add(JSON_SITE_DATA, this.getSaveSiteTabJson())	
                .add(JSON_SYLLABUS_DATA, this.getSyllabusJson())
                .add(JSON_MEETING_DATA, this.getMeetingJson())
                .add(JSON_SCHEDULE_DATA, this.getScheduleJson())
                .build();
        
	
	// AND NOW OUTPUT IT TO A JSON FILE WITH PRETTY PRINTING
	Map<String, Object> properties = new HashMap<>(1);
	properties.put(JsonGenerator.PRETTY_PRINTING, true);
	JsonWriterFactory writerFactory = Json.createWriterFactory(properties);
	StringWriter sw = new StringWriter();
	JsonWriter jsonWriter = writerFactory.createWriter(sw);
	jsonWriter.writeObject(dataManagerJSO);
	jsonWriter.close();

	// INIT THE WRITER
	OutputStream os = new FileOutputStream(filePath);
	JsonWriter jsonFileWriter = Json.createWriter(os);
	jsonFileWriter.writeObject(dataManagerJSO);
	String prettyPrinted = sw.toString();
	PrintWriter pw = new PrintWriter(filePath);
	pw.write(prettyPrinted);
	pw.close();
        this.saved = true;
       Button button = (Button) app.getGUIModule().getGUINode(EXPORT_BUTTON);
       button.setDisable(false);
    }
    
    public void saveComboTextFieldOption(Object comboID) throws IOException{
        ComboBox combo = (ComboBox) app.getGUIModule().getGUINode(comboID);
        String filePath = System.getProperty("user.dir");
        filePath += File.separator + "work" + File.separator + comboID + ".json"; 
         new File(filePath).createNewFile();
        
        JsonArrayBuilder optionsArrayBuilder = Json.createArrayBuilder();
        for(Object item : combo.getItems()){
            JsonObject optionName = Json.createObjectBuilder().add("Option", item.toString()).build();
            optionsArrayBuilder.add(optionName);
        }
        
        JsonArray optionsArray = optionsArrayBuilder.build();
        
        JsonObject dataManagerJSO = Json.createObjectBuilder()
	.add(comboID+" OPTIONS", optionsArray)
	.build();
        
        Map<String, Object> properties = new HashMap<>(1);
        properties.put(JsonGenerator.PRETTY_PRINTING, true);
        JsonWriterFactory writerFactory = Json.createWriterFactory(properties);
        StringWriter sw = new StringWriter();
        JsonWriter jsonWriter = writerFactory.createWriter(sw);
        jsonWriter.writeObject(dataManagerJSO);
        jsonWriter.close();
        
        // INIT THE WRITER
        OutputStream os = new FileOutputStream(filePath);
        JsonWriter jsonFileWriter = Json.createWriter(os);
        jsonFileWriter.writeObject(dataManagerJSO);
        String prettyPrinted = sw.toString();
        PrintWriter pw = new PrintWriter(filePath);
        pw.write(prettyPrinted);
        pw.close();
        
    }
    
    public void loadComboTextFieldOption(){
        ComboBox subjectCombo = (ComboBox) app.getGUIModule().getGUINode(GeneratorProperty.GENERATOR_SITE_SUBJECT_COMBOBOX);
        ComboBox numberCombo = (ComboBox) app.getGUIModule().getGUINode(GeneratorProperty.GENERATOR_SITE_NUMBER_COMBOBOX);
        ObservableList subjectItems = subjectCombo.getItems();
        ObservableList numberItems = numberCombo.getItems();
        String parentFilePath = System.getProperty("user.dir");
        parentFilePath += File.separator + "work";
        
        if(new File(parentFilePath+File.separator+ GeneratorProperty.GENERATOR_SITE_SUBJECT_COMBOBOX+".json").exists()){
            subjectItems.clear();
            String filePath = parentFilePath+File.separator+ GeneratorProperty.GENERATOR_SITE_SUBJECT_COMBOBOX+".json";
            try{
            this.loadComboItems(subjectItems, GeneratorProperty.GENERATOR_SITE_SUBJECT_COMBOBOX, filePath, subjectCombo);
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        
        if(new File(parentFilePath+File.separator+ GeneratorProperty.GENERATOR_SITE_NUMBER_COMBOBOX+".json").exists()){
            numberItems.clear();
            String filePath = parentFilePath+File.separator+ GeneratorProperty.GENERATOR_SITE_NUMBER_COMBOBOX+".json";
            try{
            this.loadComboItems(numberItems, GeneratorProperty.GENERATOR_SITE_NUMBER_COMBOBOX, filePath, numberCombo);
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        
        
       
    }
    
     private void loadComboItems(ObservableList items, Object comboID, String filePath, ComboBox combo) throws IOException {
	JsonObject json = loadJSONFile(filePath);
                  JsonArray jsonTAArray = json.getJsonArray(comboID+" OPTIONS");
                  for (int i = 0; i < jsonTAArray.size(); i++) {
                     JsonObject jsonOption = jsonTAArray.getJsonObject(i);
                     String option = jsonOption.getString("Option");
                     items.add(option);
                  }  
                  combo.getSelectionModel().select(0);
                 
                  
        }
     
     public JsonObject getOHJson(){
         // GET THE DATA
	GeneratorData dataManager = (GeneratorData)app.getDataComponent();
        // NOW BUILD THE TA JSON OBJCTS TO SAVE
	JsonArrayBuilder gradTAsArrayBuilder = Json.createArrayBuilder();
                  JsonArrayBuilder undergradTAsArrayBuilder = Json.createArrayBuilder();
	Iterator<TeachingAssistantPrototype> tasIterator = dataManager.teachingAssistantsIterator();
        while (tasIterator.hasNext()) {
            TeachingAssistantPrototype ta = tasIterator.next();
	    JsonObject taJson = Json.createObjectBuilder()
		    .add(JSON_NAME, ta.getName())
		    .add(JSON_EMAIL, ta.getEmail())
                    .add(JSON_TYPE, ta.getType().toString()).build();
            if (ta.getType().equals(TAType.Graduate.toString()))
                gradTAsArrayBuilder.add(taJson);
            else
                undergradTAsArrayBuilder.add(taJson);
	}
        JsonArray gradTAsArray = gradTAsArrayBuilder.build();
	JsonArray undergradTAsArray = undergradTAsArrayBuilder.build();

	// NOW BUILD THE OFFICE HOURS JSON OBJCTS TO SAVE
	JsonArrayBuilder officeHoursArrayBuilder = Json.createArrayBuilder();
        Iterator<TimeSlot> timeSlotsIterator = dataManager.backUpOfficeHoursIterator();
        while (timeSlotsIterator.hasNext()) {
            TimeSlot timeSlot = timeSlotsIterator.next();
            for (int i = 0; i < TimeSlot.DayOfWeek.values().length; i++) {
                TimeSlot.DayOfWeek dow = TimeSlot.DayOfWeek.values()[i];
                tasIterator = timeSlot.getTAsIterator(dow);
                while (tasIterator.hasNext()) {
                    TeachingAssistantPrototype ta = tasIterator.next();
                    JsonObject tsJson = Json.createObjectBuilder()
                        .add(JSON_START_TIME, timeSlot.getStartTime().replace(":", "_"))
                        .add(JSON_DAY_OF_WEEK, dow.toString())
                        .add(JSON_NAME, ta.getName())
                        .build();
                    officeHoursArrayBuilder.add(tsJson);
                }
            }
	}
	JsonArray officeHoursArray = officeHoursArrayBuilder.build();
        JsonObject officeHourJSO  = Json.createObjectBuilder()
		.add(JSON_START_HOUR, "" + dataManager.getStartHour())
		.add(JSON_END_HOUR, "" + dataManager.getEndHour())
                .add(JSON_OH_START_DEFAULT, this.getComboDefault(GeneratorProperty.GENERATOR_OH_START_TIME_COMBO))
                .add(JSON_OH_END_DEFAULT, this.getComboDefault(GeneratorProperty.GENERATOR_OH_END_TIME_COMBO))
                .add(JSON_GRAD_TAS, gradTAsArray)
                .add(JSON_UNDERGRAD_TAS, undergradTAsArray)
                .add(JSON_OFFICE_HOURS, officeHoursArray).build();
        return officeHourJSO;
     }
     
     public JsonObject getMeetingJson(){
         GeneratorData data = (GeneratorData) app.getDataComponent();
         JsonObjectBuilder builder = Json.createObjectBuilder();
         
         // create a json array for the lecture items table. 
         JsonArrayBuilder lectureArrayBuilder = Json.createArrayBuilder();
         ObservableList<Lecture> lectureList = data.getLectures();
         for(Lecture item : lectureList){
             JsonObject lectureJson = Json.createObjectBuilder()
             .add(JSON_LECTURE_SECTION, item.getSection()) 
             .add(JSON_LECTURE_DAY, item.getDay())
             .add(JSON_LECTURE_TIME, item.getTime())
             .add(JSON_LECTURE_ROOM, item.getRoom())
             .build();
             lectureArrayBuilder.add(lectureJson);
         }
         JsonArray lectureArray = lectureArrayBuilder.build();
         builder.add(JSON_LECTURE_ARRAY , lectureArray);
         
         
         // create a json array for the recitation items table.
         JsonArrayBuilder recitationArrayBuilder = Json.createArrayBuilder();
         ObservableList<Recitation> recitationList = data.getRecitations();
         for(Recitation item : recitationList){
             JsonObject recitationJson = Json.createObjectBuilder()
             .add(JSON_RECITATION_SECTION, item.getSection()) 
             .add(JSON_RECITATION_DAYS_AND_TIME, item.getDay())
             .add(JSON_RECITATION_ROOM, item.getRoom())
             .add(JSON_RECITATION_TA1, item.getTa1())
             .add(JSON_RECITATION_TA2, item.getTa2())
             .build();
             recitationArrayBuilder.add(recitationJson);
         }
         JsonArray recitationArray = recitationArrayBuilder.build();
         builder.add(JSON_RECITATION_ARRAY  , recitationArray);
         
         
         // create a json array for the lab items table. 
         JsonArrayBuilder labArrayBuilder = Json.createArrayBuilder();
        ObservableList<Lab>  labList = data.getLabs();
        for(Lab item : labList){
             JsonObject labJson = Json.createObjectBuilder()
             .add(JSON_LAB_SECTION, item.getSection()) 
             .add(JSON_LAB_DAYS_AND_TIME, item.getDay())
             .add(JSON_LAB_ROOM, item.getRoom())
             .add(JSON_LAB_TA1, item.getTa1())
             .add(JSON_LAB_TA2, item.getTa2())
             .build();
             labArrayBuilder.add(labJson);
         }
         JsonArray labArray = labArrayBuilder.build();
         builder.add(JSON_LAB_ARRAY   , labArray);
         return builder.build();
     }
     
     public JsonObject getSaveSiteTabJson() {
         AppGUIModule gui = app.getGUIModule();
         JsonObjectBuilder builder = Json.createObjectBuilder();
         ComboBox combo = (ComboBox) gui.getGUINode(GeneratorProperty.GENERATOR_SITE_SUBJECT_COMBOBOX);
         builder.add(JSON_SUBJECT_DEFAULT, combo.getValue().toString());
         combo = (ComboBox) gui.getGUINode(GeneratorProperty.GENERATOR_SITE_NUMBER_COMBOBOX);
         builder.add(JSON_NUMBER_DEFAULT, combo.getValue().toString());
         combo = (ComboBox) gui.getGUINode(GeneratorProperty.GENERATOR_SITE_SEMESTER_COMBOBOX);
         builder.add(JSON_SEMESRER_DEAFULT, combo.getValue().toString());
         combo = (ComboBox) gui.getGUINode(GeneratorProperty.GENERATOR_SITE_YEAR_COMBOBOX);
         builder.add(JSON_YEAR_DEAFULT, combo.getValue().toString());
         Label label = (Label) gui.getGUINode(GeneratorProperty.GENERATOR_SITE_EXPORT_LABEL);
         builder.add(JSON_EXPORT_LABEL, label.getText());
         CheckBox check = (CheckBox) gui.getGUINode(GeneratorProperty.GENERATOR_SITE_HOME_CHECKBOX);
         builder.add(JSON_HOME_CHECK, String.valueOf(check.isSelected()));
         check = (CheckBox) gui.getGUINode(GeneratorProperty.GENERATOR_SITE_SYLLABUS_CHECKBOX);
         builder.add(JSON_SYLLABUS_CHECK, String.valueOf(check.isSelected()));
         check = (CheckBox) gui.getGUINode(GeneratorProperty.GENERATOR_SITE_SCHEDULE_CHECKBOX);
         builder.add(JSON_SCHEDULE_CHECK, String.valueOf(check.isSelected()));
         check = (CheckBox) gui.getGUINode(GeneratorProperty.GENERATOR_SITE_HOMEWORK_CHECKBOX);
         builder.add(JSON_HW_CHECK, String.valueOf(check.isSelected()));
         AppNodesBuilder.imageButton imageButton = (AppNodesBuilder.imageButton) gui.getGUINode(GeneratorProperty.SITE_FAVICON);
         builder.add(JSON_FAVICON_IMAGE, imageButton.getFilePath());
         imageButton = (AppNodesBuilder.imageButton) gui.getGUINode(GeneratorProperty.SITE_NAVICON);
         builder.add(JSON_NAVCON_IMAGE, imageButton.getFilePath());
         imageButton = (AppNodesBuilder.imageButton) gui.getGUINode(GeneratorProperty.SITE_LEFT_FOOTERCON);
         builder.add(JSON_LEFTFOOT_IMAGE, imageButton.getFilePath());
         imageButton = (AppNodesBuilder.imageButton) gui.getGUINode(GeneratorProperty.SITE_RIGHT_FOOTERCON);
         builder.add(JSON_RIGHTFOOT_IMAGE, imageButton.getFilePath());
         combo = (ComboBox) gui.getGUINode(GeneratorProperty.GENERATOR_SITE_CSS_COMBOBOX);
         builder.add(JSON_CSS_DEFAULT, combo.getValue().toString());
         TextField textField = (TextField) gui.getGUINode(GeneratorProperty.GENERATOR_SITE_NAME_TEXTFIELD);
         builder.add(JSON_INSTRUCTOR_NAME, textField.getText());
         textField = (TextField) gui.getGUINode(GeneratorProperty.GENERATOR_SITE_ROOM_TEXTFIELD);
         builder.add(JSON_INSTRUCTOR_ROOM, textField.getText());
         textField = (TextField) gui.getGUINode(GeneratorProperty.GENERATOR_SITE_EMAIL_TEXTFIELD);
         builder.add(JSON_INSTRUCTOR_EMAIL, textField.getText());
         textField = (TextField) gui.getGUINode(GeneratorProperty.GENERATOR_SITE_HOME_PAGE_TEXTFIELD);
         builder.add(JSON_INSTRUCTOR_HOMEPAGE, textField.getText());
         
/*-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
         TextArea area = (TextArea) gui.getGUINode(GeneratorProperty.GENERATOR_SITE_OFFICE_HOURS_TEXT_AREA);
         builder.add(JSON_SITE_OFFICE_HOURS, area.getText());
         return  builder.build();
     }
     
     public JsonObject getSyllabusJson(){
         AppGUIModule gui = app.getGUIModule();
         JsonObjectBuilder builder = Json.createObjectBuilder();
         TextArea area = (TextArea) gui.getGUINode(GeneratorProperty.GENERATOR_SYLLABUS_AREA_DECSRCIPTION);
         builder.add(JSON_DESCRIPTION  , area.getText());
         area = (TextArea) gui.getGUINode(GeneratorProperty.GENERATOR_SYLLABUS_AREA_TOPIC);
         builder.add(JSON_TOPIC  , area.getText());
         area = (TextArea) gui.getGUINode(GeneratorProperty.GENERATOR_SYLLABUS_AREA_PERE);
         builder.add(JSON_PEREQUISITE   , area.getText());
         area = (TextArea) gui.getGUINode(GeneratorProperty.GENERATOR_SYLLABUS_AREA_OUT);
         builder.add(JSON_OUTCOME    , area.getText());
         area = (TextArea) gui.getGUINode(GeneratorProperty.GENERATOR_SYLLABUS_AREA_BOOK);
         builder.add(JSON_TEXTBOOK    , area.getText());
         area = (TextArea) gui.getGUINode(GeneratorProperty.GENERATOR_SYLLABUS_AREA_GRADE);
         builder.add(JSON_GRADED_COMPONENET     , area.getText());
         area = (TextArea) gui.getGUINode(GeneratorProperty.GENERATOR_SYLLABUS_AREA_NOTE);
         builder.add(JSON_GRADING     , area.getText());
         area = (TextArea) gui.getGUINode(GeneratorProperty.GENERATOR_SYLLABUS_AREA_DISHONESTY);
         builder.add(JSON_ACADEMIC_DISHONESTY     , area.getText());
         area = (TextArea) gui.getGUINode(GeneratorProperty.GENERATOR_SYLLABUS_AREA_ASIS);
         builder.add(JSON_ASISTANCE     , area.getText());
         return builder.build();
     }
     
     public JsonObject getScheduleJson(){
         AppGUIModule gui = app.getGUIModule();
         JsonObjectBuilder builder = Json.createObjectBuilder();
         DatePicker dateCombo = (DatePicker) gui.getGUINode(GeneratorProperty.GENERATOR_SCHEDULE_STARTING_COMBO);
         builder.add(JSON_MONDAY_DEFAULT , dateCombo.getValue().toString());
         dateCombo = (DatePicker) gui.getGUINode(GeneratorProperty.GENERATOR_SCHEDULE_ENDING_COMBO);
         builder.add(JSON_FRIDAY_DEFAULT , dateCombo.getValue().toString());
         
         
         JsonArrayBuilder scheduleItemsBuilder = Json.createArrayBuilder();
         GeneratorData data = (GeneratorData) app.getDataComponent();
         for(ScheduleItem item : data.getBackUpSchedules()){
         JsonObject scheduleJson = Json.createObjectBuilder()
             .add(JSON_SCHEDULE_ITEMS_TYPE, item.getType()) 
             .add(JSON_SCHEDULE_ITEMS_DATE, item.getUnformattedDate())
             .add(JSON_SCHEDULE_ITEMS_TITLE, item.getTitle())
             .add(JSON_SCHEDULE_ITEMS_TOPIC, item.getTopic())
             .add(JSON_SCHEDULE_ITEMS_LINK, item.getLink())
             .build();
             scheduleItemsBuilder.add(scheduleJson);
         }
         JsonArray scheduleItemsArray = scheduleItemsBuilder.build();
         builder.add(JSON_SCHEDULE_ITEMS_ARRAY , scheduleItemsArray);
         
         ComboBox combo = (ComboBox) gui.getGUINode(GeneratorProperty.GENERATOR_SCHEDULE_TYPE_COMBOBOX);
         builder.add(JSON_TYPE_DEFAULT  , combo.getValue().toString());
         dateCombo = (DatePicker) gui.getGUINode(GeneratorProperty.GENERATOR_SCHEDULE_DATE_COMBOBOX);
         builder.add(JSON_DATE_DEFAULT  , dateCombo.getValue().toString());
         TextField textField = (TextField) gui.getGUINode(GeneratorProperty.GENERATOR_SCHEDULE_TITLE_TEXTFIELD);
         builder.add(JSON_TITLE , textField.getText());
         textField = (TextField) gui.getGUINode(GeneratorProperty.GENERATOR_SCHEDULE_TOPIC_TEXTFIELD);
         builder.add(JSON_SCHEDULE_TOPIC , textField.getText());
         textField = (TextField) gui.getGUINode(GeneratorProperty.GENERATOR_SCHEDULE_LINK_TEXTFIELD);
         builder.add(JSON_LINK  , textField.getText());
         return builder.build();
     }
     
     
     private void loadMeetingTab(JsonObject meetingJson){
         GeneratorData data = (GeneratorData) app.getDataComponent();
         
         JsonArray lectureArray = meetingJson.getJsonArray(JSON_LECTURE_ARRAY);
          for (int i = 0; i < lectureArray.size(); i++) {
            JsonObject lectureJson = lectureArray.getJsonObject(i);
            String section = lectureJson.getString(JSON_LECTURE_SECTION);
            String day = lectureJson.getString(JSON_LECTURE_DAY);
            String time = lectureJson.getString(JSON_LECTURE_TIME);
            String room =  lectureJson.getString(JSON_LECTURE_ROOM);
            Lecture lecture = new Lecture();
            lecture.setAllItems(section, day, time, room);
            data.addLecture(lecture);
        }     
         
         JsonArray recitationArray = meetingJson.getJsonArray(JSON_RECITATION_ARRAY);
          for (int i = 0; i < recitationArray.size(); i++) {
            JsonObject recitationJson = recitationArray.getJsonObject(i);
            String section = recitationJson.getString(JSON_RECITATION_SECTION);
            String day = recitationJson.getString(JSON_RECITATION_DAYS_AND_TIME);
            String room = recitationJson.getString(JSON_RECITATION_ROOM);
            String ta1 =  recitationJson.getString(JSON_RECITATION_TA1);
            String ta2 =  recitationJson.getString(JSON_RECITATION_TA2);
            Recitation recitation = new Recitation();
            recitation.setAllItems(section, day, room, ta1, ta2);
            data.addRecitation(recitation);
        }     
         
         JsonArray labArray = meetingJson.getJsonArray(JSON_LAB_ARRAY);
          for (int i = 0; i < labArray.size(); i++) {
            JsonObject labJson = labArray.getJsonObject(i);
            String section = labJson.getString(JSON_LAB_SECTION);
            String day = labJson.getString(JSON_LAB_DAYS_AND_TIME);
            String room = labJson.getString(JSON_LAB_ROOM);
            String ta1 =  labJson.getString(JSON_LAB_TA1);
            String ta2 =  labJson.getString(JSON_LAB_TA2);
            Lab lab = new Lab();
            lab.setAllItems(section, day, room, ta1, ta2);
            data.addLab(lab);
        }    
     }
     
     
     
     private void loadComboDeafult(String defaultValue, ComboBox combo){
         ObservableList<String> items = combo.getItems();
         if(!defaultValue.equals("") && !items.contains(defaultValue)){
            items.add(defaultValue);
          }
         combo.getSelectionModel().select(defaultValue);
     }
     
     private void loadDateComboDeafult(String defaultValue, DatePicker dateCombo){
        LocalDate locateDate = LocalDate.parse(defaultValue);
        dateCombo.setValue(locateDate);
     }
     
     private void loadButtonImage( String imagePath, Button button){
         Image buttonImage = new Image(imagePath);
        if (!buttonImage.isError()) {
             button.setGraphic(new ImageView(buttonImage));
            ((AppNodesBuilder.imageButton) button).setFilePath(imagePath);
        }
     }
     
     private String getComboDefault(Object comboID){
         AppGUIModule gui = app.getGUIModule();
        ComboBox combo = (ComboBox) gui.getGUINode(comboID);
        return combo.getValue().toString();
     }
     
    


    public boolean isJSONValid(String test) {
        try {
            new JSONObject(test);
         } catch (JSONException ex) {
          // edited, to include @Arthur's comment
          // e.g. in case JSONArray is valid as well...
        try {
            new JSONArray(test);
        } catch (JSONException ex1) {
            return false;
          }
        }
     return true;
    }
    
    public String getExportFilePath(){
        String result = "";
        result = AppTemplate.PATH_DATA + "export" + File.separator + "CourseSite"+File.separator+ "js";
        return result;
    }
    
    
    
    
    
    // IMPORTING/EXPORTING DATA IS USED WHEN WE READ/WRITE DATA IN AN
    // ADDITIONAL FORMAT USEFUL FOR ANOTHER PURPOSE, LIKE ANOTHER APPLICATION

    @Override
    public void importData(AppDataComponent data, String filePath) throws IOException {
         System.out.println("Not supported yet");
    }

    @Override
    public void exportData(AppDataComponent data, String filePath) throws IOException {

         AppGUIModule gui = app.getGUIModule();
         GeneratorData genData = (GeneratorData) data;
         CheckBox home = (CheckBox) gui.getGUINode(GeneratorProperty.GENERATOR_SITE_HOME_CHECKBOX);
         CheckBox syllabus = (CheckBox) gui.getGUINode(GeneratorProperty.GENERATOR_SITE_SYLLABUS_CHECKBOX);
         CheckBox schedule = (CheckBox) gui.getGUINode(GeneratorProperty.GENERATOR_SITE_SCHEDULE_CHECKBOX);
         CheckBox hw = (CheckBox) gui.getGUINode(GeneratorProperty.GENERATOR_SITE_HOMEWORK_CHECKBOX);
         filePath = this.getExportFilePath();
         Label exportLable = (Label) app.getGUIModule().getGUINode(GeneratorProperty.GENERATOR_SITE_EXPORT_LABEL);
         GeneratorExporter exporter = new GeneratorExporter(this, filePath, genData, exportLable.getText());
         try{
            exporter.generatePageDataJson();
            exporter.generateOfficeHoursDataJson();
            exporter.generateSchedulejson(hw.isSelected());
            exporter.generateSectionsJson();
            exporter.generateSyllabusjson();
            exporter.moveFilesIntoPositions();
            new ExportedSiteDialog(app);
         }catch(Exception e){
             e.printStackTrace();
             customAlert.showAlert("Check if Text Areas are in the correct json format", "Export Error");
             return;
         }
         
    }
    
    private class invalidSaveException extends Exception{
            public invalidSaveException(String message){
                super(message);
            }
        
    }
    
    
    
}
