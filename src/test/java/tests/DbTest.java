package tests;

import model.user.dao.CustomerDao;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.junit.jupiter.api.Test;

import java.util.List;

import static java.lang.System.out;
import static java.time.LocalTime.now;
import static org.junit.jupiter.api.Assertions.*;

public class DbTest {

    @Test
    void shouldFetchAllCustomersFromDatabase() {
        out.println("DB Test");
        // A SessionFactory is set up once for an application!
        final StandardServiceRegistry registry = new StandardServiceRegistryBuilder().build();
        try {
            SessionFactory sessionFactory =
                    new MetadataSources(registry)
                            .addAnnotatedClass(CustomerDao.class)
                            .buildMetadata()
                            .buildSessionFactory();

            sessionFactory.inTransaction(session -> {
                session.createSelectionQuery("from CustomerDao", CustomerDao.class)
                        .getResultList()
                        .forEach(customer -> {
                            out.println("Customer ID: " + customer.getId());
                        });
            });
        } catch (Exception e) {
            // The registry would be destroyed by the SessionFactory, but we
            // had trouble building the SessionFactory so destroy it manually.
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }

    @Test
    void shouldFetchAllCustomersFromDatabaseEnhanced() {
        out.println("Running DB verification test...");

        final StandardServiceRegistry registry = new StandardServiceRegistryBuilder().build();
        SessionFactory sessionFactory = null;

        try {
            sessionFactory = new MetadataSources(registry)
                    .addAnnotatedClass(CustomerDao.class)
                    .buildMetadata()
                    .buildSessionFactory();

            sessionFactory.inTransaction(session -> {
                List<CustomerDao> customers = session
                        .createSelectionQuery("from CustomerDao", CustomerDao.class)
                        .getResultList();

                assertNotNull(customers, "Customers list should not be null");
                assertFalse(customers.isEmpty(), "Customers list should not be empty");

                for (CustomerDao customer : customers) {
                    out.println("Customer ID: " + customer.getId());
                    out.println("Customer First Name: " + customer.getFirstName());
                    out.println("Customer Last Name: " + customer.getLastName());
                    out.println("Customer Middle Name: " + customer.getMiddleName());
                    out.println("Customer Birthday: " + customer.getBirthday());
                    out.println("Customer Email: " + customer.getEmail());
                    out.println("Customer Phone: " + customer.getPhone());
                    out.println("Customer Created At: " + customer.getCreatedAt());
                    out.println("Customer Updated At: " + customer.getUpdatedAt());
                    out.println("Checked At: " + now());

                    // Sample assertion: Replace these with actual expected values if known
                    assertNotNull(customer.getId(), "Customer ID should not be null");
                    assertNotNull(customer.getEmail(), "Customer email should not be null");
                }
            });

        } catch (Exception e) {
            StandardServiceRegistryBuilder.destroy(registry);
        } finally {
            if (sessionFactory != null) {
                sessionFactory.close();
            }
        }
    }
}
