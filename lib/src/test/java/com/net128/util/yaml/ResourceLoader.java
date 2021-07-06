package com.net128.util.yaml;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

public interface ResourceLoader {
	default String fromResource(String location) throws IOException, URISyntaxException {
		return new String(Files.readAllBytes(
			Paths.get(Objects.requireNonNull(getClass().getResource(location)).toURI())));
	}
}
