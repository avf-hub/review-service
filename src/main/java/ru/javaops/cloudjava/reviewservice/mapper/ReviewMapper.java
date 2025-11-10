package ru.javaops.cloudjava.reviewservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import ru.javaops.cloudjava.reviewservice.dto.CreateReviewRequest;
import ru.javaops.cloudjava.reviewservice.dto.ReviewResponse;
import ru.javaops.cloudjava.reviewservice.storage.model.Review;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ReviewMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(source = "username", target = "createdBy")
    Review toDomain(CreateReviewRequest dto, String username);

    ReviewResponse toReviewResponse(Review review);

    List<ReviewResponse> toReviewResponseList(List<Review> reviews);
}
