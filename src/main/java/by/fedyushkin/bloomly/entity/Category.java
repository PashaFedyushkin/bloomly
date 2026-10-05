package by.fedyushkin.bloomly.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "category_id_generator")
    @SequenceGenerator(name = "category_id_generator", sequenceName = "category_id_seq", allocationSize = 1)
    private Long id;

    @Column(length = 200, nullable = false)
    private String name;
}
