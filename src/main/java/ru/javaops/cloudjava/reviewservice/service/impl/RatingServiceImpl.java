package ru.javaops.cloudjava.reviewservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.javaops.cloudjava.reviewservice.dto.GetRatingsRequest;
import ru.javaops.cloudjava.reviewservice.dto.RatingsResponse;
import ru.javaops.cloudjava.reviewservice.service.RatingService;
import ru.javaops.cloudjava.reviewservice.storage.model.MenuRatingInfo;
import ru.javaops.cloudjava.reviewservice.storage.repositories.RatingRepository;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RatingServiceImpl implements RatingService {

    private final RatingRepository repository;

    @Transactional
    @Override
    public void saveRating(Long menuId, Integer rate) {
        ensureRatingExists(menuId);
        repository.incrementRating(menuId, rate);
    }


    @Override
    public MenuRatingInfo getRatingOfMenu(Long menuId) {
        return repository.findRatingInfoByMenuId(menuId)
                .orElse(defaultRating(menuId));
    }

    @Override
    public RatingsResponse getRatingsOfMenus(GetRatingsRequest request) {
        var menuIdToRatings = repository
                .findRatingInfosByMenuIdIn(request.getMenuIds())
                .stream()
                .collect(Collectors.toMap(MenuRatingInfo::getMenuId, Function.identity()));

        List<MenuRatingInfo> result = request.getMenuIds().stream()
                .map(id -> menuIdToRatings.getOrDefault(id, defaultRating(id)))
                .collect(Collectors.toList());

        return RatingsResponse.builder()
                .menuRatings(result)
                .build();
    }

    private void ensureRatingExists(Long menuId) {
        repository.insertNoConflict(menuId);
    }

    private MenuRatingInfo defaultRating(Long menuId) {
        return MenuRatingInfo.builder()
                .menuId(menuId)
                .wilsonScore(0.0f)
                .avgStars(0.0f)
                .build();
    }
}