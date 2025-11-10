package ru.javaops.cloudjava.reviewservice.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import ru.javaops.cloudjava.reviewservice.exception.ReviewServiceException;

@AllArgsConstructor
public enum SortBy {
    DATE_ASC(Sort.by(Sort.Direction.ASC, "createdAt")),
    DATE_DESC(Sort.by(Sort.Direction.DESC, "createdAt"));

    @JsonCreator
    public static SortBy fromString(String str) {
        try {
            return SortBy.valueOf(str.toUpperCase());
        } catch (IllegalArgumentException e) {
            var msg = "Failed to create SortBy from string: %s".formatted(str);
            throw new ReviewServiceException(msg, HttpStatus.BAD_REQUEST);
        }
    }

    @Getter
    private final Sort sort;
}