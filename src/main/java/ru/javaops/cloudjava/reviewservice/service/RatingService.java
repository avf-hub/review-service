package ru.javaops.cloudjava.reviewservice.service;

import ru.javaops.cloudjava.reviewservice.dto.GetRatingsRequest;
import ru.javaops.cloudjava.reviewservice.dto.RatingsResponse;
import ru.javaops.cloudjava.reviewservice.storage.model.MenuRatingInfo;

public interface RatingService {

    void saveRating(Long menuId, Integer rate);

    MenuRatingInfo getRatingOfMenu(Long menuId);

    RatingsResponse getRatingsOfMenus(GetRatingsRequest request);
}
