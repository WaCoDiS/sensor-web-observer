package de.wacodis.codede;

import de.wacodis.observer.config.ExecutionIntervalConfig;
import de.wacodis.observer.core.JobFactory;
import de.wacodis.observer.model.AbstractSubsetDefinition;
import de.wacodis.observer.model.CopernicusSubsetDefinition;
import de.wacodis.observer.model.WacodisJobDefinition;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author <a href="mailto:tim.kurowski@hs-bochum.de">Tim Kurowski</a>
 * @author <a href="mailto:christian.koert@hs-bochum.de">Christian Koert</a>
 */
@Component
public class CodeDeJobFactory implements JobFactory {
    private final static String PRODUCT_IDENTIFIER_PREFIX = "EOP:CODE-DE:";
    private final static Map<String, String> SATELLTIE_MAPPING;

    static {
        Map<String, String> map = new HashMap<>();
        map.put("sentinel-1", "S1");
        map.put("sentinel-2", "S2");
        map.put("sentinel-3", "S3");
        SATELLTIE_MAPPING = Collections.unmodifiableMap(map);
    }

    private static final Logger LOG = LoggerFactory.getLogger(CodeDeJobFactory.class);

    @Autowired
    private ExecutionIntervalConfig intervalConfig;

    @Override
    public boolean supportsJobDefinition(WacodisJobDefinition job) {
        Optional<AbstractSubsetDefinition> def = job.getInputs().stream()
                .filter(in -> in instanceof CopernicusSubsetDefinition).findAny();

        return def.isPresent();
    }

    @Override
    public JobDetail initializeJob(WacodisJobDefinition job, JobDataMap data) {
        LOG.info("Preparing CodeDeJob JobDetail");

        Optional<AbstractSubsetDefinition> defOpt = job.getInputs().stream()
                .filter((i -> i instanceof CopernicusSubsetDefinition)).findFirst();

        // this should always be the case
        if (defOpt.isPresent()) {
            CopernicusSubsetDefinition def = (CopernicusSubsetDefinition) defOpt.get();

            // Put all required request parameters into JobDataMap

            // data.put(DwdJob.LAYER_NAME_KEY, def.getLayerName());
            data.put(CodeDeJob.PRODUCT_IDENTIFIER, buildProductIdentifier(def));
            data.put(CodeDeJob.TEMPORAL_COVERAGE_KEY, job.getTemporalCoverage().getDuration());
            data.put(CodeDeJob.CLOUD_COVER_KEY, def.getMaximumCloudCoverage());
            data.put(CodeDeJob.LATEST_REQUEST_END_DATE, null);
            String extent = job.getAreaOfInterest().getExtent().get(0) + " "
                    + job.getAreaOfInterest().getExtent().get(1) + ","
                    + job.getAreaOfInterest().getExtent().get(2) + " "
                    + job.getAreaOfInterest().getExtent().get(3);
            data.put(CodeDeJob.BBOX_KEY, extent);    // e.g. "52.0478 6.0124,52.5687 7.1420"
            data.put(CodeDeJob.EXECUTION_INTERVAL_KEY, intervalConfig.getSentinel());

        }
        return JobBuilder.newJob(CodeDeJob.class).withIdentity(job.getId().toString(), job.getName()).usingJobData(data)
                .build();
    }

    protected String buildProductIdentifier(CopernicusSubsetDefinition def) {
        String satelliteAbr = SATELLTIE_MAPPING.get(def.getSatellite().toString());
        String productId = PRODUCT_IDENTIFIER_PREFIX
                + String.join("_", satelliteAbr, def.getInstrument(), def.getProductLevel());

        return def.getProductType() != null && !def.getProductType().isEmpty()
                ? String.join("_", productId, def.getProductType())
                : productId;
    }
}
