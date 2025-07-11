package com.example.kickmatch.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "matches")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Match {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private LocalDateTime matchDate;

    private Integer maxPlayers;

    private String status;  // 모집중, 마감 등

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;  // 작성자

    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL)
    private List<Participant> participants;

    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL)
    private List<Comment> comments;
}
