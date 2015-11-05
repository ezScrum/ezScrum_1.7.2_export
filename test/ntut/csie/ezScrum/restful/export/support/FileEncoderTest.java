package ntut.csie.ezScrum.restful.export.support;

import static org.junit.Assert.assertEquals;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sun.syndication.io.impl.Base64;

import ntut.csie.ezScrum.restful.export.support.FileEncoder;

public class FileEncoderTest {
	private final String SOURCE_CONTENT = "TEST_TO_BASE64_BINARY";
	private final String FILE_PATH = "./TestData/source.txt";
	private File mSourceFile;

	@Before
	public void setUp() throws IOException {
		mSourceFile = new File(FILE_PATH);
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(FILE_PATH));
			writer.write(SOURCE_CONTENT);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			writer.close();
		}
	}

	@After
	public void tearDown() {
		mSourceFile.delete();
	}

	@Test
	public void testToBase64BinaryString() {
		String expectedOutput = Base64.encode(SOURCE_CONTENT);
		assertEquals(expectedOutput, FileEncoder.toBase64BinaryString(mSourceFile));
	}
}
