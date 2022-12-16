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
package com.redhat.parodos.workflow.execution.transaction;

import java.util.UUID;
import org.modelmapper.ConfigurationException;
import org.modelmapper.MappingException;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

/**
 * Converts DTO to Entities and provides persistence operations for WorkFlowTransactionEntities
 * 
 * @author Luke Shannon (Github: lshannon)
 *
 */
@Service
@Slf4j
public class WorkTransactionService {
	
    private final WorkFlowTransactionRepository workFlowTransactionRepository;
    private final ModelMapper modelMapper;

    public WorkTransactionService(WorkFlowTransactionRepository workFlowTransactionRepository, ModelMapper modelMapper) {
        this.workFlowTransactionRepository = workFlowTransactionRepository;
        this.modelMapper = modelMapper;
    }
    
    /**
     * Creates a new ExistingInfrastructureEntity from a DTO
     * 
     * @param existingInfraStructure DTO constructed from a client (i.e: the UI)
     * @return ExistingInfrastructureDto from the new persisted ExistingInfrastructureEntity, returns null if this fails
     * 
     */
    public WorkFlowTransactionEntity createWorkFlowTransactionEntity(WorkFlowTransactionDTO workFlowExecutionDTO) {
    	WorkFlowTransactionEntity entity = convertToEntity(workFlowExecutionDTO);
        return workFlowTransactionRepository.save(entity);
    }
    
    /**
     * Get an entity and convert it to a DTO
     * 
     * @param uuid for referencing a Entity
     * @return ExistingInfrastructureDto for transmission
     */
    public WorkFlowTransactionDTO getWorkFlowTransactionEntity(String uuid) {
        try {
            WorkFlowTransactionEntity entity = workFlowTransactionRepository.findById(UUID.fromString(uuid)).get();
            return convertToDto(entity);
        } catch (IllegalArgumentException ex) {
            log.error("Unable to look up and convert WorkFlowExecutionEntity with UUID: {}", uuid);
        }
        return WorkFlowTransactionDTO.builder().build();
    }

    /**
     * Get all entities
     *
     * @return ExistingInfrastructureDto for transmission
     */
    public Page<WorkFlowTransactionEntity> getWorkFlowTransactions(Pageable pageable) {
        try {
            return workFlowTransactionRepository.findAll(pageable);
        } catch (IllegalArgumentException ex) {
            log.error("Unable to look up and convert WorkFlowExecutionEntities");
            throw ex;
        }
    }

    /**
     * Converts a ExistingInfrastructureDto to an ExistingInfrastructureEntity
     * 
     * @param workFlowTransactionDTO DTO reference
     * @return ExistingInfrastructureEntity entity with the data from the DTO
     */
    private WorkFlowTransactionEntity convertToEntity(WorkFlowTransactionDTO workFlowTransactionDTO) {
        try {
            return modelMapper.map(workFlowTransactionDTO, WorkFlowTransactionEntity.class);
        } catch (IllegalArgumentException | ConfigurationException | MappingException  ex) {
            log.error("Unable Convert DTO: {} {} to an Entity", ex.getMessage(), workFlowTransactionDTO);
        }
            return new WorkFlowTransactionEntity();
    }
    
    /**
     * Converts a ExistingInfrastructureEntity to an DTO
     * 
     * @param existingInfraEntity 
     * @return ExistingInfrastructureDto with the data of an Entity
     */
    private WorkFlowTransactionDTO convertToDto(WorkFlowTransactionEntity existingInfraEntity) {
        try {
            return modelMapper.map(existingInfraEntity, WorkFlowTransactionDTO.class);
        } catch (IllegalArgumentException | ConfigurationException | MappingException  ex) {
            log.error("Unable Convert Entity: {} {} to a DTO", ex.getMessage(), existingInfraEntity);
        }
        return WorkFlowTransactionDTO.builder().build();
    }
    
}
