package com.redhat.parodos.project.service;

import com.redhat.parodos.project.dto.ProjectRequestDTO;
import com.redhat.parodos.project.dto.ProjectResponseDTO;
import com.redhat.parodos.project.entity.Project;
import com.redhat.parodos.project.repository.ProjectRepository;
import com.redhat.parodos.workflow.WorkFlowType;
import com.redhat.parodos.workflow.definition.entity.WorkFlowDefinition;
import com.redhat.parodos.workflow.definition.repository.WorkFlowDefinitionRepository;
import com.redhat.parodos.workflow.definition.repository.WorkFlowTaskDefinitionRepository;
import com.redhat.parodos.workflow.definition.service.WorkFlowDefinitionServiceImpl;
import com.redhat.parodos.workflow.task.WorkFlowTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ProjectServiceImplTest {

    private ProjectRepository projectRepository;
    private ProjectServiceImpl projectService;

    @BeforeEach
    public void initEach() {
        this.projectRepository  = Mockito.mock(ProjectRepository.class);
        this.projectService = new ProjectServiceImpl(this.projectRepository, new ModelMapper());
    }

   private Project getSampleProject(String name){
       Project project = Project.builder()
               .name(name)
               .description("test description")
               .createDate(new Date())
               .modifyDate(new Date())
               .build();
       project.setId(UUID.randomUUID());
       return project;
   }

    @Test
    public void testFindProjectByIdWithValidData() {
        // given
        Project project = getSampleProject("test");
        Mockito.when(this.projectRepository.findById(project.getId())).thenReturn(Optional.of(project));

        // when
        ProjectResponseDTO res = this.projectService.getProjectById(project.getId());

        // then
        assertNotNull(res);
        assertEquals(res.getId().toString(), project.getId().toString());
    }

    @Test
    public void testFindProjectByIdWithInvalidData() {
        // given
        Project project = getSampleProject("test");
        Mockito.when(this.projectRepository.findById(project.getId())).thenReturn(Optional.empty());


        // when
        Exception exception = assertThrows(RuntimeException.class, () -> {
            this.projectService.getProjectById(project.getId());
        });

        // then
        assertEquals(exception.getMessage(), String.format("Project with id: %s not found", project.getId()));
    }

    @Test
    public void testGetProjectsWithValidData() {
        // given
        Mockito.when(this.projectRepository.findAll()).thenReturn(Arrays.asList(
                getSampleProject("test"),
                getSampleProject("foo")
        ));

        // when
        List<ProjectResponseDTO> res = this.projectService.getProjects();

        // then
        assertNotNull(res);
        assertEquals(res.size(), 2);
        assertEquals(res.get(0).getName(), "test");
        assertEquals(res.get(1).getName(), "foo");
    }


    @Test
    public void testGetProjectsWithInvalidData() {
        // given
        Mockito.when(this.projectRepository.findAll()).thenReturn(new ArrayList<Project>());

        // when
        List<ProjectResponseDTO> res = this.projectService.getProjects();

        // then
        assertNotNull(res);
        assertEquals(res.size(), 0);
    }

    @Test
    public void testSaveWithValidData() {
        // given
        Project project = getSampleProject("test");
        Mockito.when(this.projectRepository.save(Mockito.any(Project.class))).thenReturn(project);

        ProjectRequestDTO projectDTO = ProjectRequestDTO.builder()
                .name("dto").description("dto description").build();

        // when
        ProjectResponseDTO res = this.projectService.save(projectDTO);

        // then
        ArgumentCaptor<Project> argument = ArgumentCaptor.forClass(Project.class);
        Mockito.verify(this.projectRepository, Mockito.times(1)).save(argument.capture());

        assertEquals(argument.getValue().getDescription(), "dto description");
        assertEquals(argument.getValue().getName(), "dto");

        assertNotNull(res);
        assertEquals(res.getId(), project.getId().toString());
    }
}
