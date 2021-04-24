/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package manager;

/**
 *
 * @author mgiannini
 */

import org.asteriskjava.manager.event.UserEvent;

public class CallBackRequestEvent extends UserEvent{
    
    private String callerextension;
    private String extension;
    
    public CallBackRequestEvent(Object source){
        super(source);
    }

    public String getCallerextension() {
        return callerextension;
    }

    public void setCallerextension(String callerextension) {
        this.callerextension = callerextension;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }
}