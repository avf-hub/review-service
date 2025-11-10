package ru.javaops.cloudjava.reviewservice.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetRatingsRequest {
    @NotEmpty
    private Set<Long> menuIds;
}
