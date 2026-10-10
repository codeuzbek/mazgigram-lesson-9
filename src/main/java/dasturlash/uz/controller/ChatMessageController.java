package dasturlash.uz.controller;

import dasturlash.uz.dto.chat.ChatHistoryRequestDTO;
import dasturlash.uz.dto.chat.ChatMessageCreateDTO;
import dasturlash.uz.dto.chat.ChatMessageDTO;
import dasturlash.uz.dto.chat.ChatMessageUpdateDTO;
import dasturlash.uz.enums.ChatScrollDirection;
import dasturlash.uz.service.ChatMessageService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/chat-message")
public class ChatMessageController {

    @Autowired
    private ChatMessageService chatMessageService;

    @PostMapping("/{chatId}")
    public ResponseEntity<List<ChatMessageDTO>> getChatHistory(
            @PathVariable("chatId") String chatId,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "scrollDirection", required = false, defaultValue = "DOWN") ChatScrollDirection scrollDirection,
            @RequestParam(value = "justChatOpened", required = false, defaultValue = "false") boolean justChatOpened,
            Principal principal,
            @RequestBody ChatHistoryRequestDTO chatHistoryRequest) {
        List<ChatMessageDTO> result = chatMessageService.getChatMessageHistory(justChatOpened,
                principal, chatId, scrollDirection, chatHistoryRequest.getLmCreatedDate(), size);
        return ResponseEntity.ok(result);
    }

    @PostMapping("")
    public ResponseEntity<ChatMessageDTO> createChatMessage(@Valid @RequestBody ChatMessageCreateDTO dto) { //ChatMessage
        return ResponseEntity.ok(chatMessageService.createChatMessage(dto));
    }

    @PutMapping("/{messageId}")
    public ResponseEntity<Boolean> updateMessage(@PathVariable("messageId") String messageId,
                                                 @RequestBody ChatMessageUpdateDTO dto) { //ChatMessage
        return ResponseEntity.ok(chatMessageService.updateChatMessage(messageId, dto));
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<Boolean> deleteMessage(@PathVariable("messageId") String messageId) { //ChatMessage
        return ResponseEntity.ok(chatMessageService.deleteChatMessage(messageId));
    }
}
