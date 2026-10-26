package org.kfokam48.stagemanagementbackend.chat;

import lombok.RequiredArgsConstructor;

import org.kfokam48.stagemanagementbackend.chat.conversation.ConversationResponseDTO;
import org.kfokam48.stagemanagementbackend.chat.message.MessageDTO;
import org.kfokam48.stagemanagementbackend.chat.message.MessageResponseDTO;
import org.kfokam48.stagemanagementbackend.service.auth.CurrentUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Historique et envoi de messages. Un utilisateur ne peut lire que ses propres conversations
 * et ne peut envoyer des messages qu'en son nom. Les erreurs sont gérées par GlobalExceptionHandler.
 */
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final ChatService chatService;
    private final CurrentUserService currentUserService;

    @GetMapping("/conversation/{user1Id}/{user2Id}")
    public ResponseEntity<List<MessageResponseDTO>> getConversation(
            @PathVariable Long user1Id,
            @PathVariable Long user2Id) {
        currentUserService.checkSelf(user1Id);
        return ResponseEntity.ok(chatService.getConversation(user1Id, user2Id));
    }

    @PostMapping("/mark-as-read/{senderId}/{recipientId}")
    public ResponseEntity<Void> markMessagesAsRead(
            @PathVariable Long senderId,
            @PathVariable Long recipientId) {
        // Seul le destinataire peut marquer ses messages comme lus
        currentUserService.checkSelf(recipientId);
        chatService.markMessagesAsRead(senderId, recipientId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/send")
    public ResponseEntity<MessageResponseDTO> sendMessage(@RequestBody MessageDTO messageDTO) {
        messageDTO.setExpediteurId(currentUserService.getCurrentUserId());
        Message message = chatService.sendMessages(messageDTO);
        return ResponseEntity.ok(chatService.convertToResponseDTO(message));
    }

    @GetMapping("/conversations/{userId}")
    public ResponseEntity<List<ConversationResponseDTO>> getUserConversations(@PathVariable Long userId) {
        currentUserService.checkSelf(userId);
        return ResponseEntity.ok(chatService.getUserConversations(userId));
    }
}
