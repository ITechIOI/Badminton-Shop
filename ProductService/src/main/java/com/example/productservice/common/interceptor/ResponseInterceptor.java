package com.example.productservice.common.interceptor;

import com.example.productservice.modules.Products.dto.ProductResponse;
import com.example.productservice.utils.Response;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.List;

// Đoạn code này được sử dụng để tạo format chung cho API Response

@RestControllerAdvice
public class ResponseInterceptor implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true; // Áp dụng cho tất cả response
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {

        String path = ((ServletServerHttpRequest) request)
                .getServletRequest()
                .getRequestURI();
        // Bỏ qua actuator
        if (path.startsWith("/actuator")) {
            return body;
        }

        if (body instanceof Response) {
            return body;
        }

        if (body instanceof ProductResponse) {
            return body;
        }

        if (body instanceof String) {
            return body;
        }

        if (body instanceof List<?>) {
            return body;
        }

        if (body instanceof ResponseEntity<?>) {
            ResponseEntity<?> responseEntity = (ResponseEntity<?>) body;
            return Response.error(responseEntity.getStatusCode().value(), "Request Error");
        }

        // Bao response vào format thống nhất
        return Response.success(body);
    }
}