package by.fedyushkin.bloomly.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import lombok.Data;

@Entity
@Data
public class Portfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "portfolio_id_generator")
    @SequenceGenerator(name = "portfolio_id_generator", sequenceName = "portfolio_id_seq", allocationSize = 1)
    private Long id;

    @Column(length = 2000)
    private String description;

    @ManyToOne(optional = false)
    @JoinColumn(name = "master_id", nullable = false)
    private Master master;

    @Column(length = 500)
    private String photoKey;
}
