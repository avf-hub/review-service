package ru.javaops.cloudjava.reviewservice.testutil;

import ru.javaops.cloudjava.reviewservice.dto.CreateReviewRequest;
import ru.javaops.cloudjava.reviewservice.storage.model.Rating;

import java.util.List;

import static ru.javaops.cloudjava.reviewservice.testutil.TestConstants.*;

public class TestData {
    public static Rating ratingMenuOne() {
        return Rating.newRating(MENU_ONE, 0, 0, 0, 0, 1);
    }

    public static Rating ratingMenuTwo() {
        return Rating.newRating(MENU_TWO);
    }

    public static Rating ratingMenuFour() {
        return Rating.newRating(MENU_FOUR, 0, 0, 0, 0, 1);
    }

    public static Rating ratingMenuFive() {
        return Rating.newRating(MENU_FIVE, 0, 0, 0, 1, 0);
    }

    public static Rating ratingMenuSix() {
        return Rating.newRating(MENU_SIX, 0, 0, 1, 0, 0);
    }

    public static Rating ratingMenuSeven() {
        return Rating.newRating(MENU_SEVEN, 0, 1, 0, 0, 0);
    }

    public static Rating ratingMenuEight() {
        return Rating.newRating(MENU_EIGHT, 1, 0, 0, 0, 0);
    }

    public static Rating ratingMenuTen() {
        return Rating.newRating(MENU_TEN, 1, 1, 1, 1, 1);
    }

    public static List<Rating> allRatingsHaveReviews() {
        return List.of(
                ratingMenuFour(),
                ratingMenuFive(),
                ratingMenuSix(),
                ratingMenuSeven(),
                ratingMenuEight()
        );
    }

    public static CreateReviewRequest createReviewRequest(Long menuId, Integer rate) {
        return CreateReviewRequest.builder()
                .menuId(menuId)
                .comment("This is a comment")
                .rate(rate)
                .build();
    }
}