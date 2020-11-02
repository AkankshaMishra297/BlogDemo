package com.akm.blog.demo.controller;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.akm.blog.demo.DTO.BlogDTO;
import com.akm.blog.demo.model.Role;
import com.akm.blog.demo.repository.RoleRepository;
import com.akm.blog.demo.service.BlogService;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/api")
public class BlogController {

	private static final Logger LOGGER = LoggerFactory.getLogger(BlogController.class);

	@Autowired
	private BlogService blogService;

	@Autowired
	private RoleRepository roleRepository;

	/*
	 * Create Blog
	 */
	@PostMapping(value = "/createBlog", produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasRole('USER')")
	public ResponseEntity<?> createBlog(@RequestBody BlogDTO blogBean) throws Exception {
		LOGGER.info("Starting createBlog() from BlogController");
		ResponseEntity<?> responseEntity = null;
		String jsonString = blogService.createBlog(blogBean);
		if(jsonString != null){
			responseEntity = ResponseEntity.ok(jsonString);
		} else
			responseEntity = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		LOGGER.info("Exiting createBlog() from BlogController");
		return responseEntity;
	}


	/*
	 * Edit Blog
	 */
	@PutMapping(value = "/editBlog/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasRole('USER')")
	public ResponseEntity<?> editBlog(@RequestBody BlogDTO blogBean, @PathVariable("id") Long id) throws Exception {
		LOGGER.info("Starting editBlog() from BlogController");
		ResponseEntity<?> responseEntity = null;
		String jsonString = blogService.editBlog(blogBean,id);
		if(jsonString != null){
			responseEntity = ResponseEntity.ok(jsonString);
		} else
			responseEntity = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		LOGGER.info("Exiting editBlog() from BlogController");
		return responseEntity;
	}


	/*
	 * Read Blogs
	 */
	@GetMapping(value = "/getBlogs", produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasRole('USER')")
	public ResponseEntity<?> getBlogs(Pageable pageable) throws Exception {
		LOGGER.info("Starting getBlogs() from BlogController");
		ResponseEntity<?> responseEntity = null;
		String jsonString = blogService.getBlogs(pageable);
		if(jsonString != null){
			responseEntity = ResponseEntity.ok(jsonString);
		} else
			responseEntity = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		LOGGER.info("Exiting getBlogs() from BlogController");
		return responseEntity;
	}


	/*
	 * Add Role
	 */
	@PostMapping(value = "/auth/addRole", produces = MediaType.APPLICATION_JSON_VALUE)
	public Role addRole(@RequestBody Role role ) throws IOException {

		Role savedRole = roleRepository.save(role);
		return savedRole;

	}

}
