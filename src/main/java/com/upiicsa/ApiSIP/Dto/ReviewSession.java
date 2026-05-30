package com.upiicsa.ApiSIP.Dto;

import java.time.LocalDateTime;

public record ReviewSession(
        Integer operatorId,
        LocalDateTime lockedAt
) {
}
