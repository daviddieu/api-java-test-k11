package model.user.dao;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "customers")
@Data
public class CustomerDao {
    @Id
    private UUID id;
    private String firstName;
    private String lastName;
    private String middleName;
    private String birthday;
    private String email;
    private String phone;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /*
    @OneToMany tells Hibernate that a customer can have many addresses.
    @JoinColumn(name = "customerId", referencedColumnName = "id") links the CustomerDao.id to CustomerAddressDao.customerId.
     */
    @OneToMany(fetch = FetchType.EAGER) // or LAZY, depending on use-case
    @JoinColumn(name = "customerId", referencedColumnName = "id") // join by foreign key
    private List<CustomerAddressDao> addresses;
}
