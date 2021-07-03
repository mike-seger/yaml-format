package com.net128.util.yaml.format;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

public interface StringLoader {
    default String fromFile(File file) throws IOException {
        return Files.readString(file.toPath());
    }

    default String fromResource(String location) throws IOException, URISyntaxException {
        return new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(getClass().getResource(location)).toURI())));
    }
}
