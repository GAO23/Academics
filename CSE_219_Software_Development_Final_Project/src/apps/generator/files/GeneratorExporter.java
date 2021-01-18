/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package apps.generator.files;

import apps.generator.data.GeneratorData;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParsingException;

import libs.DesktopJavaFramework.AppTemplate;
import org.apache.commons.io.FileUtils;
import sun.security.krb5.internal.PAData;

/**
 *
 * @author gaoxi
 */
public class GeneratorExporter {
    private JsonObject siteJson;
    private JsonObject syllabusJson;
    private JsonObject meetingJson;
    private JsonObject ohJson;
    private JsonObject scheduleJson;
    private String filePath;
    private GeneratorData data;
    
    public static final String EXPORTED_OH_JSON_FILE = "OfficeHoursData.json";
    public static final String EXPORTED_OH_INSTRUCTOR_JSON = "instructor";
    
    public static final String EXPORTED_PAGE_JSON_FILE = "PageData.json";
    public static final String EXPORTED_PAGE_TITLE = "title";
    public static final String EXPORTED_PAGE_TITLE_TEXT = "Course Site Generator Exported Site";
    public static final String EXPORTED_PAGE_LOGOS_JSON = "logos";
    public static final String EXPORTED_PAGE_LOGO_SRC = "src";
    public static final String EXPORTED_PAGE_LOGO_NAV = "./images/SBUDarkRedShieldLogo.png";
    public static final String EXPORTED_PAGE_LOGO_FAVI = "./images/SBUShieldFavicon.ico";
    public static final String EXPORTED_PAGE_LOGO_BOTTOM_RIGHT = "./images/SBUCSLogo.png";
    public static final String EXPORTED_PAGE_LOGO_BOTTOM_LEFT = "./images/SBUWhiteShieldLogo.jpg";
    public static final String EXPORTED_CSS_FILE = "sea_wolf.css";
    
    public static final String EXPORTED_PAGE_ARRAY = "pages";
    
    public static final String EXPORTED_SCHEDULE_JSON_FILE = "ScheduleData.json";
    public static final String EXPORTED_SCHEDULE_STARTING_MONTH = "startingMondayMonth";
    public static final String EXPORTED_SCHEDULE_STARTING_DAY = "startingMondayDay";
    public static final String EXPORTED_SCHEDULE_ENDING_MONTH = "endingFridayMonth";
    public static final String EXPORTED_SCHEDULE_ENDING_DAY = "endingFridayDay";
    public static final String EXPORTED_SCHEDULE_HOLLIDAYS = "holidays";
    public static final String EXPORTED_SCHEDULE_LECTURES = "lectures";
    public static final String EXPORTED_SCHEDULE_REFERENCES = "references";
    public static final String EXPORTED_SCHEDULE_RECITATIONS = "recitations";
    public static final String EXPORTED_SCHEDULE_HWS = "hws";
    public static final String EXPORTED_SCHEDULE_MONTH = "month";
    public static final String EXPORTED_SCHEDULE_DAY = "day";
    
    public static final String EXPORTED_SECTIONS_JSON_FILE = "SectionsData.json";
    
    public static final String EXPORTED_SYLLABUS_JSON_FILE = "SyllabusData.json";
    
    private String exportPath;
    
    public GeneratorExporter(GeneratorFiles file, String filePath, GeneratorData data, String exportPath) throws IOException{
        this.siteJson = file.getSaveSiteTabJson();
        this.syllabusJson = file.getSyllabusJson();
        this.meetingJson = file.getMeetingJson();
        this.ohJson = file.getOHJson();
        this.scheduleJson = file.getScheduleJson();
        this.filePath = filePath;
        this.data = data;
        this.exportPath = exportPath;
        this.deleteAllJsons();
    }
    
    private void deleteAllJsons() throws IOException{
        String ohPath = this.filePath+File.separator+EXPORTED_OH_JSON_FILE;
        String pagePath = this.filePath+File.separator+EXPORTED_PAGE_JSON_FILE;
        String schedulePath = this.filePath+File.separator+EXPORTED_SCHEDULE_JSON_FILE;
        String sectionPath = this.filePath+File.separator+EXPORTED_SECTIONS_JSON_FILE;
        String syllabusPath = this.filePath+File.separator+EXPORTED_SYLLABUS_JSON_FILE;
        new File(ohPath).delete();
        new File(pagePath).delete();
        new File(schedulePath).delete();
        new File(sectionPath).delete();
        new File(syllabusPath).delete();
        System.out.println("deleting " + this.exportPath);
        FileUtils.deleteDirectory(new File(this.exportPath));
    }
    
    
    public void generateOfficeHoursDataJson() throws FileNotFoundException, IOException{
        JsonObjectBuilder builder = Json.createObjectBuilder();
        
        JsonObject dataManagerJSO = builder
                .add(GeneratorFiles.JSON_START_HOUR, ohJson.getString(GeneratorFiles.JSON_START_HOUR))
                .add(GeneratorFiles.JSON_END_HOUR, ohJson.getString(GeneratorFiles.JSON_END_HOUR))
                .add(EXPORTED_OH_INSTRUCTOR_JSON, this.getInstructorJson())
                .add(GeneratorFiles.JSON_GRAD_TAS, ohJson.getJsonArray(GeneratorFiles.JSON_GRAD_TAS))
                .add(GeneratorFiles.JSON_UNDERGRAD_TAS, ohJson.getJsonArray(GeneratorFiles.JSON_UNDERGRAD_TAS))
                .add(GeneratorFiles.JSON_OFFICE_HOURS, ohJson.getJsonArray(GeneratorFiles.JSON_OFFICE_HOURS))
                .build();
        
        
        String path = this.filePath+File.separator+EXPORTED_OH_JSON_FILE;
        if(!new File(path).exists()){
            new File(path).createNewFile();
        }
        
        // AND NOW OUTPUT IT TO A JSON FILE WITH PRETTY PRINTING
	Map<String, Object> properties = new HashMap<>(1);
	properties.put(JsonGenerator.PRETTY_PRINTING, true);
	JsonWriterFactory writerFactory = Json.createWriterFactory(properties);
	StringWriter sw = new StringWriter();
	JsonWriter jsonWriter = writerFactory.createWriter(sw);
	jsonWriter.writeObject(dataManagerJSO);
	jsonWriter.close();

	// INIT THE WRITER
	OutputStream os = new FileOutputStream(path);
	JsonWriter jsonFileWriter = Json.createWriter(os);
	jsonFileWriter.writeObject(dataManagerJSO);
	String prettyPrinted = sw.toString();
	PrintWriter pw = new PrintWriter(path);
	pw.write(prettyPrinted);
	pw.close();
                
    }
    
    public void generatePageDataJson() throws FileNotFoundException, IOException{
        JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
        
        
        JsonObject dataManagerJSO = jsonBuilder
                .add(GeneratorFiles.JSON_SUBJECT_DEFAULT, siteJson.getString(GeneratorFiles.JSON_SUBJECT_DEFAULT))
                .add(GeneratorFiles.JSON_NUMBER_DEFAULT, siteJson.getString(GeneratorFiles.JSON_NUMBER_DEFAULT))
                .add(GeneratorFiles.JSON_SEMESRER_DEAFULT, siteJson.getString(GeneratorFiles.JSON_SEMESRER_DEAFULT))
                .add(GeneratorFiles.JSON_YEAR_DEAFULT, siteJson.getString(GeneratorFiles.JSON_YEAR_DEAFULT))
                .add(EXPORTED_PAGE_TITLE, EXPORTED_PAGE_TITLE_TEXT)
                .add(EXPORTED_PAGE_LOGOS_JSON, this.getLogosJson())
                .add(EXPORTED_OH_INSTRUCTOR_JSON, this.getInstructorJson())
                .add(EXPORTED_PAGE_ARRAY, this.getPagesArray()).build();
        
        String path = this.filePath+File.separator+EXPORTED_PAGE_JSON_FILE;
        if(!new File(path).exists()){
            new File(path).createNewFile();
        }
        // AND NOW OUTPUT IT TO A JSON FILE WITH PRETTY PRINTING
	Map<String, Object> properties = new HashMap<>(1);
	properties.put(JsonGenerator.PRETTY_PRINTING, true);
	JsonWriterFactory writerFactory = Json.createWriterFactory(properties);
	StringWriter sw = new StringWriter();
	JsonWriter jsonWriter = writerFactory.createWriter(sw);
	jsonWriter.writeObject(dataManagerJSO);
	jsonWriter.close();

	// INIT THE WRITER
	OutputStream os = new FileOutputStream(path);
	JsonWriter jsonFileWriter = Json.createWriter(os);
	jsonFileWriter.writeObject(dataManagerJSO);
	String prettyPrinted = sw.toString();
	PrintWriter pw = new PrintWriter(path);
	pw.write(prettyPrinted);
	pw.close();
                
    }
    
    public void generateSchedulejson(Boolean hasHW) throws IOException{
         JsonObjectBuilder builder = Json.createObjectBuilder();
         
         JsonObject dataManagerJSO = builder
                 .add(EXPORTED_SCHEDULE_STARTING_MONTH, data.getStartingMonth())
                 .add(EXPORTED_SCHEDULE_STARTING_DAY, data.getStartingDay())
                 .add(EXPORTED_SCHEDULE_ENDING_MONTH, data.getEndingMonth())
                 .add(EXPORTED_SCHEDULE_ENDING_DAY, data.getEndingDay())
                 .add(EXPORTED_SCHEDULE_HOLLIDAYS, this.getHollidayArray())
                 .add(EXPORTED_SCHEDULE_LECTURES, this.getLectureArray())
                 .add(EXPORTED_SCHEDULE_REFERENCES, this.getReferenceArray())
                 .add(EXPORTED_SCHEDULE_RECITATIONS, this.getRecitationArray())
                 .add(EXPORTED_SCHEDULE_HWS, this.getHWArray(hasHW))
                 .build();
         
        String path = this.filePath+File.separator+EXPORTED_SCHEDULE_JSON_FILE;
        if(!new File(path).exists()){
            new File(path).createNewFile();
        }
         
         // AND NOW OUTPUT IT TO A JSON FILE WITH PRETTY PRINTING
	Map<String, Object> properties = new HashMap<>(1);
	properties.put(JsonGenerator.PRETTY_PRINTING, true);
	JsonWriterFactory writerFactory = Json.createWriterFactory(properties);
	StringWriter sw = new StringWriter();
	JsonWriter jsonWriter = writerFactory.createWriter(sw);
	jsonWriter.writeObject(dataManagerJSO);
	jsonWriter.close();

	// INIT THE WRITER
	OutputStream os = new FileOutputStream(path);
	JsonWriter jsonFileWriter = Json.createWriter(os);
	jsonFileWriter.writeObject(dataManagerJSO);
	String prettyPrinted = sw.toString();
	PrintWriter pw = new PrintWriter(path);
	pw.write(prettyPrinted);
	pw.close();
    }
    
    public void generateSectionsJson() throws IOException{
         JsonObjectBuilder builder = Json.createObjectBuilder();
         
        String path = this.filePath+File.separator+EXPORTED_SECTIONS_JSON_FILE;
        if(!new File(path).exists()){
            new File(path).createNewFile();
        }
         
         // AND NOW OUTPUT IT TO A JSON FILE WITH PRETTY PRINTING
	Map<String, Object> properties = new HashMap<>(1);
	properties.put(JsonGenerator.PRETTY_PRINTING, true);
	JsonWriterFactory writerFactory = Json.createWriterFactory(properties);
	StringWriter sw = new StringWriter();
	JsonWriter jsonWriter = writerFactory.createWriter(sw);
	jsonWriter.writeObject(meetingJson);
	jsonWriter.close();

	// INIT THE WRITER
	OutputStream os = new FileOutputStream(path);
	JsonWriter jsonFileWriter = Json.createWriter(os);
	jsonFileWriter.writeObject(meetingJson);
	String prettyPrinted = sw.toString();
	PrintWriter pw = new PrintWriter(path);
	pw.write(prettyPrinted);
	pw.close();
    }
    
    public void generateSyllabusjson() throws IOException{
         JsonObjectBuilder builder = Json.createObjectBuilder();
         
         String description = syllabusJson.getString(GeneratorFiles.JSON_DESCRIPTION);
         String topics = syllabusJson.getString(GeneratorFiles.JSON_TOPIC);
         String prerequisites = syllabusJson.getString(GeneratorFiles.JSON_PEREQUISITE);
         String outcomes = syllabusJson.getString(GeneratorFiles.JSON_OUTCOME);
         String textbooks = syllabusJson.getString(GeneratorFiles.JSON_TEXTBOOK);
         String gradedComponents = syllabusJson.getString(GeneratorFiles.JSON_GRADED_COMPONENET);
         String gradingNote = syllabusJson.getString(GeneratorFiles.JSON_GRADING);
         String academicDishonesty = syllabusJson.getString(GeneratorFiles.JSON_ACADEMIC_DISHONESTY);
         String specialAssistance = syllabusJson.getString(GeneratorFiles.JSON_ASISTANCE);
         
         
         

         JsonObject dataManagerJSO = builder
                 .add(GeneratorFiles.JSON_DESCRIPTION, description)
                 .add(GeneratorFiles.JSON_TOPIC, this.getSyllabusTextAreaArray(topics))
                 .add(GeneratorFiles.JSON_PEREQUISITE, prerequisites)
                 .add(GeneratorFiles.JSON_OUTCOME,this.getSyllabusTextAreaArray(outcomes))
                 .add(GeneratorFiles.JSON_TEXTBOOK, this.getSyllabusTextAreaArray(textbooks))
                 .add(GeneratorFiles.JSON_GRADED_COMPONENET, this.getSyllabusTextAreaArray(gradedComponents))
                 .add(GeneratorFiles.JSON_GRADING, gradingNote)
                 .add(GeneratorFiles.JSON_ACADEMIC_DISHONESTY, academicDishonesty)
                 .add(GeneratorFiles.JSON_ASISTANCE, specialAssistance)
                 .build();
         
        String path = this.filePath+File.separator+EXPORTED_SYLLABUS_JSON_FILE;
        if(!new File(path).exists()){
            new File(path).createNewFile();
        }
         
         // AND NOW OUTPUT IT TO A JSON FILE WITH PRETTY PRINTING
	Map<String, Object> properties = new HashMap<>(1);
	properties.put(JsonGenerator.PRETTY_PRINTING, true);
	JsonWriterFactory writerFactory = Json.createWriterFactory(properties);
	StringWriter sw = new StringWriter();
	JsonWriter jsonWriter = writerFactory.createWriter(sw);
	jsonWriter.writeObject(dataManagerJSO);
	jsonWriter.close();

	// INIT THE WRITER
	OutputStream os = new FileOutputStream(path);
	JsonWriter jsonFileWriter = Json.createWriter(os);
	jsonFileWriter.writeObject(dataManagerJSO);
	String prettyPrinted = sw.toString();
	PrintWriter pw = new PrintWriter(path);
	pw.write(prettyPrinted);
	pw.close();
    }
    
    
    
    
    public JsonObject getInstructorJson(){
        
        JsonObjectBuilder instructorBuilder = Json.createObjectBuilder();
        JsonObject instructorJson = instructorBuilder.add(GeneratorFiles.JSON_INSTRUCTOR_NAME, siteJson.getString(GeneratorFiles.JSON_INSTRUCTOR_NAME))
                .add(GeneratorFiles.JSON_INSTRUCTOR_HOMEPAGE, siteJson.getString(GeneratorFiles.JSON_INSTRUCTOR_HOMEPAGE))
                .add(GeneratorFiles.JSON_INSTRUCTOR_EMAIL, siteJson.getString(GeneratorFiles.JSON_INSTRUCTOR_EMAIL))
                .add(GeneratorFiles.JSON_INSTRUCTOR_ROOM, siteJson.getString(GeneratorFiles.JSON_INSTRUCTOR_ROOM))
                .add(GeneratorFiles.JSON_SITE_OFFICE_HOURS, this.getInstructorOfficeHoursArray())
                .build();
        return instructorJson;
    }
    
    
    
    
    
    
    public JsonArray getInstructorOfficeHoursArray() throws JsonParsingException{
        String s = siteJson.getString(GeneratorFiles.JSON_SITE_OFFICE_HOURS);
        JsonReader r = Json.createReader(new StringReader(s));
        return r.readArray();
    }
    
    public JsonArray getSyllabusTextAreaArray(String textAreaText) throws JsonParsingException{
        JsonReader r = Json.createReader(new StringReader(textAreaText));
        return r.readArray();
    }
    
    
    public JsonObject getLogosJson(){
        JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
        
        JsonObject favJson = jsonBuilder.add(EXPORTED_PAGE_LOGO_SRC, EXPORTED_PAGE_LOGO_FAVI).build();
        jsonBuilder = Json.createObjectBuilder();
        JsonObject navJson = jsonBuilder.add(EXPORTED_PAGE_LOGO_SRC, EXPORTED_PAGE_LOGO_NAV).build();
        jsonBuilder = Json.createObjectBuilder();
        JsonObject BoLeJson = jsonBuilder.add(EXPORTED_PAGE_LOGO_SRC, EXPORTED_PAGE_LOGO_BOTTOM_LEFT).build();
        jsonBuilder = Json.createObjectBuilder();
        JsonObject BoRiJson = jsonBuilder.add(EXPORTED_PAGE_LOGO_SRC, EXPORTED_PAGE_LOGO_BOTTOM_RIGHT).build();
        jsonBuilder = Json.createObjectBuilder();
        
        JsonObject logosJson = jsonBuilder
                .add(GeneratorFiles.JSON_FAVICON_IMAGE, favJson)
                .add(GeneratorFiles.JSON_NAVCON_IMAGE, navJson)
                .add(GeneratorFiles.JSON_LEFTFOOT_IMAGE, BoLeJson)
                .add(GeneratorFiles.JSON_RIGHTFOOT_IMAGE, BoRiJson)
                .build();
        
        return logosJson;
    }
    
    public JsonArray getPagesArray(){
        String nameTag = "name";
        String linkTag = "link";
        
        JsonObjectBuilder builder = Json.createObjectBuilder();
        
        JsonObject homeJson = builder
                .add(nameTag, "Home")
                .add(linkTag, "index.html")
                .build();
        
        JsonObject syllabusJson = builder
                .add(nameTag, "Syllabus")
                .add(linkTag, "syllabus.html")
                .build();
        
        JsonObject scheduleJson = builder
                .add(nameTag, "Schedule")
                .add(linkTag, "schedule.html")
                .build();
        
        JsonObject hwJson = builder
                .add(nameTag, "HWs")
                .add(linkTag, "hws.html")
                .build();
                
        
        JsonArray pageArray = Json.createArrayBuilder()
                .add(homeJson)
                .add(syllabusJson)
                .add(scheduleJson)
                .add(hwJson)
                .build();
        
        return pageArray;
    }
    
    public JsonArray getHollidayArray(){
        JsonArray scheduleArray = scheduleJson.getJsonArray(GeneratorFiles.JSON_SCHEDULE_ITEMS_ARRAY);
        JsonObject[] jsons = new JsonObject[scheduleArray.size()];
        for (int i = 0; i < scheduleArray.size(); i++) {
            JsonObject schduleItemJson = scheduleArray.getJsonObject(i);
            String type = schduleItemJson.getString(GeneratorFiles.JSON_SCHEDULE_ITEMS_TYPE);
            if(!type.equals("Holiday")){
                continue;
            }
            String date = schduleItemJson.getString(GeneratorFiles.JSON_SCHEDULE_ITEMS_DATE);
            String title = schduleItemJson.getString(GeneratorFiles.JSON_SCHEDULE_ITEMS_TITLE);
            String topic =  schduleItemJson.getString(GeneratorFiles.JSON_SCHEDULE_ITEMS_TOPIC);
            String link =  schduleItemJson.getString(GeneratorFiles.JSON_SCHEDULE_ITEMS_LINK);
            jsons[i] = Json.createObjectBuilder()
                    .add(EXPORTED_SCHEDULE_MONTH, this.getMonth(date))
                    .add(EXPORTED_SCHEDULE_DAY, this.getDay(date))
                    .add(GeneratorFiles.JSON_SCHEDULE_ITEMS_TITLE, title)
                    .add(GeneratorFiles.JSON_SCHEDULE_ITEMS_LINK, link)
                    .build();
        }
        
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        for(JsonObject json : jsons){
            if (json==null) continue;
            arrayBuilder.add(json);
        }
        return arrayBuilder.build();
    }
    
    public JsonArray getLectureArray(){
        JsonArray scheduleArray = scheduleJson.getJsonArray(GeneratorFiles.JSON_SCHEDULE_ITEMS_ARRAY);
        JsonObject[] jsons = new JsonObject[scheduleArray.size()];
        for (int i = 0; i < scheduleArray.size(); i++) {
            JsonObject schduleItemJson = scheduleArray.getJsonObject(i);
            String type = schduleItemJson.getString(GeneratorFiles.JSON_SCHEDULE_ITEMS_TYPE);
            if(!type.equals("Lecture")){
                continue;
            }
            String date = schduleItemJson.getString(GeneratorFiles.JSON_SCHEDULE_ITEMS_DATE);
            String title = schduleItemJson.getString(GeneratorFiles.JSON_SCHEDULE_ITEMS_TITLE);
            String topic =  schduleItemJson.getString(GeneratorFiles.JSON_SCHEDULE_ITEMS_TOPIC);
            String link =  schduleItemJson.getString(GeneratorFiles.JSON_SCHEDULE_ITEMS_LINK);
            jsons[i] = Json.createObjectBuilder()
                    .add(EXPORTED_SCHEDULE_MONTH, this.getMonth(date))
                    .add(EXPORTED_SCHEDULE_DAY, this.getDay(date))
                    .add(GeneratorFiles.JSON_SCHEDULE_ITEMS_TITLE, title)
                    .add(GeneratorFiles.JSON_SCHEDULE_ITEMS_TOPIC, topic)
                    .add(GeneratorFiles.JSON_SCHEDULE_ITEMS_LINK, link)
                    .build();
        }
        
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        for(JsonObject json : jsons){
            if (json==null) continue;
            arrayBuilder.add(json);
        }
        return arrayBuilder.build();
    }
    
     public JsonArray getReferenceArray(){
        JsonArray scheduleArray = scheduleJson.getJsonArray(GeneratorFiles.JSON_SCHEDULE_ITEMS_ARRAY);
        JsonObject[] jsons = new JsonObject[scheduleArray.size()];
        for (int i = 0; i < scheduleArray.size(); i++) {
            JsonObject schduleItemJson = scheduleArray.getJsonObject(i);
            String type = schduleItemJson.getString(GeneratorFiles.JSON_SCHEDULE_ITEMS_TYPE);
            if(!type.equals("Reference")){
                continue;
            }
            String date = schduleItemJson.getString(GeneratorFiles.JSON_SCHEDULE_ITEMS_DATE);
            String title = schduleItemJson.getString(GeneratorFiles.JSON_SCHEDULE_ITEMS_TITLE);
            String topic =  schduleItemJson.getString(GeneratorFiles.JSON_SCHEDULE_ITEMS_TOPIC);
            String link =  schduleItemJson.getString(GeneratorFiles.JSON_SCHEDULE_ITEMS_LINK);
            jsons[i] = Json.createObjectBuilder()
                    .add(EXPORTED_SCHEDULE_MONTH, this.getMonth(date))
                    .add(EXPORTED_SCHEDULE_DAY, this.getDay(date))
                    .add(GeneratorFiles.JSON_SCHEDULE_ITEMS_TITLE, title)
                    .add(GeneratorFiles.JSON_SCHEDULE_ITEMS_TOPIC, topic)
                    .add(GeneratorFiles.JSON_SCHEDULE_ITEMS_LINK, link)
                    .build();
        }
        
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        for(JsonObject json : jsons){
            if (json==null) continue;
            arrayBuilder.add(json);
        }
        return arrayBuilder.build();
    }
     
     public JsonArray getRecitationArray(){
        JsonArray scheduleArray = scheduleJson.getJsonArray(GeneratorFiles.JSON_SCHEDULE_ITEMS_ARRAY);
        JsonObject[] jsons = new JsonObject[scheduleArray.size()];
        for (int i = 0; i < scheduleArray.size(); i++) {
            JsonObject schduleItemJson = scheduleArray.getJsonObject(i);
            String type = schduleItemJson.getString(GeneratorFiles.JSON_SCHEDULE_ITEMS_TYPE);
            if(!type.equals("Recitation")){
                continue;
            }
            String date = schduleItemJson.getString(GeneratorFiles.JSON_SCHEDULE_ITEMS_DATE);
            String title = schduleItemJson.getString(GeneratorFiles.JSON_SCHEDULE_ITEMS_TITLE);
            String topic =  schduleItemJson.getString(GeneratorFiles.JSON_SCHEDULE_ITEMS_TOPIC);
            String link =  schduleItemJson.getString(GeneratorFiles.JSON_SCHEDULE_ITEMS_LINK);
            jsons[i] = Json.createObjectBuilder()
                    .add(EXPORTED_SCHEDULE_MONTH, this.getMonth(date))
                    .add(EXPORTED_SCHEDULE_DAY, this.getDay(date))
                    .add(GeneratorFiles.JSON_SCHEDULE_ITEMS_TITLE, title)
                    .add(GeneratorFiles.JSON_SCHEDULE_ITEMS_TOPIC, topic)
                    .add(GeneratorFiles.JSON_SCHEDULE_ITEMS_LINK, link)
                    .build();
        }
        
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        for(JsonObject json : jsons){
            if (json==null) continue;
            arrayBuilder.add(json);
        }
        return arrayBuilder.build();
    }
     
     public JsonArray getHWArray(Boolean hasHW){
         
        JsonArray scheduleArray = scheduleJson.getJsonArray(GeneratorFiles.JSON_SCHEDULE_ITEMS_ARRAY);
        JsonObject[] jsons = new JsonObject[scheduleArray.size()];
        
        for (int i = 0; i < scheduleArray.size(); i++) {
            JsonObject schduleItemJson = scheduleArray.getJsonObject(i);
            String type = schduleItemJson.getString(GeneratorFiles.JSON_SCHEDULE_ITEMS_TYPE);
            if(!type.equals("HW")){
                continue;
            }
            String date = schduleItemJson.getString(GeneratorFiles.JSON_SCHEDULE_ITEMS_DATE);
            String title = schduleItemJson.getString(GeneratorFiles.JSON_SCHEDULE_ITEMS_TITLE);
            String topic =  schduleItemJson.getString(GeneratorFiles.JSON_SCHEDULE_ITEMS_TOPIC);
            String link =  schduleItemJson.getString(GeneratorFiles.JSON_SCHEDULE_ITEMS_LINK);
            jsons[i] = Json.createObjectBuilder()
                    .add(EXPORTED_SCHEDULE_MONTH, this.getMonth(date))
                    .add(EXPORTED_SCHEDULE_DAY, this.getDay(date))
                    .add(GeneratorFiles.JSON_SCHEDULE_ITEMS_TITLE, title)
                    .add(GeneratorFiles.JSON_SCHEDULE_ITEMS_TOPIC, topic)
                    .add(GeneratorFiles.JSON_SCHEDULE_ITEMS_LINK, link)
                    .build();
        }
        
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        if(hasHW != false){
             for(JsonObject json : jsons){
            if (json==null) continue;
            arrayBuilder.add(json);
        }
         }
        
        return arrayBuilder.build();
    }
    
    
     public String getMonth(String result){
       result = result.substring(5);
       result = result.substring(0, result.length() - 3);
       char firstLetter = result.charAt(0);
       if(firstLetter == '0'){
           result = result.substring(1);
       }
       return result;
    }
    
    public String getDay(String result){
       result = result.substring(8);
       char firstLetter = result.charAt(0);
       if(firstLetter == '0'){
           result = result.substring(1);
       }
       return result;
    }
    
    
    public void moveFilesIntoPositions() throws IOException, URISyntaxException{
       Path choosenFav = Paths.get(new URI(siteJson.getString(GeneratorFiles.JSON_FAVICON_IMAGE)));
       Path choosenNav = Paths.get(new URI(siteJson.getString(GeneratorFiles.JSON_NAVCON_IMAGE)));
       Path choosenLF = Paths.get(new URI(siteJson.getString(GeneratorFiles.JSON_LEFTFOOT_IMAGE)));
       Path choosenRF = Paths.get(new URI(siteJson.getString(GeneratorFiles.JSON_RIGHTFOOT_IMAGE)));
       String courseSiteImagePath = AppTemplate.PATH_DATA + "export" + File.separator + "CourseSite" + File.separator+ "images";
        
       Path favPath = Paths.get(courseSiteImagePath+File.separator+"SBUShieldFavicon.ico");
       Path navPath = Paths.get(courseSiteImagePath+File.separator+"SBUDarkRedShieldLogo.png");
       Path LFPath = Paths.get(courseSiteImagePath+File.separator+"SBUWhiteShieldLogo.jpg");
       Path RFPath = Paths.get(courseSiteImagePath+File.separator+"SBUCSLogo.png");

       
       File favImage = new File(favPath.toUri());
       favImage.delete();
       File navImage = new File(navPath.toUri());
       navImage.delete();
       File LFImage = new File(LFPath.toUri());
       LFImage.delete();
       File RFImage = new File(RFPath.toUri());
       RFImage.delete();
       
       String rootDir = AppTemplate.PATH_DATA;
       
       Files.copy(choosenFav, favPath);
       Files.copy(choosenNav, navPath);
       Files.copy(choosenLF, LFPath);
       Files.copy(choosenRF, RFPath);
       
       Path choosenCSS = Paths.get(rootDir + "export" + File.separator + "CourseSite" + File.separator + siteJson.getString(GeneratorFiles.JSON_CSS_DEFAULT));
       Path siteCSSPath = Paths.get(rootDir + "export" + File.separator + "CourseSite" + File.separator + "css" + File.separator + EXPORTED_CSS_FILE);
       new File(siteCSSPath.toUri()).delete();
       Files.copy(choosenCSS, siteCSSPath);
       
       
       Path exportPath = Paths.get(this.exportPath);
       Path courseSitePath = Paths.get(rootDir+"export"+File.separator+"CourseSite");
       Files.createDirectories(exportPath);
       FileUtils.copyDirectory(new File(courseSitePath.toUri()), new File(exportPath.toUri()));
      

    }
    
    
    
    
   
    
    
    
    
}
