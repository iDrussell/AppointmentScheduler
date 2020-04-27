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
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.DBConnector;


class LoginFailureException extends Exception {
    public LoginFailureException(ResourceBundle rb) {
        super(rb.getString("errorMessage"));
    }
}

/**
 * FXML Controller class
 *
 * @author dalton
 */
public class LoginScreenController implements Initializable {

    @FXML
    private Label usernameLabel;
    @FXML
    private Label passwordLabel;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel;
    @FXML
    private Button loginButton;
    @FXML
    private Label greeting;
    
    ResourceBundle rb;
    @FXML
    private TextField usernameField;
    
    static String USER;
    
    static Logger logger = Logger.getLogger("LoginLogger");
    private FileHandler logFile;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        try {
            logFile = new FileHandler("LogFile.txt", true);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        SimpleFormatter formatter = new SimpleFormatter();
        logFile.setFormatter(formatter);
        logger.addHandler(logFile);
        
        this.rb = ResourceBundle.getBundle("lang/rb", Locale.getDefault());
        System.out.println(Locale.getDefault());

        
        greeting.setText(this.rb.getString("greeting"));
        usernameLabel.setText(this.rb.getString("usernameLabel"));
        passwordLabel.setText(this.rb.getString("passwordLabel"));
        loginButton.setText(this.rb.getString("loginButtonLabel"));
        
    }    
    
    private boolean attemptLogin() throws LoginFailureException {
        // get connection
       Connection conn = DBConnector.getConnection();
       Statement stmt = null;
       boolean valid = false;
       try {
           // validate credentials
           stmt = conn.createStatement();
           ResultSet rs = stmt.executeQuery("SELECT * FROM user WHERE userName='" + usernameField.getText() + "' AND password='" + passwordField.getText() + "'");

           if(rs.next()) {
               valid = true;

           } else {
               throw new LoginFailureException(this.rb);
           }
       } catch (SQLException esql) {
           System.out.println("error in login attempt.");
       }

       return valid;


    }

    @FXML
    private void loginButtonHandler(ActionEvent event) throws IOException {
        
        // attempt login
        try {
            // attemptLogin()
            if (attemptLogin()) {
                // Switch to Main menu
                logger.info("Successful login by: " + usernameField.getText());
                USER = usernameField.getText();
                FXMLLoader main = new FXMLLoader(getClass().getResource("MainMenu.fxml"));
                Parent parent = main.load();
                Scene scene = new Scene(parent);
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(scene);
                stage.centerOnScreen();
                stage.show();
            }
        } catch (LoginFailureException e) {
            logger.info("Failed login Attempt by: " + usernameField.getText());
            errorLabel.setText(e.getMessage()) ;
        }
    }
}
