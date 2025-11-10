package ru.javaops.cloudjava.reviewservice.storage.repositories;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import ru.javaops.cloudjava.reviewservice.BaseTest;
import ru.javaops.cloudjava.reviewservice.storage.model.MenuRatingInfo;
import ru.javaops.cloudjava.reviewservice.storage.model.Rating;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.javaops.cloudjava.reviewservice.storage.model.Rating.newRating;
import static ru.javaops.cloudjava.reviewservice.testutil.TestConstants.*;
import static ru.javaops.cloudjava.reviewservice.testutil.TestData.ratingMenuOne;
import static ru.javaops.cloudjava.reviewservice.testutil.TestData.ratingMenuTwo;
import static ru.javaops.cloudjava.reviewservice.testutil.TestUtils.*;

@DataJpaTest
@Transactional(propagation = Propagation.NEVER)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class RatingRepositoryTest extends BaseTest {

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    void incrementRating_incrementsRatingCorrectlyForConcurrentRequests() throws Exception {
        Long menuId = MENU_ONE;
        ExecutorService executor = Executors.newFixedThreadPool(12);

        List<Callable<Void>> incrementors = new ArrayList<>();
        int numIncrementors = 100;
        for (int i = 1; i <= numIncrementors; i++) {
            // по итогу к каждой оценке добавится 20 значений. Так как изначально у оценки rateFive было значение 1, то
            // ее новое значение будет равно 21.
            final int rate = (i % 5 == 0) ? 5 : i % 5;
            incrementors.add(() -> {
                ratingRepository.incrementRating(menuId, rate);
                return null;
            });
        }

        var results = executor.invokeAll(incrementors);
        executor.shutdown();

        for (var result : results) {
            assertDoesNotThrow(() -> result.get());
        }
        Rating expectedRating = ratingMenuOne();
        incrementExpectedRating(expectedRating, 20, 20, 20, 20, 20);

        Rating actualRating = ratingRepository.findByMenuId(menuId).get();
        assertRatesEqual(actualRating, expectedRating);
    }

    @Test
    void insertNoConflict_succeeds_whenOneTransactionCommitsAndOneRollsBack() throws Exception {
        Long menuId = MENU_UNKNOWN;
        TransactionDefinition def = new DefaultTransactionDefinition();
        var latch = new CountDownLatch(1);
        Callable<Void> t1 = () -> {
            // стартуем транзакцию
            TransactionStatus status = transactionManager.getTransaction(def);
            // позволяем второму потоку стартовать транзакцию
            latch.countDown();

            ratingRepository.insertNoConflict(menuId);
            transactionManager.rollback(status);
            return null;
        };

        Callable<Void> t2 = () -> {
            // ждем, когда первый поток стартует транзакцию
            latch.await();
            // стартуем транзакцию
            TransactionStatus status = transactionManager.getTransaction(def);
            ratingRepository.insertNoConflict(menuId);
            transactionManager.commit(status);
            return null;
        };

        var results = Executors.newFixedThreadPool(2).invokeAll(Arrays.asList(t2, t1));
        assertDoesNotThrow(() -> {
            results.get(1).get();
            results.get(0).get();
        });

        assertThat(ratingRepository.findByMenuId(menuId)).isPresent().hasValueSatisfying(rating -> {
            assertThat(rating.getMenuId()).isEqualTo(menuId);
        });
    }

    @Test
    void insertNoConflict_succeeds_whenTwoConcurrentThreadsPerformInsert() throws Exception {
        Long menuId = MENU_UNKNOWN;
        var latch = new CountDownLatch(1);

        Callable<Void> t1 = () -> {
            latch.countDown();
            ratingRepository.insertNoConflict(menuId);
            return null;
        };

        Callable<Void> t2 = () -> {
            latch.await();
            ratingRepository.insertNoConflict(menuId);
            return null;
        };
        var results = Executors.newFixedThreadPool(2).invokeAll(Arrays.asList(t2, t1));

        assertDoesNotThrow(() -> {
            results.get(1).get();
            results.get(0).get();
        });

        assertThat(ratingRepository.findByMenuId(menuId)).isPresent().hasValueSatisfying(rating -> {
            assertThat(rating.getMenuId()).isEqualTo(menuId);
        });
    }

    @Test
    void insertNoConflict_doesNothing_whenRowWithThatMenuIdExistsInDb() {
        Long existingMenuId = MENU_ONE;
        ratingRepository.insertNoConflict(existingMenuId);

        assertThat(ratingRepository.findByMenuId(existingMenuId)).isPresent().hasValueSatisfying(rating -> {
            assertThat(rating.getMenuId()).isEqualTo(existingMenuId);
            assertThat(rating.getRateFive()).isEqualTo(1);
        });
    }

    @Test
    void insertNoConflict_insertsRowWithMenuId_whenNoRowWithThatMenuIdInDb() {
        Long menuId = MENU_UNKNOWN;
        ratingRepository.insertNoConflict(menuId);

        assertThat(ratingRepository.findByMenuId(menuId)).isPresent().hasValueSatisfying(rating -> assertThat(rating.getMenuId()).isEqualTo(menuId));
    }

    @Test
    void findRatingInfosByMenuIdIn_returnsCorrectListWhenSomeMenusHaveRatings() {
        Rating ratingMenuOne = ratingMenuOne();
        Rating ratingMenuTwo = ratingMenuTwo();

        var menuIds = Set.of(MENU_ONE, MENU_TWO, MENU_UNKNOWN);
        incrementRatingsForMenuId(ratingMenuOne, 10, 10, 10, 10, 10);
        incrementRatingsForMenuId(ratingMenuTwo, 11, 11, 11, 11, 11);

        List<MenuRatingInfo> menuRatingInfos = ratingRepository.findRatingInfosByMenuIdIn(menuIds);
        compareMenuInfos(List.of(ratingMenuOne, ratingMenuTwo), menuRatingInfos);
    }

    @Test
    void findRatingInfosByMenuIdIn_returnsCorrectListWhenAllMenusHaveRatings() {
        Rating ratingMenuOne = ratingMenuOne();
        Rating ratingMenuTwo = ratingMenuTwo();
        Rating ratingMenuThree = newRating(MENU_THREE);

        var menuIds = Set.of(MENU_ONE, MENU_TWO, MENU_THREE);
        incrementRatingsForMenuId(ratingMenuOne, 10, 10, 10, 10, 10);
        incrementRatingsForMenuId(ratingMenuTwo, 11, 11, 11, 11, 11);
        incrementRatingsForMenuId(ratingMenuThree, 12, 12, 12, 12, 12);

        var menuRatingInfos = ratingRepository.findRatingInfosByMenuIdIn(menuIds);
        compareMenuInfos(List.of(ratingMenuOne, ratingMenuTwo, ratingMenuThree), menuRatingInfos);
    }

    @Test
    void findRatingInfosByMenuIdIn_returnsEmptyList_whenNoMenusHaveRatings() {
        var unknown = Set.of(1000L, 2000L, 3000L);
        var ratings = ratingRepository.findRatingInfosByMenuIdIn(unknown);
        assertThat(ratings).isEmpty();
    }

    @Test
    void findRatingInfoByMenuId_returnsCorrectInfo() {
        Rating ratingMenuOne = ratingMenuOne();
        Long menuOne = ratingMenuOne.getMenuId();
        incrementRatingsForMenuId(ratingMenuOne, 10, 10, 10, 10, 10);

        var actual = ratingRepository.findRatingInfoByMenuId(menuOne).get();
        compareMenuInfo(ratingMenuOne, actual);
    }

    @Test
    void findRatingInfoByMenuId_returnsEmptyOptionalWhenNoRatingForMenu() {
        var opt = ratingRepository.findRatingInfoByMenuId(MENU_UNKNOWN);
        assertThat(opt).isEmpty();
    }

    @Test
    void incrementRating_alsoUpdatesWilsonScore_andAvgStars3() {
        Rating ratingMenuOne = ratingMenuOne();
        Rating ratingMenuTwo = ratingMenuTwo();
        Rating ratingMenuThree = newRating(MENU_THREE);

        incrementRatingsForMenuId(ratingMenuOne, 0, 0, 0, 10, 1);
        incrementRatingsForMenuId(ratingMenuTwo, 0, 0, 2, 0, 0);
        incrementRatingsForMenuId(ratingMenuThree, 0, 0, 0, 0, 1);

        MenuRatingInfo first = ratingRepository.findRatingInfoByMenuId(MENU_ONE).get();
        MenuRatingInfo second = ratingRepository.findRatingInfoByMenuId(MENU_TWO).get();
        MenuRatingInfo third = ratingRepository.findRatingInfoByMenuId(MENU_THREE).get();

        compareMenuInfos(List.of(ratingMenuOne, ratingMenuTwo, ratingMenuThree), List.of(first, second, third));

        assertTrue(first.getWilsonScore() > third.getWilsonScore());
        assertTrue(third.getWilsonScore() > second.getWilsonScore());

        assertTrue(third.getAvgStars() > first.getAvgStars());
        assertTrue(first.getAvgStars() > second.getAvgStars());

        printWilsonScoreAndAvgStars(List.of(first, second, third));
    }

    @Test
    void incrementRating_alsoUpdatesWilsonScore_andAvgStars2() {
        Rating ratingMenuOne = ratingMenuOne();
        Rating ratingMenuTwo = ratingMenuTwo();
        Rating ratingMenuThree = newRating(MENU_THREE);

        incrementRatingsForMenuId(ratingMenuOne, 0, 0, 0, 10, 1);
        incrementRatingsForMenuId(ratingMenuTwo, 0, 0, 2, 10, 0);
        incrementRatingsForMenuId(ratingMenuThree, 0, 1, 10, 0, 0);

        MenuRatingInfo first = ratingRepository.findRatingInfoByMenuId(MENU_ONE).get();
        MenuRatingInfo second = ratingRepository.findRatingInfoByMenuId(MENU_TWO).get();
        MenuRatingInfo third = ratingRepository.findRatingInfoByMenuId(MENU_THREE).get();

        compareMenuInfos(List.of(ratingMenuOne, ratingMenuTwo, ratingMenuThree), List.of(first, second, third));

        assertTrue(first.getWilsonScore() > second.getWilsonScore());
        assertTrue(second.getWilsonScore() > third.getWilsonScore());

        assertTrue(first.getAvgStars() > second.getAvgStars());
        assertTrue(second.getAvgStars() > third.getAvgStars());

        printWilsonScoreAndAvgStars(List.of(first, second, third));
    }

    @Test
    void incrementRating_alsoUpdatesWilsonScore_andAvgStars() {
        incrementActualRatingsForMenuId(MENU_ONE, 0, 0, 0, 2, 100);
        incrementActualRatingsForMenuId(MENU_TWO, 0, 0, 2, 10, 0);
        incrementActualRatingsForMenuId(MENU_THREE, 0, 0, 0, 0, 1);

        MenuRatingInfo first = ratingRepository.findRatingInfoByMenuId(MENU_ONE).get();
        MenuRatingInfo second = ratingRepository.findRatingInfoByMenuId(MENU_TWO).get();
        MenuRatingInfo third = ratingRepository.findRatingInfoByMenuId(MENU_THREE).get();

        Assertions.assertTrue(first.getWilsonScore() > second.getWilsonScore());
        Assertions.assertTrue(second.getWilsonScore() > third.getWilsonScore());

        Assertions.assertTrue(first.getAvgStars() > second.getAvgStars());
        Assertions.assertTrue(third.getAvgStars() > first.getAvgStars());

        printWilsonScoreAndAvgStars(List.of(first, second, third));
    }

    @Test
    void incrementRating_incrementsCorrectRating() {
        Rating ratingMenuOne = ratingMenuOne();
        incrementRatingsForMenuId(ratingMenuOne, 5, 5, 5, 5, 4);

        Rating updated = ratingRepository.findByMenuId(MENU_ONE).get();
        assertRatesEqual(updated, ratingMenuOne);
    }

    private void printWilsonScoreAndAvgStars(List<MenuRatingInfo> ratings) {
        ratings.forEach(r -> {
            System.out.println("Wilson Score: " + r.getWilsonScore());
            System.out.println("Average Stars: " + r.getAvgStars());
        });
    }
}
