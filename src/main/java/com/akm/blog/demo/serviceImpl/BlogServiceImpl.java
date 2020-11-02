package com.akm.blog.demo.serviceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.akm.blog.demo.common.CommonResponse;
import com.akm.blog.demo.DTO.BlogDTO;
import com.akm.blog.demo.common.CommonConstants;
import com.akm.blog.demo.model.Blog;
import com.akm.blog.demo.model.User;
import com.akm.blog.demo.repository.BlogRepository;
import com.akm.blog.demo.repository.RoleRepository;
import com.akm.blog.demo.repository.UserRepository;
import com.akm.blog.demo.security.SecurityUtils;
import com.akm.blog.demo.service.BlogService;
import com.fasterxml.jackson.databind.ObjectMapper;


@Service
public class BlogServiceImpl implements BlogService {

	private static final Logger LOGGER = LoggerFactory.getLogger(BlogServiceImpl.class);
	private static final ObjectMapper MAPPER = new ObjectMapper();
	private static final String MESSAGE = "message";


	@Autowired
	private BlogService blogService;

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private BlogRepository blogRepository;

	@Autowired
	RoleRepository roleRepository;

	@Override
	public String createBlog(BlogDTO blogBean) throws Exception {
		LOGGER.trace("Starting createBlog() from BlogServiceImpl");
		String returnValue = null;
		String errorMsg = null;
		CommonResponse dashboardResponse = new CommonResponse();
		try {

			User loggedUser = blogService.getLoggedInUserBean();

			if(loggedUser ==  null)
				throw new RuntimeException("Invalid User");

			List<Blog> blogList = new ArrayList<>();

			if(blogBean != null) {
				Blog blog = new Blog();
				blog.setName(blogBean.getName() != null ? blogBean.getName() : null);
				blog.setDescription(blogBean.getDescription() != null ? blogBean.getDescription() : null);
				blog.setUser(loggedUser);
				Blog savedBlog = blogRepository.save(blog);
				blogList.add(savedBlog);

				loggedUser.setBlog(blogList);
			}
			User savedUser = this.userRepo.save(loggedUser);

			LOGGER.trace("USER_BLOG:: "+blogList);
			if(savedUser != null) {
				dashboardResponse.setStatusCode(CommonConstants.SUCCESS);
				dashboardResponse.setResponseData(MESSAGE, "Blog created");
			}
		} catch (Exception e) {
			errorMsg = e.getMessage();
			LOGGER.error(errorMsg + "\n\r : "+ e.getStackTrace());
			e.printStackTrace();
		}
		if(errorMsg != null){
			dashboardResponse.setStatusCode(CommonConstants.FAIL);
			dashboardResponse.setErrorMsg(errorMsg);
		}
		returnValue = MAPPER.writeValueAsString(dashboardResponse);
		LOGGER.trace("Exiting createBlog() from BlogServiceImpl with return:: returnValue: "+returnValue);
		return returnValue;
	}


	@Override
	public String editBlog(BlogDTO blogBean, Long id) throws Exception {
		LOGGER.trace("Starting editBlog() from BlogServiceImpl");
		String returnValue = null;
		String errorMsg = null;
		CommonResponse dashboardResponse = new CommonResponse();
		try {

			User loggedUser = blogService.getLoggedInUserBean();

			if(loggedUser ==  null)
				throw new RuntimeException("Invalid User");

			List<Blog> blogList = new ArrayList<>();

			if(blogBean != null) {
				Blog blog = this.blogRepository.findById(id).orElseThrow(() -> new RuntimeException("No blog found for given ID"));

				//Blog blog = this.blogRepository.findById(id).get();
				blog.setName(blogBean.getName() != null ? blogBean.getName() : null);
				blog.setDescription(blogBean.getDescription() != null ? blogBean.getDescription() : null);
				blog.setUser(loggedUser);
				Blog savedBlog = blogRepository.save(blog);
				blogList.add(savedBlog);

				loggedUser.setBlog(blogList);
			}
			User savedUser = this.userRepo.save(loggedUser);

			LOGGER.trace("USER_EDUCATION:: "+blogList);
			if(savedUser != null) {
				dashboardResponse.setStatusCode(CommonConstants.SUCCESS);
				dashboardResponse.setResponseData(MESSAGE, "Blog edited");
			}
		} catch (Exception e) {
			errorMsg = e.getMessage();
			LOGGER.error(errorMsg + "\n\r : "+ e.getStackTrace());
			e.printStackTrace();
		}
		if(errorMsg != null){
			dashboardResponse.setStatusCode(CommonConstants.FAIL);
			dashboardResponse.setErrorMsg(errorMsg);
		}
		returnValue = MAPPER.writeValueAsString(dashboardResponse);
		LOGGER.trace("Exiting editBlog() from BlogServiceImpl with return:: returnValue: "+returnValue);
		return returnValue;
	}



	@Override
	public String getBlogs(Pageable pageable) throws Exception {
		LOGGER.trace("Starting getBlog() from BlogServiceImpl");
		String returnValue = null;
		String errorMsg = null;
		CommonResponse dashboardResponse = new CommonResponse();
		try {

			User loggedUser = blogService.getLoggedInUserBean();

			if(loggedUser ==  null)
				throw new RuntimeException("Invalid User");

			Page<Blog> blogs = this.blogRepository.findAllByUser(loggedUser, pageable);

			List<BlogDTO> blogList = new ArrayList<>();

			if(blogs != null ) {
				for(Blog blog : blogs) {
					BlogDTO blogBean = new BlogDTO();
					blogBean.setId(blog.getId());
					if(blog.getName() != null)
						blogBean.setName(blog.getName());
					if(blog.getDescription() != null)
						blogBean.setDescription(blog.getDescription());

					blogList.add(blogBean);
				}
			}

			LOGGER.trace("BLOG_LIST:: "+blogList);
			if(!blogList.isEmpty()) {
				dashboardResponse.setStatusCode(CommonConstants.SUCCESS);
				dashboardResponse.setResponseData(MESSAGE, blogList);
			}
		} catch (Exception e) {
			errorMsg = e.getMessage();
			LOGGER.error(errorMsg + "\n\r : "+ e.getStackTrace());
			e.printStackTrace();
		}
		if(errorMsg != null){
			dashboardResponse.setStatusCode(CommonConstants.FAIL);
			dashboardResponse.setErrorMsg(errorMsg);
		}
		returnValue = MAPPER.writeValueAsString(dashboardResponse);
		LOGGER.trace("Exiting getBlog() from BlogServiceImpl with return:: returnValue: "+returnValue);
		return returnValue;
	}


	@Override
	public User getLoggedInUserBean() throws Exception {
		User user = getUserWithAuthorities()
				.orElseThrow(() -> new RuntimeException("Account not found"));
		return user;
	}
	public Optional<User> getUserWithAuthorities() {
		return SecurityUtils.getCurrentUserLogin().flatMap(userRepo::findOneWithAuthoritiesByUsername);
	}



}
