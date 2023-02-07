package com.redhat.parodos.workflow.version;

import java.io.IOException;

public interface WorkFlowVersionService {
    String getHash(Object workFlowRef) throws IOException;
}
