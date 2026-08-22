package by.fedyushkin.bloomly.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class Master {
    @Id
    private Long id;
    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private User user;
    @Column(length = 100)
    private String unp;
    @OneToMany(mappedBy = "master")
    private List<Session> sessions;
    @OneToMany(mappedBy = "master")
    private List<Service> services;
}
