package client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Iterator;
import java.util.List;

import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import message.Message;
import security.CaesarCipher;
import security.LoginHash;

public class BackgroundClient<V> extends Service<V> {
	private Socket socket;
	private ObjectInputStream in;
	private ObjectOutputStream out;
	private LoginController loginController;
	private ChatController chatController;
	private String name;
	private char[] password;
	private int cipherKey = 3;

	public BackgroundClient(LoginController loginController, String serverAddress, int port, String name,
			char[] password) throws IOException {
		socket = new Socket(serverAddress, port);
		this.loginController = loginController;
		this.name = name;
		this.password = password;
	}

	@Override
	protected Task<V> createTask() {
		return new Task<>() {
			@SuppressWarnings("unchecked")
			@Override
			protected V call()
					throws IOException, ClassNotFoundException, NoSuchAlgorithmException, InvalidKeySpecException {
				try {
					// Establish connection with the server and set name on server
					out = new ObjectOutputStream(socket.getOutputStream());
					in = new ObjectInputStream(socket.getInputStream());
					authenticationProtocol();

					// Get chat history from server and decipher into plaintext
					List<Message> storedMessages = (List<Message>) in.readObject();
					for (Message msg : storedMessages) {
						msg.setContent(CaesarCipher.decipher(msg.getContent(), cipherKey));
					}
					// Remove messages that are private and for different client, then add to view
					Iterator<Message> iter = storedMessages.iterator();
					while (iter.hasNext()) {
						Message msg = iter.next();
						// If the @ is not addressed to client
						if (msg.getContent().startsWith("@") && !msg.getContent().startsWith("@" + name)) {
							// If client was not the one who sent it
							if (!msg.getName().equals(name)) {
								iter.remove();
							}
						}
					}
					chatController.getMessageHistory().addAll(storedMessages);

					// Listen for messages from the server
					while (!socket.isClosed()) {
						Message input = (Message) in.readObject();
						if (input.getType() == Message.QUIT_MESSAGE) {
							break;
						}
						// Decipher message and add to chat view on UI
						if (input != null) {
							input.setContent(CaesarCipher.decipher(input.getContent(), cipherKey));
							Platform.runLater(new Runnable() {
								@Override
								public void run() {
									chatController.getMessageHistory().add(input);
								}
							});
						}
					}
				} finally {
					chatController.disconnect();
					socket.close();
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							Alert alert = new Alert(AlertType.ERROR);
							alert.setTitle("Connection lost");
							alert.setHeaderText("Connection with server lost");
							alert.setContentText(
									"The connection to the server has been and lost and the application will now close");
							alert.showAndWait();
							Platform.exit();
						}
					});
				}
				return null;
			}
		};
	}

	private void authenticationProtocol()
			throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, ClassNotFoundException {
		// Send authentication request with name
		sendMessage(new Message(Message.AUTHENTICATION_MESSAGE, name));
		// Receive hashed password and salt from server
		Message authenticationReply = (Message) in.readObject();
		if (authenticationReply != null) {
			if (LoginHash.validatePassword(password, authenticationReply.getHashedPassword(),
					authenticationReply.getSalt())) {
				// Username and password are correct, send confirmation to server
				password = null;
				sendMessage(new Message(Message.AUTHENTICATION_MESSAGE, name));
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						try {
							loginController.finishConnecting(true);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				});
			} else {
				// Username and password are incorrect, send quit to server and break connection
				password = null;
				sendMessage(new Message(Message.QUIT_MESSAGE));
				loginController.finishConnecting(false);
			}
		}

	}

	public void sendMessage(String text) throws IOException {
		text = CaesarCipher.cipher(text, cipherKey);
		out.writeObject(new Message(name, text));
		out.flush();
	}

	public void sendMessage(Message msg) throws IOException {
		out.writeObject(msg);
		out.flush();
	}

	public Socket getSocket() {
		return socket;
	}

	public void setChatController(ChatController chatController) {
		this.chatController = chatController;
	}
}
