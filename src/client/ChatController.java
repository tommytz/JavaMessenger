package client;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import message.Message;

public class ChatController implements Initializable {
	private ClientApp clientApp;
	private BackgroundClient<Object> backgroundClient;
	private ObservableList<Message> messageHistory = FXCollections.observableArrayList();
	private boolean connected = false;
	private String name;

	@FXML
	private ListView<Message> chatWindow;
	@FXML
	private Label connectionStatus;
	@FXML
	private TextArea messageInput;
	@FXML
	private Button sendButton;

	@FXML
	void sendMessageHandler(ActionEvent event) throws IOException {
		if (connected) {
			if (messageInput.getText() != null && !messageInput.getText().equals("")) {
				String text = messageInput.getText();
				messageInput.clear();
				backgroundClient.sendMessage(text);
			}
		}
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		chatWindow.setItems(messageHistory);

		messageInput.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent keyEvent) {
				if (connected && keyEvent.getCode() == KeyCode.ENTER) {
					if (messageInput.getText() != null && !messageInput.getText().equals("")) {
						String text = messageInput.getText();
						messageInput.clear();
						try {
							backgroundClient.sendMessage(text);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		});

	}

	public void finishConnecting() throws IOException {
		ConnectionData cd = (ConnectionData) clientApp.getPrimaryStage().getUserData();
		backgroundClient = cd.getBackgroundClient();
		backgroundClient.setChatController(this);
		connected = true;
		clientApp.getPrimaryStage().setTitle("Messenger Client ~ @" + cd.getName());
		connectionStatus.setText("Connected to server " + cd.getServerAddress() + " on port " + cd.getPort());
	}

	public void setClientApp(ClientApp clientApp) {
		this.clientApp = clientApp;
	}

	public void disconnect() {
		connected = false;
	}

	public boolean isConnected() {
		return connected;
	}

	public BackgroundClient<Object> getBackgroundClient() {
		return backgroundClient;
	}

	public ObservableList<Message> getMessageHistory() {
		return messageHistory;
	}

	public String getName() {
		return name;
	}
}
