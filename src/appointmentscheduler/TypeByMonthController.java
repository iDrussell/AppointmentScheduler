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
import model.DBConnector;
import model.TypeByMonthReport;

/**
 * FXML Controller class
 *
 * @author dalton
 */
public class TypeByMonthController implements Initializable {

    @FXML
    private TableColumn<TypeByMonthReport, String> month;
    @FXML
    private TableColumn<TypeByMonthReport, String> type;
    @FXML
    private TableColumn<TypeByMonthReport, Integer> total;
    @FXML
    private TableView<TypeByMonthReport> table;
    
    ObservableList<TypeByMonthReport> results = FXCollections.observableArrayList();
    
    
    private void getReportData() {
        int month, total;
        String type;
        Connection conn = DBConnector.getConnection();
        Statement stmnt = null;
        
        try {
            stmnt = conn.createStatement();
            ResultSet rs = stmnt.executeQuery("SELECT EXTRACT(MONTH FROM start) AS month, TYPE, COUNT(type) AS total FROM appointment GROUP BY month, type");
            
            while(rs.next()) {
                month = rs.getInt("month");
                type = rs.getString("type");
                total = rs.getInt("total");
                
                
                results.add(new TypeByMonthReport(month, type, total));
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
        month.setCellValueFactory(new PropertyValueFactory<>("month"));
        type.setCellValueFactory(new PropertyValueFactory<>("type"));
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
