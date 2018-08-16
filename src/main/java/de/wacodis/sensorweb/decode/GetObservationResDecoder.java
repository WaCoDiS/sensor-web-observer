package de.wacodis.sensorweb.decode;

import java.util.ArrayList;
import java.util.List;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.n52.janmayen.Producer;
import org.n52.janmayen.Producers;
import org.n52.shetland.ogc.om.ObservationStream;
import org.n52.shetland.ogc.om.OmObservation;
import org.n52.shetland.ogc.ows.exception.OwsExceptionReport;
import org.n52.shetland.ogc.sos.response.GetObservationResponse;
import org.n52.svalbard.decode.AbstractXmlDecoder;
import org.n52.svalbard.decode.Decoder;
import org.n52.svalbard.decode.DecoderRepository;
import org.n52.svalbard.decode.GetObservationResponseDocumentDecoder;
import org.n52.svalbard.decode.GmlDecoderv321;
import org.n52.svalbard.decode.OmDecoderv20;
import org.n52.svalbard.decode.exception.DecodingException;
import org.w3.x2003.x05.soapEnvelope.Body;
import org.w3.x2003.x05.soapEnvelope.Envelope;
import org.w3.x2003.x05.soapEnvelope.EnvelopeDocument;
import net.opengis.sos.x20.GetObservationResponseDocument;

public class GetObservationResDecoder {

	public List<OmObservation> decode(String soapResponse)
			throws XmlException, DecodingException, OwsExceptionReport {

		// load SOAPResponse via XMLBeans
		EnvelopeDocument doc = EnvelopeDocument.Factory.parse(soapResponse);
		Envelope soapEnv = doc.getEnvelope();
		Body soapBody = soapEnv.getBody();
		GetObservationResponseDocument getObsResDoc = GetObservationResponseDocument.Factory.parse(soapBody.xmlText());

		// use ResponseDecoder-Implementation(s)
		GetObservationResponseDocumentDecoder getObsDecoder = new GetObservationResponseDocumentDecoder();
		// set up decoders
		prepareDecoders(getObsDecoder, new OmDecoderv20(), new GmlDecoderv321());

		// decode
		GetObservationResponse getObsRes = getObsDecoder.decode(getObsResDoc);
		
		ObservationStream obsDataCollection = getObsRes.getObservationCollection();
		List<OmObservation> omObsList = new ArrayList<>();
		while (obsDataCollection.hasNext()) {
			OmObservation omObs = obsDataCollection.next();
			omObsList.add(omObs);
		}
		return omObsList;
	}
	
	private void prepareDecoders(AbstractXmlDecoder<?, ?>... decoders) {
		Producer<XmlOptions> xoptions = Producers.forInstance(new XmlOptions().setSavePrettyPrint());
		DecoderRepository decoderRepository = new DecoderRepository();
		List<Decoder<?, ?>> decoderList = new ArrayList<Decoder<?, ?>>();
		for(AbstractXmlDecoder<?, ?> d : decoders) {
			// http://www.opengis.net/om/2.0 - xmlOptions is mandatory for XML encoding
			d.setXmlOptions(xoptions);
			// decoder repository is used to search for (internally used) decoders
			// we need to add the decoders and initialize the repository
			decoderList.add(d);
			d.setDecoderRepository(decoderRepository);
		}
		decoderRepository.setDecoders(decoderList);
		decoderRepository.init();
	}

	
}
