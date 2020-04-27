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
import model.DBConnector;

/**
 * FXML Controller class
 *
 * @author dalton
 */
public class AddApptController implements Initializable {
    
    private Timestamp stringDateTimeToTimestamp(String time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd kk:mm:ss");
        LocalDateTime tm = LocalDateTime.parse(time, formatter);
        ZonedDateTime zonedDT = tm.atZone(ZoneId.systemDefault());
        ZonedDateTime utcZonedDT = zonedDT.withZoneSameInstant(ZoneId.of("UTC"));
        tm = utcZonedDT.toLocalDateTime();
        return Timestamp.valueOf(tm);
    }
    
    private void checkOverlappingAppts(LocalDateTime start, LocalDateTime end) throws SQLException, OverlappingTimesException {
        // check for overlapping appointments
        Connection conn = DBConnector.getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM appointment");
        ZonedDateTime tsStart, tsEnd;
        ZonedDateTime startTime = start.atZone(ZoneId.systemDefault());
        ZonedDateTime endTime = end.atZone(ZoneId.systemDefault());
        while(rs.next()) {
            ZonedDateTime tempStart = rs.getTimestamp("appointment.start").toLocalDateTime().atZone(ZoneId.of("UTC"));
            ZonedDateTime tempEnd = rs.getTimestamp("appointment.end").toLocalDateTime().atZone(ZoneId.of("UTC"));
            tsStart = tempStart.withZoneSameInstant(ZoneId.systemDefault());
            tsEnd = tempEnd.withZoneSameInstant(ZoneId.systemDefault());
            System.out.print("Checking for overlaps: Appt Start " + startTime + " Appt end " + endTime + " Test start " + tsStart + " Test end " + tsEnd+"\n");
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

    private int getCustomerId(String name, Connection conn) throws SQLException {
        int customerId = 0;
        PreparedStatement stmt = conn.prepareStatement("SELECT customerId FROM customer WHERE customerName='" +name +"'");
        ResultSet rs = stmt.executeQuery();
        while(rs.next()) {
           customerId = rs.getInt("customer.customerId");
        }
        return customerId;
    }

    private int getConsultantId(String name, Connection conn) throws SQLException {
        int userId = 0;
        PreparedStatement stmt = conn.prepareStatement("SELECT userId FROM user WHERE userName='" + name + "'");
        ResultSet rs = stmt.executeQuery();
        while(rs.next()) {
            userId = rs.getInt("user.userId");
        }
        return userId;
    }

    private void addAppointment(Connection conn, int customerId, int userId, String type, String sDT,
                                String eDT, String createdBy, String createDate, String lastUpdate) throws SQLException {
        String query = "INSERT INTO appointment VALUES (null,?,?,'not needed','not needed','not needed','not needed',?,'not needed',?,?,?,?,?,?)";
        PreparedStatement add = conn.prepareStatement(query);
        add.setInt(1, customerId);
        add.setInt(2, userId);
        add.setString(3, type);
        add.setTimestamp(4, stringDateTimeToTimestamp(sDT));
        add.setTimestamp(5, stringDateTimeToTimestamp(eDT));
        add.setTimestamp(6, stringDateTimeToTimestamp(createDate));
        add.setString(7, createdBy);
        add.setTimestamp(8, stringDateTimeToTimestamp(lastUpdate));
        add.setString(9, createdBy);
        System.out.println(add.toString());
        add.execute();
    }

    private void isTimeIsWithinWorkDay( LocalTime time) throws OutsideHoursException {
        if (time.isBefore(LocalTime.of(8,0)) || time.isAfter(LocalTime.of(17, 0))) {
            throw new OutsideHoursException();
        }
    }

    private void checkAppointmentDateTime(LocalTime startTime, LocalTime endTime, LocalDate startDate, LocalDate endDate,
                                          StringBuilder startDateTime, StringBuilder endDateTime) throws OutsideHoursException, OverlappingTimesException, SQLException {
        isTimeIsWithinWorkDay(startTime);
        isTimeIsWithinWorkDay(endTime);
        checkOverlappingAppts(LocalDateTime.of(startDate, startTime), LocalDateTime.of(endDate, endTime));
        startDateTime.append(" ");
        startDateTime.append(startTime.format(DateTimeFormatter.ofPattern("kk:mm:ss")));
        endDateTime.append(" ");
        endDateTime.append(startTime.format(DateTimeFormatter.ofPattern("kk:mm:ss")));

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
    private Button addButton;
    @FXML
    private Button cancelButton;

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

        // Initialize start and end time choices
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
    private void addButtonHandler(ActionEvent event) throws IOException {
        //TODO: Fix this. This is ugly.
        StringBuilder errorMessage = new StringBuilder();
        StringBuilder startDateTime = new StringBuilder();
        StringBuilder endDateTime = new StringBuilder();
        boolean invalid = false;
        Connection conn = DBConnector.getConnection();
        int customerId = -1 , userId = -1;
        String type, createdBy, createDate, lastUpdate;

        // do input checks
        try {
            // Get customer ID and consultant ID
            String name = customerChoice.getSelectionModel().getSelectedItem();
            if (name == null) {
                errorMessage.append("Please specify the customer.");
                invalid = true;
            } else {
                customerId = getCustomerId(name, conn);
            }
            name = consultantChoice.getSelectionModel().getSelectedItem();
            if (name == null) {
                errorMessage.append("Please specify the consultant.");
            } else {
                userId = getConsultantId(name, conn);
            }
            type = typeField.getText();
            if (type.equals("")) {
                errorMessage.append("Type field is required.\n");
                invalid = true;
            }
            // Check start and end dates.
            LocalDate sDate = startDate.getValue();
            LocalDate eDate = endDate.getValue();
            if (sDate != null && eDate != null) {
                if (eDate.isBefore(sDate)) {
                    errorMessage.append("End date cannot be before start date.\n");
                    invalid = true;
                } else {
                    startDateTime.append(sDate.toString());
                    endDateTime.append(eDate.toString());
                }
            } else if (sDate == null) {
                errorMessage.append("Please specify a start date.\n");
                invalid = true;
            } else {
                errorMessage.append("Please specify an end date.\n");
                invalid = true;
            }
            LocalTime sT = startTime.getSelectionModel().getSelectedItem();
            LocalTime eT = endTime.getSelectionModel().getSelectedItem();
            try {
                if (sT != null && eT != null ) {
                    if (eT.isBefore(sT)) {
                        errorMessage.append("End time cannot be before start time.");
                        invalid = true;
                    } else {
                        checkAppointmentDateTime(sT, eT, sDate, eDate, startDateTime, endDateTime);
                    }
                } else if (sT == null) {
                    errorMessage.append("Please specify a start time.\n");
                    invalid = true;
                } else {
                    errorMessage.append("Please specify a end time.\n");
                    invalid = true;
                }
            } catch (OutsideHoursException | OverlappingTimesException e) {
                errorMessage.append(e.getMessage());
                invalid = true;
            }

            if (invalid) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Information Dialog");
                alert.setHeaderText("Invalid input!");
                alert.setContentText("Details: \n" + errorMessage.toString());
                alert.showAndWait();
                return;
            }
            
            // do some dateTime building
            String createTime = LocalTime.now().format(DateTimeFormatter.ofPattern("kk:mm:ss"));
            createDate = LocalDate.now().toString();
            createDate = createDate + " " + createTime;
            createdBy = USER;
            String lastUpdateTime = LocalTime.now().format(DateTimeFormatter.ofPattern("kk:mm:ss"));
            lastUpdate = LocalDate.now().toString();
            lastUpdate = lastUpdate + " " + lastUpdateTime;
            String sDT = startDateTime.toString();
            String eDT = endDateTime.toString();
            addAppointment(conn, customerId, userId, type, sDT, eDT, createdBy, createDate, lastUpdate);

            // return to main menu
            Parent main_parent = FXMLLoader.load(getClass().getResource("MainMenu.fxml"));
            Scene main_scene = new Scene (main_parent);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(main_scene);
            stage.centerOnScreen();
            stage.show();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
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
    
    private ObservableList<String> getComboItems(String group) throws SQLException {
        // retrieve all available customers
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
}
