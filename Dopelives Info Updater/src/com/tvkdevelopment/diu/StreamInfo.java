package com.tvkdevelopment.diu;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.tvkdevelopment.diu.util.HttpHelper;

/**
 * A static helper class for getting stream info from the IRC topic.
 */
public class StreamInfo {

    /** The formatter for time prefixes per line */
    private static final SimpleDateFormat sTimeFormatter = new SimpleDateFormat("[HH:mm:ss] ");

    /** The URL at which to get the topic info */
    private static final String URL_TOPIC = "http://goalitium.kapsi.fi/dopelives_status2";

    /** The pattern used to match active streams */
    private static final Pattern sTopicParser = Pattern.compile("^(.+)\n([^:]+): ?(.+)$");

    /** Whether or not a stream is currently active */
    private static boolean sStreamActive = false;
    /** The last detected streamer */
    private static String sStreamer;
    /** The last detected stream type */
    private static String sType;
    /** The last detected game */
    private static String sGame;

    /** The listeners that will receive updates of stream info changes */
    private static final List<StreamInfoListener> sListeners = new LinkedList<>();

    /** The last printed topic info */
    private static String sLastPrintInfo = "";

    /**
     * Periodically refreshes the latest stream info. This method is synchronous and will deny any calls after it.
     */
    public synchronized static void startRequestInterval() {
        while (true) {
            try {
                // Check the newest stream info
                final String result = HttpHelper.get(URL_TOPIC);

                if (result != null) {
                    // Overwrite the last print info by new info
                    final String printInfo = sTimeFormatter.format(Calendar.getInstance().getTime())
                            + result.replace("\n", " ");
                    System.out.print(printInfo);
                    for (int i = printInfo.length(); i < sLastPrintInfo.length(); ++i) {
                        System.out.print(" ");
                    }
                    System.out.print("\r");
                    sLastPrintInfo = printInfo;

                    // Parse the topic
                    final Matcher matcher = sTopicParser.matcher(result.trim());
                    if (matcher.find()) {
                        // Stream info found, see if it needs to be updated
                        final String streamer = matcher.group(1);
                        final String type = matcher.group(2);
                        final String game = matcher.group(3);

                        if (!sStreamActive || !sStreamer.equals(streamer) || !sType.equals(type) || !sGame.equals(game)) {
                            sStreamActive = true;
                            sStreamer = streamer;
                            sType = type;
                            sGame = game;

                            // Notify all listeners of a change in stream info
                            for (final StreamInfoListener listener : sListeners) {
                                listener.onStreamInfoUpdated(sStreamer, sType, sGame);
                            }
                        }

                    } else {
                        // No stream info found
                        if (sStreamActive) {
                            sStreamActive = false;

                            for (final StreamInfoListener listener : sListeners) {
                                listener.onStreamInfoRemoved();
                            }
                        }
                    }
                } else {
                    System.out.println("\nCouldn't retrieve topic");
                }

            } catch (final Throwable ex) {
                System.out.println("\nTopic updater exception/error");
                ex.printStackTrace();
            }

            // Add interval between checks
            try {
                Thread.sleep(Params.REQUEST_INTERVAL_TOPIC);

            } catch (final Throwable ex) {
                System.out.println("\nThread sleep exception/error");
                ex.printStackTrace();
            }
        }
    }

    /**
     * Adds a listener that will be informed of any stream info update.
     *
     * @param listener
     *            The listener to receive updates
     */
    public static void addListener(final StreamInfoListener listener) {
        sListeners.add(listener);
    }

    /**
     * This is a static-only class.
     */
    private StreamInfo() {}

    /**
     * The interface for receiving updates of stream info changes.
     */
    public interface StreamInfoListener {

        /**
         * Called when the stream info is updated for an active stream.
         *
         * @param streamer
         *            The streamer
         * @param The
         *            type of stream
         * @param game
         *            The game being streamed
         */
        void onStreamInfoUpdated(String streamer, String type, String game);

        /**
         * Called when a streamer has stopped.
         */
        void onStreamInfoRemoved();

    }

}
