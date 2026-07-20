package world.hv2.starterpack.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Resolves a user-typed player name to an online {@link Player}, tolerating the
 * username prefix Floodgate puts on Bedrock accounts.
 *
 * <p>Floodgate joins a Bedrock account under a prefixed Java-side username --
 * {@code .acarm} for a player who thinks of themselves as {@code carm} -- using the
 * {@code username-prefix: "."} default from Floodgate's shipped {@code config.yml}.
 * Neither Bukkit lookup handles this: {@link Bukkit#getPlayerExact(String)} is an exact
 * match, and {@link Bukkit#getPlayer(String)} matches a prefix of the *name*, so
 * {@code getPlayer("carm")} does not find {@code .acarm} either -- that name starts with
 * a dot, not a {@code c}. An operator naming the unprefixed form gets "not online" for a
 * player standing in front of them.
 *
 * <p>This matters more than an ordinary lookup nicety because Bedrock players get no tab
 * completion at all: Geyser bakes the whole command tree into one login packet and never
 * sends command-suggestion packets, so a Bedrock player cannot discover the prefixed
 * form. That is also why {@link #noSuchPlayerMessage} names who *is* online instead of
 * dead-ending -- it is the only channel through which the correct name reaches them.
 *
 * <p>The prefix is hardcoded to Floodgate's default rather than made configurable. A
 * plugin config key would be a second, unvalidatable source of truth for a value owned by
 * a different plugin's config, and it would silently rot if the server changed one and not
 * the other. A server that has reconfigured the prefix still resolves through the
 * case-insensitive sweep in {@link #resolve}.
 *
 * <p>Deliberately duplicated per plugin rather than extracted to a shared library; see
 * the ecosystem design note on why no shared module exists across these repositories.
 */
public final class PlayerLookup {

    /** The Floodgate default prefix on a Bedrock account's Java-side username. */
    private static final String FLOODGATE_PREFIX = ".";

    private PlayerLookup() {
    }

    /**
     * The usernames to try, in order, for a typed target name.
     *
     * @param typed the raw name argument
     * @return candidate usernames, most likely first; empty for a null or blank input
     */
    public static List<String> targetNameCandidates(String typed) {
        if (typed == null) {
            return List.of();
        }
        String trimmed = typed.trim();
        if (trimmed.isEmpty()) {
            return List.of();
        }
        if (trimmed.startsWith(FLOODGATE_PREFIX)) {
            return List.of(trimmed);
        }
        return List.of(trimmed, FLOODGATE_PREFIX + trimmed);
    }

    /**
     * The message shown when no online player matches {@code typed}.
     *
     * <p>Naming who <em>is</em> online turns a dead end into a usable correction: it is
     * the only way a Bedrock player discovers a Floodgate-prefixed username without
     * knowing Floodgate exists.
     *
     * @param typed       the name the sender typed
     * @param onlineNames the usernames currently online
     */
    public static String noSuchPlayerMessage(String typed, List<String> onlineNames) {
        if (onlineNames == null || onlineNames.isEmpty()) {
            return "No player matches '" + typed + "'; no players are online.";
        }
        return "No player matches '" + typed + "'. Online: " + String.join(", ", onlineNames);
    }

    /**
     * Resolves a typed name to an online player, trying each candidate exactly, then
     * case-insensitively.
     *
     * @param typed the raw name argument
     * @return the matching online player, or empty if none matches
     */
    public static Optional<Player> resolve(String typed) {
        List<String> candidates = targetNameCandidates(typed);
        for (String candidate : candidates) {
            Player player = Bukkit.getPlayerExact(candidate);
            if (player != null) {
                return Optional.of(player);
            }
        }
        // Last resort: a case-insensitive sweep, which also covers a server that has
        // reconfigured Floodgate's username prefix away from the default.
        for (String candidate : candidates) {
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (online.getName().equalsIgnoreCase(candidate)) {
                    return Optional.of(online);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * As {@link #resolve}, plus Bukkit's own partial-name matching as a final tier.
     *
     * <p>For call sites that previously used {@link Bukkit#getPlayer(String)}. That
     * method prefix-matches, so operators may already be typing partial names; dropping
     * to exact-only would be an unrequested regression on top of the Floodgate fix. The
     * partial tier runs last so an exact match always wins over a partial one.
     *
     * @param typed the raw name argument
     * @return the matching online player, or empty if none matches
     */
    public static Optional<Player> resolveAllowingPartial(String typed) {
        Optional<Player> exact = resolve(typed);
        if (exact.isPresent()) {
            return exact;
        }
        for (String candidate : targetNameCandidates(typed)) {
            Player player = Bukkit.getPlayer(candidate);
            if (player != null) {
                return Optional.of(player);
            }
        }
        return Optional.empty();
    }

    /** The names of every online player, for {@link #noSuchPlayerMessage}. */
    public static List<String> onlineNames() {
        List<String> names = new ArrayList<>();
        for (Player online : Bukkit.getOnlinePlayers()) {
            names.add(online.getName());
        }
        return names;
    }
}
