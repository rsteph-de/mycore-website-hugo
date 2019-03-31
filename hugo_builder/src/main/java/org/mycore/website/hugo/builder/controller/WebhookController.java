package org.mycore.website.hugo.builder.controller;

import org.mycore.website.hugo.builder.HugoBuilderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WebhookController {

	@Autowired
	HugoBuilderService hugobuilderService;
	
	@PostMapping("/rebuild")
	public String rebuildWebsite() {
		return null;
	}
	
	@GetMapping("/run")
	public String run() {
		hugobuilderService.run();
		return null;
	}
	
	
	@GetMapping("/status")
	public String status() {
		return null;
		
	}
}
