package com.example.kickmatch.controller;

import com.example.kickmatch.domain.Location;
import com.example.kickmatch.service.LocationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/location")
public class LocationController {
    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping("/list")
    public String showLocationList(Model model) {
        model.addAttribute("locations", locationService.findAll());
        return "location/list";
    }

    @GetMapping("/{id}")
    public String showLocationDetail(@PathVariable Long id, Model model) {
        Location location = locationService.findById(id);
        model.addAttribute("location", location);
        return "location/detail"; // 일반 유저용 뷰
    }
}
