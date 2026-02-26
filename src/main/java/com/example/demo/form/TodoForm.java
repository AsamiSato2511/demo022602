package com.example.demo.form;

import com.example.demo.model.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TodoForm {
    @NotBlank(message = "作成者は必須です")
    @Size(max = 50, message = "作成者は50文字以内で入力してください")
    private String author;

    @NotBlank(message = "タイトルは必須です")
    @Size(max = 100, message = "タイトルは100文字以内で入力してください")
    private String title;

    @Size(max = 500, message = "詳細は500文字以内で入力してください")
    private String detail;

    @NotNull(message = "優先度は必須です")
    private Priority priority = Priority.MEDIUM;
}