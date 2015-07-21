package com.tvkdevelopment.diu;

import com.tvkdevelopment.diu.StreamInfo.StreamInfoListener;
import com.tvkdevelopment.diu.services.Hitbox;
import com.tvkdevelopment.diu.services.Twitch;

/**
 * The info updater that searches the game and updates Twitch and Hitbox when informed of topic info.
 */
public class InfoUpdater implements StreamInfoListener {

    /** The type that a stream must be for it to update Twitch and Hitbox */
    private static final String REQUIRED_TYPE = "game";

    public static void main(final String[] args) {
        System.out.println("Start scanning for topic changes");
        StreamInfo.addListener(new InfoUpdater());
        StreamInfo.startRequestInterval();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStreamInfoUpdated(final String streamer, final String type, final String game) {
        // Only update for games
        if (!REQUIRED_TYPE.equals(type.toLowerCase())) {
            System.out.println("\nStream is a " + type.toLowerCase());
            onStreamInfoRemoved();
            return;
        }

        final String statusInfo = "[" + streamer + "] " + game.trim();
        System.out.println("\nNew game: " + statusInfo);
        final String status = statusInfo + " | " + Params.STATUS_POSTFIX;

        // Search game on Twitch and update it
        final String gameTwitch = Twitch.searchGame(game);
        System.out.println("Twitch game: " + gameTwitch);
        Twitch.updateInfo(status, gameTwitch);

        // Search game on Hitbox and update it
        final String gameHitbox = Hitbox.searchGame(gameTwitch);
        System.out.println("Hitbox game: " + (Params.HITBOX_DEFAULT_GAME.equals(gameHitbox) ? "DEFAULT" : gameHitbox));
        Hitbox.updateInfo(status, gameHitbox);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStreamInfoRemoved() {
        System.out.println("\nNo (game) stream");

        Twitch.updateInfo(Params.STATUS_POSTFIX, "");
        Hitbox.updateInfo(Params.STATUS_POSTFIX, Params.HITBOX_DEFAULT_GAME);
    }

}
