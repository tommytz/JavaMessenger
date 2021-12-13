package database;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.common.collect.Lists;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;

import message.Message;
import security.CaesarCipher;

public class FirestoreDatabase {
	private String keyPath;
	private GoogleCredentials credentials;
	private Firestore database;
	private int cipherKey = 3;

	public FirestoreDatabase() throws FileNotFoundException, IOException {
		keyPath = "javamessenger-36f7c-firebase-adminsdk-qy63p-1619a16edb.json";
		credentials = getCredentials(keyPath);
		database = getInstance(credentials, "javamessenger-36f7c");
	}

	public void saveMessage(String name, String content, Timestamp timestamp) {
		try {
			Map<String, Object> data = new HashMap<>();
			data.put("name", name);
			data.put("content", content);
			data.put("timestamp", timestamp);
			ApiFuture<DocumentReference> addedDocRef = database.collection("messages").add(data);
			String id = addedDocRef.get().getId();
			System.out.println("Uploaded document with ID: " + id);
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
	}
	
	public List<Message> getStoredMessages() throws InterruptedException, ExecutionException {
		List<Message> serverMessages = new ArrayList<Message>();
		ApiFuture<QuerySnapshot> future = database.collection("messages").get();
		// future.get() blocks on response
		List<QueryDocumentSnapshot> documents = future.get().getDocuments();
		// Encrypt message content using Caesar cipher
		for (QueryDocumentSnapshot document : documents) {
		  String name = document.getString("name");
		  String content = document.getString("content");
		  content = CaesarCipher.cipher(content, cipherKey);
		  Timestamp timestamp = document.getTimestamp("timestamp");
		  serverMessages.add(new Message(name, content, timestamp));
		}
		serverMessages.sort(null);
		return serverMessages;
	}

	public Message lookupUser(String username) throws InterruptedException, ExecutionException {
		DocumentReference docRef = database.collection("users").document(username);
		ApiFuture<DocumentSnapshot> future = docRef.get();
		DocumentSnapshot document = future.get();
		if (document.exists()) {
			String hashedPassword = document.getString("hashedPassword");
			String salt = document.getString("salt");
			return new Message(Message.AUTHENTICATION_MESSAGE, hashedPassword, salt);
		}
		System.out.println("No such user exists");
		return null;
	}

	public GoogleCredentials getCredentials(String jsonPath) throws FileNotFoundException, IOException {
		GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(jsonPath))
				.createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));
		return credentials;
	}

	@SuppressWarnings("deprecation")
	public Firestore getInstance(GoogleCredentials credentials, String projectId) {
		FirebaseOptions options = new FirebaseOptions.Builder().setCredentials(credentials).setProjectId(projectId)
				.build();
		FirebaseApp.initializeApp(options);
		Firestore database = FirestoreClient.getFirestore();
		return database;
	}

//	private void addUser(String username, String password) {
//		try {
//			byte[] byteSalt = LoginHash.generateSalt();
//			byte[] bytePassword = LoginHash.generatePasswordHash(password.toCharArray(), byteSalt);
//			String salt = LoginHash.toHex(byteSalt);
//			String hashedPassword = LoginHash.toHex(bytePassword);
//
//			DocumentReference docRef = database.collection("users").document(username);
//			Map<String, Object> data = new HashMap<>();
//			data.put("username", username);
//			data.put("hashedPassword", hashedPassword);
//			data.put("salt", salt);
//			ApiFuture<WriteResult> result = docRef.set(data);
//			System.out.println("Update time : " + result.get().getUpdateTime());
//		} catch (InterruptedException | ExecutionException | NoSuchAlgorithmException | InvalidKeySpecException e) {
//			e.printStackTrace();
//		}
//	}
//
//	public static void main(String[] args) throws Exception {
//		FirestoreDatabase fsdb = new FirestoreDatabase();
//		fsdb.addUser("admin", "admin");
//		fsdb.addUser("tommytz", "password");
//		fsdb.addUser("AliAhmed", "grading");
//		fsdb.lookupUser("tommytz");
//		fsdb.getStoredMessages();
//	}
}
