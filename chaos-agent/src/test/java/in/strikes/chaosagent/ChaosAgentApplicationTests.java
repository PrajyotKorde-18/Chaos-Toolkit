package in.strikes.chaosagent;

import in.strikes.chaosagent.config.ChaosAgentProperties;
import in.strikes.chaosagent.core.ChaosFaultRegistry;
import in.strikes.chaosagent.model.FaultConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChaosAgentApplicationTests {

    @Test
    void testRegistryKillSwitchAndDefaults() {
        ChaosAgentProperties properties = new ChaosAgentProperties();
        properties.setServiceName("test-service");
        properties.setEnabled(true);

        ChaosFaultRegistry registry = new ChaosFaultRegistry(properties);
        assertFalse(registry.isKillSwitchEngaged());

        FaultConfig config = registry.getActiveFault("non-existent");
        assertNotNull(config);
        assertEquals("non-existent", config.getFaultId());

        registry.engageKillSwitch();
        assertTrue(registry.isKillSwitchEngaged());

        registry.releaseKillSwitch();
        assertFalse(registry.isKillSwitchEngaged());
    }
}
