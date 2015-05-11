/**
 *
 */
package anksafe;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import anksafe.Helper.ExcludedFiles;
import anksafe.Helper.FileHandlerUtil;
import anksafe.service.IEncryptorService;
import anksafe.service.impl.EncryptorService;

/**
 * @author CHANDRAYAN
 *
 */
public class ANKsafe {

	/**
	 * @param args
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public static void main(final String[] args) throws InterruptedException, IOException {

		final List<String> excludedList = ExcludedFiles.getAllExcludedFiles();
		List<File> fileList = null;
		if (args.length > 0) {
			fileList = new ArrayList<File>();
			for (final String s : args) {
				fileList.add(new File(s));
			}
		} else {
			fileList = FileHandlerUtil.getAllFiles(FileHandlerUtil.WD, excludedList);
		}
		final IEncryptorService ecs = new EncryptorService();

		if (fileList != null) {
			System.out.print("Press 1 for Encrypt, 2 For Decrypt: ");
			final Scanner sc = new Scanner(System.in);
			String response = sc.nextLine();
			if ("1".equals(response)) {
				// encrypt
				System.out.print("Enter the Password to Encrypt:");
				final String password1 = sc.nextLine();
				System.out.print("Re-Enter the Password to Encrypt:");
				final String password2 = sc.nextLine();
				if (password1.equals(password2)) {
					System.out.print("Enter the Password Hint:");
					response = sc.nextLine();
					FileHandlerUtil.createHintFile(response);
					// Encrypt eachFile
					for (final File file : fileList) {
						if (ecs.encyptFile(file, password1)) {
							file.delete();
						}
					}
					// if possible delete any possible subdirs;
					FileHandlerUtil.deleteSubdirs();
				} else {
					System.out.println("Passwords doesn't match, Try Again!");
				}
			} else if ("2".equals(response)) {
				// decrypt
				System.out.print("Enter the Password to Decrypt:");
				final String password = sc.nextLine();
				// decrypt each file
				int totalFilesDeleted = 0;
				for (final File file : fileList) {
					if (ecs.decyptFile(file, password)) {
						file.delete();
						totalFilesDeleted++;
					}
				}
				if (totalFilesDeleted == fileList.size()) {
					FileHandlerUtil.deleteHintFile();
				}
			} else {
				System.out.println("Not a Good Response, Try Again!");
			}
			sc.close();
			final String os = System.getProperty("os.name");
			if ((os != null) && os.contains("Window")) {
				new ProcessBuilder("pause");
			} else {
				Thread.sleep(2000l);
			}
		}
	}
}
