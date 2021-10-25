package ru.job4j.chat.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.sql.Timestamp;

import static org.springframework.http.HttpStatus.Series.CLIENT_ERROR;

@Component
public class RestTemplateResponseErrorHandler implements ResponseErrorHandler {
    private final ObjectMapper objectMapper;

    public RestTemplateResponseErrorHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Data
    private static class ErrorMessage {
        private Timestamp timestamp;
        private int status;
        private String error;
        private String message;
        private String path;
    }

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        return response.getStatusCode().series() == CLIENT_ERROR;
    }

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        String responseBody = new String(response.getBody().readAllBytes());
        ErrorMessage errorMessage = objectMapper.readValue(responseBody, ErrorMessage.class);
        throw new ResponseStatusException(response.getStatusCode(), errorMessage.getMessage());
    }
}

