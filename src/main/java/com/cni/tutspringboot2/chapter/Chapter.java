package com.cni.tutspringboot2.chapter;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document
public class Chapter {

	@Id
	private String id;
	private String name;

	public Chapter(String name) {
		this.name = name;
	}

}
