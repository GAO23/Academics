/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package apps.generator.data;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author xgao
 */
public class Recitation {
    private final StringProperty section = new SimpleStringProperty("?");
    private final StringProperty day = new SimpleStringProperty("?");
    private final StringProperty room = new SimpleStringProperty("?");
    private final StringProperty ta1 = new SimpleStringProperty("?");
    private final StringProperty ta2 = new SimpleStringProperty("?");
    
    public Recitation(){
        
    }
    
    public String getSection(){
        return this.section.getValue();
    }
    
    public String getDay(){
        return this.day.getValue();
    }
    
    public String getRoom(){
        return this.room.getValue();
    }    
    
    public String getTa1(){
        return this.ta1.getValue();
    }
        
    public String getTa2(){
       return  this.ta2.getValue();
    }
    
    public void setSection(String initSection){
        this.section.setValue(initSection);
    }
    
    public void setDay(String initDay){
        this.day.setValue(initDay);
    }
    
    public void setRoom(String initRoom){
       this.room.setValue(initRoom);
    }    
    
    public void setTa1(String initTa1){
        this.ta1.setValue(initTa1);
    }
        
    public void setTa2(String initTa2){
       this.ta2.setValue(initTa2);
    }
        
    public void setAllItems(String section, String day, String room, String ta1, String ta2){
        this.setSection(section);
        this.setDay(day);
        this.setRoom(room);
        this.setTa1(ta1);
        this.setTa2(ta2);
    }
    
}
