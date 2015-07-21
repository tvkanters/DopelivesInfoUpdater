package com.tvkdevelopment.diu.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileHelper {

    public static String getContent(final String filename) {
        try {
            return new String(Files.readAllBytes(Paths.get(filename)));
        } catch (final IOException ex) {
            throw new RuntimeException("Couldn't read file: " + filename);
        }
    }

    public static void write(final String filename, final String content) {
        try {
            final PrintWriter writer = new PrintWriter(filename, "UTF-8");
            writer.print(content);
            writer.close();
        } catch (final IOException ex) {
            throw new RuntimeException("Couldn't write file: " + filename);
        }
    }
}
