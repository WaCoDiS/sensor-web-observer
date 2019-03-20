/*
 * Copyright 2019 WaCoDiS Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.wacodis.sentinel;

import de.wacodis.sentinel.apihub.ApiHubClient;
import de.wacodis.api.model.AbstractDataEnvelopeAreaOfInterest;
import de.wacodis.sentinel.apihub.ProductMetadata;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.joda.time.DateTime;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;

/**
 * 
 * @author matthes rieke
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class SentinelJob implements Job {

    public static String MAX_CLOUD_COVERAGE_KEY = "maximumCloudCoverage";
    public static String PLATFORM_KEY = "platformName";
    public static String PREVIOUS_DAYS_KEY = "previousDays";
    public static String LAST_LATEST_PRODUCT_KEY = "lastLatestProduct";
    public static String LAST_EXECUTION_PRODUCT_IDS_KEY = "lastExecutionProductIds";

    @Override
    public void execute(JobExecutionContext ctxt) throws JobExecutionException {
        JobDataMap dataMap = ctxt.getJobDetail().getJobDataMap();
        
        /** possibly stored in previous execution */
        Object lastLatest = dataMap.get(LAST_LATEST_PRODUCT_KEY);
        DateTime lastLatestDate = null;
        if (lastLatest != null && lastLatest instanceof DateTime) {
            lastLatestDate = (DateTime) lastLatest;
        }
        
        /** defined on the job level */
        Object maxCloud = dataMap.get(MAX_CLOUD_COVERAGE_KEY);
        Double maxCloudPercentage = null;
        if (maxCloud != null && maxCloud instanceof Double) {
            maxCloudPercentage = (Double) maxCloud;
        }
        
        /** defined on the job level */
        Object platform = dataMap.get(PLATFORM_KEY);
        String platformName = null;
        if (maxCloud != null && platform instanceof String) {
            platformName = (String) platform;
        }
        
        /** TODO: temporal coverage not yet clear in business logic */
        
        /** defined on the job level **/
        Object aoi = dataMap.get("areaOfInterest");
        AbstractDataEnvelopeAreaOfInterest areaOfInterest = null;
        if (aoi != null && aoi instanceof AbstractDataEnvelopeAreaOfInterest) {
            areaOfInterest = (AbstractDataEnvelopeAreaOfInterest) aoi;
        }
        
        /**
         * retrieve new products from the API
         */
        List<ProductMetadata> newProductCandidates = new ApiHubClient()
                .requestProducts(lastLatestDate, maxCloudPercentage, platformName, areaOfInterest);
        
        /** filter out duplicates from previous executions **/
        Object lastIds = dataMap.get(LAST_EXECUTION_PRODUCT_IDS_KEY);
        List<ProductMetadata> newProducts;
        if (lastIds != null && lastIds instanceof Set) {
            Set<String> lastIdList = (Set<String>) lastIds;
            newProducts = newProductCandidates.stream()
                    .filter(p -> !lastIdList.contains(p.getId()))
                    .collect(Collectors.toList());
        } else {
            newProducts = newProductCandidates;
        }
        
        /** sort by beginPosition so we can uses this in upcoming executions */
        newProducts.sort((ProductMetadata pm1, ProductMetadata pm2) -> {
            return pm1.getBeginPosition().isBefore(pm2.getBeginPosition()) ? 1 : -1;
        });
        
        /** store the last begin position */
        dataMap.put(LAST_LATEST_PRODUCT_KEY, newProducts.get(newProducts.size() - 1).getBeginPosition());

        /** store the last IDs in the execution environment */
        dataMap.put(LAST_EXECUTION_PRODUCT_IDS_KEY, newProducts.stream()
                .map(p -> p.getId())
                .distinct()
                .collect(Collectors.toSet()));
    }
    
}
