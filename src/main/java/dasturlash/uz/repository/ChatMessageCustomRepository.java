package dasturlash.uz.repository;

import dasturlash.uz.entity.ChatMessageEntity;
import dasturlash.uz.enums.ChatMessageType;
import dasturlash.uz.enums.ChatScrollDirection;
import dasturlash.uz.enums.ChatType;
import dasturlash.uz.mapper.ChatMessageMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.Tuple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Repository
public class ChatMessageCustomRepository {
    @Autowired
    private EntityManager entityManager;

    public List<ChatMessageEntity> getUserChatMessageList(String currentProfileId, String chatId,
                                                          ChatScrollDirection scrollDirection,
                                                          LocalDateTime lmCreatedDate, int size) {
        StringBuilder selectQueryBuilder = new StringBuilder("FROM ChatMessageEntity as cm ");
        selectQueryBuilder.append(" left join fetch cm.repliedMessage as rc ");
        selectQueryBuilder.append(" where cm.visible = true  ");
        selectQueryBuilder.append(" and ( (cm.senderId =:senderId and cm.receiverId=:receiverId) or (cm.senderId =:receiverId and cm.receiverId=:senderId)) ");
        if (scrollDirection.equals(ChatScrollDirection.UP)) {
            selectQueryBuilder.append(" and cm.createdDate < :lmCreatedDate ");
            selectQueryBuilder.append("  order by  cm.createdDate desc ");
        } else if (scrollDirection.equals(ChatScrollDirection.DOWN)) {
            selectQueryBuilder.append(" and cm.createdDate > :lmCreatedDate ");
            selectQueryBuilder.append("  order by  cm.createdDate asc ");
        } else {
            selectQueryBuilder.append("  order by  cm.createdDate asc ");
        }

        Query query = entityManager.createQuery(selectQueryBuilder.toString());
        query.setParameter("senderId", currentProfileId);
        query.setParameter("receiverId", chatId);
        if (lmCreatedDate != null) {
            query.setParameter("lmCreatedDate", lmCreatedDate);
        }
        query.setMaxResults(size);

        List<ChatMessageEntity> profileEntityList = query.getResultList();
        if (scrollDirection == ChatScrollDirection.UP) {
            Collections.reverse(profileEntityList); // Reverses
        }
        return profileEntityList;
    }
}

/* scrollDirection = UP

SELECT cm
FROM ChatMessageEntity cm
LEFT JOIN FETCH cm.repliedMessage rc
WHERE cm.visible = true
  AND (
        (cm.senderId = :senderId AND cm.receiverId = :receiverId)
        OR
        (cm.senderId = :receiverId AND cm.receiverId = :senderId)
      )
  AND cm.createdDate < :lmCreatedDate
ORDER BY cm.createdDate DESC

*/


/* scrollDirection = DOWN

SELECT cm
FROM ChatMessageEntity cm
LEFT JOIN FETCH cm.repliedMessage rc
WHERE cm.visible = true
  AND (
        (cm.senderId = :senderId AND cm.receiverId = :receiverId)
        OR
        (cm.senderId = :receiverId AND cm.receiverId = :senderId)
      )
  AND cm.createdDate > :lmCreatedDate
ORDER BY cm.createdDate ASC
*/

