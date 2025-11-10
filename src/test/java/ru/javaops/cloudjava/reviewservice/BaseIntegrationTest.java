package ru.javaops.cloudjava.reviewservice;

import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class BaseIntegrationTest extends BaseTest {

    @Autowired
    private EntityManager em;

    protected Long getReviewIdByMenuId(Long menuId) {
        return em.createQuery("select r.id from Review r where r.menuId= ?1", Long.class)
                .setParameter(1, menuId)
                .getSingleResult();
    }
}
