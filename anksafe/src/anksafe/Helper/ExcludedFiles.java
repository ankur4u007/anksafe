/**
 *
 */
package anksafe.Helper;

import java.util.ArrayList;
import java.util.List;

import anksafe.ANKsafe;

/**
 * @author CHANDRAYAN
 *
 */
public enum ExcludedFiles {

	PROGRAM(ANKsafe.class.getSimpleName() + ".exe"), HINT_FILE(ANKsafe.class.getSimpleName() + "_PasswordHint.txt");

	private final String fileName;

	private ExcludedFiles(final String name) {
		fileName = name;
	}

	public static List<String> getAllExcludedFiles() {
		final ExcludedFiles efArr[] = ExcludedFiles.values();
		final List<String> listToReturn = new ArrayList<String>(efArr.length);
		for (final ExcludedFiles ef : efArr) {
			listToReturn.add(ef.fileName.toLowerCase());
		}
		return listToReturn;
	}

	public final String getFileName() {
		return fileName;
	}

}
