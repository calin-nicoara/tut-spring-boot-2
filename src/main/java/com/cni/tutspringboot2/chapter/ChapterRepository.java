package com.cni.tutspringboot2.chapter;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface ChapterRepository
	extends ReactiveCrudRepository<Chapter, String> {

}
