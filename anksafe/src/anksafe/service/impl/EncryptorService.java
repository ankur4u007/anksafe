package anksafe.service.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

import org.apache.commons.codec.digest.DigestUtils;

import anksafe.Helper.FileHandlerUtil;
import anksafe.service.IEncryptorService;

public class EncryptorService implements IEncryptorService {

	private static final String MARKER = "<------ANK------>";
	private static final int DEFAULT_DIFF = 15;
	private static final int SANDDARD_BLOCK = 1024;

	@Override
	public boolean encyptFile(final File fileToEncrypt, final String password) throws IOException {
		// TODO Auto-generated method stub
		final String passwordSha1 = DigestUtils.sha1Hex(password);
		final int magicNumber = getMagicNumber(passwordSha1);
		final String fileNameSha1 = DigestUtils.sha1Hex(fileToEncrypt.getPath());
		final BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fileToEncrypt));
		final File encryptedFile = FileHandlerUtil.createFileInWD(fileNameSha1);
		final BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(encryptedFile));
		// writing the sha1 of password in bytes first
		bos.write(passwordSha1.getBytes());
		// writing the marker
		bos.write(MARKER.getBytes());
		// writing the encrypted fileNmae
		bos.write(getByteArrayFromCharArr(getEncryptedCharsFromString(fileToEncrypt.getPath(), magicNumber)));
		// writing the marker again
		bos.write(MARKER.getBytes());

		final byte[] chunk = new byte[SANDDARD_BLOCK];
		int chunkLen = 0;
		while ((chunkLen = bis.read(chunk)) != -1) {
			if (chunkLen < SANDDARD_BLOCK) {
				bos.write(getEncyptedByteArray(trimByteArrays(chunk, chunkLen), magicNumber));
			} else {
				bos.write(getEncyptedByteArray(chunk, magicNumber));
			}
		}
		bis.close();
		bos.close();
		return true;

	}

	@Override
	public boolean decyptFile(final File fileToDecrypt, final String password) throws IOException {
		// TODO Auto-generated method stub
		boolean toReturn = false;
		final String passwordSha1 = DigestUtils.sha1Hex(password);
		final int magicNumber = getMagicNumber(passwordSha1);
		final byte passwordSha1Bytes[] = passwordSha1.getBytes();
		final int passwordByteLength = passwordSha1Bytes.length;

		final BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fileToDecrypt));
		final byte[] passwordSha1BytesFromFile = new byte[passwordByteLength];
		bis.read(passwordSha1BytesFromFile, 0, passwordByteLength);
		final String passwordSha1FromFile = new String(passwordSha1BytesFromFile);
		if (passwordSha1.equals(passwordSha1FromFile)) {
			// finding the marker
			final byte[] markerBytesFromFile = new byte[MARKER.getBytes().length];
			bis.read(markerBytesFromFile);
			final String markerFromFile = new String(markerBytesFromFile);
			if (MARKER.equals(markerFromFile)) {
				// finding the next marker
				// asssuming the 255 char file name
				final int fileNameLength = 255;
				final byte[] totalByteArr = new byte[fileNameLength * 2];
				int i = 0;
				String fileName = null;
				// file name shouldn;t exceed 255 - assumption
				for (; i < (fileNameLength * 2); i++) {

					final byte b = (byte) bis.read();
					// copy the read bytes to totalByteArray
					totalByteArr[i] = b;
					final char[] charsOfProbableNames = getCharArrayFromByteArray(totalByteArr);
					fileName = getFileNameFromListOfProbableFileNames(charsOfProbableNames, magicNumber);
					if (fileName != null) {
						break;
					}
				}
				if ((i >= (fileNameLength * 2)) && (fileName == null)) {
					System.out.println("SecondMarker Not Found, File has been corupted: " + fileToDecrypt.getName());
				} else {
					// creating the new decrypted file and copying the data
					final File decryptedFile = FileHandlerUtil.createFileWithDirs(fileName.trim());
					final BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(decryptedFile));
					final byte[] chunk = new byte[SANDDARD_BLOCK];
					int chunkLen = 0;
					while ((chunkLen = bis.read(chunk)) != -1) {
						if (chunkLen < SANDDARD_BLOCK) {
							bos.write(getDecyptedByteArray(trimByteArrays(chunk, chunkLen), magicNumber));
						} else {
							bos.write(getDecyptedByteArray(chunk, magicNumber));
						}
					}
					bos.close();
					toReturn = true;
				}
			} else {
				System.out.println("FirstMarker Not Found, File has been corupted: " + fileToDecrypt.getName());
			}
		} else {
			System.out.println("password isnt same, not touching the file: " + fileToDecrypt.getName());
		}
		bis.close();
		return toReturn;

	}

	private char[] getEncryptedCharsFromString(final String text, final int magicNumber) {
		final char[] tempArr = text.trim().toCharArray();
		final char[] toReturnArray = new char[tempArr.length];
		for (int i = 0; i < tempArr.length; i++) {
			toReturnArray[i] = (char) (tempArr[i] - (char) magicNumber);
		}
		return toReturnArray;
	}

	private String getDecryptedStringFromChars(final char[] charArr, final int magicNumber) {
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < charArr.length; i++) {
			sb.append((char) (charArr[i] + (char) magicNumber));
		}
		return sb.toString();
	}

	private byte[] getEncyptedByteArray(final byte[] toEncyptByteArr, final int magicNumber) {
		final byte[] toReturnByteArr = new byte[toEncyptByteArr.length];
		for (int i = 0; i < toEncyptByteArr.length; i++) {
			toReturnByteArr[i] = (byte) (toEncyptByteArr[i] + (byte) magicNumber);
		}
		return toReturnByteArr;
	}

	private byte[] getDecyptedByteArray(final byte[] toDecyptByteArr, final int magicNumber) {
		final byte[] toReturnByteArr = new byte[toDecyptByteArr.length];
		for (int i = 0; i < toDecyptByteArr.length; i++) {
			toReturnByteArr[i] = (byte) (toDecyptByteArr[i] - (byte) magicNumber);
		}
		return toReturnByteArr;
	}

	private byte[] getByteArrayFromCharArr(final char[] charArr) {
		final char[] trimmedChars = new char[getActualLengthOfCharArray(charArr)];
		for (int i = 0; i < trimmedChars.length; i++) {
			trimmedChars[i] = charArr[i];
		}
		return Charset.forName("UTF-8").encode(CharBuffer.wrap(trimmedChars)).array();
	}

	private char[] getCharArrayFromByteArray(final byte[] byteArr) {
		final byte[] trimmedBytes = new byte[getActualLengthOfByteArray(byteArr)];
		for (int i = 0; i < trimmedBytes.length; i++) {
			trimmedBytes[i] = byteArr[i];
		}
		return Charset.forName("UTF-8").decode(ByteBuffer.wrap(trimmedBytes)).array();
	}

	private String getFileNameFromListOfProbableFileNames(final char[] charsFromFile, final int magicNumber) {
		String toReturnString = null;
		if (charsFromFile != null) {
			final int charLength = getActualLengthOfCharArray(charsFromFile);
			if (charLength >= MARKER.length()) {
				final StringBuilder sb = new StringBuilder();
				for (int i = charLength - MARKER.length(); i < charLength; i++) {
					sb.append(charsFromFile[i]);
				}
				if (MARKER.equals(sb.toString())) {
					final char[] toreturnArray = new char[charLength - MARKER.length()];
					for (int i = 0; i < (charLength - MARKER.length()); i++) {
						toreturnArray[i] = charsFromFile[i];
					}
					toReturnString = getDecryptedStringFromChars(toreturnArray, magicNumber);
				}

			}
		}
		return toReturnString;
	}

	private int getActualLengthOfCharArray(final char[] chars) {
		int toReturn = 0;
		if (chars != null) {
			for (int i = chars.length - 1; i >= 0; i--) {
				if (chars[i] != 0) {
					toReturn = i + 1;
					break;
				}
			}
		}
		return toReturn;
	}

	private int getActualLengthOfByteArray(final byte[] bytes) {
		int toReturn = 0;
		if (bytes != null) {
			for (int i = bytes.length - 1; i >= 0; i--) {
				if (bytes[i] != 0) {
					toReturn = i + 1;
					break;
				}
			}
		}
		return toReturn;
	}

	private int getMagicNumber(final String sha1) {
		return (sha1.hashCode() % DEFAULT_DIFF) == 0 ? DEFAULT_DIFF : sha1.hashCode() % DEFAULT_DIFF;
	}

	private byte[] trimByteArrays(final byte[] initialByteArr, final int length) {
		final byte toReturnByte[] = new byte[length];
		for (int i = 0; i < length; i++) {
			toReturnByte[i] = initialByteArr[i];
		}
		return toReturnByte;
	}
}
