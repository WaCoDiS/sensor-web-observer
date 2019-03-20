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

package de.wacodis.sentinel.apihub.decode;

import com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl;
import de.wacodis.sentinel.apihub.ApiHubException;
import de.wacodis.sentinel.apihub.ProductMetadata;
import de.wacodis.sentinel.apihub.SearchResult;
import java.io.IOException;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.hamcrest.CoreMatchers;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author Matthes Rieke (m.rieke@52north.org)
 */
public class FeedDecoderTest {

    @Test
    public void testDecoding() throws ParserConfigurationException, SAXException, IOException, ApiHubException {
        FeedDecoder decoder = new FeedDecoder();
        Document doc = readXml("/os-response.xml");
        SearchResult result = decoder.parse(doc);
        
        Assert.assertThat(result.getTotalResults(), CoreMatchers.is(96));
        Assert.assertThat(result.getItemsPerPage(), CoreMatchers.is(10));
        Assert.assertThat(result.getStartIndex(), CoreMatchers.is(0));
        
        List<ProductMetadata> p = result.getProducts();
        Assert.assertThat(p.size(), CoreMatchers.is(10));
        ProductMetadata p1 = p.get(0);
        Assert.assertThat(p1.getId(), CoreMatchers.equalTo("62cb5ca1-b1c5-4ae9-9ea2-f13f90300490"));
        Assert.assertThat(p1.getTitle(), CoreMatchers.equalTo("S2A_MSIL2A_20190130T103251_N0211_R108_T31UGS_20190130T115213"));
        Assert.assertThat(p1.getInstrumentShortName(), CoreMatchers.equalTo("MSI"));
        Assert.assertThat(p1.getIngestionDate(), CoreMatchers.equalTo(new DateTime("2019-01-30T20:05:43.4Z")));
        Assert.assertThat(p1.getCloudCoverPercentage(), CoreMatchers.equalTo(86.171287));
    }

    private Document readXml(String osresponsexml) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory fac = new DocumentBuilderFactoryImpl();
        fac.setNamespaceAware(true);
        return fac.newDocumentBuilder().parse(getClass().getResourceAsStream(osresponsexml));
    }
    
}
