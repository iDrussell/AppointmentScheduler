/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appointmentscheduler;

import java.sql.SQLException;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.DBConnector;

/**
 *
 * @author dalton
 */
public class AppointmentScheduler extends Application {
    
    @Override
    public void start(Stage stage) throws Exception {
        
        Parent root = null;
        // determine locale
        Locale locale = Locale.getDefault();
        
        //test locale change
        //Locale.setDefault(new Locale("no", "NO"));
        
        // grab resource bundle
        ResourceBundle rb = ResourceBundle.getBundle("lang/rb");
        
        // change to login screen
        FXMLLoader loader = new FXMLLoader(getClass().getResource("LoginScreen.fxml"));
        loader.setResources(rb);
        root = loader.load();
                
        Scene scene = new Scene(root);
        
        stage.setScene(scene);
        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        DBConnector.dbConnect();
        launch(args);
        DBConnector.dbClose();
    }
    
}
