/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

/**
 *
 * @author dalton
 */
public class ApptsByCustomer {
    private String customerName;
    private int total;
    
    public ApptsByCustomer(String customerName, int total) {
        this.customerName = customerName;
        this.total = total;
    }
    
    public String getCustomerName() {
        return this.customerName;
    }
    
    public int getTotal() {
        return this.total;
    }
    
}
