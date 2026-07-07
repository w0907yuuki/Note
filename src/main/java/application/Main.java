package application;

import database.DatabaseInitializer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
	@Override
	public void start(Stage stage) {
	    try {

	        DatabaseInitializer.initialize();

	        FXMLLoader loader =
	                new FXMLLoader(getClass().getResource("/view/Note.fxml"));

	        Scene scene = new Scene(loader.load());

	        stage.setTitle("上達ノート");
	        stage.setScene(scene);
	        stage.show();

	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
}