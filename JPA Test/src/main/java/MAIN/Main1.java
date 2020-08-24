package MAIN;

import Entity.Product;
import org.hibernate.Transaction;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class Main1 {
    public static void main(String[] args) {

        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("my-persistence-unit");
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction entityTransaction = entityManager.getTransaction();

        Query query = entityManager.createQuery("Select e.name from Employee e");
        List<String> list = query.getResultList();
        for (String e : list) {
            System.out.println("Employee NAME :" + e);
        }
    }
}
