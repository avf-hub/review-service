package ru.javaops.cloudjava.reviewservice.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import ru.javaops.cloudjava.reviewservice.BaseIntegrationTest;
import ru.javaops.cloudjava.reviewservice.dto.GetRatingsRequest;
import ru.javaops.cloudjava.reviewservice.dto.RatedReviewsResponse;
import ru.javaops.cloudjava.reviewservice.dto.RatingsResponse;
import ru.javaops.cloudjava.reviewservice.dto.ReviewResponse;
import ru.javaops.cloudjava.reviewservice.storage.model.MenuRatingInfo;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Set;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static ru.javaops.cloudjava.reviewservice.testutil.TestConstants.*;
import static ru.javaops.cloudjava.reviewservice.testutil.TestData.*;
import static ru.javaops.cloudjava.reviewservice.testutil.TestUtils.compareMenuInfo;

@AutoConfigureMockMvc
class ReviewControllerTest extends BaseIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void getRatingsOfMenus_returnsCorrectRatings_whenSomeMenusHaveReviews() {
        var menusWithReviews = Set.of(4L, 5L, 6L, 7L, 8L, 11L, 12L);
        var request = GetRatingsRequest.builder()
                .menuIds(menusWithReviews)
                .build();

        webTestClient.post()
                .uri(BASE_URL + "/ratings")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(RatingsResponse.class)
                .value(response -> {
                    var menuRatingInfos = response.getMenuRatings();
                    menuRatingInfos.sort(Comparator.comparing(MenuRatingInfo::getMenuId));
                    assertThat(menuRatingInfos).hasSize(menusWithReviews.size());

                    var allRatingsHaveReviews = allRatingsHaveReviews();

                    for (int i = 0; i < menuRatingInfos.size(); i++) {
                        var rating = menuRatingInfos.get(i);
                        if (i < allRatingsHaveReviews.size()) {
                            compareMenuInfo(allRatingsHaveReviews.get(i), rating);
                        } else {
                            compareDefaultMenuInfo(rating.getMenuId(), rating);
                        }
                    }
                });
    }

    @Test
    void getRatingsOfMenus_returnsCorrectRatings_whenAllMenusHaveReviews() {
        var menusWithReviews = Set.of(4L, 5L, 6L, 7L, 8L);
        var request = GetRatingsRequest.builder()
                .menuIds(menusWithReviews)
                .build();

        webTestClient.post()
                .uri(BASE_URL + "/ratings")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(RatingsResponse.class)
                .value(response -> {
                    var menuRatingInfos = response.getMenuRatings();
                    assertThat(menuRatingInfos).hasSize(menusWithReviews.size());
                    menuRatingInfos.sort(Comparator.comparing(MenuRatingInfo::getMenuId));
                    var allRatingsHaveReviews = allRatingsHaveReviews();
                    for (int i = 0; i < menuRatingInfos.size(); i++) {
                        compareMenuInfo(allRatingsHaveReviews.get(i), menuRatingInfos.get(i));
                    }
                });
    }

    @Test
    void getRatingsOfMenus_returnsDefaultRatingsWhenMenusHaveNoReviews() {
        var menusWithNoReviews = Set.of(1000L, 2000L, 3000L);
        var request = GetRatingsRequest.builder()
                .menuIds(menusWithNoReviews)
                .build();

        webTestClient.post()
                .uri(BASE_URL + "/ratings")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(RatingsResponse.class)
                .value(response -> {
                    var menuRatingInfos = response.getMenuRatings();
                    assertThat(menuRatingInfos).hasSize(menusWithNoReviews.size());
                    for (var menuRatingInfo : menuRatingInfos) {
                        compareDefaultMenuInfo(menuRatingInfo.getMenuId(), menuRatingInfo);
                    }
                });
    }

    @Test
    void getReviewsOfMenu_returnsCorrectResponse_whenMenuHasReviews() {
        webTestClient.get()
                .uri(BASE_URL + "/menu-item/" + MENU_TEN + "?from=0&size=10&sortBy=date_asc")
                .exchange()
                .expectStatus().isOk()
                .expectBody(RatedReviewsResponse.class)
                .value(response -> {
                    assertThat(response.getReviews())
                            .hasSize(5)
                            .isSortedAccordingTo(Comparator.comparing(ReviewResponse::getCreatedAt));
                    compareMenuInfo(ratingMenuTen(), response.getMenuRating());
                });
    }

    @Test
    void getReviewsOfMenu_returnsEmptyListWithDefaultRating_whenMenuHasNoReviews() {
        long menuWithNoReviews = 1000;
        webTestClient.get()
                .uri(BASE_URL + "/menu-item/" + menuWithNoReviews + "?from=0&size=10&sortBy=date_asc")
                .exchange()
                .expectStatus().isOk()
                .expectBody(RatedReviewsResponse.class)
                .value(response -> {
                    assertThat(response.getReviews()).isEmpty();
                    compareDefaultMenuInfo(menuWithNoReviews, response.getMenuRating());
                });

    }

    @Test
    void getReviewsOfUser_returnsCorrectList_whenUserHasReviews() {
        webTestClient.get()
                .uri(BASE_URL + "/my?" + "from=0&size=10&sortBy=date_asc")
                .header(ReviewController.USER_HEADER, USER_NAME)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ReviewResponse.class)
                .value(reviewResponses -> {
                    assertThat(reviewResponses)
                            .hasSize(5)
                            .isSortedAccordingTo(Comparator.comparing(ReviewResponse::getCreatedAt));
                });
    }

    @Test
    void getReviewsOfUser_returnsEmptyList_whenUserHasNoReviews() {
        String userWithNoReviews = "Unknown user";
        webTestClient.get()
                .uri(BASE_URL + "/my?" + "from=0&size=10&sortBy=date_asc")
                .header(ReviewController.USER_HEADER, userWithNoReviews)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ReviewResponse.class)
                .value(reviewResponses -> {
                    assertThat(reviewResponses).isEmpty();
                });
    }

    @Test
    void getReview_returnsReview() {
        var reviewId = getReviewIdByMenuId(MENU_ONE);

        webTestClient.get()
                .uri(BASE_URL + "/" + reviewId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ReviewResponse.class)
                .value(response -> {
                    assertThat(response.getId()).isNotNull();
                    assertThat(response.getMenuId()).isEqualTo(MENU_ONE);
                    assertThat(response.getCreatedBy()).isEqualTo(USER_ONE);
                    assertThat(response.getRate()).isEqualTo(RATE_FIVE);
                    assertThat(response.getCreatedAt()).isEqualTo(REVIEW_DATE);
                    assertThat(response.getComment()).isEqualTo(COMMENT_ONE);
                });
    }

    @Test
    void getReview_returnsNotFoundWhenNoReviewWithThatId() {
        long unknown = 1000L;
        webTestClient.get()
                .uri(BASE_URL + "/" + unknown)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void createReview_createsReview() {
        var request = createReviewRequest(MENU_ONE, 5);
        var username = "Alex";

        LocalDateTime now = LocalDateTime.now().minusNanos(1000);

        webTestClient.post()
                .uri(BASE_URL)
                .header(ReviewController.USER_HEADER, username)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ReviewResponse.class)
                .value(response -> {
                    assertThat(response.getId()).isNotNull();
                    assertThat(response.getMenuId()).isEqualTo(request.getMenuId());
                    assertThat(response.getCreatedBy()).isEqualTo(username);
                    assertThat(response.getRate()).isEqualTo(request.getRate());
                    assertThat(response.getCreatedAt()).isAfter(now);
                });
    }

    @Test
    void createReview_returnsConflictWhenUserTriesToSendSecondReviewToSameMenu() {
        var request = createReviewRequest(MENU_ONE, 5);

        webTestClient.post()
                .uri(BASE_URL)
                .header(ReviewController.USER_HEADER, USER_ONE)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT);
    }
}