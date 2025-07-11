package com.example.kickmatch.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "teams")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Team {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;           // 팀 이름

    private String logoUrl;        // 팀 로고 이미지 경로

    private String description;    // 팀 소개

    private String region;         // 활동 지역 (예: 서울 강남, 인천 등)

    private boolean isRecommended; // 추천 여부 플래그 (true면 추천팀으로 표시)
}
