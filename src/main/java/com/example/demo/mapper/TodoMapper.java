package com.example.demo.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.example.demo.model.Todo;

public interface TodoMapper {
    List<Todo> findPageByCondition(@Param("keyword") String keyword,
                                   @Param("sort") String sort,
                                   @Param("direction") String direction,
                                   @Param("limit") int limit,
                                   @Param("offset") int offset);

    long countByCondition(@Param("keyword") String keyword);

    Todo findById(Long id);

    int insert(Todo todo);

    int update(Todo todo);

    int deleteById(Long id);
}