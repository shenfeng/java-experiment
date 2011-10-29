package util;

import java.util.Random;

public class StringUtil {

	private static final String ALPHA = "abcdefghijklmnopqrstuvwxyz"
			+ "1234567890";
	private static final Random r = new Random();

	public static String getRandomString(int max) {
		int length = r.nextInt(max) + 5;
		StringBuilder sb = new StringBuilder(length);
		for (int i = 0; i < length; ++i) {
			sb.append(ALPHA.charAt(r.nextInt(ALPHA.length())));
		}
		return sb.toString();
	}
}
