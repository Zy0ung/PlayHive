package org.myteam.server.chat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myteam.server.chat.domain.Chat;
import org.myteam.server.chat.domain.ChatRoom;
import org.myteam.server.chat.repository.ChatRepository;
import org.myteam.server.chat.repository.ChatRoomRepository;

import org.myteam.server.filter.domain.BadWordFilter;
import org.myteam.server.filter.domain.FilterData;
import org.myteam.server.filter.repository.FilterDataRepository;
import org.myteam.server.global.exception.ErrorCode;
import org.myteam.server.global.exception.PlayHiveException;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatRoomRepository roomRepository;
    private final ChatRepository chatRepository;
    private final FilterDataRepository filterDataRepository;

    private final BadWordFilter badWordFilter;

    /**
     * 애플리케이션 시작 시 필터링 단어 로드
     */
    @EventListener(ApplicationReadyEvent.class)
    public void loadFilteredWords() {
        List<String> words = filterDataRepository.findAll()
                .stream()
                .map(FilterData::getWord)
                .collect(Collectors.toList());
        badWordFilter.loadFilteredWords(words);
    }

    // ==========================
    // 채팅방 관련 메서드
    // ==========================

    /**
     * 모든 채팅방 조회
     */
    public List<ChatRoom> findAllRoom() {
        return roomRepository.findAll();
    }

    /**
     * ID로 채팅방 조회
     */
    public ChatRoom findRoomById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new PlayHiveException(ErrorCode.ROOM_NOT_FOUND));
    }

    /**
     * 채팅방 생성
     */
    public ChatRoom createChatRoom(String roomName) {
        ChatRoom newRoom = ChatRoom.builder()
                .name(roomName)
                .build();

        roomRepository.save(newRoom); // DB에 저장
        return newRoom;
    }

    // ==========================
    // 채팅 관련 메서드
    // ==========================

    /**
     * 채팅 생성
     */
    public Chat createChat(Long roomId, String sender, String senderEmail, String message) {
        ChatRoom room = findRoomById(roomId);
        String filteredMessage = badWordFilter.filterMessage(message);
        Chat chat = Chat.createChat(room, sender, senderEmail, filteredMessage);
        return chatRepository.save(chat);
    }

    /**
     * 채팅방 ID로 모든 채팅 조회
     */
    public List<Chat> findAllChatByRoomId(Long roomId) {
        return chatRepository.findAllByRoomId(roomId);
    }

    // ==========================
    // 필터링 관련 메서드
    // ==========================

    public void addFilteredWord(String word) {
        filterDataRepository.save(new FilterData(word));
        badWordFilter.addFilteredWord(word);
    }

    public void removeFilteredWord(String word) {
        filterDataRepository.deleteByWord(word);
        badWordFilter.removeFilteredWord(word);
    }
}