package com.net128.util.yaml.format;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.net128.util.yaml.ResourceLoader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.net.URISyntaxException;

public class YamlFormatterTest implements ResourceLoader {
	private final YamlFormatter yamlFormatter = new YamlFormatter();
	@ParameterizedTest(name = "{1} -> {0}.")
	@CsvSource({
		"application-formatted.yaml, application.yaml"
		,"application-formatted.yaml, application-formatted.yaml"
		,"test1-formatted.yaml, test1.yaml"
		,"test2-formatted.yaml, test2.yaml"
		,"test3-formatted.yaml, test3.yaml"
	})
	@DisplayName("Test formatted YAML against expected result.")
	public void testFormatYaml(String expected, String actual) throws IOException, URISyntaxException {
		assertEquals(fromResource(expected), yamlFormatter.format(fromResource(actual)));
	}

	@Test
	@DisplayName("Test failing YAML.")
	public void testFormatYamlFail() {
		Exception thrown = assertThrows(IllegalStateException.class, () -> yamlFormatter.format(fromResource("test4-fail.yaml")));
		assertEquals("Merge collision at key: jkl.mno", thrown.getMessage());
	}
}
