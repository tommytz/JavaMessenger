package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutionException;

import database.FirestoreDatabase;
import message.Message;
import security.CaesarCipher;

public class ClientThread extends Thread {
	private Client client;
	private Socket socket;
	ObjectInputStream in;
	ObjectOutputStream out;
	FirestoreDatabase firestoreDatabase;
	private int cipherKey = 3;
	private String name;

	public ClientThread(Socket socket, FirestoreDatabase firestoreDatabase) {
		this.socket = socket;
		this.firestoreDatabase = firestoreDatabase;
	}

	@Override
	public void run() {
		try {
			// Establish connection with client
			in = new ObjectInputStream(socket.getInputStream());
			out = new ObjectOutputStream(socket.getOutputStream());
			authenticationProtocol();
			out.writeObject(firestoreDatabase.getStoredMessages());

			// Listen for messages from the client
			while (!socket.isClosed()) {
				Message input = (Message) in.readObject();
				if (input.getType() == Message.QUIT_MESSAGE) {
					break;
				}
				// relay message to connected clients and save on firestore
				if (input != null) {
					System.out
							.println("@" + input.getName() + " " + CaesarCipher.decipher(input.getContent(), cipherKey)
									+ " " + socket.getRemoteSocketAddress().toString());
					relayMessage(input);
					uploadMessage(input);
				}
			}
		} catch (IOException | ClassNotFoundException | InterruptedException | ExecutionException e) {
			System.out.println("@" + name + " has disconnected: " + e);
		} finally {
			ServerController.getConnectedClients().remove(client);
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void uploadMessage(Message msg) {
		firestoreDatabase.saveMessage(msg.getName(), CaesarCipher.decipher(msg.getContent(), cipherKey),
				msg.getTimestamp());
	}

	private void relayMessage(Message msg) throws IOException {
		// Whisper to one client
		if (msg.getContent().startsWith("@")) {
			String text = CaesarCipher.decipher(msg.getContent(), cipherKey);
			for (Client connectedClient : ServerController.getConnectedClients()) {
				if (text.startsWith("@" + connectedClient.getName())) {
					connectedClient.getOutputStream().writeObject(msg);
					connectedClient.outputStream.flush();
				}
				// And also send to self!
				if(msg.getName().equals(client.getName())) {
					client.getOutputStream().writeObject(msg);
					client.outputStream.flush();
				}
			}
		// Send to all clients
		} else {
			for (Client connectedClient : ServerController.getConnectedClients()) {
				connectedClient.getOutputStream().writeObject(msg);
				connectedClient.outputStream.flush();
			}
		}
	}

	public void sendMessage(Message msg) throws IOException {
		out.writeObject(msg);
		out.flush();
	}

	private void authenticationProtocol()
			throws ClassNotFoundException, IOException, InterruptedException, ExecutionException {
		// Receive authentication request with name from client
		Message authenticationRequest = (Message) in.readObject();
		name = authenticationRequest.getName();
		// Lookup name in database, returns null if no user exists
		Message authenticationReply = firestoreDatabase.lookupUser(name);
		if (authenticationReply != null) {
			// Send hashed password and salt back to client
			sendMessage(authenticationReply);
		}
		Message authenticationEnd = (Message) in.readObject();
		if (authenticationEnd.getType() == Message.AUTHENTICATION_MESSAGE) {
			// Correct username and password, make client object
			client = setupNewClient();
			ServerController.getConnectedClients().add(client);
		} else if (authenticationEnd.getType() == Message.QUIT_MESSAGE) {
			// Incorrect username and password, close socket
			socket.close();
		}
	}

	private Client setupNewClient() throws ClassNotFoundException, IOException {
		client = new Client(name, socket);
		client.setOutputStream(out);
		return client;
	}
}
