package ru.javaops.cloudjava.reviewservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.*;
import ru.javaops.cloudjava.reviewservice.dto.*;
import ru.javaops.cloudjava.reviewservice.service.RatingService;
import ru.javaops.cloudjava.reviewservice.service.ReviewService;

import java.util.List;

@Tag(name = "ReviewController", description = "REST API для работы с отзывами.")
@Slf4j
@RestController
@RequestMapping("/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {
    public static final String USER_HEADER = "X-User-Name";

    private final ReviewService reviewService;
    private final RatingService ratingService;

    @Operation(
            summary = "${api.review-create.summary}",
            description = "${api.review-create.description}"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "${api.response.createOk}"),
            @ApiResponse(
                    responseCode = "409",
                    description = "${api.response.createConflict}",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "${api.response.createBadRequest}",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    )),
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewResponse createReview(@RequestBody
                                       @Valid
                                       CreateReviewRequest request,
                                       @RequestHeader(USER_HEADER)
                                       @NotBlank(message = "Имя пользователя не может быть пустым.")
                                       String username) {
        log.info("Received POST request to create Review: {} by user: {}",
                request, username);
        return reviewService.createReview(request, username);
    }

    @Operation(
            summary = "${api.review-get.summary}",
            description = "${api.review-get.description}"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "${api.response.getOk}"),
            @ApiResponse(
                    responseCode = "404",
                    description = "${api.response.notFound}",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "${api.response.getBadRequest}",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    )),
    })
    @GetMapping("/{id}")
    public ReviewResponse getReview(@PathVariable("id") @Positive Long reviewId) {
        log.info("Received request to GET Review with id={}", reviewId);
        return reviewService.getReview(reviewId);
    }

    @Operation(
            summary = "${api.user-reviews-get.summary}",
            description = "${api.user-reviews-get.description}"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "${api.response.getUserReviewsOk}"),
            @ApiResponse(
                    responseCode = "400",
                    description = "${api.response.getUserReviewsBadRequest}",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    ))
    })
    @GetMapping("/my")
    public List<ReviewResponse> getReviewsOfUser(@RequestHeader(USER_HEADER)
                                                 @NotBlank(message = "Имя пользователя не может быть пустым.")
                                                 String username,
                                                 @RequestParam(value = "from", defaultValue = "0")
                                                 @PositiveOrZero(message = "Страница должна быть >= 0.")
                                                 int from,
                                                 @RequestParam(value = "size", defaultValue = "10")
                                                 @Positive(message = "Размер страницы должен быть > 0.")
                                                 int size,
                                                 @RequestParam(value = "sortBy", defaultValue = "date_asc")
                                                 @NotBlank(message = "Параметр сортировки не должен быть пустым.")
                                                 String sortBy) {
        log.info("Received request to GET list of Reviews made by user: {}", username);
        return reviewService.getReviewsOfUser(username, SortBy.fromString(sortBy), from, size);
    }

    @Operation(
            summary = "${api.menu-reviews-get.summary}",
            description = "${api.menu-reviews-get.description}"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "${api.response.getMenuReviewsOk}"),
            @ApiResponse(
                    responseCode = "400",
                    description = "${api.response.getMenuReviewsBadRequest}",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    ))
    })
    @GetMapping("/menu-item/{menuId}")
    public RatedReviewsResponse getReviewsOfMenu(@PathVariable("menuId")
                                                 @Positive(message = "Идентификатор блюда должен быть > 0.")
                                                 Long menuId,
                                                 @RequestParam(value = "from", defaultValue = "0")
                                                 @PositiveOrZero(message = "Страница должна быть >= 0.")
                                                 int from,
                                                 @RequestParam(value = "size", defaultValue = "10")
                                                 @Positive(message = "Размер страницы должен быть > 0.")
                                                 int size,
                                                 @RequestParam(value = "sortBy", defaultValue = "date_asc")
                                                 @NotBlank(message = "Параметр сортировки не должен быть пустым.")
                                                 String sortBy) {
        log.info("Received request to GET list of reviews with ratings of menu with id={}", menuId);
        return reviewService.getRatedReviewsForMenu(menuId, SortBy.fromString(sortBy), from, size);
    }

    @Operation(
            summary = "${api.ratings-get.summary}",
            description = "${api.ratings-get.description}"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "${api.response.getRatingsOk}"),
            @ApiResponse(
                    responseCode = "400",
                    description = "${api.response.getRatingsBadRequest}",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    ))
    })
    @PostMapping("/ratings")
    public RatingsResponse getRatingsOfMenus(@RequestBody @Valid GetRatingsRequest request) {
        log.info("Received POST request to get ratings of menus: {}", request.getMenuIds());
        return ratingService.getRatingsOfMenus(request);
    }
}