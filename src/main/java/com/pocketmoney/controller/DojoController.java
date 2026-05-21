package com.pocketmoney.controller;

import com.pocketmoney.entity.DojoAttendance;
import com.pocketmoney.entity.DojoJournal;
import com.pocketmoney.entity.DojoSkill;
import com.pocketmoney.service.DojoService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dojo")
public class DojoController {

    private final DojoService dojoService;

    public DojoController(DojoService dojoService) {
        this.dojoService = dojoService;
    }

    // ===== Attendance =====

    /** GET /api/dojo/attendance?user=노건우 */
    @GetMapping("/attendance")
    public ResponseEntity<List<DojoAttendance>> getAttendance(@RequestParam String user) {
        return ResponseEntity.ok(dojoService.getAttendance(user));
    }

    /** POST /api/dojo/attendance  body: {user, date} */
    @PostMapping("/attendance")
    public ResponseEntity<DojoAttendance> addAttendance(@RequestBody Map<String, String> body) {
        String user = body.get("user");
        LocalDate date = LocalDate.parse(body.get("date"));
        return ResponseEntity.status(HttpStatus.CREATED).body(dojoService.addAttendance(user, date));
    }

    /** DELETE /api/dojo/attendance?user=노건우&date=2026-05-26 */
    @DeleteMapping("/attendance")
    public ResponseEntity<Void> removeAttendance(
            @RequestParam String user,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        dojoService.removeAttendance(user, date);
        return ResponseEntity.noContent().build();
    }

    // ===== Skills =====

    /** GET /api/dojo/skills?user=노건우 */
    @GetMapping("/skills")
    public ResponseEntity<List<DojoSkill>> getSkills(@RequestParam String user) {
        return ResponseEntity.ok(dojoService.getSkills(user));
    }

    /** POST /api/dojo/skills  body: {user, skillId} */
    @PostMapping("/skills")
    public ResponseEntity<DojoSkill> addSkill(@RequestBody Map<String, String> body) {
        String user = body.get("user");
        String skillId = body.get("skillId");
        return ResponseEntity.status(HttpStatus.CREATED).body(dojoService.addSkill(user, skillId));
    }

    /** DELETE /api/dojo/skills?user=노건우&skillId=jab */
    @DeleteMapping("/skills")
    public ResponseEntity<Void> removeSkill(
            @RequestParam String user,
            @RequestParam String skillId) {
        dojoService.removeSkill(user, skillId);
        return ResponseEntity.noContent().build();
    }

    // ===== Journal =====

    /** GET /api/dojo/journal  (전체) or ?user=노건우 (특정 자녀) */
    @GetMapping("/journal")
    public ResponseEntity<List<DojoJournal>> getJournal(@RequestParam(required = false) String user) {
        return ResponseEntity.ok(user == null ? dojoService.getJournal() : dojoService.getJournalByUser(user));
    }

    /** POST /api/dojo/journal  body: {user, date, text} */
    @PostMapping("/journal")
    public ResponseEntity<DojoJournal> addJournal(@RequestBody Map<String, String> body) {
        String user = body.get("user");
        LocalDate date = LocalDate.parse(body.get("date"));
        String text = body.get("text");
        return ResponseEntity.status(HttpStatus.CREATED).body(dojoService.addJournal(user, date, text));
    }

    /** DELETE /api/dojo/journal/{id} */
    @DeleteMapping("/journal/{id}")
    public ResponseEntity<Void> deleteJournal(@PathVariable Long id) {
        dojoService.deleteJournal(id);
        return ResponseEntity.noContent().build();
    }
}
