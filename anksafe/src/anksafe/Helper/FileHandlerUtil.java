package anksafe.Helper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileHandlerUtil {

	public static final String WD = ".";

	public static List<File> getAllFiles(final String fileParam, final List<String> excludedList) {
		final File file = new File(fileParam);
		List<File> returnList = null;
		if (file != null) {
			final File[] fileList = file.listFiles();
			if (fileList != null) {
				returnList = new ArrayList<File>(fileList.length);
				for (final File f : fileList) {
					if (f.isDirectory()) {
						final List<File> filesToAdd = getAllFiles(f.getPath(), excludedList);
						if (filesToAdd != null) {
							returnList.addAll(filesToAdd);
						}
					} else {
						if ((excludedList != null) && !excludedList.contains(f.getName().toLowerCase())) {
							returnList.add(f);
						}
					}
				}
			}
		}
		return returnList;
	}

	public static void createHintFile(final String hint) throws IOException {
		final String fileName = WD.equals(".") ? ExcludedFiles.HINT_FILE.getFileName() : WD
				+ ExcludedFiles.HINT_FILE.getFileName();
		final File f = new File(fileName);
		final BufferedWriter bw = new BufferedWriter(new FileWriter(f));
		bw.write(hint);
		bw.close();
	}

	public static boolean deleteHintFile() throws IOException {
		final String fileName = WD.equals(".") ? ExcludedFiles.HINT_FILE.getFileName() : WD
				+ ExcludedFiles.HINT_FILE.getFileName();
		final File f = new File(fileName);
		return f.delete();
	}

	public static File createFileWithDirs(final String filePath) {
		final File file = new File(filePath);
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		return file;
	}

	public static File createFileInWD(final String fileName) {
		return new File(WD + fileName);
	}

	public static void deleteSubdirs() {
		final File file = new File(WD);
		deleteSubdirsRecursively(file);
	}

	public static void deleteSubdirsRecursively(final File file) {
		final File[] fileList = file.listFiles();
		if (fileList != null) {
			for (final File f : fileList) {
				deleteSubdirsRecursively(f);
				if (f.isDirectory()) {
					f.delete();
				}
			}
		}
	}
}
