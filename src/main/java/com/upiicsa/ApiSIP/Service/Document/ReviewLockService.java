package com.upiicsa.ApiSIP.Service.Document;

import com.upiicsa.ApiSIP.Dto.ReviewSession;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ReviewLockService {

    private final ConcurrentHashMap <String, ReviewSession> activeReviews = new ConcurrentHashMap<>();

    private static final int TIMEOUT_MINUTES = 15;

    public boolean acquireLock(String enrollment, Integer operatorId){
        ReviewSession currentSession = activeReviews.get(enrollment);

        if(currentSession == null || currentSession.lockedAt().plusMinutes(TIMEOUT_MINUTES)
                .isBefore(LocalDateTime.now())){
            activeReviews.put(enrollment, new ReviewSession(operatorId, LocalDateTime.now()));
            return true;
        }

        if(currentSession.operatorId().equals(operatorId)){
            activeReviews.put(enrollment, new ReviewSession(operatorId, LocalDateTime.now()));
            return true;
        }

        return false;
    }

    public void releaseLock(String enrollment, Integer operatorId) {
        ReviewSession currentSession = activeReviews.get(enrollment);
        if (currentSession != null && currentSession.operatorId().equals(operatorId)) {
            activeReviews.remove(enrollment);
        }
    }
}
