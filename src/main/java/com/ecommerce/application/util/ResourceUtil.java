package com.ecommerce.application.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author Amirhossein Zamanzade
 * @since 8/18/25
 */
@Slf4j
@Component
public class ResourceUtil {


    public byte[] getBytesOfResource(String path) throws IOException {
        String relativePath = getRelativePath(path);
        if (StringUtils.isNotEmpty(relativePath)) {
            if (relativePath.startsWith("file:")) {
                return getBytes(new FileSystemResource(path));
            } else if (relativePath.startsWith("classpath:")) {
                return getBytes(new ClassPathResource(path));
            }
        }
        return null;
    }

    public String getRelativePath(String path) {
        if (StringUtils.isEmpty(path)) {
            return null;
        }

        if (path.startsWith("file:") || path.startsWith("classpath:")) {
            log.info("Received already-prefixed resource path: [{}]", path);
            return path;
        }

        String relativePath;
        if (checkExistFileSystem(path)) {
            relativePath = "file:" + path;
            log.info("Resolved resource from file-system path [{}]", relativePath);
        } else {
            relativePath = "classpath:" + path;
            log.info("Resolved resource from classpath resource [{}]", relativePath);
        }
        return relativePath;
    }

    private boolean checkExistFileSystem(String path) {
        return new FileSystemResource(path).getFile().exists();
    }

    private byte[] getBytes(Resource resource) throws IOException {
        return (resource != null && resource.exists()) ? resource.getContentAsByteArray() : null;
    }
}
