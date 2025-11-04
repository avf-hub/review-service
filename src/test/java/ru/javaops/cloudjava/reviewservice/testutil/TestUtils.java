package ru.javaops.cloudjava.reviewservice.testutil;

import ru.javaops.cloudjava.reviewservice.storage.model.MenuRatingInfo;
import ru.javaops.cloudjava.reviewservice.storage.model.Rating;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class TestUtils {

    public static void incrementExpectedRating(Rating rating, int one, int two, int three, int four, int five) {
        rating.setRateOne(rating.getRateOne() + one);
        rating.setRateTwo(rating.getRateTwo() + two);
        rating.setRateThree(rating.getRateThree() + three);
        rating.setRateFour(rating.getRateFour() + four);
        rating.setRateFive(rating.getRateFive() + five);
    }

    public static void assertRatesEqual(Rating actual, Rating expected) {
        assertThat(actual)
                .usingRecursiveComparison()
                .comparingOnlyFields("rateOne", "rateTwo", "rateThree", "rateFour", "rateFive")
                .isEqualTo(expected);
    }

    public static void compareMenuInfos(List<Rating> ratings, List<MenuRatingInfo> actual) {
        assertThat(actual.size()).isEqualTo(ratings.size());
        var sortedActual = actual.stream().sorted(Comparator.comparing(MenuRatingInfo::getMenuId)).toList();
        for (int i = 0; i < sortedActual.size(); i++) {
            compareMenuInfo(ratings.get(i), sortedActual.get(i));
        }
    }

    public static void compareMenuInfo(Rating rating, MenuRatingInfo actual) {
        MenuRatingInfo expected = calculateWilsonScoreAvgStars(rating);
        assertThat(actual).isEqualTo(expected);
    }

    public static MenuRatingInfo calculateWilsonScoreAvgStars(Rating rating) {
        var positive = rating.getRateFive() * 1.0 +
                rating.getRateFour() * 0.75 +
                rating.getRateThree() * 0.5 +
                rating.getRateTwo() * 0.25 +
                rating.getRateOne() * 0.0;
        var negative = rating.getRateOne() * 1.0 +
                rating.getRateTwo() * 0.75 +
                rating.getRateThree() * 0.5 +
                rating.getRateFour() * 0.25 +
                rating.getRateFive() * 0.0;
        var total = rating.getRateOne() +
                rating.getRateTwo() +
                rating.getRateThree() +
                rating.getRateFour() +
                rating.getRateFive();

        var wilsonScore = (float) ((
                (positive + 1.9208) / (positive + negative) -
                        1.96 * Math.sqrt((positive * negative) / (positive + negative) + 0.9604) / (positive + negative)
        ) / (1 + 3.8416 / (positive + negative)));
        var avgStars = (float) round((((positive / total) * 4) + 1) * 2, 2) / 2;
        return new MenuRatingInfo(rating.getMenuId(), wilsonScore, avgStars);
    }

    private static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}