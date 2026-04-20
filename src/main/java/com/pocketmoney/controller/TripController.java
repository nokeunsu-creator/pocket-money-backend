package com.pocketmoney.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pocketmoney.entity.Trip;
import com.pocketmoney.repository.TripRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/trips")
public class TripController {

    private final TripRepository tripRepo;
    private final ObjectMapper objectMapper;

    public TripController(TripRepository tripRepo, ObjectMapper objectMapper) {
        this.tripRepo = tripRepo;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public List<Map<String, Object>> list() {
        return tripRepo.findAllByOrderByStartDateAsc().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    public Map<String, Object> get(@PathVariable Long id) {
        return toResponse(tripRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "여행 없음: " + id)));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> create(@RequestBody Map<String, Object> body) {
        Trip trip = new Trip();
        applyFields(trip, body);
        return toResponse(tripRepo.save(trip));
    }

    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Trip trip = tripRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "여행 없음: " + id));
        applyFields(trip, body);
        return toResponse(tripRepo.save(trip));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tripRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private void applyFields(Trip trip, Map<String, Object> body) {
        if (body.containsKey("title")) trip.setTitle((String) body.get("title"));
        if (body.containsKey("subtitle")) trip.setSubtitle((String) body.get("subtitle"));
        if (body.containsKey("familyInfo")) trip.setFamilyInfo((String) body.get("familyInfo"));
        if (body.containsKey("notes")) trip.setNotes((String) body.get("notes"));
        if (body.containsKey("startDate")) {
            Object v = body.get("startDate");
            trip.setStartDate(v == null || ((String) v).isEmpty() ? null : LocalDate.parse((String) v));
        }
        if (body.containsKey("endDate")) {
            Object v = body.get("endDate");
            trip.setEndDate(v == null || ((String) v).isEmpty() ? null : LocalDate.parse((String) v));
        }
        if (body.containsKey("days")) trip.setDaysJson(writeJson(body.get("days")));
        if (body.containsKey("budget")) trip.setBudgetJson(writeJson(body.get("budget")));
    }

    private String writeJson(Object value) {
        if (value == null) return null;
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "JSON 변환 실패: " + e.getMessage());
        }
    }

    private JsonNode readJson(String raw) {
        if (raw == null || raw.isEmpty()) return null;
        try {
            return objectMapper.readTree(raw);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private Map<String, Object> toResponse(Trip trip) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", trip.getId());
        out.put("title", trip.getTitle());
        out.put("subtitle", trip.getSubtitle());
        out.put("startDate", trip.getStartDate() != null ? trip.getStartDate().toString() : null);
        out.put("endDate", trip.getEndDate() != null ? trip.getEndDate().toString() : null);
        out.put("familyInfo", trip.getFamilyInfo());
        out.put("days", readJson(trip.getDaysJson()));
        out.put("budget", readJson(trip.getBudgetJson()));
        out.put("notes", trip.getNotes());
        return out;
    }
}
