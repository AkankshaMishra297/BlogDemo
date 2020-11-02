package com.akm.blog.demo.service;

import org.springframework.data.domain.Pageable;

import com.akm.blog.demo.DTO.BlogDTO;
import com.akm.blog.demo.model.User;

public interface BlogService {

	
	public String getBlogs(Pageable pageable) throws Exception;

	public User getLoggedInUserBean() throws Exception;

	public String createBlog(BlogDTO blogBean) throws Exception;

	public String editBlog(BlogDTO blogBean, Long id) throws Exception;


}
