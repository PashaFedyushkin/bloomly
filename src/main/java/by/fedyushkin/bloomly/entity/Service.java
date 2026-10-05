package by.fedyushkin.bloomly.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class Service {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "service_id_generator")
    @SequenceGenerator(name = "service_id_generator", sequenceName = "service_id_seq", allocationSize = 1)
    private Long id;
    private String name;
    private String description;
    private Float price;
    private Integer durationMinutes;
    @ManyToOne
    @JoinColumn(name = "master_id", referencedColumnName = "id")
    private Master master;
    @ManyToOne
    @JoinColumn(name = "category_id", referencedColumnName = "id")
    private Category category;
    @OneToMany(mappedBy = "service")
    private List<Session> sessions;
}
