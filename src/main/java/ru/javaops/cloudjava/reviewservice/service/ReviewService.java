package ru.javaops.cloudjava.reviewservice.service;

import ru.javaops.cloudjava.reviewservice.dto.CreateReviewRequest;
import ru.javaops.cloudjava.reviewservice.dto.RatedReviewsResponse;
import ru.javaops.cloudjava.reviewservice.dto.ReviewResponse;
import ru.javaops.cloudjava.reviewservice.dto.SortBy;

import java.util.List;

public interface ReviewService {

    ReviewResponse createReview(CreateReviewRequest request, String username);

    ReviewResponse getReview(Long reviewId);

    List<ReviewResponse> getReviewsOfUser(String username, SortBy sort, int from, int size);

    RatedReviewsResponse getRatedReviewsForMenu(Long menuId, SortBy sort, int from, int size);
}
