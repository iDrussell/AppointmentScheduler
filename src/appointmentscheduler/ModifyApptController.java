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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.Appointment;
import model.DBConnector;

/**
 * FXML Controller class
 *
 * @author dalton
 */
public class ModifyApptController implements Initializable {
    
    private Timestamp stringDateTimeToTimestamp(String time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd kk:mm:ss");
        LocalDateTime tm = LocalDateTime.parse(time, formatter);
        
        ZonedDateTime zonedDT = tm.atZone(ZoneId.systemDefault());
        ZonedDateTime utcZonedDT = zonedDT.withZoneSameInstant(ZoneId.of("UTC"));
        tm = utcZonedDT.toLocalDateTime();
        Timestamp sqlDT = Timestamp.valueOf(tm);
        
        return sqlDT;
        
        
    }
    
    private void checkOverlappingAppts(LocalDateTime start, LocalDateTime end) throws SQLException, OverlappingTimesException {
        // check for overlapping appointments
        ArrayList<Appointment> appts;
        Connection conn = DBConnector.getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM appointment");
        ZonedDateTime tsStart, tsEnd;
        ZonedDateTime startTime = start.atZone(ZoneId.systemDefault());
        ZonedDateTime endTime = end.atZone(ZoneId.systemDefault());
        
        while(rs.next()) {
            int apptId = rs.getInt("appointment.appointmentId");
            ZonedDateTime tempStart = rs.getTimestamp("appointment.start").toLocalDateTime().atZone(ZoneId.of("UTC"));
            ZonedDateTime tempEnd = rs.getTimestamp("appointment.end").toLocalDateTime().atZone(ZoneId.of("UTC"));
            tsStart = tempStart.withZoneSameInstant(ZoneId.systemDefault());
            tsEnd = tempEnd.withZoneSameInstant(ZoneId.systemDefault());
            System.out.print("Checking for overlaps: Appt Start " + startTime + " Appt end " + endTime + " Test start " + tsStart + " Test end " + tsEnd+"\n");
            
            if (apptId != appt.getAppointmentId()) {
                if (startTime.isBefore(tsStart) && endTime.isAfter(tsStart)) {
                    throw new OverlappingTimesException();
                }
            
                if (endTime.isAfter(tsEnd) && startTime.isBefore(tsEnd)) {
                    throw new OverlappingTimesException();
                }
            
                if (startTime.isAfter(tsStart) && endTime.isBefore(tsEnd)) {
                    throw new OverlappingTimesException();
                }
            }
            
            
        }
    }

    @FXML
    private ChoiceBox<String> customerChoice;
    @FXML
    private ChoiceBox<String> consultantChoice;
    @FXML
    private TextField typeField;
    @FXML
    private DatePicker startDate;
    @FXML
    private ChoiceBox<LocalTime> startTime;
    @FXML
    private DatePicker endDate;
    @FXML
    private ChoiceBox<LocalTime> endTime;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;

    Appointment appt;
    
    private LocalDate parseZonedDate (ZonedDateTime dt) {
        LocalDate date = dt.toLocalDate();
        return date;
    }
    private LocalTime parseZonedTime (ZonedDateTime dt) {
        LocalTime time = dt.toLocalTime();
        return time;
    }
    private ObservableList<String> getComboItems(String group) throws SQLException {
        // retrieve all available customers or consultants
        ObservableList<String> results = FXCollections.observableArrayList();
        Connection conn = DBConnector.getConnection();
        
        if(group.equals("customers")) {
            PreparedStatement query = conn.prepareStatement("SELECT customerName FROM customer");
            ResultSet rs = query.executeQuery();
        
            while(rs.next()) {
                results.add(rs.getString("customer.customerName"));
            }
        } else if(group.equals("consultants")) {
            PreparedStatement query = conn.prepareStatement("SELECT userName FROM user");
            ResultSet rs = query.executeQuery();
            
            while (rs.next()) {
                results.add(rs.getString("user.userName"));
            }
        }
        
        
        
        return results;
        
    }
    
    public void setAppt(Appointment appt) {
        this.appt = new Appointment(appt.getAppointmentId(),
                                    appt.getCustomerId(),
                                    appt.getCustomerName(),
                                    appt.getUserId(),
                                    appt.getUserName(),
                                    appt.getType(),
                                    appt.getStart(),
                                    appt.getEnd());
        
        
        customerChoice.getSelectionModel().select(this.appt.getCustomerName());
        consultantChoice.getSelectionModel().select(appt.getUserName());
        typeField.setText(appt.getType());
        startDate.setValue(parseZonedDate(appt.getStart()));
        startTime.getSelectionModel().select(parseZonedTime(appt.getStart()));
        endDate.setValue(parseZonedDate(appt.getEnd()));
        endTime.getSelectionModel().select(parseZonedTime(appt.getEnd()));
        
        
        
        
    }
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        ObservableList<String> combo;
        ObservableList<LocalTime> time = FXCollections.observableArrayList();
        // Initialize choice boxes
        // Since you can't add an appointment to
        // the DB with a null customer ID
        
        try {
            combo = getComboItems("customers");
            customerChoice.setItems(combo);
            combo = getComboItems("consultants");
            consultantChoice.setItems(combo);
            
        } catch (SQLException e) {
            System.out.println("Bad SQL");
            e.printStackTrace();
        }
        
        LocalTime midnight = LocalTime.of(0, 0);
        time.add(midnight);
        midnight = midnight.plusMinutes(30);
        while (midnight.isAfter(LocalTime.of(0,0))) {
            time.add(midnight);
            
            midnight = midnight.plusMinutes(30);
    
        }
        
        startTime.setItems(time);
        endTime.setItems(time);
    }    

    @FXML
    private void saveButtonHandler(ActionEvent event) throws IOException {
        
        StringBuilder errorMessage = new StringBuilder();
        StringBuilder startDateTime = new StringBuilder();
        StringBuilder endDateTime = new StringBuilder();
        boolean invalid = false;
        Connection conn = DBConnector.getConnection();
        
        // all values 
        int customerId = 0, userId = 0;
        String type, lastUpdate, sTime, eTime;
        
        
        
        
        
        // do input checks
        try {
            String query = "UPDATE appointment SET customerId=?, userId=?, type=?, start=?, end=?, lastUpdate=?, lastUpdateBy=? WHERE appointmentId=?";
            PreparedStatement update = conn.prepareStatement(query);
            
            String name = customerChoice.getSelectionModel().getSelectedItem();
            PreparedStatement stmt = conn.prepareStatement("SELECT customerId FROM customer WHERE customerName='" +name +"'");
            ResultSet rs = stmt.executeQuery();
            while(rs.next()) {
                customerId = rs.getInt("customer.customerId");
            }
            
            name = consultantChoice.getSelectionModel().getSelectedItem();
            stmt = conn.prepareStatement("SELECT userId FROM user WHERE userName='" + name + "'");
            rs = stmt.executeQuery();
            while(rs.next()) {
                userId = rs.getInt("user.userId");
            }
            
            type = typeField.getText();
            if (type == null) {
                errorMessage.append("Type field is required.\n");
                invalid = true;
            }
            LocalDate sDate = startDate.getValue();
            if (sDate == null) {
                errorMessage.append("Please specify a start date.\n");
                invalid = true;
            } else {
                startDateTime.append(sDate.toString());
            }
            
            
            LocalDate eDate = endDate.getValue();
            if (eDate == null) {
                errorMessage.append("Please specify an end date.\n");
                invalid = true;
            } else if (eDate.isBefore(sDate)) {
                errorMessage.append("End date cannot be before start date.\n");
                invalid = true;
            } else {
                endDateTime.append(eDate.toString());
                
            }
            
            LocalTime sT = startTime.getSelectionModel().getSelectedItem();
            LocalTime eT = endTime.getSelectionModel().getSelectedItem();
            
            try {
               
                
                if (sT == null) {
                    errorMessage.append("Please specify a start time.\n");
                    invalid = true;
                } else {
                    
                    if (sT.isBefore(LocalTime.of(8,0)) || sT.isAfter(LocalTime.of(17, 0))) {
                        throw new OutsideHoursException();
                    }
                
                    startDateTime.append(" ");

                    startDateTime.append(sT.format(DateTimeFormatter.ofPattern("kk:mm:ss")));
                }
            
                
                if (eT == null) {
                    errorMessage.append("Please specify a end time.\n");
                    invalid = true;
                } else if (eT.isBefore(sT)) {
                    errorMessage.append("End time cannot be before start time.");
                    invalid = true;
                } else {
                    if (eT.isBefore(LocalTime.of(8, 0)) || eT.isAfter(LocalTime.of(17, 0))) {
                        throw new OutsideHoursException();
                    }
                    endDateTime.append(" ");
                    endDateTime.append(eT.format(DateTimeFormatter.ofPattern("kk:mm:ss")));
                }
                
            } catch (OutsideHoursException e) {
                errorMessage.append(e.getMessage());
                invalid = true;
            }
            
            try {
                checkOverlappingAppts(LocalDateTime.of(sDate, sT), LocalDateTime.of(eDate, eT));
            } catch(OverlappingTimesException e) {
                errorMessage.append(e.getMessage());
                invalid = true;
            }
            
            if (invalid) {
                throw new InvalidInputException();
            }
            
            // do some dateTime building
            String lastUpdateTime = LocalTime.now().format(DateTimeFormatter.ofPattern("kk:mm:ss"));
            lastUpdate = LocalDate.now().toString();
            lastUpdate = lastUpdate + " " + lastUpdateTime;
            
            String sDT = startDateTime.toString();
            String eDT = endDateTime.toString();
            
            //set values of query
            update.setInt(1, customerId);
            update.setInt(2, userId);
            update.setString(3, type);
            update.setTimestamp(4, stringDateTimeToTimestamp(sDT));
            update.setTimestamp(5, stringDateTimeToTimestamp(eDT));
            update.setTimestamp(6, stringDateTimeToTimestamp(lastUpdate));
            update.setString(7, USER);
            update.setInt(8, appt.getAppointmentId());
            
            
            
            
            
            
            System.out.println(update.toString());
           
            update.execute();
            
            // return to main menu
            Parent customerMenu = FXMLLoader.load(getClass().getResource("MainMenu.fxml"));
            Scene customerMenuScene = new Scene (customerMenu);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(customerMenuScene);
            stage.centerOnScreen();
            stage.show();;
            
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (InvalidInputException e) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information Dialog");
            alert.setHeaderText(e.getMessage());
            alert.setContentText("Details: \n" + errorMessage.toString());

            alert.showAndWait();
        }
    }

    @FXML
    private void cancelButtonHandler(ActionEvent event) throws IOException {
        
        // close stage
        Parent main_parent = FXMLLoader.load(getClass().getResource("MainMenu.fxml"));
        Scene main_scene = new Scene (main_parent);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(main_scene);
        stage.centerOnScreen();
        stage.show();
    }
    
}
