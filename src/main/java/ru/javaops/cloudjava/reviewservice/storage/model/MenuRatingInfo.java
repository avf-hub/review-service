package ru.javaops.cloudjava.reviewservice.storage.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuRatingInfo {
    private Long menuId;
    private Float wilsonScore;
    private Float avgStars;
}