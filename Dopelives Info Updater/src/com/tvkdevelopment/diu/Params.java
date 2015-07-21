package com.tvkdevelopment.diu;

/**
 * A static class for general parameters.
 */
public class Params {

    /** The amount of time in milliseconds before between each topic request */
    public static final int REQUEST_INTERVAL_TOPIC = 5 * 1000;
    /** The string to put behind the streamer/game info in the status */
    public static final String STATUS_POSTFIX = "Join us in our main chat: http://dopelives.com";

    /** The Twitch channel to update */
    public static final String TWITCH_CHANNEL = "ENTER INFO HERE";
    /** The Twitch authentication token to use */
    public static final String TWITCH_TOKEN = "ENTER INFO HERE";

    /** The Hitbox channel to update */
    public static final String HITBOX_CHANNEL = "ENTER INFO HERE";
    /** The Hitbox authentication token to use */
    public static final String HITBOX_TOKEN = "ENTER INFO HERE";
    /** The default Hitbox game category ID to fall back to when the game isn't found */
    public static final String HITBOX_DEFAULT_GAME = "50077";

}
