package ru.javaops.cloudjava.reviewservice.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.javaops.cloudjava.reviewservice.BaseIntegrationTest;
import ru.javaops.cloudjava.reviewservice.dto.RatedReviewsResponse;
import ru.javaops.cloudjava.reviewservice.dto.ReviewResponse;
import ru.javaops.cloudjava.reviewservice.dto.SortBy;
import ru.javaops.cloudjava.reviewservice.exception.ReviewServiceException;
import ru.javaops.cloudjava.reviewservice.service.ReviewService;
import ru.javaops.cloudjava.reviewservice.storage.model.Rating;
import ru.javaops.cloudjava.reviewservice.storage.repositories.RatingRepository;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static ru.javaops.cloudjava.reviewservice.testutil.TestConstants.*;
import static ru.javaops.cloudjava.reviewservice.testutil.TestData.createReviewRequest;
import static ru.javaops.cloudjava.reviewservice.testutil.TestData.ratingMenuTen;
import static ru.javaops.cloudjava.reviewservice.testutil.TestUtils.compareMenuInfo;

class ReviewServiceImplTest extends BaseIntegrationTest {

    @Autowired
    private ReviewService reviewService;
    @Autowired
    private RatingRepository ratingRepository;

    @Test
    void getRatedReviewsForMenu_returnsCorrectResponse_whenMenuHasReviews() {
        Long menuTen = MENU_TEN;
        Rating ratingMenuTen = ratingMenuTen();
        RatedReviewsResponse response = reviewService.getRatedReviewsForMenu(menuTen, SortBy.DATE_ASC, 0, 5);
        var reviews = response.getReviews();

        assertThat(reviews)
                .map(ReviewResponse::getRate)
                .isEqualTo(List.of(5, 4, 3, 2, 1));

        assertThat(reviews)
                .map(ReviewResponse::getCreatedAt)
                .isEqualTo(List.of(REVIEW_DATE_MENU_4,
                        REVIEW_DATE_MENU_5,
                        REVIEW_DATE_MENU_6,
                        REVIEW_DATE_MENU_7,
                        REVIEW_DATE_MENU_8));

        compareMenuInfo(ratingMenuTen, response.getMenuRating());
    }

    @Test
    void getRatedReviewsForMenu_returnsEmptyListAndDefaultRating_whenMenuHasNoReviews() {
        Long noReviewsMenu = MENU_UNKNOWN;
        RatedReviewsResponse response = reviewService.getRatedReviewsForMenu(noReviewsMenu, SortBy.DATE_ASC, 0, 10);
        assertThat(response.getReviews()).isEmpty();
        compareDefaultMenuInfo(noReviewsMenu, response.getMenuRating());
    }

    @Test
    void getReviewsOfUser_returnsCorrectList_whenUserHasReviews() {
        List<ReviewResponse> reviews = reviewService.getReviewsOfUser(USER_NAME, SortBy.DATE_ASC, 0, 3);
        assertThat(reviews).hasSize(3);

        assertThat(reviews)
                .map(ReviewResponse::getRate)
                .isEqualTo(List.of(5, 4, 3));

        assertThat(reviews)
                .map(ReviewResponse::getCreatedAt)
                .isEqualTo(List.of(REVIEW_DATE_MENU_4,
                        REVIEW_DATE_MENU_5,
                        REVIEW_DATE_MENU_6));
    }

    @Test
    void getReviewsOfUser_returnsEmptyList_whenUserHasNoReviews() {
        List<ReviewResponse> reviews = reviewService
                .getReviewsOfUser("Unknown", SortBy.DATE_ASC, 0, 10);
        assertThat(reviews).isEmpty();
    }

    @Test
    void getReview_throwsIfReviewIsNotPresent() {
        Long unknownId = 1000L;
        assertThrows(ReviewServiceException.class,
                () -> reviewService.getReview(unknownId));
    }

    @Test
    void getReview_returnsReviewIfReviewIsPresent() {
        var id = getReviewIdByMenuId(MENU_ONE);
        ReviewResponse response = reviewService.getReview(id);
        assertThat(response.getRate()).isEqualTo(RATE_FIVE);
        assertThat(response.getComment()).isEqualTo(COMMENT_ONE);
        assertThat(response.getMenuId()).isEqualTo(MENU_ONE);
        assertThat(response.getCreatedBy()).isEqualTo(USER_ONE);
        assertThat(response.getCreatedAt()).isEqualTo(REVIEW_DATE);
    }

    @Test
    void createReview_throwsWhenUserAlreadyPlacedReviewForMenu() {
        var request = createReviewRequest(MENU_ONE, 5);
        assertThrows(ReviewServiceException.class,
                () -> reviewService.createReview(request, "UserOne"));
    }

    @Test
    void createReview_createsReviewAddsRatingWhenMenuHadNoRating() {
        var request = createReviewRequest(1000L, 5);
        ReviewResponse response = reviewService.createReview(request, "Alex");
        assertThat(response.getRate()).isEqualTo(request.getRate());
        assertThat(response.getMenuId()).isEqualTo(request.getMenuId());
        assertThat(response.getId()).isNotNull();
        assertThat(response.getCreatedBy()).isEqualTo("Alex");
        assertThat(response.getComment()).isEqualTo(request.getComment());

        var rating = ratingRepository.findByMenuId(request.getMenuId()).get();
        assertThat(rating.getRateFive()).isEqualTo(1);
    }

    @Test
    void createReview_createsReviewIncreasesRatingWhenMenuAlreadyHasRating() {
        var request = createReviewRequest(MENU_ONE, 5);

        ReviewResponse response = reviewService.createReview(request, "Alex");
        assertThat(response.getRate()).isEqualTo(request.getRate());
        assertThat(response.getMenuId()).isEqualTo(request.getMenuId());
        assertThat(response.getId()).isNotNull();
        assertThat(response.getCreatedBy()).isEqualTo("Alex");
        assertThat(response.getComment()).isEqualTo(request.getComment());

        Rating rating = ratingRepository.findByMenuId(request.getMenuId()).get();
        assertThat(rating.getRateFive()).isEqualTo(2);
    }
}