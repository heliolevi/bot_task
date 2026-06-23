package com.br.task.controller;

import com.br.task.model.Task;
import com.br.task.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    // Rota para criar uma nova tarefa (POST http://localhost:8081/api/tasks)
    @PostMapping
    public ResponseEntity<Task> criar(@RequestBody Task task) {
        try {
            Task novaTarefa = taskService.criarTarefa(task);
            return ResponseEntity.ok(novaTarefa);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Rota para listar todas as tarefas (GET http://localhost:8081/api/tasks)
    @GetMapping
    public ResponseEntity<List<Task>> listar() {
        List<Task> tarefas = taskService.listarTodas();
        return ResponseEntity.ok(tarefas);
    }
}