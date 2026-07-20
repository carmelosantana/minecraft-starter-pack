package world.hv2.starterpack.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Exercises the pure parts of {@link PlayerLookup} -- the candidate-name list and the
 * failure message -- as plain functions over strings, with no Bukkit types and no
 * running server.
 *
 * <p>{@link PlayerLookup#resolve} is deliberately not covered here: it calls
 * {@code Bukkit.getPlayerExact} and {@code Bukkit.getOnlinePlayers}, which need a live
 * server, and {@code Player} cannot be constructed headlessly. The decisions worth
 * pinning were extracted into the two static methods below precisely so that the
 * untestable remainder is thin Bukkit glue.
 */
class PlayerLookupTest {

    /**
     * Resolving a typed target name.
     *
     * <p>A Bedrock account joins through Floodgate under a {@code .}-prefixed username,
     * so an operator typing the bare name they see in chat matched nothing and got
     * "not found or not online" for a player standing in front of them. These cases pin
     * the candidate list and the failure message that replaced it.
     */
    @Nested
    @DisplayName("target resolution")
    class TargetResolution {

        @Test
        @DisplayName("a bare name also tries the Floodgate '.' prefix")
        void bareNameTriesFloodgatePrefix() {
            assertEquals(List.of("carm", ".carm"), PlayerLookup.targetNameCandidates("carm"));
        }

        @Test
        @DisplayName("an already-prefixed name is not prefixed twice")
        void prefixedNameIsNotDoubled() {
            assertEquals(List.of(".acarm"), PlayerLookup.targetNameCandidates(".acarm"));
        }

        @Test
        @DisplayName("surrounding whitespace is trimmed")
        void whitespaceIsTrimmed() {
            assertEquals(List.of("carm", ".carm"), PlayerLookup.targetNameCandidates("  carm  "));
        }

        @Test
        @DisplayName("null and blank yield no candidates")
        void nullAndBlankYieldNothing() {
            assertTrue(PlayerLookup.targetNameCandidates(null).isEmpty());
            assertTrue(PlayerLookup.targetNameCandidates("   ").isEmpty());
        }

        @Test
        @DisplayName("the failure message lists who is actually online")
        void failureMessageListsOnlinePlayers() {
            String message = PlayerLookup.noSuchPlayerMessage("carm", List.of(".acarm", "Steve"));
            assertTrue(message.contains("carm"), message);
            assertTrue(message.contains(".acarm"), message);
            assertTrue(message.contains("Steve"), message);
        }

        @Test
        @DisplayName("the failure message says so plainly when nobody is online")
        void failureMessageWhenNobodyOnline() {
            String message = PlayerLookup.noSuchPlayerMessage("carm", List.of());
            assertTrue(message.contains("carm"), message);
            assertTrue(message.toLowerCase(Locale.ROOT).contains("no players"), message);
        }
    }
}
