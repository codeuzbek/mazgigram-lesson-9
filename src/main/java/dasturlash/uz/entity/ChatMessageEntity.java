package dasturlash.uz.entity;


import dasturlash.uz.enums.ChatMessageType;
import dasturlash.uz.enums.ChatType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "chat_message")
public class ChatMessageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "sender_id")
    private String senderId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", insertable = false, updatable = false)
    private ProfileEntity sender;

    private String receiverId; // profileId or groupId

    @Enumerated(EnumType.STRING)
    @Column(name = "chat_type")
    private ChatType chatType; // USER,GROUP

    @Column(name = "replied_message_id")
    private String repliedMessageId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "replied_message_id", insertable = false, updatable = false)
    private ChatMessageEntity repliedMessage;

    @Column(name = "capture", columnDefinition = "TEXT")
    private String capture;
    @Column(name = "attach_id")
    private String attachId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attach_id", insertable = false, updatable = false)
    private AttachEntity attach;

    @Enumerated(EnumType.STRING)
    @Column(name = "chat_message_type")
    private ChatMessageType chatMessageType;
    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "is_receiver_read")
    private Boolean isReceiverRead = Boolean.FALSE;
    @Column(name = "visible")
    private Boolean visible = Boolean.TRUE;

//    @Column(name = "chat_id")
//    private String chatId;
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "chat_id", insertable = false, updatable = false)
//    private ChatEntity chat;

}