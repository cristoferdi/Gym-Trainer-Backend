package com.softech.entrenaback.student;

import com.softech.entrenaback.student.dto.StudentRequest;
import com.softech.entrenaback.trainer.TrainerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class StudentService {

    private final StudentRepository studentRepository;
    private final TrainerRepository trainerRepository;

    public StudentService(StudentRepository studentRepository, TrainerRepository trainerRepository) {
        this.studentRepository = studentRepository;
        this.trainerRepository = trainerRepository;
    }

    public Student create(String trainerEmail, StudentRequest request) {
        var trainer = trainerRepository.findByEmail(trainerEmail)
                .orElseThrow(() -> new IllegalArgumentException("Entrenador no encontrado"));

        var student = new Student();
        student.setTrainer(trainer);
        student.setFullName(request.fullName());
        student.setPhoneNumber(request.phoneNumber());
        student.setAge(request.age());
        student.setGender(request.gender());
        student.setDisciplina(request.disciplina());
        student.setObjetivos(request.objetivos());

        return studentRepository.save(student);
    }

    public List<Student> list(String trainerEmail) {
        var trainer = trainerRepository.findByEmail(trainerEmail)
                .orElseThrow(() -> new IllegalArgumentException("Entrenador no encontrado"));
        return studentRepository.findByTrainerId(trainer.getId());
    }

    public Student getById(String trainerEmail, String studentId) {
        var student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Alumno no encontrado"));

        var trainer = trainerRepository.findByEmail(trainerEmail)
                .orElseThrow(() -> new IllegalArgumentException("Entrenador no encontrado"));

        if (!student.getTrainer().getId().equals(trainer.getId())) {
            throw new IllegalArgumentException("Alumno no pertenece a este entrenador");
        }

        return student;
    }

    public Student update(String trainerEmail, String studentId, StudentRequest request) {
        var student = getById(trainerEmail, studentId);

        student.setFullName(request.fullName());
        student.setPhoneNumber(request.phoneNumber());
        student.setAge(request.age());
        student.setGender(request.gender());
        student.setDisciplina(request.disciplina());
        student.setObjetivos(request.objetivos());

        return studentRepository.save(student);
    }

    public void delete(String trainerEmail, String studentId) {
        var student = getById(trainerEmail, studentId);
        studentRepository.delete(student);
    }
}
