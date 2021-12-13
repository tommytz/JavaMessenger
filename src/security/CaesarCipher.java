package security;

public class CaesarCipher {
	private static String uppercase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private static String lowercase = "abcdefghijklmnopqrstuvwxyz";
	private static String special = "!#$%&'()*+,-./:;<=>?@[]^_`{|}~";
	private static String numbers = "0123456789";

	public static String cipher(String message, int key) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < message.length(); i++) {
			if (message.charAt(i) == ' ') {
				result.append(' ');
			} else {
				for (int j = 0; j < 26; j++) {
					if (j < special.length() && message.charAt(i) == special.charAt(j)) {
						result.append(special.charAt(j)); // print special characters as is
					} else if (j < numbers.length() && message.charAt(i) == numbers.charAt(j)) {
						result.append(numbers.charAt(j)); // print numbers as it is
					} else if (message.charAt(i) == lowercase.charAt(j)) {
						result.append(lowercase.charAt((j + key) % 26));
					} else if (message.charAt(i) == uppercase.charAt(j)) {
						result.append(uppercase.charAt((j + key) % 26));
					}
				}
			}

		}
		return result.toString();
	}

	public static String decipher(String message, int key) {
		return cipher(message, 26 - (key % 26));
	}

//	public static void main(String[] args) {
//		String cipher = CaesarCipher.cipher("Hello World!", 3);
//		System.out.println(cipher);
//		cipher = CaesarCipher.decipher(cipher, 3);
//		System.out.println(cipher);
//	}
}
