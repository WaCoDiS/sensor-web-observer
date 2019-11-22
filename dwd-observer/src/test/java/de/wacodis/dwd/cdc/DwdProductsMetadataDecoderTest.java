package de.wacodis.dwd.cdc;

import de.wacodis.dwd.cdc.model.DwdProductsMetadata;
import de.wacodis.dwd.cdc.model.DwdProductsMetadataDecoder;
import de.wacodis.dwd.cdc.model.Envelope;
import de.wacodis.observer.model.DwdDataEnvelope;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class DwdProductsMetadataDecoderTest {

    private static DwdProductsMetadata metadata;
    private static DwdProductsMetadataDecoder decoder;

    @BeforeAll
    static void init() {
        metadata = new DwdProductsMetadata();
        metadata.setStartDate(DateTime.parse("2019-04-24T01:00:00Z", DwdWfsRequestorBuilder.FORMATTER));
        metadata.setEndDate(DateTime.parse("2019-04-25T10:00:00Z", DwdWfsRequestorBuilder.FORMATTER));
        Envelope envelope = new Envelope();
        envelope.setMinLon(6.966f);
        envelope.setMinLat(51.402f);
        envelope.setMaxLon(6.969f);
        envelope.setMaxLat(51.405f);
        metadata.setEnvelope(envelope);
        metadata.setLayerName("CDC:VGSL_FX_MN003");
        metadata.setServiceUrl("https://cdc.dwd.de:443/geoserver/CDC/wfs?");
        metadata.setParameter("Windspitzen");
        decoder = new DwdProductsMetadataDecoder();
    }

    @Test
    void testDecodeMetadata() {
        DwdDataEnvelope dataEnvelope = decoder.decode(metadata);

        Assertions.assertEquals(metadata.getLayerName(), dataEnvelope.getLayerName());
        Assertions.assertEquals(metadata.getParameter(), dataEnvelope.getParameter());
        Assertions.assertEquals(metadata.getStartDate(), dataEnvelope.getTimeFrame().getStartTime());
        Assertions.assertEquals(metadata.getEndDate(), dataEnvelope.getTimeFrame().getEndTime());
        Assertions.assertEquals(metadata.getServiceUrl(), dataEnvelope.getServiceUrl());
        Assertions.assertEquals(metadata.getEnvelope().getMinLon(), dataEnvelope.getAreaOfInterest().getExtent().get(0));
        Assertions.assertEquals(metadata.getEnvelope().getMinLat(), dataEnvelope.getAreaOfInterest().getExtent().get(1));
        Assertions.assertEquals(metadata.getEnvelope().getMaxLon(), dataEnvelope.getAreaOfInterest().getExtent().get(2));
        Assertions.assertEquals(metadata.getEnvelope().getMaxLat(), dataEnvelope.getAreaOfInterest().getExtent().get(3));
    }
}
