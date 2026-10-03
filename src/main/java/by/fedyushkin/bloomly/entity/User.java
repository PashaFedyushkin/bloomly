package by.fedyushkin.bloomly.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_id_generator")
    @SequenceGenerator(name = "user_id_generator", sequenceName = "user_id_seq", allocationSize = 1)
    private Long id;
    private String name;
    private String lastName;
    private LocalDateTime creationDate;
    private String phone;
    @Column(length = 500)
    private String photoKey;
    @ManyToMany
    @JoinTable(
            name = "user_role",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id")
    )
    private List<Role> roles;
    @OneToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH}, mappedBy = "user")
    private Master master;
    @OneToMany(mappedBy = "user")
    private List<Session> sessions;

    public User () {}

    public User (String phone) {
        this.phone = phone;
    }
}
