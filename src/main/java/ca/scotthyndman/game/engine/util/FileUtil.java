package ca.scotthyndman.game.engine.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtil {
	
	/**
	 * Reads a string from a stream.
	 * 
	 * @param in
	 * @return
	 * @throws IOException
	 */
	public static String readStringFromStream(InputStream in) throws IOException {
		StringBuffer out = new StringBuffer();
		byte[] b = new byte[4096];
		for (int n; (n = in.read(b)) != -1;) {
			out.append(new String(b, 0, n));
		}

		return out.toString();
	}

	/**
	 * Reads a string from a file.
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static String readStringFromFile(File file) throws IOException {
		return new String(readBytesFromFile(file));
	}

	/**
	 * Returns the bytes contained in a file.
	 * 
	 * @param file
	 * @return
	 */
	public static byte[] readBytesFromFile(File file) throws IOException {
		//
		// Read the file
		//
		byte[] bytes;
		InputStream is = new FileInputStream(file);
		long length = file.length();

		//
		// Check if the length is within range (we can only specify array
		// dimensions by using an int)
		//
		if (length > Integer.MAX_VALUE) {
			throw new IOException("File too large - " + file.getName());
		}

		return readBytesFromStream(is, length);
	}

	/**
	 * Reads and returns the bytes from a stream, then closes the stream.
	 * 
	 * @param is
	 * @param length
	 * @return
	 * @throws IOException
	 */
	public static byte[] readBytesFromStream(InputStream is, long length) throws IOException {
		byte[] bytes;
		bytes = new byte[(int) length];
		int offset = 0;
		int numRead = 0;
		while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
			offset += numRead;
		}

		//
		// Ensure all the bytes have been read in
		//
		if (offset < bytes.length) {
			is.close();
			throw new IOException("Could not completely read file");
		}

		is.close();
		return bytes;
	}

}
