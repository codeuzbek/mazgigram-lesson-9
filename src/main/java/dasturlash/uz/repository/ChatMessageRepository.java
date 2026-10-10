package dasturlash.uz.repository;

import dasturlash.uz.entity.ChatMessageEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatMessageRepository extends CrudRepository<ChatMessageEntity, String>, PagingAndSortingRepository<ChatMessageEntity, String> {

    @Query("From ChatMessageEntity as c " +
            " where c.visible = true " +
            " and c.senderId =:senderId and c.receiverId=:currentProfileId " +
            " and c.isReceiverRead = true order by  c.createdDate desc limit 1")
    Optional<ChatMessageEntity> findLastReadMessage_userChat(@Param("currentProfileId") String currentProfileId, @Param("senderId") String senderId);


}
