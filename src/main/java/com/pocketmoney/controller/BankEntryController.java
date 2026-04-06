package com.pocketmoney.controller;

import com.pocketmoney.entity.BankEntry;
import com.pocketmoney.service.BankEntryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bank")
public class BankEntryController {

    private final BankEntryService bankEntryService;

    public BankEntryController(BankEntryService bankEntryService) {
        this.bankEntryService = bankEntryService;
    }

    @PostMapping("/entries")
    public ResponseEntity<BankEntry> createEntry(@Valid @RequestBody BankEntry entry) {
        BankEntry saved = bankEntryService.createEntry(entry);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/entries/{id}")
    public ResponseEntity<BankEntry> updateEntry(@PathVariable Long id, @Valid @RequestBody BankEntry entry) {
        BankEntry updated = bankEntryService.updateEntry(id, entry);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/entries/{id}")
    public ResponseEntity<Void> deleteEntry(@PathVariable Long id) {
        bankEntryService.deleteEntry(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/entries")
    public ResponseEntity<List<BankEntry>> getEntries(
            @RequestParam String user,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {

        List<BankEntry> entries;
        if (year != null && month != null) {
            entries = bankEntryService.getEntriesByMonth(user, year, month);
        } else {
            entries = bankEntryService.getAllEntries(user);
        }
        return ResponseEntity.ok(entries);
    }

    @GetMapping("/entries/deleted")
    public ResponseEntity<List<BankEntry>> getDeletedEntries(@RequestParam String user) {
        List<BankEntry> entries = bankEntryService.getDeletedEntries(user);
        return ResponseEntity.ok(entries);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats(
            @RequestParam String user,
            @RequestParam int year,
            @RequestParam int month) {

        Map<String, Object> stats = bankEntryService.getMonthlyStats(user, year, month);
        return ResponseEntity.ok(stats);
    }
}
