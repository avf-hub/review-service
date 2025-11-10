package ru.javaops.cloudjava.reviewservice.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import ru.javaops.cloudjava.reviewservice.BaseIntegrationTest;
import ru.javaops.cloudjava.reviewservice.service.ReviewService;
import ru.javaops.cloudjava.reviewservice.storage.repositories.RatingRepository;

class ReviewServiceImplTest extends BaseIntegrationTest {

    @Autowired
    private ReviewService reviewService;
    @Autowired
    private RatingRepository ratingRepository;

}