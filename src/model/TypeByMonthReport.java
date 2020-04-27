/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.time.Month;

/**
 *
 * @author dalton
 */
public class TypeByMonthReport {
    
    private String month;
    private String type;
    private int total;
    
    public TypeByMonthReport(int monthNumber, String type, int total) {
        this.month = Month.of(monthNumber).toString();
        this.type = type;
        this.total = total;
    }
    
    public String getMonth() {
        return this.month;
    }
    
    public String getType() {
        return this.type;
    }
    
    public int getTotal() {
        return this.total;
    }
    
}
