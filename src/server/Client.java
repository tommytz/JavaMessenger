package server;

import java.io.ObjectOutputStream;
import java.net.Socket;

public class Client {
	private Socket socket;
	private String name;
	private String address;
	private int port;
	ObjectOutputStream outputStream = null;

	public Client(String name, Socket socket) {
		this.name = name;
		this.socket = socket;
		this.address = socket.getInetAddress().toString();
		this.port = socket.getPort();
	}

	public String getName() {
		return name;
	}

	public Socket getSocket() {
		return socket;
	}

	public String getAddress() {
		return address;
	}

	public int getPort() {
		return port;
	}

	public ObjectOutputStream getOutputStream() {
		return outputStream;
	}

	public void setOutputStream(ObjectOutputStream outputStream) {
		this.outputStream = outputStream;
	}

	@Override
	public String toString() {
		return "Client [socket=" + socket + ", name=" + name + ", address=" + address + ", port=" + port + "]";
	}
}
