package ru.javaops.cloudjava.reviewservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReviewResponse {
    private Long id;
    private Long menuId;
    private String createdBy;
    private String comment;
    private Integer rate;
    private LocalDateTime createdAt;
}
