package by.fedyushkin.bloomly.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Data
public class WorkInterval {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "work_interval_id_generator")
    @SequenceGenerator(name = "work_interval_id_generator", sequenceName = "work_interval_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "master_id", referencedColumnName = "id")
    private Master master;

    private LocalDate workDate;
    private LocalTime startTime;
    private LocalTime endTime;
}
