package com.tvkdevelopment.diu.services;

import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

import com.tvkdevelopment.diu.Params;
import com.tvkdevelopment.diu.util.HttpHelper;

/**
 * A class for communication with Twitch.
 */
public class Twitch {

    /** The maximum amount of update attempts to be performed before giving up */
    private static final int MAX_UPDATE_ATTEMPT = 5;

    /** The URL used for updating Twitch info */
    private static final String UPDATE_URL = "https://api.twitch.tv/kraken/channels/" + Params.TWITCH_CHANNEL
            + "?oauth_token=" + Params.TWITCH_TOKEN;

    /** The Accept header to target the right API with */
    private static final String ACCEPT_HEADER = "application/vnd.twitchtv.v3+json";

    /** The HTTP helper to use for requests */
    private static final HttpHelper sHttpHelper = new HttpHelper(ACCEPT_HEADER);

    /** The filters for things that should be removed step-by-step from the query */
    private static final Pattern[] sQueryFilters = { Pattern.compile("http[^ )]+"), Pattern.compile("\\([^)]*\\)"),
            Pattern.compile("filler", Pattern.CASE_INSENSITIVE), Pattern.compile(" [-+~](?: .*|$)"),
            Pattern.compile("[-+~:].*") };
    /** A filter for converting multiple spaces to one */
    private static final Pattern sDoubleSpaceFilter = Pattern.compile(" {2,}");
    /** A filter for removing the last word */
    private static final Pattern sWordCutFilter = Pattern.compile(" [^ ]+$");

    /**
     * Searches a game on Twitch and tries to find the best match.
     *
     * @param query
     *            The game to search for
     *
     * @return The name of the game according to Twitch or the original query if it wasn't found
     */
    public static String searchGame(final String query) {
        // Clean the first URL
        String lastOption = cleanupQuery(query);

        // Attempt the full query
        String result = executeGameSearch(lastOption);
        if (result != null) {
            return result;
        }

        // Use filters to trim the option
        for (int i = 0;; ++i) {
            final String option;

            // While filters are available, use those to trim the options
            if (i < sQueryFilters.length) {
                option = cleanupQuery(sQueryFilters[i].matcher(lastOption).replaceAll(""));
                if (option.equals(lastOption)) {
                    continue;
                }

            } else {
                // After all filters are exhausted, cut words from the end
                option = sWordCutFilter.matcher(lastOption).replaceAll("");
                if (option.equals(lastOption)) {
                    break;
                }
            }

            // Perform the actual search
            result = executeGameSearch(option);
            if (result != null) {
                return result;
            }
            lastOption = option;
        }

        return query;
    }

    /**
     * Cleans up a query. Useful to perform after each filter.
     *
     * @param query
     *            The query to clean
     *
     * @return The cleaned query
     */
    private static String cleanupQuery(final String query) {
        return sDoubleSpaceFilter.matcher(query).replaceAll(" ").trim();
    }

    /**
     * Performs the actual game query request.
     *
     * @param query
     *            The game to search for
     *
     * @return The name of the game according to Twitch or null if it wasn't found
     */
    private static String executeGameSearch(final String query) {
        System.out.println("Twitch search: " + query);
        final String result = sHttpHelper.get("https://api.twitch.tv/kraken/search/games?q=" + HttpHelper.encode(query)
                + "&type=suggest&oauth_token=" + Params.TWITCH_TOKEN);
        if (result == null) {
            return null;
        }
        final JSONArray games = new JSONObject(result).getJSONArray("games");

        // Check if there's a match
        if (games.length() > 0) {
            // Scan all matches for an exact match to prevent newer games having priority
            for (int i = 0; i < games.length(); ++i) {
                final String name = games.getJSONObject(i).getString("name");
                if (name.equals(query)) {
                    return name;
                }
            }

            // With no exact match, just use the first
            return games.getJSONObject(0).getString("name");
        }

        return null;
    }

    /**
     * Updates the status and game on Twitch.
     *
     * @param status
     *            The new status
     * @param game
     *            The new game in a full name format
     */
    public static void updateInfo(final String status, String game) {
        // Prevent Twitch from banning us for certain games
        for (final String bannedGame : Params.TWITCH_BLACKLIST) {
            if (game.equals(bannedGame)) {
                game = Params.TWITCH_BLACKLIST_REPLACEMENT;
                break;
            }
        }

        // Construct the URL
        final String data = "channel[status]=" + HttpHelper.encode(status) + "&channel[game]="
                + HttpHelper.encode(game);

        // Twitch updates sometimes randomly fail, so try until we succeed
        int tryCount = 0;
        while (tryCount++ < MAX_UPDATE_ATTEMPT) {
            // Send data
            final String result = sHttpHelper.put(UPDATE_URL, data);

            // Check if the update was successful
            if (result != null) {
                final JSONObject json = new JSONObject(result);
                if (status.equals(json.optString("status")) && game.equals(json.optString("game"))) {
                    System.out.println("Twitch update successful");
                    return;
                }
            }
        }

        System.out.println("Couldn't update Twitch");
    }

    /**
     * Requests an authentication token.
     *
     * @param clientID
     *            The DIU client token
     * @param clientSecret
     *            The DIU client secret
     * @param redirectUri
     *            The DIU redirect URI
     * @param code
     *            The code given to the user after accepting
     * @param state
     *            The DIU state
     *
     * @return The authentication token
     */
    public static String requestToken(final String clientID, final String clientSecret, final String redirectUri,
            final String code, final String state) {
        return sHttpHelper.post("https://api.twitch.tv/kraken/oauth2/token", "client_id=" + clientID + "&client_secret="
                + clientSecret + "&grant_type=authorization_code&redirect_uri=" + HttpHelper.encode(redirectUri)
                + "&code=" + code + "&state=" + state);
    }

}
