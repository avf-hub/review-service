package ru.javaops.cloudjava.reviewservice.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.javaops.cloudjava.reviewservice.BaseIntegrationTest;
import ru.javaops.cloudjava.reviewservice.service.RatingService;
import ru.javaops.cloudjava.reviewservice.storage.model.Rating;
import ru.javaops.cloudjava.reviewservice.storage.repositories.RatingRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static ru.javaops.cloudjava.reviewservice.storage.model.Rating.newRating;
import static ru.javaops.cloudjava.reviewservice.testutil.TestConstants.MENU_ONE;
import static ru.javaops.cloudjava.reviewservice.testutil.TestConstants.MENU_UNKNOWN;
import static ru.javaops.cloudjava.reviewservice.testutil.TestData.ratingMenuOne;
import static ru.javaops.cloudjava.reviewservice.testutil.TestUtils.assertRatesEqual;
import static ru.javaops.cloudjava.reviewservice.testutil.TestUtils.incrementExpectedRating;

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
}