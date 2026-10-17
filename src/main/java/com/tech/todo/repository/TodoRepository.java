package com.tech.todo.repository;

import com.tech.todo.entity.Todo;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TodoRepository extends JpaRepository<Todo, Long> {

	List<Todo> findAllByOrderByCreatedForAsc();
}
