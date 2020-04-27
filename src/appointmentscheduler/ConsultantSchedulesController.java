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
import model.ConsultantSchedule;
import model.DBConnector;
import model.TypeByMonthReport;

/**
 * FXML Controller class
 *
 * @author dalton
 */
public class ConsultantSchedulesController implements Initializable {

    @FXML
    private TableView<ConsultantSchedule> table;
    @FXML
    private TableColumn<ConsultantSchedule, String> consultant;
    @FXML
    private TableColumn<ConsultantSchedule, String> customer;
    @FXML
    private TableColumn<ConsultantSchedule, String> start;
    @FXML
    private TableColumn<ConsultantSchedule, String> end;
    
    ObservableList<ConsultantSchedule> results = FXCollections.observableArrayList();
    
    private void getReportData() {
        String consultantName, customerName;
        ZonedDateTime start, end;
        Connection conn = DBConnector.getConnection();
        Statement stmnt = null;
        
        try {
            stmnt = conn.createStatement();
            ResultSet rs = stmnt.executeQuery("SELECT user.userName, customer.customerName, appointment.start AS start, appointment.end AS end FROM appointment, user, customer WHERE appointment.userId = user.userId AND appointment.customerId = customer.customerId GROUP BY userName, start");
            
            while(rs.next()) {
                consultantName = rs.getString("userName");
                customerName = rs.getString("customerName");
                ZonedDateTime tempStart = rs.getTimestamp("start").toLocalDateTime().atZone(ZoneOffset.UTC);
                start = tempStart.withZoneSameInstant(ZoneId.systemDefault());
                ZonedDateTime tempEnd = rs.getTimestamp("end").toLocalDateTime().atZone(ZoneOffset.UTC);
                end = tempEnd.withZoneSameInstant(ZoneId.systemDefault());
                
                results.add(new ConsultantSchedule(consultantName, customerName, start, end));
                
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
        
        consultant.setCellValueFactory(new PropertyValueFactory<>("consultantName"));
        customer.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        start.setCellValueFactory(new PropertyValueFactory("stringStart"));
        end.setCellValueFactory(new PropertyValueFactory<>("stringEnd"));
        
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
