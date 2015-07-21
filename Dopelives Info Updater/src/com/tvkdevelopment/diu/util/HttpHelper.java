package com.tvkdevelopment.diu.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * A helper class for HTTP requests.
 */
public class HttpHelper {

    /**
     * Retrieves the content from a URL.
     *
     * @param url
     *            The URL to request
     *
     * @return The contents or null if the URL couldn't be read
     */
    public static String get(final String url) {
        final StringBuilder result = new StringBuilder();
        try {
            final URLConnection connection = new URL(url).openConnection();
            final BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(),
                    Charset.forName("UTF-8")));
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line + "\n");
            }
            reader.close();
        } catch (final IOException ex) {
            System.out.println("\nCouldn't load URL " + url + ": " + ex.getClass());
            return null;
        }

        return result.toString();
    }

    /**
     * Performs a PUT request.
     *
     * @param url
     *            The URL to PUT to
     * @param parameters
     *            The parameters to PUT
     *
     * @return The result or null if the URL couldn't be read
     */
    public static String put(final String url, final String parameters) {
        return update(url, parameters, "PUT");
    }

    /**
     * Performs a POST request.
     *
     * @param url
     *            The URL to POST to
     * @param parameters
     *            The parameters to POST
     *
     * @return The result or null if the URL couldn't be read
     */
    public static String post(final String url, final String parameters) {
        return update(url, parameters, "POST");
    }

    /**
     * Performs a PUT or POST request.
     *
     *
     * @param url
     *            The URL to PUT or POST to
     * @param parameters
     *            The parameters to PUT or POST
     * @param requestMethod
     *            The HTTP request method, such as PUT or POST
     *
     * @return The result or null if the URL couldn't be read
     */
    private static String update(final String url, final String parameters, final String requestMethod) {
        final StringBuilder result = new StringBuilder();

        try {
            // Create the connection
            final HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setDoOutput(true);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestMethod(requestMethod);

            // Add the data
            final byte[] postData = parameters.getBytes(StandardCharsets.UTF_8);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Content-Length", Integer.toString(postData.length));
            connection.getOutputStream().write(postData);

            // Get the response
            InputStream input;
            if (connection.getResponseCode() == 200) {
                input = connection.getInputStream();
            } else {
                input = connection.getErrorStream();
            }

            // Print the response
            final BufferedReader reader = new BufferedReader(new InputStreamReader(input, Charset.forName("UTF-8")));
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            reader.close();

        } catch (final IOException ex) {
            System.out.println("Couldn't " + requestMethod + " to URL " + url + ": " + ex.getClass());
            return null;
        }

        return result.toString();
    }

    /**
     * Encodes a parameter value.
     *
     * @param value
     *            The value to encode
     *
     * @return The encoded value
     */
    public static String encode(final String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
        } catch (final UnsupportedEncodingException ex) {
            // Won't ever happen
            throw new RuntimeException(ex);
        }
    }
}
