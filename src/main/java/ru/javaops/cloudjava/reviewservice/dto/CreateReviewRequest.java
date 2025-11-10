package ru.javaops.cloudjava.reviewservice.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;
import ru.javaops.cloudjava.reviewservice.dto.validation.NullOrNotBlank;

@Data
@Builder
public class CreateReviewRequest {
    @Positive(message = "Идентификатор блюда должен быть > 0")
    private Long menuId;
    @NullOrNotBlank(message = "Комментарий не должен быть пустым.")
    private String comment;
    @Min(value = 1, message = "Рейтинг должен быть от 1 до 5.")
    @Max(value = 5, message = "Рейтинг должен быть от 1 до 5.")
    private Integer rate;
}
