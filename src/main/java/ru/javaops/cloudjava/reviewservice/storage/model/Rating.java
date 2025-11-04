package ru.javaops.cloudjava.reviewservice.storage.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.Hibernate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Table(name = "ratings")
@Entity
public class Rating {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "menu_id", nullable = false)
    private Long menuId;
    @Column(name = "rate_one", nullable = false)
    private Integer rateOne;
    @Column(name = "rate_two", nullable = false)
    private Integer rateTwo;
    @Column(name = "rate_three", nullable = false)
    private Integer rateThree;
    @Column(name = "rate_four", nullable = false)
    private Integer rateFour;
    @Column(name = "rate_five", nullable = false)
    private Integer rateFive;
    @Column(name = "wilson_score", nullable = false)
    private Float wilsonScore;
    @Column(name = "avg_stars", nullable = false)
    private Float avgStars;

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
        return getId() != null && getId().equals(((Rating) o).getId());
    }

    @Override
    public final int hashCode() {
        return Hibernate.getClass(this).hashCode();
    }
}
