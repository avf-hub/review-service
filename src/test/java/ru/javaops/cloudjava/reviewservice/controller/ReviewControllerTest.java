package ru.javaops.cloudjava.reviewservice.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import ru.javaops.cloudjava.reviewservice.BaseIntegrationTest;
import ru.javaops.cloudjava.reviewservice.dto.GetRatingsRequest;
import ru.javaops.cloudjava.reviewservice.dto.RatingsResponse;
import ru.javaops.cloudjava.reviewservice.storage.model.MenuRatingInfo;

import java.util.Comparator;
import java.util.Set;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static ru.javaops.cloudjava.reviewservice.testutil.TestConstants.BASE_URL;
import static ru.javaops.cloudjava.reviewservice.testutil.TestData.allRatingsHaveReviews;
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
}