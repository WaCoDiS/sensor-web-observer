package de.wacodis.dwd;

import de.wacodis.observer.model.AbstractSubsetDefinition;
import de.wacodis.observer.model.DwdSubsetDefinition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public class DwdJobFactoryTest {

    @Test
    public void testGenerateSubsetSpecificIdentifier(){
        DwdSubsetDefinition def = new DwdSubsetDefinition();
        def.setSourceType(AbstractSubsetDefinition.SourceTypeEnum.DWDSUBSETDEFINITION);
        def.setServiceUrl("https://cdc.dwd.de:443/geoserver/CDC/wfs?");
        def.setLayerName("FX_MN003");

        DwdJobFactory jobFactory = new DwdJobFactory();
        String identifier = jobFactory.generateSubsetSpecificIdentifier(def);

        Assertions.assertEquals("DwdSubsetDefinition_[https://cdc.dwd.de:443/geoserver/CDC/wfs?]_[FX_MN003]", identifier);
    }
}
