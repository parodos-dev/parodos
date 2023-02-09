package com.redhat.parodos.notification.dto;

import com.redhat.parodos.notification.controller.NotificationRecordController;
import com.redhat.parodos.notification.jpa.entity.NotificationRecord;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;

/**
 * Notification assembler DTO
 *
 * @author Richard Wang (Github: RichardW98)
 * @author Annel Ketcha (Github: anludke)
 */
public class NotificationAssemblerDTO
		extends RepresentationModelAssemblerSupport<NotificationRecord, NotificationRecordResponseDTO> {

	public NotificationAssemblerDTO() {
		super(NotificationRecordController.class, NotificationRecordResponseDTO.class);
	}

	@Override
	public NotificationRecordResponseDTO toModel(NotificationRecord entity) {
		NotificationRecordResponseDTO dto = new NotificationRecordResponseDTO();
		dto.setBody(entity.getNotificationMessage().getBody());
		dto.setCreatedOn(entity.getNotificationMessage().getCreatedOn());
		dto.setFolder(entity.getFolder());
		dto.setFromuser(entity.getNotificationMessage().getFromuser());
		dto.setRead(entity.isRead());
		dto.setSubject(entity.getNotificationMessage().getSubject());
		dto.setMessageType(entity.getNotificationMessage().getMessageType());
		dto.setTags(entity.getTags());
		dto.setId(entity.getId());
		return dto;
	}

}
