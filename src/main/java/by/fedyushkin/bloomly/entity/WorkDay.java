package by.fedyushkin.bloomly.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.SequenceGenerator;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class WorkDay {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "work_day_id_generator")
    @SequenceGenerator(name = "work_day_id_generator", sequenceName = "work_day_id_seq", allocationSize = 1)
    private Long id;

    private LocalDate workDate;

    @ManyToOne(optional = false)
    @JoinColumn(name = "master_id", nullable = false)
    private Master master;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "work_day_interval",
            joinColumns = @JoinColumn(name = "work_day_id"),
            inverseJoinColumns = @JoinColumn(name = "work_interval_id")
    )
    @OrderBy("startTime asc")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private List<WorkInterval> workIntervals = new ArrayList<>();
}
