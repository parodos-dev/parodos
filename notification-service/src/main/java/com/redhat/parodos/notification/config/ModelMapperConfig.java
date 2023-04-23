package com.redhat.parodos.notification.config;

import com.redhat.parodos.notification.dto.NotificationRecordResponseDTO;
import com.redhat.parodos.notification.jpa.entity.NotificationRecord;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Model mapper configuration
 *
 * @author Nir Argaman (Github: nirarg)
 */
@Configuration
public class ModelMapperConfig {

	@Bean
	ModelMapper modelMapper() {
		ModelMapper modelMapper = new ModelMapper();
		modelMapper.getConfiguration().setAmbiguityIgnored(true);
		addNotificationMapping(modelMapper);
		return modelMapper;
	}

	private void addNotificationMapping(ModelMapper modelMapper) {

		PropertyMap<NotificationRecord, NotificationRecordResponseDTO> notificationResponseDTOTypeMap = new PropertyMap<NotificationRecord, NotificationRecordResponseDTO>() {
			@Override
			protected void configure() {
				map().setBody(source.getNotificationMessage().getBody());
				map().setCreatedOn(source.getNotificationMessage().getCreatedOn());
				map().setFolder(source.getFolder());
				map().setFromuser(source.getNotificationMessage().getFromuser());
				map().setRead(source.isRead());
				map().setSubject(source.getNotificationMessage().getSubject());
				map().setMessageType(source.getNotificationMessage().getMessageType());
				map().setTags(source.getTags());
				map().setId(source.getId());
			}
		};
		modelMapper.addMappings(notificationResponseDTOTypeMap);
	}

}
