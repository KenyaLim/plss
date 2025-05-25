package projek_keuangan;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import projek_keuangan.db.DatabaseConnection;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        // Initialize database
        DatabaseConnection.initializeDatabase();
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/projek_keuangan/login-view.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
