//package com.linkare.assinare.server.nativeimage.substitution;
//
///**
// * This file contains several substitutions for classes that in one way or
// * another carelessly make use of other classes not on the classpath. This is
// * usually OK in JVM mode if such code is ever accessed in runtime. Native
// * compilations however will outright fail when encountering such situations.
// *
// * @author bnazare
// */
//interface CXFIncompleteClasspathSubstitutions {
//
//    static final String NOT_SUPPORTED_IN_NATIVE_BUILD = "Not supported in native build";
//
//}
//
//@TargetClass(AbstractSTSClient.class)
//final class Target_org_apache_cxf_ws_security_trust_AbstractSTSClient {
//
//    @Alias
//    @SuppressWarnings("NonConstantLogger")
//    private static Logger LOG;
//
//    @Alias
//    protected Bus bus;
//
//    @Alias
//    protected Client client;
//
//    @Alias
//    protected String location;
//
//    @Alias
//    protected String wsdlLocation;
//
//    @Alias
//    protected QName serviceName;
//
//    @Alias
//    protected QName endpointName;
//
//    @Alias
//    protected native String findMEXLocation(EndpointReferenceType ref, boolean useEPRWSAAddrAsMEXLocation);
//
//    /**
//     * Substitutes {@link AbstractSTSClient#configureViaEPR}. The original
//     * method, under certain circunstances, makes use of classes from package
//     * {@code org.apache.cxf.ws.mex} which is not on the classpath. This
//     * implementation removes the entire offending block and replaces it with
//     * throwing an unchecked exception. This should not be a problem as we do
//     * not need WS-MetadataExchange support.
//     *
//     * @param ref
//     * @param useEPRWSAAddrAsMEXLocation
//     */
//    @Substitute
//    @SuppressWarnings("LoggerStringConcat")
//    public void configureViaEPR(EndpointReferenceType ref, boolean useEPRWSAAddrAsMEXLocation) {
//        if (client != null) {
//            return;
//        }
//        location = EndpointReferenceUtils.getAddress(ref);
//        if (location != null) {
//            location = location.trim();
//        }
//        if (LOG.isLoggable(Level.FINE)) {
//            LOG.fine("EPR address: " + location);
//        }
//
//        final QName sName = EndpointReferenceUtils.getServiceName(ref, bus);
//        if (sName != null) {
//            serviceName = sName;
//            final QName epName = EndpointReferenceUtils.getPortQName(ref, bus);
//            if (epName != null) {
//                endpointName = epName;
//            }
//            if (LOG.isLoggable(Level.FINE)) {
//                LOG.fine("EPR endpoint: " + serviceName + " " + endpointName);
//            }
//        }
//        final String wsdlLoc = EndpointReferenceUtils.getWSDLLocation(ref);
//        if (wsdlLoc != null) {
//            wsdlLocation = wsdlLoc;
//        }
//
//        String mexLoc = findMEXLocation(ref, useEPRWSAAddrAsMEXLocation);
//        if (LOG.isLoggable(Level.FINE)) {
//            LOG.fine("WS-MEX location: " + mexLoc);
//        }
//        if (mexLoc != null) {
//            throw new UnsupportedOperationException(CXFIncompleteClasspathSubstitutions.NOT_SUPPORTED_IN_NATIVE_BUILD);
////            try {
////                JaxWsProxyFactoryBean proxyFac = new JaxWsProxyFactoryBean();
////                proxyFac.setBindingId(soapVersion);
////                proxyFac.setAddress(mexLoc);
////                MetadataExchange exc = proxyFac.create(MetadataExchange.class);
////                Metadata metadata = exc.get2004();
////
////                Definition definition = null;
////                List<Schema> schemas = new ArrayList<>();
////                // Parse the MetadataSections into WSDL definition + associated schemas
////                for (MetadataSection s : metadata.getMetadataSection()) {
////                    if ("http://schemas.xmlsoap.org/wsdl/".equals(s.getDialect())) {
////                        definition =
////                            bus.getExtension(WSDLManager.class).getDefinition((Element)s.getAny());
////                    } else if ("http://www.w3.org/2001/XMLSchema".equals(s.getDialect())) {
////                        Element schemaElement = (Element)s.getAny();
////                        if (schemaElement ==  null) {
////                            String schemaLocation = s.getLocation();
////                            LOG.info("XSD schema location: " + schemaLocation);
////                            schemaElement = downloadSchema(schemaLocation);
////                        }
////                        QName schemaName =
////                            new QName(schemaElement.getNamespaceURI(), schemaElement.getLocalName());
////                        WSDLManager wsdlManager = bus.getExtension(WSDLManager.class);
////                        ExtensibilityElement
////                            exElement = wsdlManager.getExtensionRegistry().createExtension(Types.class, schemaName);
////                        ((Schema)exElement).setElement(schemaElement);
////                        schemas.add((Schema)exElement);
////                    }
////                }
////
////                if (definition != null) {
////                    // Add any extra schemas to the WSDL definition
////                    for (Schema schema : schemas) {
////                        definition.getTypes().addExtensibilityElement(schema);
////                    }
////
////                    WSDLServiceFactory factory = new WSDLServiceFactory(bus, definition);
////                    SourceDataBinding dataBinding = new SourceDataBinding();
////                    factory.setDataBinding(dataBinding);
////                    Service service = factory.create();
////                    service.setDataBinding(dataBinding);
////
////                    // Get the endpoint + service names by matching the 'location' to the
////                    // address in the WSDL. If the 'location' is 'anonymous' then just fall
////                    // back to the first service + endpoint name in the WSDL, if the endpoint
////                    // name is not defined in the Metadata
////                    List<ServiceInfo> services = service.getServiceInfos();
////                    String anonymousAddress = "http://www.w3.org/2005/08/addressing/anonymous";
////
////                    if (!anonymousAddress.equals(location)) {
////                        for (ServiceInfo serv : services) {
////                            for (EndpointInfo ei : serv.getEndpoints()) {
////                                if (ei.getAddress().equals(location)) {
////                                    endpointName = ei.getName();
////                                    serviceName = serv.getName();
////                                    LOG.fine("Matched endpoint to location");
////                                }
////                            }
////                        }
////                    }
////
////                    EndpointInfo ei = service.getEndpointInfo(endpointName);
////                    if (ei == null && anonymousAddress.equals(location)
////                        && !services.isEmpty() && !services.get(0).getEndpoints().isEmpty()) {
////                        LOG.fine("Anonymous location so taking first endpoint");
////                        serviceName = services.get(0).getName();
////                        endpointName = services.get(0).getEndpoints().iterator().next().getName();
////                        ei = service.getEndpointInfo(endpointName);
////                    }
////
////                    if (ei == null) {
////                        throw new TrustException(LOG, "ADDRESS_NOT_MATCHED", location);
////                    }
////
////                    if (location != null && !anonymousAddress.equals(location)) {
////                        ei.setAddress(location);
////                    }
////                    Endpoint endpoint = new EndpointImpl(bus, service, ei);
////                    client = new ClientImpl(bus, endpoint);
////                }
////            } catch (Exception ex) {
////                throw new TrustException("WS_MEX_ERROR", ex, LOG);
////            }
//        }
//    }
//
//}
//
///**
// * Substitutes
// * {@link org.apache.cxf.staxutils.validation.Stax2ValidationUtils Stax2ValidationUtils}
// * with a completely different implementation. The original class attempts to
// * instantiate {@link W3CMultiSchemaFactory} during its own static
// * initialization which is why we can't do a simple partial substitution. Both
// * classes have a myriad of classpath issues which make them imposssible to
// * compile to native but also hard to even substitute or remove.
// * <br><br>
// * Luckily, since the holes on the classpath pertain to optional dependencies,
// * the original class already expects to not be able to load them in some
// * circunstances. Thus, this implementation simply always behaves as if the
// * necessary classes are not on the classpath without attempting any operations
// * that would compromise the native compilation.
// *
// * @author bnazare
// */
//@TargetClass(className = "org.apache.cxf.staxutils.validation.Stax2ValidationUtils")
//@Substitute
//final class Target_org_apache_cxf_staxutils_validation_Stax2ValidationUtils {
//
//    /**
//     * Substitutes
//     * {@link org.apache.cxf.staxutils.validation.Stax2ValidationUtils#Stax2ValidationUtils Stax2ValidationUtils()}
//     * to always throw an exception. The original constructor checks if static
//     * initialization completed fully and if not throws an exception. In this
//     * implementation, we throw the exact same exception without any checks as
//     * we already know the classpath is incomplete.
//     */
//    @Substitute
//    Target_org_apache_cxf_staxutils_validation_Stax2ValidationUtils() {
//        throw new RuntimeException("Could not load woodstox");
//    }
//
//    /**
//     * Substitutes
//     * {@link org.apache.cxf.staxutils.validation.Stax2ValidationUtils#setupValidation(XMLStreamReader, Endpoint, ServiceInfo) Stax2ValidationUtils.setupValidation}
//     * to always throw an exception. This should never be called as the only
//     * existing constructor as been substituted to always fail.
//     *
//     * @param reader
//     * @param endpoint
//     * @param serviceInfo
//     * @return
//     * @throws XMLStreamException
//     */
//    @Substitute
//    public boolean setupValidation(XMLStreamReader reader, Endpoint endpoint, ServiceInfo serviceInfo)
//            throws XMLStreamException {
//        throw new UnsupportedOperationException(CXFIncompleteClasspathSubstitutions.NOT_SUPPORTED_IN_NATIVE_BUILD);
//    }
//
//    /**
//     * Substitutes
//     * {@link org.apache.cxf.staxutils.validation.Stax2ValidationUtils#setupValidation(XMLStreamWriter, Endpoint, ServiceInfo) Stax2ValidationUtils.setupValidation}
//     * to always throw an exception. This should never be called as the only
//     * existing constructor as been substituted to always fail.
//     *
//     * @param writer
//     * @param endpoint
//     * @param serviceInfo
//     * @return
//     * @throws XMLStreamException
//     */
//    @Substitute
//    public boolean setupValidation(XMLStreamWriter writer, Endpoint endpoint, ServiceInfo serviceInfo)
//            throws XMLStreamException {
//        throw new UnsupportedOperationException(CXFIncompleteClasspathSubstitutions.NOT_SUPPORTED_IN_NATIVE_BUILD);
//    }
//
//}
//
//@TargetClass(Normalizer.class)
//final class Target_org_jasypt_normalization_Normalizer {
//
//    /**
//     * Substitutes {@link Normalizer#normalizeWithIcu4j} to always throw an
//     * exception. The original method makes calls to
//     * {@link com.ibm.icu.text.Normalizer} which is not on the classpath and so
//     * won't compile to native. At runtime though, other parts of the original
//     * class will check if ICU4J is on the classpath (which isn't) and will only
//     * call this method in such a case (which will never happen).
//     *
//     * @param message
//     * @return
//     */
//    @Substitute
//    static char[] normalizeWithIcu4j(final char[] message) {
//        throw new UnsupportedOperationException(CXFIncompleteClasspathSubstitutions.NOT_SUPPORTED_IN_NATIVE_BUILD);
//    }
//
//}
