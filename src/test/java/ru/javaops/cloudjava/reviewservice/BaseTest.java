package ru.javaops.cloudjava.reviewservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import ru.javaops.cloudjava.reviewservice.storage.model.Rating;
import ru.javaops.cloudjava.reviewservice.storage.repositories.RatingRepository;

import static ru.javaops.cloudjava.reviewservice.testutil.TestUtils.incrementExpectedRating;

@ActiveProfiles("test")
@SqlGroup(
        {
                @Sql(
                        scripts = "classpath:insert-data.sql",
                        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
                ),
                @Sql(
                        scripts = "classpath:delete-data.sql",
                        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
                )
        }
)
public abstract class BaseTest {
    @Autowired
    protected RatingRepository ratingRepository;

    protected void incrementRatingsForMenuId(Rating expected,
                                             int oneTimes,
                                             int twoTimes,
                                             int threeTimes,
                                             int fourTimes,
                                             int fiveTimes) {
        incrementActualRatingsForMenuId(expected.getMenuId(), oneTimes, twoTimes, threeTimes, fourTimes, fiveTimes);
        incrementExpectedRating(expected, oneTimes, twoTimes, threeTimes, fourTimes, fiveTimes);
    }

    protected void incrementActualRatingsForMenuId(Long menuId,
                                                   int oneTimes,
                                                   int twoTimes,
                                                   int threeTimes,
                                                   int fourTimes,
                                                   int fiveTimes) {
        incrementRatingForMenuId(menuId, 1, oneTimes);
        incrementRatingForMenuId(menuId, 2, twoTimes);
        incrementRatingForMenuId(menuId, 3, threeTimes);
        incrementRatingForMenuId(menuId, 4, fourTimes);
        incrementRatingForMenuId(menuId, 5, fiveTimes);
    }

    private void incrementRatingForMenuId(Long menuId,
                                          Integer rating,
                                          int times) {
        for (int i = 0; i < times; i++) {
            ratingRepository.incrementRating(menuId, rating);
        }
    }
}