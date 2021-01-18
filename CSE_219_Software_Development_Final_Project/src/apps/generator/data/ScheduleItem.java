/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package apps.generator.data;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author xgao
 */
public class ScheduleItem  {
    private final StringProperty type = new SimpleStringProperty("?");
    private final StringProperty date = new SimpleStringProperty("?");
    private final StringProperty title = new SimpleStringProperty("?");
    private final StringProperty topic = new SimpleStringProperty("?");
    private final StringProperty link = new SimpleStringProperty("?");

 
    
    public  ScheduleItem(String type, String date, String title, String topic, String link){
        this.type.setValue(type);
        this.date.setValue(date);
        this.title.setValue(title);
        this.topic.setValue(topic);
        this.link.setValue(link);
    }
    
    public String  getLink(){
        return this.link.getValue();
    }
    
    public void setLink(String initLink){
        this.link.setValue(initLink);
    }

    public String getType() {
        return type.getValue();
    }

    @Override
    public ScheduleItem clone()  {
        return new ScheduleItem(this.getType(), this.getUnformattedDate(), this.getTitle(), this.getTopic(), this.getLink());
    }

    public String getDate() {
        return this.getFormattedDate();
    }

    public String getTitle() {
        return title.getValue();
    }

    public String getTopic() {
        return topic.getValue();
    }
    
    public void setType(String initType) {
        type.setValue(initType);
    }

    public void setDate(String initDate) {
         date.setValue(initDate);
    }

    public void setTitle(String initTitle) {
        title.setValue(initTitle);
    }

    public void setTopic(String initTopic) {
         topic.setValue(initTopic);
    }
    
    public String getUnformattedDate(){
        return this.date.getValue();
    }
    
    public void copyFrom(ScheduleItem item){
        this.setDate(item.getUnformattedDate());
        this.setLink(item.getLink());
        this.setTitle(item.getTitle());
        this.setTopic(item.getTopic());
        this.setType(item.getType());
    }
    
    public boolean sameAs(ScheduleItem item){
        if(!item.getUnformattedDate().equals(this.getUnformattedDate())){
            return false;
        }
        if(!item.getLink().equals(this.getLink())){
            return false;
        }
        if(!item.getTitle().endsWith(this.getTitle())){
            return false;
        }
        if(!item.getTopic().endsWith(this.getTopic())){
            return false;
        }
        if(!item.getType().equals(this.getType())){
            return false;
        }
        return true;
    }
    
    
     private String getFormattedDate(){
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-DD"); 
        Date dateObject = null;
        try {
             dateObject = (Date)formatter.parse(this.getUnformattedDate());
        } catch (ParseException ex) {
           ex.printStackTrace();
        }
        SimpleDateFormat newFormat = new SimpleDateFormat("MM/dd/yyyy");
        String result = newFormat.format(dateObject);
        return result;
    }
    
    
}
