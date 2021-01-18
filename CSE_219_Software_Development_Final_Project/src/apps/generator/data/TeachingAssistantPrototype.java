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
public class TeachingAssistantPrototype <E extends Comparable<E>> implements Comparable<E>{
    
     // THE TABLE WILL STORE TA NAMES AND EMAILS
    private final StringProperty name;
    private final StringProperty email;
    private final StringProperty slots;
    private final StringProperty type;
    private int backUpSlot = 0;

    /**
     * Constructor initializes both the TA name and email.
     */
    public TeachingAssistantPrototype(String initName, String initEmail, TAType initType) {
        name = new SimpleStringProperty(initName);
        email = new SimpleStringProperty(initEmail);
        slots = new SimpleStringProperty("" + 0);
        type = new SimpleStringProperty(initType.toString());
    }

    // ACCESSORS AND MUTATORS FOR THE PROPERTIES

    public String getName() {
        return name.get();
    }

    public void setName(String initName) {
        name.set(initName);
    }

    public String getEmail() {
        return email.get();
    }

    public void setEmail(String initEmail) {
        email.set(initEmail);
    }
    
    public String getSlots() {
        return slots.get();
    }
    
    public void setSlots(String initSlots) {
        slots.setValue(initSlots);
    }
    
    public StringProperty slotsProperty() {
        return slots;
    }
    
    public String getType() {
        return type.get();
    }
    
    public void setType(String initType) {
        type.setValue(initType);
    }
    
    public StringProperty typeProperty() {
        return type;
    }

    @Override
    public int compareTo(E otherTA) {
        return getName().compareTo(((TeachingAssistantPrototype)otherTA).getName());
    }
    
    @Override
    public String toString() {
        return name.getValue();
    }
    
    @Override
    public boolean equals(Object testTA) {
        return name.getValue().toLowerCase().equals(((TeachingAssistantPrototype)testTA).name.getValue().toLowerCase().trim())
                || email.getValue().toLowerCase().equals(((TeachingAssistantPrototype)testTA).email.getValue().toLowerCase().trim());
    }

    public void changeTimeSlotCount(int i) {
        int count = Integer.parseInt(this.slots.get());
        count += i;
        this.setSlots("" + count);
        this.backUpSlot = count;
    }
    
    public void decrementSlotDueToCombo() {
        int count = Integer.parseInt(this.slots.get());
        count -= 1;
        this.setSlots("" + count);
    }
    
    
    
    public void restoreSlotCount(){
        this.setSlots("" + this.backUpSlot);
    }
    
    public void changeType(TAType initType) {
        type.set(initType.toString());
    }
    
    public E clone() {
        TeachingAssistantPrototype ta = new TeachingAssistantPrototype(
                this.getName(), this.getEmail(), TAType.valueOf(this.getType()));
        ta.setSlots(this.getSlots());
        return (E)ta;
    }
    
}
