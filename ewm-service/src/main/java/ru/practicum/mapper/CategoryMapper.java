package ru.practicum.mapper;

import ru.practicum.model.Category;
import ru.practicum.model.dto.CategoryDto;

public class CategoryMapper {
    public static Category fromCategoryDto(CategoryDto categoryDto) {
        return Category.builder().name(categoryDto.getName()).build();
    }

    public static CategoryDto toCategoryDto(Category category) {
        return CategoryDto.builder().id(category.getId()).name(category.getName()).build();
    }
}
