package org.myteam.server.chat.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myteam.server.chat.domain.Chat;
import org.myteam.server.chat.domain.ChatRoom;
import org.myteam.server.chat.dto.ChatMessage;
import org.myteam.server.chat.service.ChatService;
import org.myteam.server.filter.dto.FilterDataRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @MessageMapping("/{roomId}")
    @SendTo("/room/{roomId}")
    public ChatMessage chat(@DestinationVariable Long roomId, ChatMessage message) {
        log.info("chat: {}: {}{}", LocalDateTime.now(), roomId, message);

        //채팅 저장
        Chat chat = chatService.createChat(roomId, message.getSender(), message.getSenderEmail(), message.getMessage());
        return ChatMessage.builder()
                .roomId(roomId)
                .sender(chat.getSender())
                .senderEmail(chat.getSenderEmail())
                .message(chat.getMessage())
                .build();
    }

    @PostMapping("/chat/room")
    @ResponseBody
    public ResponseEntity<ChatRoom> createChatRoom(@RequestBody String roomName) {
        log.info("createChatRoom: {}", roomName);

        // 채팅 룸 생성
        ChatRoom newRoom = chatService.createChatRoom(roomName);

        return ResponseEntity.ok(newRoom);
    }

    @GetMapping("/chat/room")
    @ResponseBody
    public ResponseEntity<List<ChatRoom>> getChatRoom() {
        log.info("get Room");

        List<ChatRoom> chatRooms = chatService.findAllRoom();

        return ResponseEntity.ok(chatRooms);
    }

    @PostMapping("/chat/filter")
    @ResponseBody
    public ResponseEntity<?> addFilterData(@RequestBody FilterDataRequest filterData) {
        log.info("addFilterData: {}", filterData);

        chatService.addFilteredWord(filterData.getFilterData());

        return ResponseEntity.ok("add filter Data");
    }
}
