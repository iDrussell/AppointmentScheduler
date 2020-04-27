/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 *
 * @author dalton
 */
public class Appointment {
    private int appointmentId;
    private int customerId;
    private String customerName;
    private int userId;
    private String userName;
    private String type;
    private ZonedDateTime start;
    private ZonedDateTime end;
    
    public Appointment(int appointmentId, int customerId, String customerName, int userId, String userName, String type, ZonedDateTime start, ZonedDateTime end) {
        this.appointmentId = appointmentId;
        this.customerId = customerId;
        this.customerName = customerName;
        this.userId = userId;
        this.userName = userName;
        this.type = type;
        this.start = start;
        this.end = end;
    }
    
    public int getAppointmentId() {
        return this.appointmentId;
    }
    
    public int getCustomerId() {
        return this.customerId;
    }
    
    public String getCustomerName() {
        return this.customerName;
    }
    
    public int getUserId() {
        return this.userId;
    }
    
    public String getUserName() {
        return this.userName;
    }
    
    public String getType() {
        return this.type;
    }
    
    public ZonedDateTime getStart() {
        return this.start;
    }
    
    public ZonedDateTime getEnd() {
        return this.end;
    }
    
    public String getStartString() {
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd h:mm a");
        String stringStart = this.start.format(df);
        return stringStart;
    }
    
    public String getEndString() {
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd h:mm a");
        String stringEnd = this.end.format(df);
        return stringEnd;
    }
}
