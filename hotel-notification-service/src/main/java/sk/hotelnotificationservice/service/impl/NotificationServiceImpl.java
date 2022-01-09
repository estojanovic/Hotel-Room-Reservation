package sk.hotelnotificationservice.service.impl;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import sk.hotelnotificationservice.domain.NotificationHistory;
import sk.hotelnotificationservice.domain.NotificationType;
import sk.hotelnotificationservice.dto.*;
import sk.hotelnotificationservice.mapper.NotificationMapper;
import sk.hotelnotificationservice.repository.NotificationHistoryRepository;
import sk.hotelnotificationservice.repository.NotificationTypeRepository;
import sk.hotelnotificationservice.service.EmailService;
import sk.hotelnotificationservice.service.NotificationService;

import javax.transaction.Transactional;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private NotificationTypeRepository notificationTypeRepository;
    private NotificationMapper notificationMapper;
    private EmailService emailService;
    private NotificationHistoryRepository notificationHistoryRepository;

    public NotificationServiceImpl(NotificationTypeRepository notificationTypeRepository, NotificationMapper notificationMapper,
                                    EmailService emailService,
                                   NotificationHistoryRepository notificationHistoryRepository) {
        this.notificationTypeRepository = notificationTypeRepository;
        this.notificationMapper = notificationMapper;
        this.emailService = emailService;
        this.notificationHistoryRepository = notificationHistoryRepository;
    }

    @Override
    public NotificationTypeDto addNotificationType(NotificationTypeCreateDto notificationTypeCreateDto) {

        NotificationType notificationType = notificationMapper.notifTypeCreateDtoToNotifType(notificationTypeCreateDto);
        notificationTypeRepository.save(notificationType);
        return notificationMapper.notifTypeToNotifTypeDto(notificationType);
    }


    @Override
    public ResponseEntity<Void> sendMail(ClientDto clientDto, String notificationName) {
        NotificationType notificationType = notificationTypeRepository.findNotificationTypeByName(notificationName);

        String content = notificationType.getMessage();

        content = content.replace("%firstname", clientDto.getFirstName());
        content = content.replace("%lastname", clientDto.getLastName());
        content = content.replace("%username", clientDto.getUsername());
        content = content.replace("%email", clientDto.getEmail());
//        content = content.replace("%firstname", clientDto.getFirstName());
//        content = content.replace("%lastname", clientDto.getLastName());
        String verifyURL = "http://localhost:8081/verify?token=" + clientDto.getVerificationCode();
        content = content.replace("%link", verifyURL);

        emailService.sendSimpleMessage(clientDto.getEmail(), notificationName, content);

        NotificationHistory notificationHistory = new NotificationHistory(clientDto.getEmail(), content, notificationName);
        notificationHistoryRepository.save(notificationHistory);

        return new ResponseEntity<>(HttpStatus.OK);
    }
}