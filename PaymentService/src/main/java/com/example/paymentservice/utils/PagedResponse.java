package com.example.paymentservice.utils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class PagedResponse<T> {
    private List<T> content;
    private int totalPages;
    private long totalElements;
}