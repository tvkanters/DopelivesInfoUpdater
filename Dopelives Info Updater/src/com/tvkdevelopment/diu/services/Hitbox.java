package com.tvkdevelopment.diu.services;

import java.util.regex.Pattern;

import org.json.JSONObject;

import com.tvkdevelopment.diu.Params;
import com.tvkdevelopment.diu.util.HttpHelper;

/**
 * A class for communication with Hitbox.
 */
public class Hitbox {

    /** The HTTP helper to use for requests */
    private static final HttpHelper sHttpHelper = new HttpHelper();

    /** A filter used for replacing spaces with dashes */
    private static final Pattern sSpaceFilter = Pattern.compile(" ");
    /** A filter used for removing invalid characters */
    private static final Pattern sInvalidCharacterFilter = Pattern.compile("[^a-z0-9 -]");

    /**
     * Searches a game on Hitbox.
     *
     * @param query
     *            The game to search for
     *
     * @return The category ID of the game according to Hitbox or a default ID if it wasn't found
     */
    public static String searchGame(final String query) {
        final String result = sHttpHelper.get("http://api.hitbox.tv/game/" + HttpHelper.encode(cleanupQuery(query))
                + "?seo=true");
        if (result != null) {
            final JSONObject game = new JSONObject(result).optJSONObject("category");
            if (game != null) {
                return game.getString("category_id");
            }
        }

        return Params.HITBOX_DEFAULT_GAME;
    }

    /**
     * Cleans up a query for communication with Hitbox.
     *
     * @param query
     *            The query to clean
     *
     * @return The cleaned query
     */
    private static String cleanupQuery(final String query) {
        return sSpaceFilter.matcher(sInvalidCharacterFilter.matcher(query.toLowerCase()).replaceAll(""))
                .replaceAll("-");
    }

    /**
     * Updates the status and game on Hitbox.
     *
     * @param status
     *            The new status
     * @param game
     *            The new game in a full name format
     */
    public static void updateInfo(final String status, final String game) {
        final String url = "http://api.hitbox.tv/media/live/" + Params.HITBOX_CHANNEL + "/list?authToken="
                + Params.HITBOX_TOKEN
                + "&filter=recent&hiddenOnly=false&limit=1&nocache=true&publicOnly=false&yt=false";

        // Retrieve the media data
        final String mediaData = sHttpHelper.get(url);
        if (mediaData == null) {
            System.out.println("Couldn't update Hitbox");
            return;
        }
        final JSONObject json = new JSONObject(mediaData);

        // Update the status and game
        final JSONObject livestreamInfo = json.getJSONArray("livestream").getJSONObject(0);
        livestreamInfo.put("media_status", status);
        livestreamInfo.put("media_category_id", game);

        // Send the update media data
        sHttpHelper.put(url, json.toString());
        System.out.println("Hitbox updated");
    }

    /**
     * Requests an authentication token.
     *
     * @param login
     *            The user's username
     * @param password
     *            The user's password
     *
     * @return The authentication token
     */
    public static String requestToken(final String login, final String password) {
        return sHttpHelper.post("http://api.hitbox.tv/auth/token", "login=" + login + "&pass=" + password);
    }

}
