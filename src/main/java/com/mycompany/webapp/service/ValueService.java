package com.mycompany.webapp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;

@Getter
@Component
public class ValueService {
	@Value("${jwt.security.key}")
	private String key;
}
