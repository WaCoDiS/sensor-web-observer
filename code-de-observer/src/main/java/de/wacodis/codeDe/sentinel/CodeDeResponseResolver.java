package de.wacodis.codeDe.sentinel;

import de.wacodis.codeDe.CodeDeJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.ParserConfigurationException;
import java.util.ArrayList;
import java.util.List;

public class CodeDeResponseResolver {

    private static final String ENTRY_TAG = "entry";
    private static final String LINK_TAG = "link";
    private static final String TITLE_ATTRIBUTE = "title";
    private static final String TITLE_VALUE = "Download";
    private static final String HYPER_REFERENCE = "href";
    private static final Logger LOG = LoggerFactory.getLogger(CodeDeJob.class);


    public List<String> getDownloadLink(Document xmlDoc) throws ParserConfigurationException {
        LOG.debug("Resolve TypeName out of GetCapabilities Document");

        List<String> donwloadLinks = new ArrayList<String>();

        // search all FeatureType elements
        NodeList entryNodes = xmlDoc.getElementsByTagName(ENTRY_TAG);
        for (int i = 0; i < entryNodes.getLength(); i++) {
            // check content of childnodes <link> of every <entry>
            Element entry = (Element) entryNodes.item(i);
            NodeList linkNodes = entry.getElementsByTagName(LINK_TAG);

            for (int k = 0; k < linkNodes.getLength(); k++) {
                // check attributes of every <link>
                Element link = (Element) linkNodes.item(i);
                String attributeValue = link.getAttribute(TITLE_ATTRIBUTE);
                if(attributeValue.equals(TITLE_VALUE)){
                    String downloadLink = link.getAttribute(HYPER_REFERENCE);
                    donwloadLinks.add(downloadLink);
                }

            }
        }
            return donwloadLinks;
    }
}
