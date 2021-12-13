package server;

import java.io.IOException;
import java.net.ServerSocket;

import database.FirestoreDatabase;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import message.Message;

public class BackgroundServer<V> extends Service<V> {
	private ServerSocket serverSocket;
	private FirestoreDatabase firestoreDatabase;

	public BackgroundServer(int port, FirestoreDatabase firestoreDatabase) throws IOException {
		serverSocket = new ServerSocket(port);
		this.firestoreDatabase = firestoreDatabase;
	}

	@Override
	protected Task<V> createTask() {
		return new Task<>() {
			@Override
			protected V call() throws Exception {
				try {
					System.out.println("Server socket opened on port " + serverSocket.getLocalPort());
					while (!serverSocket.isClosed()) {
						new ClientThread(serverSocket.accept(), firestoreDatabase).start();
					}
				} finally {
					for (Client connectedClient : ServerController.getConnectedClients()) {
						connectedClient.getOutputStream().writeObject(new Message(Message.QUIT_MESSAGE));
						connectedClient.outputStream.flush();
					}
					System.out.println("Server socket closed");
				}
				return null;
			}
		};
	}

	public ServerSocket getServerSocket() {
		return serverSocket;
	}
}
