package by.fedyushkin.bloomly.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Portfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "portfolio_id_generator")
    @SequenceGenerator(name = "portfolio_id_generator", sequenceName = "portfolio_id_seq", allocationSize = 1)
    private Long id;

    @Column(length = 2000)
    private String description;

    @OneToOne(optional = false)
    @JoinColumn(name = "master_id", nullable = false, unique = true)
    private Master master;

    @ElementCollection
    @CollectionTable(name = "portfolio_photo", joinColumns = @JoinColumn(name = "portfolio_id"))
    @OrderColumn(name = "photo_order")
    @Column(name = "object_key", length = 500, nullable = false)
    private List<String> photoKeys = new ArrayList<>();
}
