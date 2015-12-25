package ntut.csie.ezScrum.restful.export.support;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;


public class FileEncoder {
	public static String toBase64BinaryString(File sourceFile) {
		String base64BinaryString = "";
		byte[] byteArray;
		try {
			// Read bytes from File 
			byteArray = Files.readAllBytes(sourceFile.toPath());
			base64BinaryString = Base64.encode(byteArray);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return base64BinaryString;
	}
}
