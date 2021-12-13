package client;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ClientApp extends Application {
	FXMLLoader loginLoader = new FXMLLoader();
	FXMLLoader chatLoader = new FXMLLoader();
	LoginController loginController;
	ChatController chatController;
	Stage primaryStage;

	@Override
	public void start(Stage primaryStage) throws Exception {
		this.primaryStage = primaryStage;
		Parent root = loginLoader.load(getClass().getResource("client_login.fxml").openStream());
		loginController = loginLoader.getController();
		loginController.setClientApp(this);
		primaryStage.setScene(new Scene(root));
		primaryStage.setTitle("Messenger Client");
		primaryStage.show();
	}

	public void changeScene(String address, int port, String name, BackgroundClient<Object> backgroundClient)
			throws IOException {
		primaryStage.setUserData(new ConnectionData(address, port, name, backgroundClient));
		Parent root = chatLoader.load(getClass().getResource("client.fxml").openStream());
		chatController = chatLoader.getController();
		chatController.setClientApp(this);
		chatController.finishConnecting();
		primaryStage.setScene(new Scene(root));
		primaryStage.show();
	}

	public Stage getPrimaryStage() {
		return primaryStage;
	}

	public static void main(String[] args) {
		launch();
	}

}
