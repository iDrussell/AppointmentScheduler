/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appointmentscheduler;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.Appointment;
import model.Customer;
import model.DBConnector;

/**
 * FXML Controller class
 *
 * @author dalton
 */
public class CustomersController implements Initializable {

    @FXML
    private Label greeting;
    @FXML
    private TableColumn<Customer, Integer> customerTableID;
    @FXML
    private TableColumn<Customer, String> customerTableName;
    @FXML
    private TableColumn<Customer, String> customerTableAddress;
    @FXML
    private TableColumn<Customer, String> customerTablePostal;
    @FXML
    private TableColumn<Customer, String> customerTableCity;
    @FXML
    private TableColumn<Customer, String> customerTableCountry;
    @FXML
    private TableView<Customer> customersTable;
    
    private ObservableList<Customer> customers = FXCollections.observableArrayList();

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        //initialize table columns
        customerTableID.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        customerTableName.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        customerTableAddress.setCellValueFactory(new PropertyValueFactory<>("address"));
        customerTablePostal.setCellValueFactory(new PropertyValueFactory<>("postalCode"));
        customerTableCity.setCellValueFactory(new PropertyValueFactory<>("city"));
        customerTableCountry.setCellValueFactory(new PropertyValueFactory<>("country"));
        
        
        parseCustomers();
        customersTable.setItems(customers);
        
    }    

    @FXML
    private void addCustomerHandler(ActionEvent event) throws IOException {
        
        FXMLLoader addCustomer = new FXMLLoader(getClass().getResource("AddCustomer.fxml"));
        Parent parent = addCustomer.load();
        Scene scene = new Scene(parent);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }

    @FXML
    private void updateCustomerHandler(ActionEvent event) throws IOException {
        
        Parent edit;
        
        Customer customer = customersTable.getSelectionModel().getSelectedItem();
        if(customer != null) {
            FXMLLoader editCustomerLoader = new FXMLLoader(getClass().getResource("UpdateCustomer.fxml"));
            edit = editCustomerLoader.load();
            Scene editCustomerScene = new Scene(edit);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(editCustomerScene);
            stage.show();
            
            UpdateCustomerController controller = editCustomerLoader.getController();
            controller.setCustomer(customer);
        } else {
            // If there was no selection
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information Dialog");
            alert.setHeaderText("Error");
            alert.setContentText("Please make a selection.");

            alert.showAndWait();
            
        }
    }

    @FXML
    private void deleteCustomerHandler(ActionEvent event) throws SQLException {
        Customer selectedCustomer = customersTable.getSelectionModel().getSelectedItem();
        
        if (selectedCustomer != null) {
            int customerId = selectedCustomer.getCustomerId();
            
            Connection conn = DBConnector.getConnection();
            PreparedStatement deleteAppts = conn.prepareStatement("DELETE FROM appointment WHERE customerId=?");
            PreparedStatement stmnt = conn.prepareStatement("DELETE FROM customer WHERE customerId=?");
            
            deleteAppts.setInt(1, customerId);
            
            stmnt.setInt(1, customerId);
            
            // confirm delete
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Delete...");
            alert.setHeaderText("Deleting...");
            alert.setContentText("Deleting " + selectedCustomer.getCustomerName() + " from the customer records will also delete all associated appointments. Do you wish to continue?");
            // Lambda for getting Button response
            alert.showAndWait().filter(response -> response == ButtonType.OK).ifPresent(response -> {
                try {
                    deleteAppts.execute();
                    stmnt.execute();
                } catch (SQLException ex) {
                    System.out.println(ex.getMessage());
                }
            });
            
            updateTable();
        } else {
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information Dialog");
            alert.setHeaderText("Error");
            alert.setContentText("Please make a selection.");

            alert.showAndWait();
        }
    }

    @FXML
    private void mainMenuHandler(ActionEvent event) throws IOException {
        FXMLLoader main = new FXMLLoader(getClass().getResource("MainMenu.fxml"));
        Parent parent = main.load();
        Scene scene = new Scene(parent);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
            
    }
    
    private void parseCustomers() {
        int customerId, addressId;
        String customerName, address, postalCode, city, country, phoneNumber;
        
        // get connection
        Connection conn = DBConnector.getConnection();
        Statement stmnt = null;
        
        
        try {
            stmnt = conn.createStatement();
            ResultSet rs = stmnt.executeQuery("SELECT customer.customerId, \n" +
                "customer.customerName, \n" +
                "customer.addressId,\n" +
                "address.address,\n" +
                "address.postalCode,\n" +
                "address.phone,\n" +
                "city.city,\n" +
                "country.country \n" +
                "FROM customer, address, city, country\n" +
                "WHERE customer.addressId = address.addressId\n" +
                "AND address.cityId = city.cityId\n" +
                "AND city.countryId = country.countryId;");
            
            
            while(rs.next()) {
                customerId = rs.getInt("customer.customerId");
                customerName = rs.getString("customer.customerName");
                addressId = rs.getInt("customer.addressId");
                address = rs.getString("address.address");
                phoneNumber = rs.getString("address.phone");
                postalCode = rs.getString("address.postalCode");
                city = rs.getString("city.city");
                country = rs.getString("country.country");
                
                customers.add(new Customer(customerId, customerName, addressId, address, phoneNumber, postalCode, city, country));
                
                
            }
            
            
            
            
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    
    private void updateTable() {
        customers.clear();
        parseCustomers();
    }
    
}
