package by.fedyushkin.bloomly.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.SequenceGenerator;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class WorkInterval {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "work_interval_id_generator")
    @SequenceGenerator(name = "work_interval_id_generator", sequenceName = "work_interval_id_seq", allocationSize = 1)
    private Long id;

    private LocalTime startTime;
    private LocalTime endTime;

    @ManyToMany(mappedBy = "workIntervals")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private List<WorkDay> workDays = new ArrayList<>();
}
