package com.linkare.assinare.server;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.ext.logging.LoggingFeature;
import org.apache.cxf.ws.policy.IgnorablePolicyInterceptorProvider;
import org.apache.cxf.ws.policy.PolicyInterceptorProviderRegistry;

import service.authentication.ama.SCMDService;
import service.authentication.ama.SCMDService_Service;

/**
 *
 * @author bnazare
 */
public class SCMDServiceFactory {

    private static final String MS_POLICY_NS = "http://schemas.microsoft.com/ws/06/2004/policy/http";
    private static final String WS_SECPOL_NS = "http://schemas.xmlsoap.org/ws/2005/07/securitypolicy";

    private static final List<QName> IGNORED_POLICIES = Arrays.asList(
            new QName(MS_POLICY_NS, "BasicAuthentication"),
            new QName(WS_SECPOL_NS, "TransportBinding"),
            new QName(WS_SECPOL_NS, "TransportToken"),
            new QName(WS_SECPOL_NS, "HttpsToken"),
            new QName(WS_SECPOL_NS, "AlgorithmSuite"),
            new QName(WS_SECPOL_NS, "Basic256"),
            new QName(WS_SECPOL_NS, "Layout"),
            new QName(WS_SECPOL_NS, "Strict")
    );

    @Produces
    @Dependent
    public SCMDService getServiceCXF(CMDConfiguration cmdConfiguration) {
        Bus defaultBus = BusFactory.getThreadDefaultBus();

//        disable CXF's entire policy handling with: defaultBus.getExtension(PolicyEngine.class).setEnabled(false);
//
//        ignore all requested policies explicitly
        PolicyInterceptorProviderRegistry reg = defaultBus.getExtension(PolicyInterceptorProviderRegistry.class);
        reg.register(new IgnorablePolicyInterceptorProvider(IGNORED_POLICIES));

        final SCMDService_Service rootService = new SCMDService_Service(cmdConfiguration.wsdlLocation());
        final SCMDService service = rootService.getBasicHttpBindingSCMDService(new LoggingFeature());

        BindingProvider port = (BindingProvider) service;
        Map<String, Object> reqCtx = port.getRequestContext();
        reqCtx.put(BindingProvider.USERNAME_PROPERTY, cmdConfiguration.applicationUser());
        reqCtx.put(BindingProvider.PASSWORD_PROPERTY, cmdConfiguration.applicationPassword());

        return service;
    }

}
