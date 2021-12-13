package server;

import java.io.IOException;
import java.net.InetAddress;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class ServerApp extends Application {
	ServerController controller;

	@Override
	public void start(Stage primaryStage) throws Exception {
		FXMLLoader fxmlLoader = new FXMLLoader();
		Parent root = fxmlLoader.load(getClass().getResource("server.fxml").openStream());
		controller = fxmlLoader.getController();

		primaryStage.setScene(new Scene(root));
		// Display server IP address and host name
		controller.getServerIP().setText(InetAddress.getLocalHost().getHostAddress());
		primaryStage.setTitle("Server Dashboard @ " + InetAddress.getLocalHost().getHostName());

		primaryStage.getScene().getWindow().addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, arg0 -> {
			try {
				closeWindowEvent(arg0);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		primaryStage.show();
	}

	private void closeWindowEvent(WindowEvent event) throws IOException {
		if (controller.isServerRunning()) {
			controller.getBackgroundService().getServerSocket().close();

		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}