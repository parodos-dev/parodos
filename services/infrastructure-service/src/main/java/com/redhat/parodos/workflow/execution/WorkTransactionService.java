/*
 * Copyright (c) 2022 Red Hat Developer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.redhat.parodos.workflow.execution;

import java.util.UUID;
import org.modelmapper.ConfigurationException;
import org.modelmapper.MappingException;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

/**
 * Converts DTO to Entities and provides persistence operations for ExistingInfrastructureEntities
 * 
 * @author Luke Shannon (Github: lshannon)
 *
 */
@Service
@Slf4j
public class WorkTransactionService {
	
    private final WorkFlowTransactionRepository repository;
    private final ModelMapper modelMapper;

    public WorkTransactionService(WorkFlowTransactionRepository repository, ModelMapper modelMapper) {
        this.repository = repository;
        this.modelMapper = modelMapper;
    }
    
    /**
     * Creates a new ExistingInfrastructureEntity from a Dto
     * 
     * @param existingInfraStructure DTO constructed from a client (ie: the UI)
     * @return ExistingInfrastructureDto from the new persisted ExistingInfrastructureEntity, returns null if this fails
     * 
     */
    public WorkFlowTransactionEntity createWorkFlowTransactionEntity(WorkFlowTransactionDto workFlowExecutionDto) {
    	WorkFlowTransactionEntity entity = convertToEntity(workFlowExecutionDto);
        return repository.save(entity);
    }
    
    /**
     * Get an entity and convert it to a DTO
     * 
     * @param uuid for referencing a Entity
     * @return ExistingInfrastructureDto for transmission
     */
    public WorkFlowTransactionDto getWorkFlowTransactionEntity(String uuid) {
        try {
            WorkFlowTransactionEntity entity = repository.findById(UUID.fromString(uuid)).get();
            return convertToDto(entity);
        } catch (IllegalArgumentException ex) {
            log.error("Unable to look up and convert WorkFlowExecutionEntity with UUID: {}", uuid);
        }
        return WorkFlowTransactionDto.builder().build();
    }
    
    
    /**
     * Converts a ExistingInfrastructureDto to an ExistingInfrastructureEntity
     * 
     * @param existingInfraStructure DTO reference
     * @return ExistingInfrastructureEntity entity with the data from the DTO
     */
    private WorkFlowTransactionEntity convertToEntity(WorkFlowTransactionDto existingInfraStructure) {
        try {
            return modelMapper.map(existingInfraStructure, WorkFlowTransactionEntity.class);
        } catch (IllegalArgumentException | ConfigurationException | MappingException  ex) {
            log.error("Unable Convert DTO: {} {} to an Entity", ex.getMessage(), existingInfraStructure);
        }
            return new WorkFlowTransactionEntity();
    }
    
    /**
     * Converts a ExistingInfrastructureEntity to an DTO
     * 
     * @param existingInfraEntity 
     * @return ExistingInfrastructureDto with the data of an Entity
     */
    private WorkFlowTransactionDto convertToDto(WorkFlowTransactionEntity existingInfraEntity) {
        try {
            return modelMapper.map(existingInfraEntity, WorkFlowTransactionDto.class);
        } catch (IllegalArgumentException | ConfigurationException | MappingException  ex) {
            log.error("Unable Convert Entity: {} {} to a DTO", ex.getMessage(), existingInfraEntity);
        }
        return WorkFlowTransactionDto.builder().build();
    }
    
}
