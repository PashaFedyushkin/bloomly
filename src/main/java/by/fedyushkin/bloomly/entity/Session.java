package by.fedyushkin.bloomly.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class Session {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "session_id_generator")
    @SequenceGenerator(name = "session_id_generator", sequenceName = "session_id_seq", allocationSize = 1)
    private Long id;
    private LocalDateTime date;
    private Float finalPrice;
    @ManyToOne
    @JoinColumn(name = "master_id", referencedColumnName = "id")
    private Master master;
    @ManyToOne
    @JoinColumn(name = "service_id", referencedColumnName = "id")
    private Service service;
}
