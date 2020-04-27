/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author dalton
 */
public class ConsultantSchedule {
    private String consultantName;
    private String customerName;
    private ZonedDateTime start;
    private ZonedDateTime end;
    
    public ConsultantSchedule(String consultantName, String customerName, ZonedDateTime start, ZonedDateTime end) {
        this.consultantName = consultantName;
        this.customerName = customerName;
        this.start = start;
        this.end = end;
    }
    
    public String getConsultantName() {
        return this.consultantName;
        
    }
    
    public String getCustomerName() {
        return this.customerName;
    }
    
    public ZonedDateTime getStart() {
        return this.start;
    }
    
    public ZonedDateTime getEnd() {
        return this.end;
    }
    
    public String getStringStart() {
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd h:mm a");
        String stringStart = this.start.format(df);
        return stringStart;
    }
    
    public String getStringEnd() {
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd h:mm a");
        String stringEnd = this.end.format(df);
        return stringEnd;
    }
    
}
