package de.wacodis.codede;

import de.wacodis.observer.model.CopernicusSubsetDefinition;
import de.wacodis.observer.model.DwdSubsetDefinition;
import de.wacodis.observer.model.WacodisJobDefinition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public class CodeDeJobFactoryTest {

    private static CodeDeJobFactory jobFactory;

    @BeforeAll
    public static void init() {
        jobFactory = new CodeDeJobFactory();
    }

    @Test
    @DisplayName("Test supportsJobDefinition should return true for supported SubsetDefinition")
    void testSupportsJobDefinitionForSupportedSubsetDefinitionReturnsTrue() {
        WacodisJobDefinition jobDef = new WacodisJobDefinition();
        jobDef.addInputsItem(new CopernicusSubsetDefinition());

        Assertions.assertTrue(jobFactory.supportsJobDefinition(jobDef));
    }

    @Test
    @DisplayName("Test supportsJobDefinition should return false for unsupported SubsetDefinition")
    void testSupportsJobDefinitionForUnsupportedSubsetDefinitioReturnsFalse() {
        WacodisJobDefinition jobDef = new WacodisJobDefinition();
        jobDef.addInputsItem(new DwdSubsetDefinition());

        Assertions.assertFalse(jobFactory.supportsJobDefinition(jobDef));
    }

    @Test
    void testBuildProductIdentifierWithoutProductType() {
        CopernicusSubsetDefinition subsetDef = new CopernicusSubsetDefinition();
        subsetDef.setSatellite(CopernicusSubsetDefinition.SatelliteEnum._2);
        subsetDef.setInstrument("MSI");
        subsetDef.setProductLevel("L2A");

        Assertions.assertEquals("EOP:CODE-DE:S2_MSI_L2A", jobFactory.buildProductIdentifier(subsetDef));
    }

    @Test
    void testBuildProductIdentifierWithProductType() {
        CopernicusSubsetDefinition subsetDef = new CopernicusSubsetDefinition();
        subsetDef.setSatellite(CopernicusSubsetDefinition.SatelliteEnum._3);
        subsetDef.setInstrument("SLSTR");
        subsetDef.setProductLevel("L1");
        subsetDef.setProductType("RBT");

        Assertions.assertEquals("EOP:CODE-DE:S3_SLSTR_L1_RBT", jobFactory.buildProductIdentifier(subsetDef));
    }
}
