package ru.javaops.cloudjava.reviewservice.storage.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import ru.javaops.cloudjava.reviewservice.storage.model.MenuRatingInfo;
import ru.javaops.cloudjava.reviewservice.storage.model.Rating;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface RatingRepository extends JpaRepository<Rating, Long> {

    Optional<MenuRatingInfo> findRatingInfoByMenuId(@Param("menuId") Long menuId);

    List<MenuRatingInfo> findRatingInfosByMenuIdIn(@Param("menuIds") Set<Long> menuIds);

    void incrementRating(@Param("menuId") Long menuId, @Param("rating") Integer rating);

    void insertNoConflict(@Param("menuId") Long menuId);

    Optional<Rating> findByMenuId(Long menuId);
}