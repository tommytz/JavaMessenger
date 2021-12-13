package client;

public class ConnectionData {
	private String serverAddress;
	private int port;
	private String name;
	private BackgroundClient<Object> backgroundClient;

	public ConnectionData(String serverAddress, int port, String name, BackgroundClient<Object> backgroundClient) {
		this.serverAddress = serverAddress;
		this.port = port;
		this.name = name;
		this.backgroundClient = backgroundClient;
	}

	public String getServerAddress() {
		return serverAddress;
	}

	public int getPort() {
		return port;
	}

	public String getName() {
		return name;
	}

	public BackgroundClient<Object> getBackgroundClient() {
		return backgroundClient;
	}
}
