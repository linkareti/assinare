package com.linkare.assinare.server.test.resources;

import static eu.europa.esig.dss.enumerations.DigestAlgorithm.*;
import static java.net.InetAddress.getLoopbackAddress;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.SignerInfoGenerator;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DigestCalculator;
import org.bouncycastle.operator.DigestCalculatorProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.tsp.TSPException;
import org.bouncycastle.tsp.TimeStampRequest;
import org.bouncycastle.tsp.TimeStampResponse;
import org.bouncycastle.tsp.TimeStampResponseGenerator;
import org.bouncycastle.tsp.TimeStampTokenGenerator;

import com.linkare.assinare.test.DummyCrypto;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

/**
 *
 * @author bnazare
 */
public class TSATestResource implements QuarkusTestResourceLifecycleManager {

    private static final ASN1ObjectIdentifier TSA_POLICY = new ASN1ObjectIdentifier("0.4.1.1.1");
    private static final PrivateKey PRIVATE_KEY = DummyCrypto.getTsaPrivKey();
    private static final X509Certificate CERTIFICATE = DummyCrypto.getTsaCert();
    private static final AlgorithmIdentifier CERT_DIGEST_ALGO = new DefaultDigestAlgorithmIdentifierFinder().find("SHA-256");
    private static final String SIGNATURE_ALGO = "SHA384withRSA";
    private static final Set<String> ACCEPTED_ALGORITHMS = Set.of(
            SHA1.getOid(),
            SHA224.getOid(),
            SHA256.getOid(),
            SHA384.getOid(),
            SHA512.getOid(),
            SHA3_224.getOid(),
            SHA3_256.getOid(),
            SHA3_384.getOid(),
            SHA3_512.getOid()
    );

    private HttpServer server;

    @Override
    public Map<String, String> start() {
        try {
            server = HttpServer.create(new InetSocketAddress(getLoopbackAddress(), 0), 0);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        TimeStampResponseGenerator tsrGen;
        try {
            DigestCalculatorProvider dcProv = new JcaDigestCalculatorProviderBuilder().build();
            DigestCalculator dgCalc = dcProv.get(CERT_DIGEST_ALGO);
            ContentSigner signer = new JcaContentSignerBuilder(SIGNATURE_ALGO).build(PRIVATE_KEY);
            SignerInfoGenerator siGen = new JcaSignerInfoGeneratorBuilder(dcProv).build(signer, CERTIFICATE);

            TimeStampTokenGenerator tstGen = new TimeStampTokenGenerator(siGen, dgCalc, TSA_POLICY);
            tstGen.addCertificates(new JcaCertStore(List.of(CERTIFICATE)));
            tsrGen = new TimeStampResponseGenerator(tstGen, ACCEPTED_ALGORITHMS);
        } catch (IllegalArgumentException | CertificateEncodingException | OperatorCreationException | TSPException ex) {
            throw new RuntimeException(ex);
        }

        server.createContext("/tsa", (HttpExchange exchange) -> {
            try (OutputStream responseBody = exchange.getResponseBody()) {
                System.out.println("doing le timestamping: " + CERT_DIGEST_ALGO);

                TimeStampRequest tsReq = new TimeStampRequest(exchange.getRequestBody());
                TimeStampResponse tsResp = tsrGen.generate(tsReq, generateSerialNumber(), new Date());

                byte[] responseData = tsResp.getEncoded();
                exchange.sendResponseHeaders(200, responseData.length);
                responseBody.write(responseData);
            } catch (IOException | TSPException ex) {
                Logger.getLogger(TSATestResource.class.getName()).log(Level.SEVERE, null, ex);
                exchange.sendResponseHeaders(500, 0);
            }
        });

        server.start();

        return Map.of("asn.signature.pdf.tsa-url", "http://localhost:" + server.getAddress().getPort() + "/tsa");
    }

    private static BigInteger generateSerialNumber() {
        return new BigInteger(32, new SecureRandom());
    }

    @Override
    public void stop() {
        server.stop(1);
    }

}
