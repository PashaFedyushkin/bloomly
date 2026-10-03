package by.fedyushkin.bloomly.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "address_id_generator")
    @SequenceGenerator(name = "address_id_generator", sequenceName = "address_id_seq", allocationSize = 1)
    private Long id;
    @Column(length = 200)
    private String country;
    @Column(length = 200)
    private String region;
    @Column(length = 200)
    private String locality;
    @Column(length = 200)
    private String district;
    @Column(length = 200)
    private String place;
    @Column(length = 200)
    private String street;
    @Column(length = 50)
    private String house;
    @Column(length = 50)
    private String apartment;
}
