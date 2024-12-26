package com.project.productservice.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
public class CollaborationDto {
    private Long collaborationId;
    private String detailInfo;
    private String eventName;
    @JsonFormat(pattern = "yyyyMMddHHmmss")
    private LocalDateTime startDate;
    @JsonFormat(pattern = "yyyyMMddHHmmss")
    private LocalDateTime endDate;

    @Builder
    public CollaborationDto(Long collaborationId, String detailInfo, String eventName, LocalDateTime startDate, LocalDateTime endDate) {
        this.collaborationId = collaborationId;
        this.detailInfo = detailInfo;
        this.eventName = eventName;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
