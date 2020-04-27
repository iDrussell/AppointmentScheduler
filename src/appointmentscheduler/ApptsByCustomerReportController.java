/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appointmentscheduler;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.ApptsByCustomer;
import model.ConsultantSchedule;
import model.DBConnector;

/**
 * FXML Controller class
 *
 * @author dalton
 */
public class ApptsByCustomerReportController implements Initializable {

    @FXML
    private TableView<ApptsByCustomer> table;
    @FXML
    private TableColumn<ApptsByCustomer, String> customer;
    @FXML
    private TableColumn<ApptsByCustomer, Integer> total;
    
    ObservableList<ApptsByCustomer> results = FXCollections.observableArrayList();
    
    private void getReportData() {
        String customerName;
        int total;
        Connection conn = DBConnector.getConnection();
        Statement stmnt = null;
        
        try {
            stmnt = conn.createStatement();
            ResultSet rs = stmnt.executeQuery("SELECT customer.customerName, COUNT(appointment.appointmentId) AS total FROM appointment, customer WHERE appointment.customerId = customer.customerId GROUP BY customer.customerName");
            
            while(rs.next()) {
                customerName = rs.getString("customerName");
                total = rs.getInt("total");
                
                results.add(new ApptsByCustomer(customerName, total));
                
            }
            
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        
    }
    /**
     * Initializes the controller class.
     */
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        
        customer.setCellValueFactory(new PropertyValueFactory<>("CustomerName"));
        total.setCellValueFactory(new PropertyValueFactory<>("total"));
        
        getReportData();
        table.setItems(results);
    }    

    @FXML
    private void closeHandler(ActionEvent event) throws IOException {
        
        FXMLLoader main = new FXMLLoader(getClass().getResource("MainMenu.fxml"));
        Parent parent = main.load();
        Scene scene = new Scene(parent);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }
    
}
