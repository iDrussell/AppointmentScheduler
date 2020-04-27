/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appointmentscheduler;

import static appointmentscheduler.LoginScreenController.USER;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.DBConnector;

/**
 * FXML Controller class
 *
 * @author dalton
 */
public class AddCustomerController implements Initializable {

    @FXML
    private TextField nameField;
    @FXML
    private TextField addressField;
    @FXML
    private TextField cityField;
    @FXML
    private TextField countryField;
    @FXML
    private TextField postalCodeField;
    @FXML
    private TextField phoneNumberField;
    @FXML
    private Button addButton;
    @FXML
    private Button cancelButton;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    

    @FXML
    private void addButtonHandler(ActionEvent event) {
        Connection conn = DBConnector.getConnection();
        boolean invalid = false;
        int addressId = 0, cityId = 0, countryId = 0;
        String customerName, phoneNumber, address, postalCode, city, country;
        StringBuilder errorMessage = new StringBuilder();
        
        String createdBy = USER;
        
        
        try {
            customerName = nameField.getText();
            if (customerName.equals("")) {
                errorMessage.append("Customer name is required.\n");
                invalid = true;
            }
            
            postalCode = postalCodeField.getText();
            if (postalCode.equals("")) {
                errorMessage.append("Postal code is required.\n");
                invalid = true;
            }
            
            phoneNumber = phoneNumberField.getText();
            if (phoneNumber.equals("")) {
                errorMessage.append("Phone number is required.\n");
                invalid = true;
            } else {
                Pattern phonePattern = Pattern.compile("\\d{3}-\\d{4}");
                if(!(phonePattern.matcher(phoneNumber).matches())) {
                    errorMessage.append("Phone number must match pattern: 123-4567.\n");
                    invalid = true;
                }
            }
            
            country = countryField.getText();
            city = cityField.getText();
            address = addressField.getText();
            if (country.equals("")) {
                errorMessage.append("Country is required.\n");
                invalid = true;
            }
            
            if (city.equals("")) {
                errorMessage.append("City is required.\n");
                invalid = true;
            }
            
            if (address.equals("")) {
                errorMessage.append("Address is required");
                invalid = true;
            }
            // make sure all input fields are available before executing any sql
            if (invalid) {
                System.out.println("in invalid");
                throw new InvalidInputException();
            } else {
                System.out.println("above country");
                Statement stmnt = conn.createStatement();
                ResultSet rs = stmnt.executeQuery("SELECT countryId, country FROM country WHERE country='" + country +"'");
                System.out.println("below query");
                    
                if (rs.next()) {
                    countryId = rs.getInt("countryId");
                        
                } else {
                    ZonedDateTime createDate = ZonedDateTime.now();
                    createDate = createDate.withZoneSameInstant(ZoneId.of("UTC"));
                    LocalDateTime createDateLocal = createDate.toLocalDateTime();
                        
                    String countryQuery = "INSERT INTO country VALUES (null, ?, ?, ?, ?, ?)";
                    PreparedStatement addCountry = conn.prepareStatement(countryQuery);
                        
                    addCountry.setString(1, country);
                    addCountry.setTimestamp(2, Timestamp.valueOf(createDateLocal));
                    addCountry.setString(3, createdBy);
                    addCountry.setTimestamp(4, Timestamp.valueOf(createDateLocal));
                    addCountry.setString(5, createdBy);
                        
                    addCountry.execute();
                        
                    stmnt = conn.createStatement();
                    rs = stmnt.executeQuery("SELECT countryId, country FROM country WHERE country='" + country +"'");
                    while(rs.next()) {
                        countryId = rs.getInt("country.countryId");
                    }
                        
                    
                }
                System.out.println("below country");
                stmnt = conn.createStatement();
                rs = stmnt.executeQuery("SELECT cityId, city FROM city WHERE city='" + city + "'" + " AND countryId=" + countryId);
                
                if (rs.next()) {
                    cityId = rs.getInt("cityId");
                } else {
                    ZonedDateTime createDate = ZonedDateTime.now();
                    createDate = createDate.withZoneSameInstant(ZoneId.of("UTC"));
                    LocalDateTime createDateLocal = createDate.toLocalDateTime();
                    
                    String cityQuery = "INSERT INTO city VALUES (null, ?, ?, ?, ?, ?, ?)";
                    PreparedStatement addCity = conn.prepareStatement(cityQuery);
                    
                    addCity.setString(1, city);
                    addCity.setInt(2, countryId);
                    addCity.setTimestamp(3, Timestamp.valueOf(createDateLocal));
                    addCity.setString(4, createdBy);
                    addCity.setTimestamp(5, Timestamp.valueOf(createDateLocal));
                    addCity.setTimestamp(6, Timestamp.valueOf(createDateLocal));
                    
                    addCity.execute();
                    
                    stmnt = conn.createStatement();
                    rs = stmnt.executeQuery("SELECT cityId, city FROM city WHERE city='" + city + "'" + " AND countryId=" + countryId);
                    
                    while (rs.next()) {
                        cityId = rs.getInt("cityId");
                    }
                    
                    System.out.println("below city");
                }
                
                stmnt = conn.createStatement();
                rs = stmnt.executeQuery("SELECT addressId FROM address WHERE address='" + address + "'" + " AND cityId=" + cityId  + " AND postalCode='" + postalCode + "'" + " AND phone='" + phoneNumber + "'");
                if (rs.next()) {
                    addressId = rs.getInt("addressId");
                } else {
                    ZonedDateTime createDate = ZonedDateTime.now();
                    createDate = createDate.withZoneSameInstant(ZoneId.of("UTC"));
                    LocalDateTime createDateLocal = createDate.toLocalDateTime();
                    
                    String addressQuery = "INSERT INTO address VALUES (null, ?, '', ?, ?, ?, ?, ?, ?, ?)";
                    PreparedStatement addAddress = conn.prepareStatement(addressQuery);
                    
                    addAddress.setString(1, address);
                    addAddress.setInt(2, cityId);
                    addAddress.setString(3, postalCode);
                    addAddress.setString(4, phoneNumber);
                    addAddress.setTimestamp(5, Timestamp.valueOf(createDateLocal));
                    addAddress.setString(6, createdBy);
                    addAddress.setTimestamp(7, Timestamp.valueOf(createDateLocal));
                    addAddress.setString(8, createdBy);
                    
                    addAddress.execute();
                    
                    stmnt = conn.createStatement();
                    rs = stmnt.executeQuery("SELECT addressId FROM address WHERE address='" + address + "'" + " AND cityId=" + cityId + " AND postalCode='" + postalCode + "'" + " AND phone='" + phoneNumber + "'");
                    
                    while (rs.next()) {
                        addressId = rs.getInt("addressId");
                    }
                    
                    System.out.println("below address");
                }
                
                String customerQuery = "INSERT INTO customer VALUES (null, ?, ?, 1, ?, ?, ?, ?)";
                PreparedStatement addCustomer = conn.prepareStatement(customerQuery);
                ZonedDateTime createDate = ZonedDateTime.now();
                createDate = createDate.withZoneSameInstant(ZoneId.of("UTC"));
                LocalDateTime createDateLocal = createDate.toLocalDateTime();
                
                
                addCustomer.setString(1, customerName);
                addCustomer.setInt(2, addressId);
                addCustomer.setTimestamp(3, Timestamp.valueOf(createDateLocal));
                addCustomer.setString(4, createdBy);
                addCustomer.setTimestamp(5, Timestamp.valueOf(createDateLocal));
                addCustomer.setString(6, createdBy);
                
                addCustomer.execute();
                
                // return to customer screen
                Parent main_parent = FXMLLoader.load(getClass().getResource("Customers.fxml"));
                Scene main_scene = new Scene (main_parent);
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(main_scene);
                stage.centerOnScreen();
                stage.show();
                
                
                
            }
    
        } catch(InvalidInputException e) {
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information Dialog");
            alert.setHeaderText(e.getMessage());
            alert.setContentText("Details: \n" + errorMessage.toString());

            alert.showAndWait();
            
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    @FXML
    private void cancelButtonHandler(ActionEvent event) throws IOException {
        // close stage
        Parent customerMenu = FXMLLoader.load(getClass().getResource("Customer.fxml"));
        Scene customerMenuScene = new Scene (customerMenu);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(customerMenuScene);
        stage.centerOnScreen();
        stage.show();
    }
    
}
