package com.example.demo.controller;

import com.example.demo.form.TodoForm;
import com.example.demo.model.Priority;
import com.example.demo.model.Todo;
import com.example.demo.service.TodoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

@Controller
@RequestMapping("/todo")
public class TodoController {
    private static final int PAGE_SIZE = 10;

    private final TodoService todoService;

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    @GetMapping({"", "/"})
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false, defaultValue = "createdAt") String sort,
                       @RequestParam(required = false, defaultValue = "desc") String direction,
                       @PageableDefault(size = PAGE_SIZE) Pageable pageable,
                       Model model) {
        String normalizedKeyword = keyword == null ? "" : keyword.trim();
        String normalizedSort = todoService.normalizeSort(sort);
        String normalizedDirection = todoService.normalizeDirection(direction);

        Pageable fixedSizePageable = PageRequest.of(pageable.getPageNumber(), PAGE_SIZE);
        Page<Todo> pageData = todoService.findPageByCondition(normalizedKeyword, normalizedSort, normalizedDirection, fixedSizePageable);

        long totalElements = pageData.getTotalElements();
        long start = totalElements == 0 ? 0 : fixedSizePageable.getOffset() + 1;
        long end = totalElements == 0 ? 0 : fixedSizePageable.getOffset() + pageData.getNumberOfElements();

        model.addAttribute("pageData", pageData);
        model.addAttribute("todos", pageData.getContent());
        model.addAttribute("keyword", normalizedKeyword);
        model.addAttribute("sort", normalizedSort);
        model.addAttribute("direction", normalizedDirection);
        model.addAttribute("totalElements", totalElements);
        model.addAttribute("start", start);
        model.addAttribute("end", end);
        return "todo/list";
    }

    @GetMapping("/new")
    public String newTodo(Model model) {
        model.addAttribute("todoForm", new TodoForm());
        return "todo/form";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable("id") Long id,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(required = false, defaultValue = "createdAt") String sort,
                       @RequestParam(required = false, defaultValue = "desc") String direction,
                       @RequestParam(required = false, defaultValue = "0") int page,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        Todo todo = todoService.findById(id);
        if (todo == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "指定されたToDoが見つかりません");
            return redirectToList(keyword, sort, direction, page);
        }
        model.addAttribute("todo", todo);
        model.addAttribute("keyword", keyword == null ? "" : keyword.trim());
        model.addAttribute("sort", todoService.normalizeSort(sort));
        model.addAttribute("direction", todoService.normalizeDirection(direction));
        model.addAttribute("page", Math.max(page, 0));
        return "todo/edit";
    }

    @PostMapping("/{id}/update")
    public String update(@PathVariable("id") Long id,
                         @RequestParam("title") String title,
                         @RequestParam(required = false) String keyword,
                         @RequestParam(required = false, defaultValue = "createdAt") String sort,
                         @RequestParam(required = false, defaultValue = "desc") String direction,
                         @RequestParam(required = false, defaultValue = "0") int page,
                         RedirectAttributes redirectAttributes) {
        boolean updated = todoService.update(id, title);
        if (updated) {
            redirectAttributes.addFlashAttribute("successMessage", "更新が完了しました");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "更新に失敗しました");
        }
        return redirectToList(keyword, sort, direction, page);
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable("id") Long id,
                         @RequestParam(required = false) String keyword,
                         @RequestParam(required = false, defaultValue = "createdAt") String sort,
                         @RequestParam(required = false, defaultValue = "desc") String direction,
                         @RequestParam(required = false, defaultValue = "0") int page,
                         RedirectAttributes redirectAttributes) {
        boolean toggled = todoService.toggleCompleted(id);
        if (!toggled) {
            redirectAttributes.addFlashAttribute("errorMessage", "状態変更に失敗しました");
        }
        return redirectToList(keyword, sort, direction, page);
    }

    @PostMapping("/confirm")
    public String confirm(@Valid @ModelAttribute("todoForm") TodoForm todoForm,
                          BindingResult bindingResult,
                          Model model) {
        if (bindingResult.hasErrors()) {
            return "todo/form";
        }
        model.addAttribute("title", todoForm.getTitle());
        model.addAttribute("priority", todoForm.getPriority());
        return "todo/confirm";
    }

    @PostMapping("/complete")
    public String complete(@RequestParam(name = "title", required = false) String title,
                           @RequestParam(name = "priority", required = false) Priority priority,
                           RedirectAttributes redirectAttributes) {
        if (title == null || title.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "タイトルが未入力です。入力画面からやり直してください。");
            return "redirect:/todo/new";
        }
        todoService.create(title.trim(), priority);
        redirectAttributes.addFlashAttribute("successMessage", "ToDoを登録しました");
        return "redirect:/todo";
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable("id") Long id,
                         @RequestParam(required = false) String keyword,
                         @RequestParam(required = false, defaultValue = "createdAt") String sort,
                         @RequestParam(required = false, defaultValue = "desc") String direction,
                         @RequestParam(required = false, defaultValue = "0") int page,
                         RedirectAttributes redirectAttributes) {
        try {
            boolean deleted = todoService.deleteById(id);
            if (deleted) {
                redirectAttributes.addFlashAttribute("successMessage", "ToDoを削除しました");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "削除に失敗しました");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "削除に失敗しました");
        }
        return redirectToList(keyword, sort, direction, page);
    }

    private String redirectToList(String keyword, String sort, String direction, int page) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/todo");

        String normalizedKeyword = keyword == null ? "" : keyword.trim();
        String normalizedSort = todoService.normalizeSort(sort);
        String normalizedDirection = todoService.normalizeDirection(direction);

        if (!normalizedKeyword.isEmpty()) {
            builder.queryParam("keyword", normalizedKeyword);
        }
        builder.queryParam("sort", normalizedSort);
        builder.queryParam("direction", normalizedDirection);
        builder.queryParam("page", Math.max(page, 0));

        return "redirect:" + builder.toUriString();
    }
}