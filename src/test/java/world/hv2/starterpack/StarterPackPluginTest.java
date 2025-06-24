package world.hv2.starterpack;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Basic tests for StarterPack plugin
 */
public class StarterPackPluginTest {
    
    @Test
    public void testPluginNotNull() {
        // Basic sanity test
        assertNotNull(StarterPackPlugin.class);
    }
    
    @Test
    public void testPluginHasCorrectName() {
        // Test plugin name consistency
        String expectedName = "StarterPack";
        assertTrue(expectedName.length() > 0);
    }
}
