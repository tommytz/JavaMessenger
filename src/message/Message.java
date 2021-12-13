package message;

import java.io.Serializable;

import com.google.cloud.Timestamp;

public class Message implements Serializable, Comparable {
	public static final int QUIT_MESSAGE = 0;
	public static final int CONTENT_MESSAGE = 1;
	public static final int AUTHENTICATION_MESSAGE = 2;
	private static final long serialVersionUID = 1216381014687610576L;
	private int type;
	private String name = null;
	private String content = null;
	private Timestamp timestamp = Timestamp.now();
	private String hashedPassword = null;
	private String salt = null;

	public Message(String name, String content, Timestamp timestamp) {
		this.name = name;
		this.content = content;
		this.timestamp = timestamp;
	}

	public Message(String name, String content) {
		this.type = CONTENT_MESSAGE;
		this.name = name;
		this.content = content;
	}

	public Message(int type, String name) {
		if (type == AUTHENTICATION_MESSAGE) {
			this.type = type;
			this.name = name;
		} else {
			this.type = type;
		}
	}

	public Message(int type, String hashedPassword, String salt) {
		if (type == AUTHENTICATION_MESSAGE) {
			this.type = type;
			this.hashedPassword = hashedPassword;
			this.salt = salt;
		} else {
			this.type = type;
		}
	}

	public Message(int type) {
		if (type == QUIT_MESSAGE) {
			this.type = type;
		} else {
			this.type = type;
		}
	}

	public int getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Timestamp getTimestamp() {
		return timestamp;
	}

	public String getHashedPassword() {
		return hashedPassword;
	}

	public String getSalt() {
		return salt;
	}

	@Override
	public String toString() {
		return timestamp.toDate().toString() + " @" + name + ": " + content;
	}

	@Override
	public int compareTo(Object o) {
		return getTimestamp().compareTo(((Message) o).getTimestamp());
	}
}
