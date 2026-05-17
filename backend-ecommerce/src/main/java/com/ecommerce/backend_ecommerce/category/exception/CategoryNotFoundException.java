package com.ecommerce.backend_ecommerce.category.exception;

public class CategoryNotFoundException extends RuntimeException {

    public CategoryNotFoundException(Integer id) {
        super("Category not found with id: " + id);
    }

    public CategoryNotFoundException(String name) {
        super("Category not found with name: " + name);
    }
}
