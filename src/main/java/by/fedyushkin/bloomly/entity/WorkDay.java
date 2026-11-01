package by.fedyushkin.bloomly.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.SequenceGenerator;
import lombok.Data;

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

    @OneToMany(mappedBy = "workDay", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("startTime asc")
    private List<WorkInterval> workIntervals = new ArrayList<>();
}
