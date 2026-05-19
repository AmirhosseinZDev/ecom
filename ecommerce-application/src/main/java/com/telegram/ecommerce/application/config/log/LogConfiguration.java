package com.telegram.ecommerce.application.config.log;

import com.tosan.http.server.starter.config.HttpHeaderMdcParameter;
import com.tosan.http.server.starter.config.MdcFilterConfig;
import com.tosan.http.server.starter.config.RandomGenerationType;
import com.tosan.http.server.starter.config.RandomParameter;
import com.tosan.tools.mask.starter.business.enumeration.MaskType;
import com.tosan.tools.mask.starter.config.SecureParameter;
import com.tosan.tools.mask.starter.config.SecureParametersConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.tosan.http.server.starter.util.Constants.MDC_REQUEST_ID;
import static com.tosan.http.server.starter.util.Constants.X_REQUEST_ID;

/**
 * @author AmirHossein ZamanZade
 * @since 8/19/25
 */
@Configuration
public class LogConfiguration {

    private static final String REQUEST_ID_PREFIX = "TELEMARKET-";

    private static final Set<SecureParameter> logSecureParameters = Set.of(
            new SecureParameter("authorization", MaskType.COMPLETE),
            new SecureParameter("proxy-authorization", MaskType.COMPLETE),
            new SecureParameter("username", MaskType.SEMI),
            new SecureParameter("password", MaskType.COMPLETE),
            new SecureParameter("token", MaskType.COMPLETE)
    );

    @Bean("http-server-util-secured-parameters")
    public SecureParametersConfig secureParametersConfig() {
        return new SecureParametersConfig(logSecureParameters);
    }

    @Bean("http-server-util-mdc-filter-config")
    public MdcFilterConfig mdcFilterConfig() {
        List<HttpHeaderMdcParameter> list = new ArrayList<>();
        HttpHeaderMdcParameter requestId = new HttpHeaderMdcParameter.
                HttpHeaderMdcParameterBuilder(X_REQUEST_ID, MDC_REQUEST_ID).randomParameter(
                new RandomParameter(REQUEST_ID_PREFIX, RandomGenerationType.ALPHANUMERIC, 8)).build();
        list.add(requestId);
        return new MdcFilterConfig(list);
    }
}
