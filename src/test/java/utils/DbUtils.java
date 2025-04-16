package utils;

import model.user.dao.CustomerAddressDao;
import model.user.dao.CustomerDao;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import java.util.UUID;

public final class DbUtils {

    private static final SessionFactory sessionFactory;

    static {
        final StandardServiceRegistry registry = new StandardServiceRegistryBuilder().build();
        try {
            sessionFactory = new MetadataSources(registry)
                    .addAnnotatedClass(CustomerDao.class)
                    .addAnnotatedClass(CustomerAddressDao.class)
                    .buildMetadata()
                    .buildSessionFactory();
        } catch (Exception e) {
            StandardServiceRegistryBuilder.destroy(registry);
            throw new ExceptionInInitializerError("SessionFactory build failed: " + e.getMessage());
        }
    }

    private DbUtils() {
        // Utility class, prevent instantiation
    }

    public static CustomerDao getCustomerById(String id) {
        try (var session = sessionFactory.openSession()) {
            return session.createSelectionQuery("FROM CustomerDao WHERE id = :id", CustomerDao.class)
                    .setParameter("id", UUID.fromString(id))
                    .uniqueResult(); // or getSingleResult() depending on your use case
        }
    }
}
