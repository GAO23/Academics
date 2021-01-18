/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package apps.generator.data;

import apps.generator.GeneratorApp;
import apps.generator.GeneratorProperty;
import  libs.DesktopJavaFramework.components.AppDataComponent;
import  libs.DesktopJavaFramework.modules.AppGUIModule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.collections.ObservableList;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableView;

import java.util.Collections;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.scene.control.DatePicker;

/**
 *
 * @author xgao
 */
public class GeneratorData implements AppDataComponent{
    // WE'LL NEED ACCESS TO THE APP TO NOTIFY THE GUI WHEN DATA CHANGES
    GeneratorApp app;
    
    // THESE ARE ALL THE TEACHING ASSISTANTS
    HashMap<TAType, ArrayList<TeachingAssistantPrototype>> allTAs;

    // NOTE THAT THIS DATA STRUCTURE WILL DIRECTLY STORE THE
    // DATA IN THE ROWS OF THE TABLE VIEW
    private final ObservableList<TeachingAssistantPrototype> teachingAssistants;
    private  ObservableList<TimeSlot> officeHours;    
    private final ArrayList<TimeSlot> restoredOfficerHours;
    private final ObservableList<Lecture> lectures; 
    private final ObservableList<Recitation> recitations; 
    private final ObservableList<Lab> labs; 
    private final ObservableList<ScheduleItem> schedules; 
    private final ArrayList<ScheduleItem> restoredScheduleItems;

    // THESE ARE THE TIME BOUNDS FOR THE OFFICE HOURS GRID. NOTE
    // THAT THESE VALUES CAN BE DIFFERENT FOR DIFFERENT FILES, BUT
    // THAT OUR APPLICATION USES THE DEFAULT TIME VALUES AND PROVIDES
    // NO MEANS FOR CHANGING THESE VALUES
    int startHour;
    int endHour;
    
    // DEFAULT VALUES FOR START AND END HOURS IN MILITARY HOURS
    public static final int MIN_START_HOUR = 9;
    public static final int MAX_END_HOUR = 20;

    /**
     * This constructor will setup the required data structures for
     * use, but will have to wait on the office hours grid, since
     * it receives the StringProperty objects from the Workspace.
     * 
     * @param initApp The application this data manager belongs to. 
     */
    public GeneratorData (GeneratorApp initApp) {
        // KEEP THIS FOR LATER
        app = initApp;
        AppGUIModule gui = app.getGUIModule();

        // SETUP THE DATA STRUCTURES
        allTAs = new HashMap();
        allTAs.put(TAType.Graduate, new ArrayList());
        allTAs.put(TAType.Undergraduate, new ArrayList());
        
        // GET THE LIST OF TAs FOR THE LEFT TABLE
        TableView<TeachingAssistantPrototype> taTableView = (TableView)gui.getGUINode(GeneratorProperty.OH_TAS_TABLE_VIEW);
        teachingAssistants = taTableView.getItems();
        
        this.lectures =  ((TableView)gui.getGUINode(GeneratorProperty.GENERATOR_LECTURE_TABLE_VIEW)).getItems();
        this.recitations =  ((TableView)gui.getGUINode(GeneratorProperty.GENERATOR_RECITATION_TABLE_VIEW)).getItems();
        this.labs =  ((TableView)gui.getGUINode(GeneratorProperty.GENERATOR_LAB_TABLE_VIEW)).getItems();
        this.schedules = ((TableView)gui.getGUINode(GeneratorProperty.GENERATOR_SCHEDULE_ITEM_TABLE)).getItems();
        

        // THESE ARE THE DEFAULT OFFICE HOURS
        startHour = MIN_START_HOUR;
        endHour = MAX_END_HOUR;
        
        resetOfficeHours();
        this.restoredOfficerHours = new ArrayList<TimeSlot>(this.officeHours);
        this.restoredScheduleItems = new ArrayList<ScheduleItem>(this.schedules);
    }
    
    // ACCESSOR METHODS

    public int getStartHour() {
        return startHour;
    }

    public int getEndHour() {
        return endHour;
    }
    
    // PRIVATE HELPER METHODS
    
    private void sortTAs() {
        Collections.sort(teachingAssistants);
    }
    
    private void resetOfficeHours() {
        //THIS WILL STORE OUR OFFICE HOURS
        AppGUIModule gui = app.getGUIModule();
        TableView<TimeSlot> officeHoursTableView = (TableView)gui.getGUINode(GeneratorProperty.OH_OFFICE_HOURS_TABLE_VIEW);
        officeHours = officeHoursTableView.getItems(); 
        officeHours.clear();
        for (int i = startHour; i <= endHour; i++) {
            TimeSlot timeSlot = new TimeSlot(   this.getTimeString(i, true),
                                                this.getTimeString(i, false));
            officeHours.add(timeSlot);
            
            TimeSlot halfTimeSlot = new TimeSlot(   this.getTimeString(i, false),
                                                    this.getTimeString(i+1, true));
            officeHours.add(halfTimeSlot);
        }
    }
    
    private String getTimeString(int militaryHour, boolean onHour) {
        String minutesText = "00";
        if (!onHour) {
            minutesText = "30";
        }

        // FIRST THE START AND END CELLS
        int hour = militaryHour;
        if (hour > 12) {
            hour -= 12;
        }
        String cellText = "" + hour + ":" + minutesText;
        if (militaryHour < 12) {
            cellText += "am";
        } else {
            cellText += "pm";
        }
        return cellText;
    }
    
    // METHODS TO OVERRIDE
        
    /**
     * Called each time new work is created or loaded, it resets all data
     * and data structures such that they can be used for new values.
     */
    @Override
    public void reset() {
        startHour = MIN_START_HOUR;
        endHour = MAX_END_HOUR;
        teachingAssistants.clear();
        
        for (TimeSlot timeSlot : officeHours) {
            timeSlot.reset();
        }
    }
    
    // SERVICE METHODS
    
    public void initHours(String startHourText, String endHourText) {
        int initStartHour = Integer.parseInt(startHourText);
        int initEndHour = Integer.parseInt(endHourText);
        if (initStartHour <= initEndHour) {
            // THESE ARE VALID HOURS SO KEEP THEM
            // NOTE THAT THESE VALUES MUST BE PRE-VERIFIED
            startHour = initStartHour;
            endHour = initEndHour;
        }
        resetOfficeHours();
    }
    
    public void addTA(TeachingAssistantPrototype ta) {
        if (!hasTA(ta)) {
            TAType taType = TAType.valueOf(ta.getType());
            ArrayList<TeachingAssistantPrototype> tas = allTAs.get(taType);
            tas.add(ta);
            this.updateTAs();
        }
    }
    
    public void updateToCombo(String start, String end){
        TableView<TimeSlot> officeHourTable = (TableView<TimeSlot>) app.getGUIModule().getGUINode(GeneratorProperty.OH_OFFICE_HOURS_TABLE_VIEW);
        ArrayList<TimeSlot> toRemoved = new ArrayList<TimeSlot>();
        officeHours.clear();
        officeHours.addAll(this.restoredOfficerHours);
        for(TimeSlot item : officeHours){
            item.restorAllTASlotCount();
        }
        for(TimeSlot time : officeHours){
            if(earlierThan(time.getStartTime(), start) || earlierThan(end, time.getEndTime())){
                toRemoved.add(time);
                time.decrementAllTASlotCount();
            } 
        }
        
        for(TimeSlot item : toRemoved){
            officeHourTable.getItems().remove(item);
        }
        this.updateTAs();
       
    }
    
    public String getStartingMonth(){
       AppGUIModule gui = app.getGUIModule();
       DatePicker box  = (DatePicker) gui.getGUINode(GeneratorProperty.GENERATOR_SCHEDULE_STARTING_COMBO);
       String result = box.getValue().toString();
       result = result.substring(5);
       result = result.substring(0, result.length() - 3);
       char firstLetter = result.charAt(0);
       if(firstLetter == '0'){
           result = result.substring(1);
       }
       return result;
    }
    
    public String getStartingDay(){
       AppGUIModule gui = app.getGUIModule();
       DatePicker box  = (DatePicker) gui.getGUINode(GeneratorProperty.GENERATOR_SCHEDULE_STARTING_COMBO);
       String result = box.getValue().toString();
       result = result.substring(8);
       char firstLetter = result.charAt(0);
       if(firstLetter == '0'){
           result = result.substring(1);
       }
       return result;
    }
    
    public String getEndingDay(){
       AppGUIModule gui = app.getGUIModule();
       DatePicker box  = (DatePicker) gui.getGUINode(GeneratorProperty.GENERATOR_SCHEDULE_ENDING_COMBO);
       String result = box.getValue().toString();
       result = result.substring(8);
       char firstLetter = result.charAt(0);
       if(firstLetter == '0'){
           result = result.substring(1);
       }
       return result;
    }
    
    public String getEndingMonth(){
       AppGUIModule gui = app.getGUIModule();
       DatePicker box  = (DatePicker) gui.getGUINode(GeneratorProperty.GENERATOR_SCHEDULE_ENDING_COMBO);
       String result = box.getValue().toString();
       result = result.substring(5);
       result = result.substring(0, result.length() - 3);
       char firstLetter = result.charAt(0);
       if(firstLetter == '0'){
           result = result.substring(1);
       }
       return result;
    }
    
    public void addSchedule(ScheduleItem item){
        if(!this.restoredScheduleItems.contains(item)) this.restoredScheduleItems.add(item);
    }
    
    public void removeSchedule(ScheduleItem item){
        this.restoredScheduleItems.remove(item);
    }
    
       public boolean earlierThan(String comparing, String compareTo){
        if(comparing.indexOf("am")!= -1 && compareTo.indexOf("pm")!= -1){
            return true;
        }
        
        else if(comparing.indexOf("pm")!= -1 && compareTo.indexOf("pm")!= -1 &&  Double.compare(this.helper(comparing),12.0)  >= 0 && Double.compare(this.helper(compareTo),12.0) < 0){
            return true;
        }
        
         else if(comparing.indexOf("pm")!= -1 && compareTo.indexOf("pm")!= -1 &&  Double.compare(this.helper(comparing),12.0)  < 0 && Double.compare(this.helper(compareTo),12.0) >= 0){
            return false;
        }
        
        if(comparing.indexOf("pm")!= -1 && compareTo.indexOf("am")!= -1){
            return false;
        }
         
        else if(Double.compare(this.helper(comparing),this.helper(compareTo))  < 0 ){
            return true;
        }
        else {return false;}
    }
       
     
    
    public double helper(String time){
        time = time.replace("am", ""); 
        time = time.replace("pm", "");
        time = time.replace(":",".");
        double result = Double.parseDouble(time);
        return result;
    }

    public void addTA(TeachingAssistantPrototype ta, HashMap<TimeSlot, ArrayList<TimeSlot.DayOfWeek>> officeHours) {
        addTA(ta);
        for (TimeSlot timeSlot : officeHours.keySet()) {
            ArrayList<TimeSlot.DayOfWeek> days = officeHours.get(timeSlot);
            for (TimeSlot.DayOfWeek dow : days) {
                timeSlot.addTA(dow, ta);
            }
        }
    }
    
    public void removeTA(TeachingAssistantPrototype ta) {
        // REMOVE THE TA FROM THE LIST OF TAs
        TAType taType = TAType.valueOf(ta.getType());
        allTAs.get(taType).remove(ta);
        
        // REMOVE THE TA FROM ALL OF THEIR OFFICE HOURS
        for (TimeSlot timeSlot : officeHours) {
            timeSlot.removeTA(ta);
        }
        
        // AND REFRESH THE TABLES
        this.updateTAs();
    }

    public void removeTA(TeachingAssistantPrototype ta, HashMap<TimeSlot, ArrayList<TimeSlot.DayOfWeek>> officeHours) {
        removeTA(ta);
        for (TimeSlot timeSlot : officeHours.keySet()) {
            ArrayList<TimeSlot.DayOfWeek> days = officeHours.get(timeSlot);
            for (TimeSlot.DayOfWeek dow : days) {
                timeSlot.removeTA(dow, ta);
            }
        }    
    }
    
    public TimeSlot.DayOfWeek getColumnDayOfWeek(int columnNumber) {
        return TimeSlot.DayOfWeek.values()[columnNumber-2];
    }

    public TeachingAssistantPrototype getTAWithName(String name) {
        Iterator<TeachingAssistantPrototype> taIterator = teachingAssistants.iterator();
        while (taIterator.hasNext()) {
            TeachingAssistantPrototype ta = taIterator.next();
            if (ta.getName().equals(name))
                return ta;
        }
        return null;
    }

    public TeachingAssistantPrototype getTAWithEmail(String email) {
        Iterator<TeachingAssistantPrototype> taIterator = teachingAssistants.iterator();
        while (taIterator.hasNext()) {
            TeachingAssistantPrototype ta = taIterator.next();
            if (ta.getEmail().equals(email))
                return ta;
        }
        return null;
    }

    public TimeSlot getTimeSlot(String startTime) {
        Iterator<TimeSlot> timeSlotsIterator = officeHours.iterator();
        while (timeSlotsIterator.hasNext()) {
            TimeSlot timeSlot = timeSlotsIterator.next();
            String timeSlotStartTime = timeSlot.getStartTime().replace(":", "_");
            if (timeSlotStartTime.equals(startTime))
                return timeSlot;
        }
        return null;
    }

    public TAType getSelectedType() {
        RadioButton allRadio = (RadioButton)app.getGUIModule().getGUINode(GeneratorProperty.OH_ALL_RADIO_BUTTON);
        if (allRadio.isSelected())
            return TAType.All;
        RadioButton gradRadio = (RadioButton)app.getGUIModule().getGUINode(GeneratorProperty.OH_GRAD_RADIO_BUTTON);
        if (gradRadio.isSelected())
            return TAType.Graduate;
        else
            return TAType.Undergraduate;
    }

    public TeachingAssistantPrototype getSelectedTA() {
        AppGUIModule gui = app.getGUIModule();
        TableView<TeachingAssistantPrototype> tasTable = (TableView)gui.getGUINode(GeneratorProperty.OH_TAS_TABLE_VIEW);
        return tasTable.getSelectionModel().getSelectedItem();
    }
    
    public HashMap<TimeSlot, ArrayList<TimeSlot.DayOfWeek>> getTATimeSlots(TeachingAssistantPrototype ta) {
        HashMap<TimeSlot, ArrayList<TimeSlot.DayOfWeek>> timeSlots = new HashMap();
        for (TimeSlot timeSlot : officeHours) {
            if (timeSlot.hasTA(ta)) {
                ArrayList<TimeSlot.DayOfWeek> daysForTA = timeSlot.getDaysForTA(ta);
                timeSlots.put(timeSlot, daysForTA);
            }
        }
        return timeSlots;
    }
    
    private boolean hasTA(TeachingAssistantPrototype testTA) {
        return allTAs.get(TAType.Graduate).contains(testTA)
                ||
                allTAs.get(TAType.Undergraduate).contains(testTA);
    }
    
    public boolean isTASelected() {
        AppGUIModule gui = app.getGUIModule();
        TableView tasTable = (TableView)gui.getGUINode(GeneratorProperty.OH_TAS_TABLE_VIEW);
        return tasTable.getSelectionModel().getSelectedItem() != null;
    }

    public boolean isLegalNewTA(String name, String email) {
        if ((name.trim().length() > 0)
                && (email.trim().length() > 0)) {
            // MAKE SURE NO TA ALREADY HAS THE SAME NAME
            TAType type = this.getSelectedType();
            TeachingAssistantPrototype testTA = new TeachingAssistantPrototype(name, email, type);
            if (this.teachingAssistants.contains(testTA))
                return false;
            if (this.isLegalNewEmail(email)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean isLegalNewName(String testName) {
        if (testName.trim().length() > 0) {
            for (TeachingAssistantPrototype testTA : this.teachingAssistants) {
                if (testTA.getName().equals(testName))
                    return false;
            }
            return true;
        }
        return false;
    }
    
    public boolean isLegalNewEmail(String email) {
        Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile(
                "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX .matcher(email);
        if (matcher.find()) {
            for (TeachingAssistantPrototype ta : this.teachingAssistants) {
                if (ta.getEmail().equals(email.trim()))
                    return false;
            }
            return true;
        }
        else return false;
    }
    
    public boolean isDayOfWeekColumn(int columnNumber) {
        return columnNumber >= 2;
    }
    
    public boolean isTATypeSelected() {
        AppGUIModule gui = app.getGUIModule();
        RadioButton allRadioButton = (RadioButton)gui.getGUINode(GeneratorProperty.OH_ALL_RADIO_BUTTON);
        return !allRadioButton.isSelected();
    }
    
    public boolean isValidTAEdit(TeachingAssistantPrototype taToEdit, String name, String email) {
        if (!taToEdit.getName().equals(name)) {
            if (!this.isLegalNewName(name))
                return false;
        }
        if (!taToEdit.getEmail().equals(email)) {
            if (!this.isLegalNewEmail(email))
                return false;
        }
        return true;
    }

    public boolean isValidNameEdit(TeachingAssistantPrototype taToEdit, String name) {
        if (!taToEdit.getName().equals(name)) {
            if (!this.isLegalNewName(name))
                return false;
        }
        return true;
    }

    public boolean isValidEmailEdit(TeachingAssistantPrototype taToEdit, String email) {
        if (!taToEdit.getEmail().equals(email)) {
            if (!this.isLegalNewEmail(email))
                return false;
        }
        return true;
    }    

    public void updateTAs() {
        TAType type = getSelectedType();
        selectTAs(type);
    }
    
    public ObservableList<ScheduleItem> getSchedules(){
        return this.schedules;
    }
    
    public ArrayList<ScheduleItem> getBackUpSchedules(){
        return this.restoredScheduleItems;
    }
    
    public boolean isStartEndValid(){
        AppGUIModule gui = app.getGUIModule();
        DatePicker start = (DatePicker) gui.getGUINode(GeneratorProperty.GENERATOR_SCHEDULE_STARTING_COMBO);
        DatePicker end = (DatePicker) gui.getGUINode(GeneratorProperty.GENERATOR_SCHEDULE_ENDING_COMBO);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date startDate = null;
        Date endDate = null;
        try {
            startDate = sdf.parse(start.getValue().toString());
            endDate = sdf.parse(end.getValue().toString());
            sdf.format(startDate);
            sdf.format(endDate);
        } catch (ParseException ex) {
           ex.printStackTrace();
        }
        
        return this.isDateEarlierThanOrEqualTo(startDate, endDate);
       
    }
    
    public boolean chechIfTimeIsInRange(String testingDate){
        AppGUIModule gui = app.getGUIModule();
        DatePicker start = (DatePicker) gui.getGUINode(GeneratorProperty.GENERATOR_SCHEDULE_STARTING_COMBO);
        DatePicker end = (DatePicker) gui.getGUINode(GeneratorProperty.GENERATOR_SCHEDULE_ENDING_COMBO);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date startDate = null;
        Date endDate = null;
        Date test = null;
        try {
            startDate = sdf.parse(start.getValue().toString());
            endDate = sdf.parse(end.getValue().toString());
            test = sdf.parse(testingDate);
            sdf.format(startDate);
            sdf.format(endDate);
             sdf.format(test);
        } catch (ParseException ex) {
           ex.printStackTrace();
        }
        if(!this.isDateEarlierThanOrEqualTo(test, endDate)){
            return false;
        }
        
        if(!this.isDateEarlierThanOrEqualTo(startDate, test)){
            return false;
        }
        
        return true;
    }
    
    public void updateScheduleStartEndSelect(){
        ArrayList<ScheduleItem> toRemoved = new ArrayList<ScheduleItem>();
        schedules.clear();
        schedules.addAll(this.restoredScheduleItems);
        for(ScheduleItem item : schedules){
            if(!this.chechIfTimeIsInRange(item.getUnformattedDate())){
                toRemoved.add(item);
            }
        }
        
        this.schedules.removeAll(toRemoved);
        this.refreshesScheduleTable();
    }
    
    public void addScheduleItemToIndex(int index, ScheduleItem item){
        if(this.restoredScheduleItems.size()>index){
            this.restoredScheduleItems.add(index, item);
        }else{
            this.restoredScheduleItems.add(item);
        }
    }
    

    
    private boolean isDateEarlierThanOrEqualTo(Date date1, Date date2){
          if (date1.compareTo(date2) > 0) {
            return false;
        } else if (date1.compareTo(date2) < 0) {
            return true;
        } else if (date1.compareTo(date2) == 0) {
            return true;
        } else {
            System.out.println("How to get here?");
            return false;
        }
    }
    
   
    
    public void selectTAs(TAType type) {
        teachingAssistants.clear();
        Iterator<TeachingAssistantPrototype> tasIt = this.teachingAssistantsIterator();
        while (tasIt.hasNext()) {
            TeachingAssistantPrototype ta = tasIt.next();
            if (type.equals(TAType.All)) {
                teachingAssistants.add(ta);
            }
            else if (ta.getType().equals(type.toString())) {
                teachingAssistants.add(ta);
            }
        }
        
        // SORT THEM BY NAME
        sortTAs();

        // CLEAR ALL THE OFFICE HOURS
        Iterator<TimeSlot> officeHoursIt = officeHours.iterator();
        while (officeHoursIt.hasNext()) {
            TimeSlot timeSlot = officeHoursIt.next();
            timeSlot.filter(type);
        }
        
        app.getFoolproofModule().updateAll();
    }
    
    public Iterator<TimeSlot> officeHoursIterator() {
        return officeHours.iterator();
    }
    
    public Iterator<TimeSlot> backUpOfficeHoursIterator(){
        return this.restoredOfficerHours.iterator();
    }

    public Iterator<TeachingAssistantPrototype> teachingAssistantsIterator() {
        return new AllTAsIterator();
    }
    
    private class AllTAsIterator implements Iterator {
        Iterator gradIt = allTAs.get(TAType.Graduate).iterator();
        Iterator undergradIt = allTAs.get(TAType.Undergraduate).iterator();

        public AllTAsIterator() {}
        
        @Override
        public boolean hasNext() {
            if (gradIt.hasNext() || undergradIt.hasNext())
                return true;
            else
                return false;                
        }

        @Override
        public Object next() {
            if (gradIt.hasNext())
                return gradIt.next();
            else if (undergradIt.hasNext())
                return undergradIt.next();
            else
                return null;
        }
    }
    
    public void backUpOfficeHours(){
        this.restoredOfficerHours.clear();
        this.restoredOfficerHours.addAll(officeHours);
    }
    
    public void backUpScheduleItems(){
        this.restoredScheduleItems.clear();
        this.restoredScheduleItems.addAll(this.schedules);
    }
    
    public void printAllTimeSlots(){
        for(TimeSlot time : officeHours){
            time.printTAs();
        }
        System.out.println("--------------------------------------------------------------------------------");
         for(TimeSlot time : this.restoredOfficerHours){
            time.printTAs();
        }
    }
    
    public void refreshesTimeSlotTable(){
        AppGUIModule gui = app.getGUIModule();
        TableView table = (TableView) gui.getGUINode(GeneratorProperty.OH_OFFICE_HOURS_TABLE_VIEW);
        this.Refresh(table, this.officeHours);
    }
    
    public void refreshesScheduleTable(){
        AppGUIModule gui = app.getGUIModule();
        TableView table = (TableView) gui.getGUINode(GeneratorProperty.GENERATOR_SCHEDULE_ITEM_TABLE);
        this.Refresh(table, this.schedules);
    }
    
    public void refreshesAllMeetingTables(){
        this.refreshLectureTable() ;
        this.refreshRecitationTable();
        this.refreshLabTable();
    }
    
    public void refreshLectureTable(){
        AppGUIModule gui = app.getGUIModule();
        TableView table = (TableView) gui.getGUINode(GeneratorProperty.GENERATOR_LECTURE_TABLE_VIEW);
        this.Refresh(table, this.lectures);
    }
    
    public void refreshRecitationTable(){
        AppGUIModule gui = app.getGUIModule();
        TableView table = (TableView) gui.getGUINode(GeneratorProperty.GENERATOR_RECITATION_TABLE_VIEW);
        this.Refresh(table, this.recitations);
    }
    
    public void refreshLabTable(){
        AppGUIModule gui = app.getGUIModule();
        TableView table = (TableView) gui.getGUINode(GeneratorProperty.GENERATOR_LAB_TABLE_VIEW);
        this.Refresh(table, this.labs);
    }
    
    public ObservableList<Lecture> getLectures() {
        return lectures;
    }

    public ObservableList<Recitation> getRecitations() {
        return recitations;
    }

    public ObservableList<Lab> getLabs() {
        return labs;
    }
    
    public void addLecture(Lecture lecture){
        this.lectures.add(lecture);
    }
    
    public void addRecitation(Recitation recitation){
        this.recitations.add(recitation);
    }
        
    public void addLab(Lab lab){
        this.labs.add(lab);
    }
    
    public void removeLecture(Lecture lecture){
        this.lectures.remove(lecture);
    }
    
    public void removeRecitation(Recitation recitation){
        this.recitations.remove(recitation);
    }
        
    public void removeLab(Lab lab){
        this.labs.remove(lab);
    }
    
    
     // Java Bug, table does not refresh when value changes
    private <T> void Refresh(final TableView<T> table, final List<T> tableList) { 
        // this fixes the stuffs
        table.setItems(null); 
        table.layout(); 
        table.setItems(FXCollections.observableList(tableList)); 
    
    }
    
}
