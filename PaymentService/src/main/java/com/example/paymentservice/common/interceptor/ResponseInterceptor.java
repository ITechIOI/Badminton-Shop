package com.example.paymentservice.common.interceptor;

import com.example.paymentservice.utils.Response;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

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

        // Nếu body đã là Response rồi thì không cần bao thêm
        if (body instanceof Response) {
            return body;
        }

        // Nếu body là String, trả về nguyên bản
        if (body instanceof String) {
            return body;
        }

        if (body instanceof ModelAndView) {
            return body;
        }

        // Nếu body là lỗi, tự động trả về format error
        if (body instanceof ResponseEntity<?>) {
            ResponseEntity<?> responseEntity = (ResponseEntity<?>) body;
            return Response.error(responseEntity.getStatusCode().value(), "Request Error");
        }

        // Bao response vào format thống nhất
        return Response.success(body);
    }
}
