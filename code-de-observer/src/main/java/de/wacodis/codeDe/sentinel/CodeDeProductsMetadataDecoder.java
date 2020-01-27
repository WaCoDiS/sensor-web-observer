package de.wacodis.codeDe.sentinel;

import de.wacodis.observer.model.AbstractDataEnvelope;
import de.wacodis.observer.model.AbstractDataEnvelopeAreaOfInterest;
import de.wacodis.observer.model.AbstractDataEnvelopeTimeFrame;
import de.wacodis.observer.model.CopernicusDataEnvelope;

/**
 * Decodes CodeDeProductsMetadata into a CopernicusDataEnvelope
 *
 * @author <a href="mailto:tim.kurowski@hs-bochum.de">Tim Kurowski</a>
 * @author <a href="mailto:christian.koert@hs-bochum.de">Christian Koert</a>
 */
public class CodeDeProductsMetadataDecoder {

    /**
     *delivers the content of a CodeDeProductsMetadata-object into a dataEnvelope
     * @param metadata object containing the metadata informations of a code de request
     * @return
     */
    public static CopernicusDataEnvelope decode(CodeDeProductsMetadata metadata){

        // create objects
        CopernicusDataEnvelope copDE = new CopernicusDataEnvelope();

        // datasetId, satellite, cloudCoverage, portal, sourceType
        copDE.setDatasetId(metadata.getDatasetId());
        //copDE.setSatellite();
        copDE.setCloudCoverage(metadata.getCloudCover());
        copDE.setPortal(CopernicusDataEnvelope.PortalEnum.CODE_DE);
        copDE.setSourceType(AbstractDataEnvelope.SourceTypeEnum.COPERNICUSDATAENVELOPE);
        // extent
        AbstractDataEnvelopeAreaOfInterest extent = new AbstractDataEnvelopeAreaOfInterest();
        extent.extent(metadata.getAreaOfInterest());
        copDE.setAreaOfInterest(extent);
        // timeframe
        AbstractDataEnvelopeTimeFrame timeframe = new AbstractDataEnvelopeTimeFrame();
        timeframe.setStartTime(metadata.getStartDate());
        timeframe.setEndTime(metadata.getEndDate());
        copDE.setTimeFrame(timeframe);

        return copDE;
    }
}
