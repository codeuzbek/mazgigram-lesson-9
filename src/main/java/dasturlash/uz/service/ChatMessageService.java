package dasturlash.uz.service;

import dasturlash.uz.config.CustomUserDetails;
import dasturlash.uz.dto.UserGroupWrapper;
import dasturlash.uz.dto.chat.ChatMessageCreateDTO;
import dasturlash.uz.dto.chat.ChatMessageDTO;
import dasturlash.uz.dto.chat.ChatMessageUpdateDTO;
import dasturlash.uz.dto.profile.ProfileDTO;
import dasturlash.uz.entity.ChatMessageEntity;
import dasturlash.uz.entity.ProfileEntity;
import dasturlash.uz.enums.ChatMessageType;
import dasturlash.uz.enums.ChatScrollDirection;
import dasturlash.uz.enums.ChatType;
import dasturlash.uz.exps.AppBadException;
import dasturlash.uz.mapper.ChatMessageMapper;
import dasturlash.uz.repository.ChatMessageCustomRepository;
import dasturlash.uz.repository.ChatMessageRepository;
import dasturlash.uz.util.SpringSecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ChatMessageService {
    @Autowired
    private ChatMessageRepository messageRepository;
    @Autowired
    private ProfileService profileService;
    @Autowired
    private AttachService attachService;
    @Autowired
    private ChatService chatService;
    @Autowired
    private ChatMessageCustomRepository chatMessageCustomRepository;

    public ChatMessageDTO createChatMessage(ChatMessageCreateDTO dto) {
        CustomUserDetails sender = SpringSecurityUtil.getCurrentProfile();
        if (dto.getChatMessageType().equals(ChatMessageType.TEXT) && (dto.getContent() == null || dto.getContent().isBlank())) {
            throw new AppBadException("Content should be given");
        }

        // check and create chat
        chatService.checkAndCreateChat(sender.getId(), dto.getReceiverId(), ChatType.USER);
        // create chatMessage
        ChatMessageDTO response = create(dto, sender.getId(), ChatType.USER); // crete message

        // response
        ProfileDTO senderDTO = new ProfileDTO();
        senderDTO.setName(sender.getName());
        senderDTO.setSurname(sender.getSurname());
        senderDTO.setPhoto(attachService.attachDTO(sender.getPhotoId()));
        response.setSender(senderDTO);
        response.setSocketMessageType("NewMessage");

        // TODO run it in new thread
        // ChatType.USER
        ProfileEntity receiver = profileService.getById(dto.getReceiverId());
        // send notification to receiver

        // // GROUP

        return response;
    }

    public ChatMessageDTO create(ChatMessageCreateDTO dto, String senderId, ChatType chatType) {
        ChatMessageEntity entity = new ChatMessageEntity();
        entity.setContent(dto.getContent());
        entity.setCapture(dto.getCapture());
        entity.setReceiverId(dto.getReceiverId());
        entity.setChatMessageType(dto.getChatMessageType()); // TEXT,PHOTO,...
        entity.setChatType(chatType);
        entity.setSenderId(senderId);
        entity.setRepliedMessageId(dto.getRepliedMessageId());
        entity.setCapture(dto.getCapture());
        entity.setAttachId(dto.getAttachId());
        entity.setCreatedDate(LocalDateTime.now());
        entity.setIsReceiverRead(Boolean.FALSE);
        entity.setVisible(Boolean.TRUE);
        messageRepository.save(entity);
        if (dto.getRepliedMessageId() != null) {
            entity.setRepliedMessage(getById(dto.getRepliedMessageId()));
        }
        return toDTO(entity);
    }

    public Boolean updateChatMessage(String messageId, ChatMessageUpdateDTO dto) {
        CustomUserDetails sender = SpringSecurityUtil.getCurrentProfile();
        ChatMessageEntity entity = getById(messageId);
        if (!entity.getSenderId().equals(sender.getId())) {
            throw new AppBadException("You are not owner mazgi");
        }
        entity.setContent(dto.getContent());
        messageRepository.save(entity); // update

        // message
        ChatMessageDTO message = toDTO(entity);
        message.setSocketMessageType("UpdateMessage");

        ProfileDTO senderDTO = new ProfileDTO();
        senderDTO.setName(sender.getName());
        senderDTO.setSurname(sender.getSurname());
        senderDTO.setPhoto(attachService.attachDTO(sender.getPhotoId()));
        message.setSender(senderDTO);

        // send notification
        // USER
        ProfileEntity receiver = profileService.getById(entity.getReceiverId());
        // send notification to receiver
        // GROUP

        return Boolean.TRUE;
    }

    public Boolean deleteChatMessage(String messageId) {
        CustomUserDetails sender = SpringSecurityUtil.getCurrentProfile();
        ChatMessageEntity entity = getById(messageId);
        if (!entity.getSenderId().equals(sender.getId())) {
            throw new AppBadException("You are not owner mazgi");
        }
        entity.setVisible(false);
        messageRepository.save(entity); // update
        ProfileEntity receiver = profileService.getById(entity.getReceiverId());
        //
        ChatMessageDTO message = new ChatMessageDTO();
        message.setId(messageId);
        message.setSocketMessageType("DeleteMessage");
        message.setReceiverId(entity.getReceiverId());
        message.setSenderId(entity.getSenderId());

        // send notification to receiver
        return Boolean.TRUE;
    }

    public List<ChatMessageDTO> getChatMessageHistory(boolean isChatJustOpened,
                                                      Principal principal,
                                                      String receiverId,
                                                      ChatScrollDirection scrollDirection,
                                                      LocalDateTime lmCreatedDate, int size) {
        CustomUserDetails currentProfile = (CustomUserDetails) ((Authentication) principal).getPrincipal();
        String currentProfileId = currentProfile.getId();

        // When chat is just opened, find the last-read message to use as pivot
        if (isChatJustOpened) {
            Optional<ChatMessageEntity> lastReadMessage = messageRepository.findLastReadMessage_userChat(currentProfileId, receiverId);

            if (lastReadMessage.isPresent()) { // Oxirgi o'qilgan messagedan tepadagilarini ko'rsatish kerak.
                lmCreatedDate = lastReadMessage.get().getCreatedDate();
                scrollDirection = ChatScrollDirection.UP;
            } else {
                lmCreatedDate = null;
                scrollDirection = ChatScrollDirection.NONE;
            }
        }

        List<ChatMessageEntity> entityList = chatMessageCustomRepository.getUserChatMessageList(
                currentProfileId, receiverId, scrollDirection, lmCreatedDate, size);
        return entityList.stream().map(this::toDTO).toList();
    }


    public ChatMessageEntity getById(String id) {
        return messageRepository.findById(id).orElseThrow(() -> {
            throw new AppBadException("Message not found");
        });
    }

    public ChatMessageDTO toDTO(ChatMessageEntity entity) {
        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setId(entity.getId());
        dto.setContent(entity.getContent());
        dto.setCapture(entity.getCapture());
        dto.setCreatedDate(entity.getCreatedDate());
        dto.setChatMessageType(entity.getChatMessageType());
        dto.setSenderId(entity.getSenderId());
        dto.setReceiverId(entity.getReceiverId());
        dto.setIsReceiverRead(entity.getIsReceiverRead());
        if (entity.getRepliedMessage() != null) {
            ChatMessageDTO repliedMessage = new ChatMessageDTO();
            repliedMessage.setId(entity.getRepliedMessageId());
            repliedMessage.setContent(entity.getRepliedMessage().getContent());
            dto.setRepliedMessage(repliedMessage);
        }
        if (entity.getAttachId() != null) {
            dto.setAttach(attachService.attachDTO(entity.getAttachId()));
        }
        return dto;
    }
}
