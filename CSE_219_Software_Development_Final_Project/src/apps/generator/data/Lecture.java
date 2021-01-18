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
public class Lecture {
    private final StringProperty section = new SimpleStringProperty("?");
    private final StringProperty day = new SimpleStringProperty("?");
    private final StringProperty time = new SimpleStringProperty("?");
    private final StringProperty room = new SimpleStringProperty("?");
    
    public Lecture(){
       
    }

    public String getSection() {
        return section.get();
    }

    public String getDay() {
        return day.get();
    }

    public String getTime() {
        return time.get();
    }

    public String getRoom() {
        return room.get();
    }
    
    public void setSection(String initSection) {
        this.section.set(initSection);
    }

    public void setDay(String initDay) {
        this.day.setValue(initDay);
    }

    public void setTime(String initTime) {
        this.time.setValue(initTime);
    }

    public void setRoom(String initRoom) {
        this.room.set(initRoom);
    }
    
    public void setAllItems(String section, String day, String time, String room){
        this.setSection(section);
        this.setDay(day);
        this.setTime(time);
        this.setRoom(room);
    }
    
    
    
    
}
