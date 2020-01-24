package de.wacodis.codeDe.sentinel;

import de.wacodis.codeDe.CodeDeJob;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Helps building the CODE-DE OpenSeaurch request for certain request parameters.
 *
 * @author <a href="mailto:tim.kurowski@hs-bochum.de">Tim Kurowski</a>
 * @author <a href="mailto:christian.koert@hs-bochum.de">Christian Koert</a>
 */

public class CodeDeOpenSearchRequestorBuilder {

    public static final String SERVICE_URL = "https://catalog.code-de.org/opensearch/request/?";
    public static final String HTTP_ACCEPT = "httpAccept=application/atom%2Bxml";
    public static final String PARENT_IDENTIFIER_PREFIX = "EOP:CODE-DE:";
    public static final DateTimeFormatter FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
    public static final String MAXIMUM_RECORDS= "50";
    public static final String RECORD_SCHEMA = "om";
    public static String START_PAGE ="1";

    /**
     * Builds the String containing the URL of the GET request.
     * @param params all necessary parameters for the OpenSearch request
     * @return url of the GET request
     */

    public static String buildGetRequestUrl(CodeDeRequestParams params){
        // parentIdentifier
        String parentIdentifier = CodeDeJob.PARENT_IDENTIFIER_KEY + "=" + PARENT_IDENTIFIER_PREFIX + params.getParentIdentifier();
        // dates
        String startDate = CodeDeJob.START_DATE_KEY + "=" + params.getStartDate().toString(FORMATTER);
        String endDate = CodeDeJob.END_DATE_KEY + "=" + params.getEndDate().toString(FORMATTER);
        // bbox
        String bboxContent = params.getBbox().toString();
        bboxContent = bboxContent.replace("[", "");
        bboxContent =  bboxContent.replace("]", "");
        bboxContent = bboxContent.replace(" ", "");
        String bbox = CodeDeJob.BBOX_KEY + "=" + bboxContent;
        // cloud coverage
        String cloudCoverContent = params.getCloudCover().toString();
        cloudCoverContent = cloudCoverContent.replace(" ", "");
        String cloudCover = CodeDeJob.CLOUD_COVER_KEY + "=" + cloudCoverContent;

        // maximum records
        String maxRecords = CodeDeJob.MAXIMUM_RECORDS_KEY + "=" + MAXIMUM_RECORDS;
        // record schema
        String recordSchema = CodeDeJob.RECORD_SCHEMA_KEY + "=" + RECORD_SCHEMA;
        // start page
        String startPage = CodeDeJob.START_PAGE_KEY + "=" + START_PAGE;

        // put together
        String getRequestUrl = SERVICE_URL
                //+ HTTP_ACCEPT + "&"
                + parentIdentifier + "&"
                + startDate + "&"
                + endDate + "&"
                + bbox + "&"
                + cloudCover + "&"
                + maxRecords + "&"
                + recordSchema + "&"
                + startPage;
        return getRequestUrl;
    }

}
