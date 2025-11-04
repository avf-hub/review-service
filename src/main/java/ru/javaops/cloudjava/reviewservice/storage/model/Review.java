package ru.javaops.cloudjava.reviewservice.storage.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.Hibernate;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.format.annotation.DateTimeFormat;
import ru.javaops.cloudjava.reviewservice.util.DateUtil;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Table(name = "reviews")
@Entity
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "menu_id", nullable = false)
    private Long menuId;
    @Column(name = "created_by", nullable = false)
    private String createdBy;
    @Column(name = "comment")
    private String comment;
    @Column(name = "rate", nullable = false)
    private int rate;
    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    @DateTimeFormat(pattern = DateUtil.DATE_FORMAT)
    private LocalDateTime createdAt;

    public static Rating newRating(Long menuId) {
        return newRating(menuId, 0, 0, 0, 0, 0);
    }

    public static Rating newRating(Long menuId, int one, int two, int three, int four, int five) {
        return Rating.builder()
                .id(null)
                .menuId(menuId)
                .rateOne(one)
                .rateTwo(two)
                .rateThree(three)
                .rateFour(four)
                .rateFive(five)
                .wilsonScore(0.0f)
                .avgStars(0.0f)
                .build();
    }

    /**
     * @see <a href="https://stackoverflow.com/a/78077907/548473">Переопределяем equals и hashCode</a>
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        return getId() != null && getId().equals(((Review) o).getId());
    }

    @Override
    public final int hashCode() {
        return Hibernate.getClass(this).hashCode();
    }
}
