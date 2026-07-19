package com.softech.entrenaback.student;

import com.softech.entrenaback.student.dto.StudentRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PostMapping
    public ResponseEntity<Student> create(Authentication auth, @Valid @RequestBody StudentRequest request) {
        var student = studentService.create(auth.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(student);
    }

    @GetMapping
    public ResponseEntity<List<?>> list(Authentication auth) {
        var students = studentService.list(auth.getName());
        return ResponseEntity.ok(students);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Student> getById(Authentication auth, @PathVariable String id) {
        var student = studentService.getById(auth.getName(), id);
        return ResponseEntity.ok(student);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Student> update(Authentication auth, @PathVariable String id,
                                          @Valid @RequestBody StudentRequest request) {
        var student = studentService.update(auth.getName(), id, request);
        return ResponseEntity.ok(student);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication auth, @PathVariable String id) {
        studentService.delete(auth.getName(), id);
        return ResponseEntity.noContent().build();
    }
}
