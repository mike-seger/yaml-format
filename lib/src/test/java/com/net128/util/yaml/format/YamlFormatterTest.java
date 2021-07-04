package com.net128.util.yaml.format;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.yaml.snakeyaml.constructor.ConstructorException;

import com.net128.util.yaml.ResourceLoader;

public class YamlFormatterTest implements ResourceLoader {
	private final YamlFormatter yamlFormatter = new YamlFormatter();

	@ParameterizedTest(name = "{1} -> {0}.")
	@CsvSource({
		"application1-formatted.yaml, application1.yaml",
		"application1-formatted.yaml, application1-formatted.yaml",
		"application2-formatted.yaml, application2.yaml",
		"application2-formatted.yaml, application2-formatted.yaml",
		"single-node-formatted.yaml, single-node.yaml",
		"dotted-keys-formatted.yaml, dotted-keys.yaml",
		"un-dotted-keys-formatted.yaml, un-dotted-keys.yaml",
		"multi-doc-formatted.yaml, multi-doc.yaml",
		"aliases-formatted.yaml, aliases.yaml"
	})
	@DisplayName("Test formatted YAML against expected result.")
	public void testFormatYaml(String expected, String actual)
			throws IOException, URISyntaxException {
		assertEquals(fromResource(expected), yamlFormatter.format(fromResource(actual)));
	}

	@ParameterizedTest(name = "{1} -> {0}.")
	@CsvSource({"issue-mod-aliases-formatted.yaml, issue-mod-aliases.yaml"
		//	,"issue-merge-valid.yaml, issue-merge-valid.yaml"
	})
	@DisplayName("Test identity YAML against expected result.")
	public void testIdentityYaml(String expected, String actual)
			throws IOException, URISyntaxException {
		assertNotEquals(fromResource(expected), yamlFormatter.identity(fromResource(actual)));
	}

	@Test
	@DisplayName("Test invalid YAML.")
	public void testFormatYamlFail() {
		Exception thrown =
				assertThrows(
						IllegalStateException.class,
						() -> yamlFormatter.format(fromResource("merge1-fail.yaml")));
		assertEquals("Merge collision at key: jkl.mno", thrown.getMessage());
		assertNull(thrown.getCause());
	}

	@Test
	@DisplayName("Test invalid YAML.")
	public void testFormatYamlFailMerge() {
		Exception thrown =
				assertThrows(
						IllegalStateException.class,
						() -> yamlFormatter.format(fromResource("merge2-fail.yaml")));
		assertEquals("Merge collision at key: def", thrown.getMessage());
		assertNull(thrown.getCause());
	}

	@Test
	@DisplayName("Test issue with valid YAML.")
	public void testValidIssue() {
		Exception thrown =
				assertThrows(
						ConstructorException.class,
						() -> yamlFormatter.format(fromResource("issue-merge-valid.yaml")));
		assertLinesMatch(
				List.of("Can't construct a java object for tag:yaml.org,2002:java.util.HashMap.*"),
				List.of(thrown.getMessage().replaceAll("\n.*", "")));
		assertNotNull(thrown.getCause());
	}
}
