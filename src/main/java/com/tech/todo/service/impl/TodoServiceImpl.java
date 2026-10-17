package com.tech.todo.service.impl;

import com.tech.todo.dto.TodoDto;
import com.tech.todo.entity.Todo;
import com.tech.todo.exception.ResourceNotFoundException;
import com.tech.todo.repository.TodoRepository;
import com.tech.todo.service.SubscriptionService;
import com.tech.todo.service.TodoService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TodoServiceImpl implements TodoService {

	@Autowired
	private TodoRepository todoRepository;

	@Autowired
	private ModelMapper modelMapper;

	@Autowired
	private SubscriptionService subscriptionService;

	@Override
	public TodoDto addTodo(TodoDto todoDto) {
		subscriptionService.assertCanCreateTodo();
		if (todoDto.getCreatedFor() == null) {
			todoDto.setCreatedFor(LocalDateTime.now());
		}
		Todo todo = modelMapper.map(todoDto, Todo.class);
		Todo savedTodo = todoRepository.save(todo);
		return modelMapper.map(savedTodo, TodoDto.class);
	}

	@Override
	public TodoDto getTodo(Long id) {
		Todo todo = todoRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Todo not found with id: " + id));
		return modelMapper.map(todo, TodoDto.class);
	}

	@Override
	public List<TodoDto> getAllTodo() {
		return todoRepository.findAllByOrderByCreatedForAsc().stream()
				.map(todo -> modelMapper.map(todo, TodoDto.class))
				.collect(Collectors.toList());
	}

	@Override
	public TodoDto updateTodo(TodoDto todoDto, Long id) {
		Todo todo = todoRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Todo not found with id: " + id));
		todo.setTitle(todoDto.getTitle());
		todo.setDescription(todoDto.getDescription());
		todo.setCompleted(todoDto.isCompleted());
		todo.setPriority(todoDto.getPriority());
		if (todoDto.getCreatedFor() != null) {
			todo.setCreatedFor(todoDto.getCreatedFor());
		}

		Todo updatedTodo = todoRepository.save(todo);
		return modelMapper.map(updatedTodo, TodoDto.class);
	}

	@Override
	public void deleteTodo(Long id) {
		todoRepository.deleteById(id);
	}

	@Override
	public TodoDto completeTodo(Long id) {
		Todo todo = todoRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Todo not found with id: " + id));
		todo.setCompleted(Boolean.TRUE);
		Todo savedTodo = todoRepository.save(todo);
		return modelMapper.map(savedTodo, TodoDto.class);
	}

	@Override
	public TodoDto incompleteTodo(Long id) {
		Todo todo = todoRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Todo not found with id: " + id));
		todo.setCompleted(Boolean.FALSE);
		Todo savedTodo = todoRepository.save(todo);
		return modelMapper.map(savedTodo, TodoDto.class);
	}
}
