package com.example.demo.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.demo.mapper.TodoMapper;
import com.example.demo.model.Priority;
import com.example.demo.model.Todo;

@Service
public class TodoService {
    private final TodoMapper todoMapper;

    public TodoService(TodoMapper todoMapper) {
        this.todoMapper = todoMapper;
    }

    public Page<Todo> findPageByCondition(String keyword, String sort, String direction, Pageable pageable) {
        String normalizedKeyword = keyword == null ? "" : keyword.trim();
        String normalizedSort = normalizeSort(sort);
        String normalizedDirection = normalizeDirection(direction);

        int limit = pageable.getPageSize();
        int offset = (int) pageable.getOffset();

        List<Todo> content = todoMapper.findPageByCondition(normalizedKeyword, normalizedSort, normalizedDirection, limit, offset);
        long total = todoMapper.countByCondition(normalizedKeyword);

        return new PageImpl<>(content, pageable, total);
    }

    public Todo findById(Long id) {
        return todoMapper.findById(id);
    }

    public void create(String title, Priority priority) {
        Todo todo = new Todo();
        todo.setTitle(title);
        todo.setCompleted(false);
        todo.setPriority(priority == null ? Priority.MEDIUM : priority);
        todoMapper.insert(todo);
    }

    public boolean update(Long id, String title) {
        Todo todo = todoMapper.findById(id);
        if (todo == null) {
            return false;
        }
        todo.setTitle(title);
        return todoMapper.update(todo) > 0;
    }

    public boolean toggleCompleted(Long id) {
        Todo todo = todoMapper.findById(id);
        if (todo == null) {
            return false;
        }
        todo.setCompleted(!Boolean.TRUE.equals(todo.getCompleted()));
        return todoMapper.update(todo) > 0;
    }

    public boolean deleteById(Long id) {
        return todoMapper.deleteById(id) > 0;
    }

    public String normalizeSort(String sort) {
        if ("title".equals(sort) || "completed".equals(sort) || "priority".equals(sort) || "createdAt".equals(sort)) {
            return sort;
        }
        return "createdAt";
    }

    public String normalizeDirection(String direction) {
        if ("asc".equalsIgnoreCase(direction)) {
            return "asc";
        }
        return "desc";
    }
}