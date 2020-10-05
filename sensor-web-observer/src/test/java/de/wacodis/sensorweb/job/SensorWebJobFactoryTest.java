package de.wacodis.sensorweb.job;

import de.wacodis.observer.model.AbstractSubsetDefinition;
import de.wacodis.observer.model.CopernicusSubsetDefinition;
import de.wacodis.observer.model.SensorWebSubsetDefinition;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public class SensorWebJobFactoryTest {

    @Test
    public void testGenerateSubsetSpecificIdentifier(){
        SensorWebSubsetDefinition def = new SensorWebSubsetDefinition();
        def.setSourceType(AbstractSubsetDefinition.SourceTypeEnum.SENSORWEBSUBSETDEFINITION);
        def.setProcedure("testProcedure");
        def.setObservedProperty("testProperty");
        def.setOffering("testOffering");
        def.setFeatureOfInterest("testFOI");
        def.setServiceUrl("testService");

        SensorWebJobFactory jobFactory = new SensorWebJobFactory();
        String identifier = jobFactory.generateSubsetSpecificIdentifier(def);

        Assert.assertEquals("SensorWebSubsetDefinition_[testProcedure]_[testProperty]_[testOffering]_[testFOI]_[testService]", identifier);
    }

}
