package com.example.kickmatch.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "locations")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Location {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String address;

    private String imageUrl;

    private String region; // 서울, 경기, 인천 등 분류용

    private String description;

    private Double latitude;  // 위도 (선택)
    private Double longitude; // 경도 (선택)

    @OneToMany(mappedBy = "location")
    private List<Match> matches;

}
