package org.myteam.server.chat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myteam.server.chat.domain.Chat;
import org.myteam.server.chat.domain.ChatRoom;
import org.myteam.server.chat.repository.ChatRepository;
import org.myteam.server.chat.repository.ChatRoomRepository;

import org.myteam.server.global.exception.ErrorCode;
import org.myteam.server.global.exception.PlayHiveException;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatRoomRepository roomRepository;
    private final ChatRepository chatRepository;

    public List<ChatRoom> findAllRoom() {
        return roomRepository.findAll();
    }

    public ChatRoom findRoomById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new PlayHiveException(ErrorCode.ROOM_NOT_FOUND));
    }

    public ChatRoom createRoom(String name) {
        return roomRepository.save(ChatRoom.createRoom(name));
    }

    public Chat createChat(Long roomId, String sender, String senderEmail, String message) {
        ChatRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new PlayHiveException(ErrorCode.ROOM_NOT_FOUND));

        Chat chat = Chat.createChat(room, sender, senderEmail, message);

        return chatRepository.save(chat);
    }

    // 채팅방 채팅내용 불러오기
    public List<Chat> findAllChatByRoomId(Long roomId) {
        return chatRepository.findAllByRoomId(roomId);
    }

    public ChatRoom createChatRoom(String roomName) {
        ChatRoom newRoom = ChatRoom.builder()
                .name(roomName)
                .build();

        roomRepository.save(newRoom); // DB에 저장
        return newRoom;
    }
}