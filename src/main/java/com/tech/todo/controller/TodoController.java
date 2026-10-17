package com.tech.todo.controller;

import com.tech.todo.dto.TodoDto;
import com.tech.todo.service.TodoService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/todos")
public class TodoController {

    @Autowired
    private TodoService todoService;

    @GetMapping
    public ResponseEntity<List<TodoDto>> listAllTodo() {
        List<TodoDto> allTodo = todoService.getAllTodo();
        return new ResponseEntity<>(allTodo, HttpStatus.OK);
    }

    @GetMapping("{id}")
    public ResponseEntity<TodoDto> get(@PathVariable Long id) {
        TodoDto todo = todoService.getTodo(id);
        return new ResponseEntity<>(todo, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<TodoDto> addTodo(@RequestBody TodoDto todoDto) {
        TodoDto savedTodo = todoService.addTodo(todoDto);
        return new ResponseEntity<>(savedTodo, HttpStatus.CREATED);
    }

    //Updating the entore object partially hence put
    @PutMapping("{id}")
    public ResponseEntity<TodoDto> update(@RequestBody TodoDto todoDto, @PathVariable Long id) {
        TodoDto updatedTodoDto = todoService.updateTodo(todoDto, id);
        return new ResponseEntity<>(updatedTodoDto, HttpStatus.OK);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<String> deleteTodo(@PathVariable Long id) {
        todoService.deleteTodo(id);
        return ResponseEntity.ok("Todo Deleted successfully.");
    }

    //Updating the object partially hence patch
    @PatchMapping("{id}/complete")
    public ResponseEntity<TodoDto> completeTodo(@PathVariable Long id)
    {
        TodoDto todoDto = todoService.completeTodo(id);
        return new ResponseEntity<>(todoDto, HttpStatus.OK);
    }

    @PatchMapping("{id}/incomplete")
    public ResponseEntity<TodoDto> incompleteTodo(@PathVariable Long id)
    {
        TodoDto todoDto = todoService.incompleteTodo(id);
        return new ResponseEntity<>(todoDto, HttpStatus.OK);
    }

}
