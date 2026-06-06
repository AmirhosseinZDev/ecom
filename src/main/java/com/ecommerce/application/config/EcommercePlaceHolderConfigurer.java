package com.ecommerce.application.config;

import com.ecommerce.application.util.ResourceUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author Amirhossein Zamanzade
 * @since 8/18/25
 */
@RequiredArgsConstructor
public class EcommercePlaceHolderConfigurer extends PropertySourcesPlaceholderConfigurer {

    protected static final List<String> PATH_PROPERTY_NAMES = new ArrayList<>() {{
        add("server.ssl.key-store");
    }};
    private final ResourceUtil resourceUtil;

    protected Properties mergeProperties() throws IOException {
        Properties properties = super.mergeProperties();
        configurePathProperty(properties);
        return properties;
    }

    protected void convertProperties(Properties props) {
        super.convertProperties(props);
        configurePathProperty(props);
    }

    protected void configurePathProperty(Properties properties) {
        for (String propertyName : PATH_PROPERTY_NAMES) {
            String propertyValue = (String) properties.get(propertyName);
            String relativePath = resourceUtil.getRelativePath(propertyValue);
            if (StringUtils.isNotEmpty(relativePath)) {
                properties.put(propertyName, relativePath);
            }
        }
    }
}
