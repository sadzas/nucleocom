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

public class DoQueueStatus extends UserEvent {
    private String member;
    private String actionid;
    
    public DoQueueStatus(Object source) {
        super(source);
    }
    
    public String getMember() {
        return this.member;
    }

    public void setMember(String member) {
        this.member = member;
    }

    public String getActionId() {
        return this.actionid;
    }

    public void setActionId(String actionid) {
        this.actionid = actionid;
    }
}