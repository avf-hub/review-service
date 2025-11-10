package ru.javaops.cloudjava.reviewservice.storage.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import ru.javaops.cloudjava.reviewservice.storage.model.MenuRatingInfo;
import ru.javaops.cloudjava.reviewservice.storage.model.Rating;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface RatingRepository extends JpaRepository<Rating, Long> {

    @Query("""
                SELECT new ru.javaops.cloudjava.reviewservice.storage.model.MenuRatingInfo(
                    r.menuId,
                    r.wilsonScore,
                    r.avgStars
                ) FROM Rating r WHERE r.menuId = :menuId
            """)
    Optional<MenuRatingInfo> findRatingInfoByMenuId(@Param("menuId") Long menuId);

    @Query(
            """
            SELECT new ru.javaops.cloudjava.reviewservice.storage.model.MenuRatingInfo(
                r.menuId,
                r.wilsonScore,
                r.avgStars
            ) FROM Rating r WHERE r.menuId in :menuIds
            """
    )
    List<MenuRatingInfo> findRatingInfosByMenuIdIn(@Param("menuIds") Set<Long> menuIds);

    @Modifying
    @Transactional
    @Query(value = """
            UPDATE ratings SET 
            rate_one = CASE WHEN :rating = 1 THEN rate_one + 1 ELSE rate_one END, 
            rate_two = CASE WHEN :rating = 2 THEN rate_two + 1 ELSE rate_two END, 
            rate_three = CASE WHEN :rating = 3 THEN rate_three + 1 ELSE rate_three END, 
            rate_four = CASE WHEN :rating = 4 THEN rate_four + 1 ELSE rate_four END, 
            rate_five = CASE WHEN :rating = 5 THEN rate_five + 1 ELSE rate_five END 
            WHERE menu_id = :menuId
            """,
            nativeQuery = true)
    void incrementRating(@Param("menuId") Long menuId, @Param("rating") Integer rating);

    @Modifying
    @Transactional
    @Query(value = """
                INSERT INTO ratings(menu_id) values(:menuId) ON CONFLICT DO NOTHING
            """, nativeQuery = true)
    void insertNoConflict(@Param("menuId") Long menuId);

    Optional<Rating> findByMenuId(Long menuId);
}