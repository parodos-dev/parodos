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
package com.redhat.parodos.infrastructure.existing;

import java.util.Optional;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ConfigurationException;
import org.modelmapper.MappingException;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

/**
 * Converts DTO to Entities and provides persistence operations for ExistingInfrastructureEntities
 * 
 * @author Luke Shannon (Github: lshannon)
 *
 */
@Service
@Slf4j
public class ExistingInfrastructureService {
    private final ExistingInfrastructureRepository repository;
    private final ModelMapper modelMapper;

    public ExistingInfrastructureService(ExistingInfrastructureRepository repository, ModelMapper modelMapper) {
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
    public ExistingInfrastructureEntity createExistingInfrastructureEntity(ExistingInfrastructureDto existingInfraStructure) {
        ExistingInfrastructureEntity entity ;
        Optional<ExistingInfrastructureEntity> entityOptional = repository.findFirstByProjectName(existingInfraStructure.getProjectName());
        if (entityOptional.isPresent()) {
            entity = entityOptional.get();
            entity.getTaskLog().addAll(existingInfraStructure.getTaskLog());
        } else  {
            entity = convertToEntity(existingInfraStructure);
        }
        return repository.save(entity);
    }
    
    /**
     * Get an entity and convert it to a DTO
     * 
     * @param uuid for referencing a Entity
     * @return ExistingInfrastructureDto for transmission
     */
    public ExistingInfrastructureDto getExistingInfraStrutureEntity(String uuid) {
        try {
            ExistingInfrastructureEntity entity = repository.findById(UUID.fromString(uuid)).get();
            return convertToDto(entity);
        } catch (IllegalArgumentException ex) {
            log.error("Unable to look up and convert Entity with UUID: {}", uuid);
        }
        return new ExistingInfrastructureDto();
    }
    
    /**
     * Add an TaskExecutionLog to the existing ExistingInfrastructureEntity
     * 
     * @param logTaskDto comment on the status of a Task execution for an ExistingInfrastructureEntity's Task Workflow
     * @return the updated ExistingInfrastructureEntity
     */
    public ExistingInfrastructureEntity updatingExistingInfrastructureEntity(TaskExecutionLogDto logTaskDto) {
        TaskExecutionLog logTask = convertToEmbedable(logTaskDto);
        Optional<ExistingInfrastructureEntity> optionalEntity = repository.findById(UUID.fromString(logTaskDto.getUuid()));
        if (optionalEntity.isPresent()) {
            ExistingInfrastructureEntity entity = optionalEntity.get();
            entity.getTaskLog().add(logTask);
            return repository.save(entity);
        }
        log.error("Unable to find/update the ExistingInfrastructureEntity entity for logTaskDto: " + logTaskDto);
        return new ExistingInfrastructureEntity();
    }
    
    /**
     * Converts a ExistingInfrastructureDto to an ExistingInfrastructureEntity
     * 
     * @param existingInfraStructure DTO reference
     * @return ExistingInfrastructureEntity entity with the data from the DTO
     */
    private ExistingInfrastructureEntity convertToEntity(ExistingInfrastructureDto existingInfraStructure) {
        try {
            return modelMapper.map(existingInfraStructure, ExistingInfrastructureEntity.class);
        } catch (IllegalArgumentException | ConfigurationException | MappingException  ex) {
            log.error("Unable Convert DTO: {} to an Entity", existingInfraStructure);
        }
            return new ExistingInfrastructureEntity();
    }
    
    /**
     * Converts a ExistingInfrastructureEntity to an DTO
     * 
     * @param existingInfraEntity 
     * @return ExistingInfrastructureDto with the data of an Entity
     */
    private ExistingInfrastructureDto convertToDto(ExistingInfrastructureEntity existingInfraEntity) {
        try {
            return modelMapper.map(existingInfraEntity, ExistingInfrastructureDto.class);
        } catch (IllegalArgumentException | ConfigurationException | MappingException  ex) {
            log.error("Unable Convert Entity: {} to a DTO", existingInfraEntity);
        }
        return new ExistingInfrastructureDto();
    }
    
    /**
     * Convert a DTO to an Embedable object
     * 
     * @param taskExecutionLogDto
     * 
     * @return TaskExecutionLog
     */
    private TaskExecutionLog convertToEmbedable(TaskExecutionLogDto taskExecutionLogDto) {
        try {
            return modelMapper.map(taskExecutionLogDto, TaskExecutionLog.class);
        } catch (IllegalArgumentException | ConfigurationException | MappingException  ex) {
            log.error("Unable to map TaskExecutionLogDto: {} to an TaskExecutionLog", taskExecutionLogDto);
        }
        return TaskExecutionLog.builder().build();
    }
}
