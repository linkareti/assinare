package com.linkare.assinare.sign.dss;

import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.model.DSSException;
import eu.europa.esig.dss.model.TimestampBinary;
import eu.europa.esig.dss.service.tsp.OnlineTSPSource;
import eu.europa.esig.dss.spi.client.http.DataLoader;

/**
 *
 * @author bnazare
 */
public class AssinareTSPSource extends OnlineTSPSource {

    private static final long serialVersionUID = 497424830919558967L;

    public AssinareTSPSource() {
    }

    public AssinareTSPSource(String tspServer) {
        super(tspServer);
    }

    public AssinareTSPSource(String tspServer, DataLoader dataLoader) {
        super(tspServer, dataLoader);
    }

    @Override
    public TimestampBinary getTimeStampResponse(DigestAlgorithm digestAlgorithm, byte[] digest) throws DSSException {
        try {
            return super.getTimeStampResponse(digestAlgorithm, digest);
        } catch (RuntimeException ex) {
            // the method from super class throws not only DSSException but also
            // NPEs on connection errors
            throw new TSPDSSException("Erro de comunicação com a TSA", ex);
        }
    }

}
