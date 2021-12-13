package client;

import java.io.IOException;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {
	private ClientApp clientApp;
	private BackgroundClient<Object> backgroundClient;
	private String address;
	private int port;
	private String name;

	@FXML
	private Button connectButton;
	@FXML
	private TextField nameField;
	@FXML
	private PasswordField passwordField;
	@FXML
	private TextField serverIPField;
	@FXML
	private TextField serverPortField;
	@FXML
	private Label loginFeedback;

	@FXML
	void connectHandler(ActionEvent event) {
		if (serverIPField.getText() != null && !serverIPField.getText().equals("") && serverPortField.getText() != null
				&& !serverPortField.getText().equals("")) {
			address = serverIPField.getText();
			port = Integer.parseInt(serverPortField.getText());
			name = nameField.getText();
			if (name != null && !name.equals("")) {
				try {
					// Connect and begin authentication
					backgroundClient = new BackgroundClient<>(this, address, port, name,
							passwordField.getText().toCharArray());
					passwordField.clear();
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							loginFeedback.setText("Connecting...");
						}
					});
					backgroundClient.start();
				} catch (IOException e) {
					System.out.println("Could not reach server: " + e);
					loginFeedback.setText("Could not reach server: Connection timed out");
					passwordField.clear();
				}
			} else {
				loginFeedback.setText("Please enter a username and password");
			}
		}
	}

	public void finishConnecting(boolean authenticated) throws IOException {
		if (authenticated) {
			clientApp.changeScene(address, port, name, backgroundClient);
		} else {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					passwordField.clear();
					loginFeedback.setText("Invalid username and/or password");
				}
			});
		}
	}

	public void setClientApp(ClientApp clientApp) {
		this.clientApp = clientApp;
	}
}