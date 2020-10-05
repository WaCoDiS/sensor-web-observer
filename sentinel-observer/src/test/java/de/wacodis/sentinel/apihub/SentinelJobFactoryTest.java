package de.wacodis.sentinel.apihub;

import de.wacodis.observer.model.AbstractSubsetDefinition;
import de.wacodis.observer.model.CopernicusSubsetDefinition;
import de.wacodis.sentinel.SentinelJobFactory;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public class SentinelJobFactoryTest {

    @Test
    public void testGenerateSubsetSpecificIdentifier(){
        CopernicusSubsetDefinition def = new CopernicusSubsetDefinition();
        def.setSourceType(AbstractSubsetDefinition.SourceTypeEnum.COPERNICUSSUBSETDEFINITION);
        def.setSatellite(CopernicusSubsetDefinition.SatelliteEnum._2);

        SentinelJobFactory jobFactory = new SentinelJobFactory();
        String identifier = jobFactory.generateSubsetSpecificIdentifier(def);

        Assert.assertThat(identifier, CoreMatchers.equalTo("CopernicusSubsetDefinition_sentinel-2"));
    }
}
