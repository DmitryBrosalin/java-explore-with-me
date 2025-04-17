package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.CategoryMapper;
import ru.practicum.model.Category;
import ru.practicum.model.dto.CategoryDto;
import ru.practicum.repository.CategoryRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryDto addCategory(CategoryDto categoryDto) {
        validateNewCategory(categoryDto);
        Category category = CategoryMapper.fromCategoryDto(categoryDto);
        if (categoryRepository.findByName(categoryDto.getName()).isPresent()) {
            throw new ConflictException("Category named: " + categoryDto.getName() + " already exists.");
        }
        return CategoryMapper.toCategoryDto(categoryRepository.save(category));

    }


    public CategoryDto getCategory(int id) {
        Optional<Category> category = categoryRepository.findById(id);
        if (category.isPresent()) {
            return CategoryMapper.toCategoryDto(category.get());
        } else {
            throw new NotFoundException("Category with id=" + id + " was not found");
        }
    }

    public Category findCategory(int id) {
        Optional<Category> category = categoryRepository.findById(id);
        if (category.isPresent()) {
            return category.get();
        } else {
            throw new NotFoundException("Category with id=" + id + " was not found");
        }
    }

    public List<CategoryDto> getCategories(int from, int size) {
        return categoryRepository.findCategories(from, size).stream().map(CategoryMapper::toCategoryDto).toList();
    }

    public void deleteCategory(int id) {
        validateCategory(id);
        try {
            categoryRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Cannot delete category because of connected events");
        }
    }

    public CategoryDto updateCategory(int id, CategoryDto categoryDto) {
        validateCategory(id);
        if (categoryDto.getName() != null
            && categoryDto.getName().length() > 50) {
            throw new BadRequestException("Category name cannot be greater than 50. It is: " + categoryDto.getName().length());
        }
        Category category = CategoryMapper.fromCategoryDto(categoryDto);
        category.setId(id);
        if (categoryRepository.findByName(categoryDto.getName()).isPresent()) {
            Category oldCategory = findCategory(id);
            if (!oldCategory.equals(category)) {
                throw new ConflictException("Category named: " + categoryDto.getName() + " already exists.");
            }
        }
        return CategoryMapper.toCategoryDto(categoryRepository.save(category));
    }

    private void validateCategory(int id) {
        if (categoryRepository.findById(id).isEmpty()) {
            throw new NotFoundException("Category with id=" + id + "was not found");
        }
    }

    private void validateNewCategory(CategoryDto categoryDto) {
        if (categoryDto.getName() == null || categoryDto.getName().isBlank()) {
            throw new BadRequestException("Category name cannot be null or empty");
        }
        if (categoryDto.getName().length() > 50) {
            throw new BadRequestException("Category name cannot be greater than 50. It is: " + categoryDto.getName().length());
        }
    }
}
