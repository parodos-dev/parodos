package com.redhat.parodos.workflow.execution.transaction;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ConfigurationException;
import org.modelmapper.MappingException;
import org.modelmapper.ModelMapper;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;

/**
 * Notification assembler DTO
 *
 * @author Richard Wang (Github: RichardW98)
 * @author Annel Ketcha (Github: anludke)
 */
@Slf4j
public class WorkFlowTransactionAssemblerDTO extends RepresentationModelAssemblerSupport<WorkFlowTransactionEntity, WorkFlowTransactionDTO> {

    public WorkFlowTransactionAssemblerDTO() {
        super(WorkFlowTransactionController.class, WorkFlowTransactionDTO.class);
    }

    @Override
    public WorkFlowTransactionDTO toModel(WorkFlowTransactionEntity workFlowTransactionEntity) {
        try {
            return new ModelMapper().map(workFlowTransactionEntity, WorkFlowTransactionDTO.class);
        } catch (IllegalArgumentException | ConfigurationException | MappingException ex) {
            log.error("Unable Convert Entity: {} {} to an DTO", ex.getMessage(), workFlowTransactionEntity);
            throw ex;
        }
    }
}
