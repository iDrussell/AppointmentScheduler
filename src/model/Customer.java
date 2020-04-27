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
public class Customer {
    private int customerId;
    private String customerName;
    private int addressId;
    private String address;
    private String phoneNumber;
    private String postalCode;
    private String city;
    private String country;
    
    public Customer(int Id, String name, int addressId, String address, String phoneNumber, String pCode, String city, String country) {
        
        this.customerId = Id;
        this.customerName = name;
        this.addressId = addressId;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.postalCode = pCode;
        this.city = city;
        this.country = country;
    }
    
    
    public int getCustomerId() {
        
        return this.customerId;
    }
    
    public String getCustomerName() {
        
        return this.customerName;
        
    }
    
    public int getAddressId() {
        
        return this.addressId;
        
    }
    
    public String getAddress() {
        
        return this.address;
        
    }
    
    public String getPhoneNumber() {
        return this.phoneNumber;
    }
    
    public String getPostalCode() {
        
        return this.postalCode;
        
    }
    
    public String getCity() {
        
        return this.city;
        
    }
    
    public String getCountry() {
        
        return this.country;
    }
}

