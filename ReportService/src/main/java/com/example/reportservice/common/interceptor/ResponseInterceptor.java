package com.example.reportservice.common.interceptor;

import com.example.reportservice.modules.feign.UserResponse;
import com.example.reportservice.utils.Response;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

// Đoạn code này được sử dụng để tạo format chung cho API Response

@RestControllerAdvice
public class ResponseInterceptor implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        // Chỉ apply cho REST controller của bạn, không apply cho SockJS handler
        String pkg = returnType.getContainingClass().getPackageName();
        return pkg.startsWith("com.example.reportservice.modules");
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> converterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        String path = request.getURI().getPath();
        // Nếu là WebSocket handshake thì trả nguyên body
        if (path.startsWith("/ws/") || path.equals("/ws/info")) {
            return body;
        }
        // --- else: phần wrap bình thường của bạn ---
        if (body instanceof Response) {
            return body;
        }
        if (body instanceof ResponseEntity<?>) {
            ResponseEntity<?> re = (ResponseEntity<?>) body;
            return Response.error(re.getStatusCodeValue(), "Request Error");
        }
        return Response.success(body);
    }
}


