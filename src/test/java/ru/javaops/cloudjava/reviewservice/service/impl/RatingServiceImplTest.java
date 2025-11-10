package ru.javaops.cloudjava.reviewservice.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.javaops.cloudjava.reviewservice.BaseIntegrationTest;
import ru.javaops.cloudjava.reviewservice.dto.GetRatingsRequest;
import ru.javaops.cloudjava.reviewservice.dto.RatingsResponse;
import ru.javaops.cloudjava.reviewservice.service.RatingService;
import ru.javaops.cloudjava.reviewservice.storage.model.MenuRatingInfo;
import ru.javaops.cloudjava.reviewservice.storage.model.Rating;
import ru.javaops.cloudjava.reviewservice.storage.repositories.RatingRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static ru.javaops.cloudjava.reviewservice.storage.model.Rating.newRating;
import static ru.javaops.cloudjava.reviewservice.testutil.TestConstants.*;
import static ru.javaops.cloudjava.reviewservice.testutil.TestData.*;
import static ru.javaops.cloudjava.reviewservice.testutil.TestUtils.*;

class RatingServiceImplTest extends BaseIntegrationTest {

    @Autowired
    private RatingService ratingService;
    @Autowired
    private RatingRepository ratingRepository;

    @Test
    void saveRating_savesNewRatingAndUpdatesItCorrectly_whenMultipleConcurrentRequestsSaveDifferentRatingsToSameMenu() throws Exception {
        Rating nonExistentRating = newRating(MENU_UNKNOWN);
        concurrentlyIncrementEachRating20Times_incrementsRatingsCorrectly(nonExistentRating);
    }

    @Test
    void saveRating_updatesRatingCorrectly_whenMultipleConcurrentRequestsSaveDifferentRatingsToSameMenu() throws Exception {
        Rating existentRating = ratingMenuOne();
        concurrentlyIncrementEachRating20Times_incrementsRatingsCorrectly(existentRating);
    }

    private void concurrentlyIncrementEachRating20Times_incrementsRatingsCorrectly(Rating expectedRating) throws InterruptedException {
        Long menuId = expectedRating.getMenuId();
        ExecutorService executor = Executors.newFixedThreadPool(12);
        List<Callable<Void>> workers = new ArrayList<>();
        int numWorkers = 100;
        for (int i = 0; i < numWorkers; i++) {
            final int rate = (i % 5 == 0) ? 5 : i % 5;
            workers.add(() -> {
                        ratingService.saveRating(menuId, rate);
                        return null;
                    }
            );
        }
        executor.invokeAll(workers);
        executor.shutdown();

        incrementExpectedRating(expectedRating, 20, 20, 20, 20, 20);

        Rating rating = ratingRepository.findByMenuId(menuId).get();
        assertRatesEqual(rating, expectedRating);
    }

    @Test
    void saveRating_updatesRatingCorrectly_whenMultipleConcurrentRequestsSaveSameRatingToSameMenu() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(12);
        List<Callable<Void>> workers = new ArrayList<>();
        int numWorkers = 100;
        for (int i = 0; i < numWorkers; i++) {
            workers.add(() -> {
                        ratingService.saveRating(MENU_ONE, 4);
                        return null;
                    }
            );
        }

        executor.invokeAll(workers);
        executor.shutdown();

        Rating expectedRating = ratingMenuOne();
        Rating rating = ratingRepository.findByMenuId(MENU_ONE).get();
        assertThat(rating.getRateFour()).isEqualTo(expectedRating.getRateFour() + numWorkers);
    }

    @Test
    void getRatingsOfMenus_returnsCorrectRatingsWhenSomeMenusHaveRatings() {
        Rating ratingMenuOne = ratingMenuOne();
        Rating ratingMenuTwo = ratingMenuTwo();
        var menuIds = Set.of(MENU_ONE, MENU_TWO, MENU_UNKNOWN);

        incrementRatingsForMenuId(ratingMenuOne, 0, 0, 0, 0, 10);
        incrementRatingsForMenuId(ratingMenuTwo, 0, 0, 0, 10, 0);

        var request = GetRatingsRequest.builder().menuIds(menuIds).build();
        List<MenuRatingInfo> menuRatingInfos = ratingService.getRatingsOfMenus(request).getMenuRatings();
        menuRatingInfos.sort(Comparator.comparing(MenuRatingInfo::getMenuId));

        compareMenuInfo(ratingMenuOne, menuRatingInfos.get(0));
        compareMenuInfo(ratingMenuTwo, menuRatingInfos.get(1));
        compareDefaultMenuInfo(MENU_UNKNOWN, menuRatingInfos.get(2));
    }

    @Test
    void getRatingsOfMenus_returnsCorrectRatingsWhenAllMenusHaveRatings() {
        Rating ratingMenuOne = ratingMenuOne();
        Rating ratingMenuTwo = ratingMenuTwo();
        Rating ratingMenuThree = newRating(MENU_THREE);

        var menuIds = Set.of(MENU_ONE, MENU_TWO, MENU_THREE);
        incrementRatingsForMenuId(ratingMenuOne, 0, 0, 0, 0, 10);
        incrementRatingsForMenuId(ratingMenuTwo, 0, 0, 0, 10, 0);
        incrementRatingsForMenuId(ratingMenuThree, 10, 0, 0, 0, 0);

        var request = GetRatingsRequest.builder().menuIds(menuIds).build();
        RatingsResponse ratingsOfMenus = ratingService.getRatingsOfMenus(request);
        compareMenuInfos(List.of(ratingMenuOne, ratingMenuTwo, ratingMenuThree), ratingsOfMenus.getMenuRatings());
    }

    @Test
    void getRatingsOfMenus_returnsDefaultRatingsWhenMenusHaveNoRatings() {
        var noRatingMenus = Set.of(1000L, 2000L, 3000L);
        var request = GetRatingsRequest.builder().menuIds(noRatingMenus).build();
        RatingsResponse ratingsOfMenus = ratingService.getRatingsOfMenus(request);
        for (var menuRatingInfo : ratingsOfMenus.getMenuRatings()) {
            compareDefaultMenuInfo(menuRatingInfo.getMenuId(), menuRatingInfo);
        }
    }

    @Test
    void getRatingOfMenu_returnsCorrectRatingInfo_whenMenuHasRating() {
        Rating ratingMenuOne = ratingMenuOne();
        incrementRatingsForMenuId(ratingMenuOne, 1, 1, 1, 1, 1);
        MenuRatingInfo rating = ratingService.getRatingOfMenu(MENU_ONE);
        compareMenuInfo(ratingMenuOne, rating);
    }

    @Test
    void getRatingOfMenu_returnsDefaultRatingInfo_whenMenuHadNoRatingBefore() {
        MenuRatingInfo rating = ratingService.getRatingOfMenu(MENU_UNKNOWN);
        compareDefaultMenuInfo(MENU_UNKNOWN, rating);
    }

    @Test
    void saveRating_createsNewRatingAndUpdatesItCorrectly_whenMenuHadNoRatingBefore() {
        ratingService.saveRating(MENU_UNKNOWN, 5);
        Rating rating = ratingRepository.findByMenuId(MENU_UNKNOWN).get();
        assertThat(rating.getRateFive()).isEqualTo(1);
    }

    @Test
    void saveRating_updatesRating_whenMenuHasRatingAlready() {
        ratingService.saveRating(MENU_ONE, 5);
        Rating rating = ratingRepository.findByMenuId(MENU_ONE).get();
        assertThat(rating.getRateFive()).isEqualTo(2);
    }
}