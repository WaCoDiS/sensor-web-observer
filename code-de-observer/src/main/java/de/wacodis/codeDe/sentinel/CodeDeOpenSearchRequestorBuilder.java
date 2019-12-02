package de.wacodis.codeDe.sentinel;

import de.wacodis.codeDe.CodeDeJob;

public class CodeDeOpenSearchRequestorBuilder {

    public static final String SERVICE_URL = "https://catalog.code-de.org/opensearch/request/?";
    public static final String HTTP_ACCEPT = "httpAccept=application/atom%2Bxml";
    public static final String PARENT_IDENTIFIER_PREFIX = "EOP:CODE-DE:";

    public String buildGetRequestUrl(CodeDeRequestParams params){
        // parentIdentifier
        String parentIdentifier = CodeDeJob.PARENT_IDENTIFIER_KEY + "=" + PARENT_IDENTIFIER_PREFIX + params.getParentIdentifier();
        // dates
        String startDate = CodeDeJob.START_DATE_KEY + "=" + params.getStartDate().toString();
        String endDate = CodeDeJob.END_DATE_KEY + "=" + params.getEndDate().toString();
        // bbox
        String bboxContent = params.getBbox().toString();
        bboxContent.replace("[", "");
        bboxContent.replace("]", "");
        bboxContent.replace(" ", "");
        String bbox = CodeDeJob.BBOX_KEY + "=" + bboxContent;
        // cloud coverage
        String cloudCoverContent = params.getCloudCover().toString();
        cloudCoverContent.replace(" ", "");
        String cloudCover = CodeDeJob.CLOUD_COVER_KEY + cloudCoverContent;
        // put together
        String getRequestUrl = SERVICE_URL
                + HTTP_ACCEPT + "&"
                + parentIdentifier + "&"
                + startDate + "&"
                + endDate + "&"
                + bbox + "&"
                + cloudCover;
        return getRequestUrl;
    }

}
