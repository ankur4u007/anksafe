package anksafe.service;

import java.io.File;
import java.io.IOException;

public interface IEncryptorService {

	boolean encyptFile(File fileToEncrypt, String password, String path) throws IOException;

	boolean decyptFile(File fileToDecrypt, String password) throws IOException;

}
