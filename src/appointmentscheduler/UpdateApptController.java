/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appointmentscheduler;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author dalton
 */
public class UpdateApptController implements Initializable {

    @FXML
    private ChoiceBox<?> customerChoice;
    @FXML
    private ChoiceBox<?> consultantChoice;
    @FXML
    private TextField typeField;
    @FXML
    private DatePicker startDate;
    @FXML
    private ChoiceBox<?> startTime;
    @FXML
    private DatePicker endDate;
    @FXML
    private ChoiceBox<?> endTime;
    @FXML
    private Button updateButton;
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
    private void updateButtonHandler(ActionEvent event) {
    }

    @FXML
    private void cancelButtonHandler(ActionEvent event) {
        
        ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
    }
    
}
