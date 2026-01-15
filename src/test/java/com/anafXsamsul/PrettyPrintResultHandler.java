package com.anafXsamsul;

import java.nio.charset.StandardCharsets;

import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

public class PrettyPrintResultHandler {

    private static final ObjectWriter prettyWritter = new ObjectMapper().writerWithDefaultPrettyPrinter();

    public static ResultHandler printBodyOnly() {
        return (MvcResult result ) -> {
            String responseBody = result.getResponse().getContentAsString(StandardCharsets.UTF_8);

            if (!responseBody.isBlank()) {
                try {
                    Object json = new ObjectMapper().readValue(responseBody, Object.class);
                    System.out.println("\n Response Body");
                    System.out.println(prettyWritter.writeValueAsString(json));
                } catch (Exception e) {
                    System.out.println("\n Response Body");
                    System.out.println(responseBody);
                }
            }
        }; 
    }

}
