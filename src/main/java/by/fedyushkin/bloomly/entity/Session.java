package by.fedyushkin.bloomly.entity;

import by.fedyushkin.bloomly.enums.CSessionStatus;
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
    @Enumerated(EnumType.STRING)
    private CSessionStatus status;
    @ManyToOne
    @JoinColumn(name = "master_id", referencedColumnName = "id")
    private Master master;
    @ManyToOne
    @JoinColumn(name = "service_id", referencedColumnName = "id")
    private Service service;
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
    @OneToOne(mappedBy = "session")
    private Review review;
}
