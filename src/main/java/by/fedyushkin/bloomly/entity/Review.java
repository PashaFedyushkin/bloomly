package by.fedyushkin.bloomly.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "review_id_generator")
    @SequenceGenerator(name = "review_id_generator", sequenceName = "review_id_seq", allocationSize = 1)
    private Long id;
    @Column(length = 1000, nullable = false)
    private String text;
    @Column(nullable = false)
    private Integer stars;
    private LocalDateTime date;
    @OneToOne(optional = false)
    @JoinColumn(name = "session_id", nullable = false, unique = true)
    private Session session;
    @ElementCollection
    @CollectionTable(name = "review_photo", joinColumns = @JoinColumn(name = "review_id"))
    @OrderColumn(name = "photo_order")
    @Column(name = "object_key", length = 500, nullable = false)
    private List<String> photoKeys = new ArrayList<>();
}
