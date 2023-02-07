package com.redhat.parodos.workflow.version;

import com.redhat.parodos.examples.simple.LoggingWorkFlowTaskExecution;
import com.redhat.parodos.workflow.task.infrastructure.InfrastructureTaskDefinition;
import com.redhat.parodos.workflow.task.parameter.WorkFlowTaskParameter;
import com.redhat.parodos.workflow.task.parameter.WorkFlowTaskParameterType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

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
