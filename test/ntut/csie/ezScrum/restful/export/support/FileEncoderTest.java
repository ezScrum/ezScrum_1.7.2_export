package ntut.csie.ezScrum.restful.export.support;

import static org.junit.Assert.assertEquals;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;

import org.junit.Test;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

public class FileEncoderTest {
	private final String SOURCE_CONTENT = "TEST_TO_BASE64_BINARY";
	private final String FILE_PATH = "./TestData/source.txt";
	private final String TXT_FILE_PATH = "./TestData/attachFiles/txt測試.txt";
	private final String RAR_FILE_PATH = "./TestData/attachFiles/rar測試.rar";
	private final String DOCX_FILE_PATH = "./TestData/attachFiles/docx測試.docx";
	private final String DOC_FILE_PATH = "./TestData/attachFiles/doc測試.doc";
	private final String JPEG_FILE_PATH = "./TestData/attachFiles/jpeg測試.jpg";
	private final String PDF_FILE_PATH = "./TestData/attachFiles/pdf測試.pdf";
	private final String PNG_FILE_PATH = "./TestData/attachFiles/png測試.png";
	private final String XLSX_FILE_PATH = "./TestData/attachFiles/xlsx測試.xlsx";
	private final String XLS_FILE_PATH = "./TestData/attachFiles/xls測試.xls";
	private final String XML_FILE_PATH = "./TestData/attachFiles/xml測試.xml";

	@Test
	public void testToBase64BinaryString() throws IOException {
		File sourceFile = new File(FILE_PATH);
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(FILE_PATH));
			writer.write(SOURCE_CONTENT);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			writer.close();
		}
		String expectedOutput = Base64.encode(SOURCE_CONTENT.getBytes("UTF-8"));
		assertEquals(expectedOutput, FileEncoder.toBase64BinaryString(sourceFile));
		sourceFile.delete();
	}

	@Test
	public void testToBase64BinaryString_Encoding_Txt() throws IOException {
		File file = new File(TXT_FILE_PATH);
		byte[] bytes = Files.readAllBytes(file.toPath());
		String expectedOutput = Base64.encode(bytes);
		assertEquals(expectedOutput, FileEncoder.toBase64BinaryString(file));
	}

	@Test
	public void testToBase64BinaryString_Encoding_Rar() throws IOException {
		File file = new File(RAR_FILE_PATH);
		byte[] bytes = Files.readAllBytes(file.toPath());
		String expectedOutput = Base64.encode(bytes);
		assertEquals(expectedOutput, FileEncoder.toBase64BinaryString(file));
	}

	@Test
	public void testToBase64BinaryString_Encoding_Docx() throws IOException {
		File file = new File(DOCX_FILE_PATH);
		byte[] bytes = Files.readAllBytes(file.toPath());
		String expectedOutput = Base64.encode(bytes);
		assertEquals(expectedOutput, FileEncoder.toBase64BinaryString(file));
	}

	@Test
	public void testToBase64BinaryString_Encoding_Doc() throws IOException {
		File file = new File(DOC_FILE_PATH);
		byte[] bytes = Files.readAllBytes(file.toPath());
		String expectedOutput = Base64.encode(bytes);
		assertEquals(expectedOutput, FileEncoder.toBase64BinaryString(file));
	}

	@Test
	public void testToBase64BinaryString_Encoding_Jpeg() throws IOException {
		File file = new File(JPEG_FILE_PATH);
		byte[] bytes = Files.readAllBytes(file.toPath());
		String expectedOutput = Base64.encode(bytes);
		assertEquals(expectedOutput, FileEncoder.toBase64BinaryString(file));
	}

	@Test
	public void testToBase64BinaryString_Encoding_Pdf() throws IOException {
		File file = new File(PDF_FILE_PATH);
		byte[] bytes = Files.readAllBytes(file.toPath());
		String expectedOutput = Base64.encode(bytes);
		assertEquals(expectedOutput, FileEncoder.toBase64BinaryString(file));
	}

	@Test
	public void testToBase64BinaryString_Encoding_Png() throws IOException {
		File file = new File(PNG_FILE_PATH);
		byte[] bytes = Files.readAllBytes(file.toPath());
		String expectedOutput = Base64.encode(bytes);
		assertEquals(expectedOutput, FileEncoder.toBase64BinaryString(file));
	}

	@Test
	public void testToBase64BinaryString_Encoding_Xlsx() throws IOException {
		File file = new File(XLSX_FILE_PATH);
		byte[] bytes = Files.readAllBytes(file.toPath());
		String expectedOutput = Base64.encode(bytes);
		assertEquals(expectedOutput, FileEncoder.toBase64BinaryString(file));
	}

	@Test
	public void testToBase64BinaryString_Encoding_Xls() throws IOException, NoSuchAlgorithmException {
		File sourceFile = new File(XLS_FILE_PATH);
		byte[] expectedBytes = Files.readAllBytes(sourceFile.toPath());
		String expectedOutput = Base64.encode(expectedBytes);
		assertEquals(expectedOutput, FileEncoder.toBase64BinaryString(sourceFile));
	}

	@Test
	public void testToBase64BinaryString_Encoding_Xml() throws IOException {
		File file = new File(XML_FILE_PATH);
		byte[] bytes = Files.readAllBytes(file.toPath());
		String expectedOutput = Base64.encode(bytes);
		assertEquals(expectedOutput, FileEncoder.toBase64BinaryString(file));
	}
}
