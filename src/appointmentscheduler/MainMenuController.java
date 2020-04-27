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
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Appointment;
import model.DBConnector;

/**
 * FXML Controller class
 *
 * @author dalton
 */

class InvalidInputException extends Exception {
    public InvalidInputException(){
        super("Failed: Invalid Input");
    }
}

class OutsideHoursException extends Exception {
    public OutsideHoursException() {
        super("Chosen appointment hours are outside of business hours (8:00am - 5:00am)\n");
    }
}

class OverlappingTimesException extends Exception {
    public OverlappingTimesException() {
        super("Appointment time is overlapping another schedule appointment");
    }
}

public class MainMenuController implements Initializable {
    

    
    @FXML
    private Label greeting;
    @FXML
    private TableColumn<Appointment, Integer> appointmentTableID;
    @FXML
    private TableColumn<Appointment, String> apptCustomerName;
    @FXML
    private TableColumn<Appointment, String> apptTableConsultant;
    @FXML
    private TableColumn<Appointment, String> apptTableType;
    @FXML
    private TableColumn<Appointment, String> apptTableStart;
    @FXML
    private TableColumn<Appointment, String> apptTableEnd;
    @FXML
    private TableView<Appointment> apptTable;
    
    private ObservableList<Appointment> appointments = FXCollections.observableArrayList();
    
    private ObservableList<Appointment> currentView = FXCollections.observableArrayList();
    @FXML
    private RadioButton viewMonth;
    @FXML
    private ToggleGroup calendarViewGroup;
    @FXML
    private RadioButton viewCurrentWeek;
    
    static boolean firstLogin = false;
    
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        greeting.setText("Hello, " + LoginScreenController.USER + ".");
        
        //initialize table columns
        appointmentTableID.setCellValueFactory(new PropertyValueFactory<>("appointmentId"));
        apptCustomerName.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        apptTableConsultant.setCellValueFactory(new PropertyValueFactory<>("userName"));
        apptTableType.setCellValueFactory(new PropertyValueFactory<>("type"));
        apptTableStart.setCellValueFactory(new PropertyValueFactory<>("startString"));
        apptTableEnd.setCellValueFactory(new PropertyValueFactory<>("endString"));
        
        // parse out current appointments
        
        parseAppointments();
        
        for (Appointment appt: appointments) {
            if (appt.getStart().getMonth().equals(LocalDate.now().getMonth())) {
                currentView.add(appt);
            }
        }
        apptTable.setItems(currentView);
        
        if (!(firstLogin)) {
            checkAppointments();
        }
        firstLogin = true;
    }    
    

    @FXML
    private void addApptHandler(ActionEvent event) throws IOException {
        
        // open window for adding appointment
        Parent addApt = FXMLLoader.load(getClass().getResource("AddAppt.fxml"));
        Scene addAptScene = new Scene (addApt);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(addAptScene);
        stage.centerOnScreen();
        stage.show();
        
        
    }

    @FXML
    private void updateApptHandler(ActionEvent event) throws IOException {
        
        Parent edit;
        
        Appointment appt = apptTable.getSelectionModel().getSelectedItem();
        if(appt != null) {
            FXMLLoader editApptLoader = new FXMLLoader(getClass().getResource("ModifyAppt.fxml"));
            edit = editApptLoader.load();
            Scene editApptScene = new Scene(edit);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(editApptScene);
            stage.show();
            
            ModifyApptController controller = editApptLoader.getController();
            
            controller.setAppt(appt);
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
    private void deletedApptHandler(ActionEvent event) throws SQLException {
        Appointment selectedAppt = apptTable.getSelectionModel().getSelectedItem();
        
        if (selectedAppt != null) {
            int appointmentId = selectedAppt.getAppointmentId();
            Connection conn = DBConnector.getConnection();
            PreparedStatement stmnt = conn.prepareStatement("DELETE FROM appointment WHERE appointmentId=?");
            
            stmnt.setInt(1, appointmentId);
            
            // confirm delete
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Delete...");
            alert.setHeaderText("Deleting...");
            alert.setContentText("Are you sure you want to delete the appointment with " + selectedAppt.getCustomerName() + "?");
            // Lambda for getting Button response
            alert.showAndWait().filter(response -> response == ButtonType.OK).ifPresent(response -> {
                try {
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
    private void viewCustomersHandler(ActionEvent event) throws IOException {
        
        FXMLLoader customers = new FXMLLoader(getClass().getResource("Customers.fxml"));
        Parent parent = customers.load();
        Scene scene = new Scene(parent);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }
    
    //parse appointments
    private void parseAppointments() {
        
        int apptId, customerId, userId;
        String customerName, userName, type;
        ZonedDateTime start, end;
        ZoneId zone = ZoneId.systemDefault();
        // Get connection
        Connection conn = DBConnector.getConnection();
        Statement stmt = null;
        // try query
        try {
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT appointment.appointmentId, appointment.customerId, customer.customerName, appointment.userId, user.userName, appointment.type, appointment.start, appointment.end FROM appointment, user, customer WHERE appointment.userId = user.userId AND appointment.customerId = customer.customerId");
            
            // 
            while(rs.next()) {
                apptId = rs.getInt("appointment.appointmentId");
                customerId = rs.getInt("appointment.customerId");
                userId = rs.getInt("appointment.userId");
                customerName = rs.getString("customer.customerName");
                userName = rs.getString("user.userName");
                type = rs.getString("appointment.type");
                ZonedDateTime tempStart = rs.getTimestamp("appointment.start").toLocalDateTime().atZone(ZoneOffset.UTC);
                start = tempStart.withZoneSameInstant(zone);
                ZonedDateTime tempEnd = rs.getTimestamp("appointment.end").toLocalDateTime().atZone(ZoneOffset.UTC);
                end = tempEnd.withZoneSameInstant(ZoneOffset.systemDefault());
                
                appointments.add(new Appointment(apptId, customerId, customerName, userId, userName, type, start, end));
                
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        
    }

    @FXML
    private void viewMonthHandler(ActionEvent event) {
        // view appointments for current month
        currentMonthView();
        
    }

    @FXML
    private void currentWeekHandler(ActionEvent event) {
        
        currentWeekView();
    }
    
    public void updateTable() {
        appointments.clear();
        parseAppointments();
        if(viewMonth.isSelected()) {
            currentMonthView();
        } else {
            currentWeekView();
        }
    }
    
    private void currentWeekView() {
        currentView.clear();
        for (Appointment appt: appointments) {
            WeekFields week = WeekFields.of(Locale.getDefault());
            
            if (appt.getStart().get(week.weekOfWeekBasedYear()) == LocalDate.now().get(week.weekOfWeekBasedYear())) {
                currentView.add(appt);
            }
        }
        apptTable.setItems(currentView);
        
    }
    
    private void currentMonthView() {
        currentView.clear();
        for (Appointment appt: appointments) {
            if (appt.getStart().getMonth().equals(LocalDate.now().getMonth())) {
                currentView.add(appt);
            }
        }
        apptTable.setItems(currentView);
    }
    
    private void checkAppointments() {
        Appointment next = null;
        LocalDateTime checkedTime = LocalDateTime.now().plusMinutes(15);
       
        for (Appointment appt: appointments) {
            
            if (appt.getUserName().equals(USER) && appt.getStart().toLocalDateTime().isAfter(LocalDateTime.now()) && appt.getStart().toLocalDateTime().isBefore(checkedTime)) {
                next = appt;
            }
        }
        
        if (next != null) {
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Appointment soon!");
            alert.setHeaderText("There is an appointment in 15 minutes");
            alert.setContentText("Details: \n\nCustomer: " + next.getCustomerName() + ".\nType: " + next.getType() + ".\nTime: " + next.getStartString() + " - " + next.getEndString());
            // Lambda for getting Button response
            alert.showAndWait();
            
        }
    }

    @FXML
    private void typesByMonthHandler(ActionEvent event) throws IOException {
        
        Parent report = FXMLLoader.load(getClass().getResource("TypeByMonth.fxml"));
        Scene reportScene = new Scene (report);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(reportScene);
        stage.centerOnScreen();
        stage.show();
    }

    @FXML
    private void schedulesHandler(ActionEvent event) throws IOException {
        
        Parent report = FXMLLoader.load(getClass().getResource("ConsultantSchedules.fxml"));
        Scene reportScene = new Scene (report);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(reportScene);
        stage.centerOnScreen();
        stage.show();
    }

    @FXML
    private void apptsByCustomerHandler(ActionEvent event) throws IOException {
        
        Parent report = FXMLLoader.load(getClass().getResource("ApptsByCustomerReport.fxml"));
        Scene reportScene = new Scene (report);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(reportScene);
        stage.centerOnScreen();
        stage.show();
    }
}
