package ru.javaops.cloudjava.reviewservice.storage.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.javaops.cloudjava.reviewservice.storage.model.Review;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findAllByCreatedBy(String createdBy, Pageable pageable);

    List<Review> findAllByMenuId(Long menuId, Pageable pageable);
}