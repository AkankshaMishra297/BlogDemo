package com.akm.blog.demo.DTO;

import java.io.Serializable;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDTO implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@JsonProperty("user_name")
	@NotNull
	@Pattern(regexp ="([A-Za-z0-9]){4,12}", message = "invalid username")
	private String userName;
	
	@JsonProperty("user_password")
	@NotNull
    //@Pattern(regexp = "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{8,36}$", message = "Invalid Password")
	private String userPassword;
	
	@JsonProperty("active")
	private String active;
	
	
	@JsonProperty("blog")
	@Valid
	private  List<BlogDTO> blogBean;

	
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}	
	
	public List<BlogDTO> getBlogBean() {
		return blogBean;
	}

	public void setBlogBean(List<BlogDTO> blogBean) {
		this.blogBean = blogBean;
	}

	public String getUserPassword() {
		return userPassword;
	}

	public void setUserPassword(String userPassword) {
		this.userPassword = userPassword;
	}

	public String getActive() {
		return active;
	}

	public void setActive(String active) {
		this.active = active;
	}

	@Override
	public String toString() {
		return "UserBean [userName=" + userName + ", userPassword=" + userPassword + ", active=" + active
				+ ", blogBean=" + blogBean + "]";
	}

	
}
