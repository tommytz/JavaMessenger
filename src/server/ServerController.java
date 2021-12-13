package server;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import database.FirestoreDatabase;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class ServerController implements Initializable {
	private int port;
	private BackgroundServer<Object> backgroundService;
	private boolean serverRunning = false;
	private static ObservableList<Client> connectedClients = FXCollections.observableArrayList();
	private FirestoreDatabase firestoreDatabase;

	@FXML
	private TextField serverIP;
	@FXML
	private TextField serverPort;
	@FXML
	private Label serverStatus;
	@FXML
	private Button serverButton;
	@FXML
	private TableView<Client> clientTable;
	@FXML
	private TableColumn<Client, String> ipColumn;
	@FXML
	private TableColumn<Client, String> nameColumn;
	@FXML
	private TableColumn<Client, Integer> portColumn;

	@FXML
	void serverButtonHandler(ActionEvent event) throws IOException {
		if (serverRunning) {
			backgroundService.getServerSocket().close();
			serverRunning = false;
			serverStatus.setText("Server is not running");
			serverButton.setText("Start Server");
		} else {
			if (serverPort.getText() != null && !serverPort.getText().equals("")) {
				port = Integer.parseInt(serverPort.getText());
				backgroundService = new BackgroundServer<>(port, firestoreDatabase);
				backgroundService.start();
				serverRunning = true;
				serverButton.setText("Stop Server");
				serverStatus.setText("Server running on port " + port);
			}
		}
	}

	public boolean isServerRunning() {
		return serverRunning;
	}

	public TextField getServerIP() {
		return serverIP;
	}

	public BackgroundServer<Object> getBackgroundService() {
		return backgroundService;
	}

	public static ObservableList<Client> getConnectedClients() {
		return connectedClients;
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
		ipColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
		portColumn.setCellValueFactory(new PropertyValueFactory<>("port"));
		clientTable.setItems(connectedClients);
		try {
			firestoreDatabase = new FirestoreDatabase();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
