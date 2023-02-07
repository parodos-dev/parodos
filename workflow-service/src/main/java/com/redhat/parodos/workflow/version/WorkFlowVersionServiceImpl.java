package com.redhat.parodos.workflow.version;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.io.InputStream;

@Service
@Slf4j
public class WorkFlowVersionServiceImpl implements WorkFlowVersionService {
    public InputStream getClassInputStream(Object workFlowRef) throws IOException {
        return new ClassPathResource(workFlowRef.getClass().getName().replace(".", "/") + ".class", workFlowRef.getClass().getClassLoader()).getInputStream();
    }

    @Override
    public String getHash(Object workFlowRef) throws IOException {
        String md5 = "";
        try (InputStream is = getClassInputStream(workFlowRef)) {
            md5 = DigestUtils.md5Hex(is);
        }
        log.info("md5 checksum version of {} is : {}", workFlowRef.getClass(), md5);
        return md5;
    }
}
