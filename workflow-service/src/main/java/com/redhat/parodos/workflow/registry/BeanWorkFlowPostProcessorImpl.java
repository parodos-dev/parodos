package com.redhat.parodos.workflow.registry;

import com.redhat.parodos.workflow.WorkFlowType;
import com.redhat.parodos.workflow.annotation.Assessment;
import com.redhat.parodos.workflow.annotation.Checker;
import com.redhat.parodos.workflow.annotation.Infrastructure;
import com.redhat.parodos.workflow.definition.service.WorkFlowDefinitionServiceImpl;
import com.redhat.parodos.workflow.task.WorkFlowTask;
import com.redhat.parodos.workflows.workflow.WorkFlow;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BeanWorkFlowPostProcessorImpl implements BeanPostProcessor {
    ConfigurableListableBeanFactory beanFactory;
    WorkFlowDefinitionServiceImpl workFlowDefinitionService;

    public BeanWorkFlowPostProcessorImpl(ConfigurableListableBeanFactory beanFactory,
                                         WorkFlowDefinitionServiceImpl workFlowDefinitionService) {
        this.beanFactory = beanFactory;
        this.workFlowDefinitionService = workFlowDefinitionService;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String name) {
        if (bean instanceof WorkFlow) {
            Map<String, WorkFlowTask> hmWorkFlowTasks = new HashMap<>();
            Arrays.stream(beanFactory.getDependenciesForBean(name))
                    .filter(dependency -> {
                        try {
                            beanFactory.getBean(dependency, WorkFlowTask.class);
                            return true;
                        } catch (BeansException e) {
                            return false;
                        }
                    })
                    .forEach(dependency -> hmWorkFlowTasks.put(dependency, beanFactory.getBean(dependency, WorkFlowTask.class)));
            workFlowDefinitionService.save(((WorkFlow) bean).getName(), getWorkFlowType(((WorkFlow) bean).getName()), hmWorkFlowTasks);
        }
        return bean;
    }

    private WorkFlowType getWorkFlowType(String beanName) {
        BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
        if (beanDefinition.getSource() instanceof AnnotatedTypeMetadata) {
            AnnotatedTypeMetadata metadata = (AnnotatedTypeMetadata) beanDefinition.getSource();
            Class<?> annotationClass = Stream.of(Assessment.class, Checker.class, Infrastructure.class)
                    .filter(clazz -> metadata.getAnnotationAttributes(clazz.getName()) != null)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("workflow missing type!"));
            return WorkFlowType.valueOf(annotationClass.getSimpleName().toUpperCase());
        }
        throw new RuntimeException("workflow with no annotated type metadata!");
    }
}