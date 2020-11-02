package com.akm.blog.demo.model;

import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import org.hibernate.annotations.CreationTimestamp;


@Entity
@Table(name = "users", uniqueConstraints = {
		@UniqueConstraint(columnNames = {
				"username"
		})
		
})
public class User{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	//    @NotBlank
	//    @Size(min=3, max = 50)
	//    private String name;

	@NotBlank
	@Size(min=3, max = 50)
	private String username;

	//    @NaturalId
	//    @NotBlank
	//    @Size(max = 50)
	//    @Email
	//    private String email;

	@NotBlank
	@Size(min=6, max = 100)
	private String password;

	private boolean active;

	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private Calendar createdAt;

	@OneToMany(mappedBy="user", cascade = CascadeType.ALL)
	private List<Blog> blog;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "user_roles", 
	joinColumns = @JoinColumn(name = "user_id"), 
	inverseJoinColumns = @JoinColumn(name = "role_id"))
	private Set<Role> roles = new HashSet<>();

	public User() {}

	
	public Long getId() {
		return id;
	}

	public User(String username, String password,
			boolean active, Calendar createdAt,
			List<Blog> blog) {
		super();
		this.username = username;
		this.password = password;
		this.active = active;
		this.createdAt = createdAt;
		this.blog = blog;
		
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}



	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Set<Role> getRoles() {
		return roles;
	}

	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public Calendar getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Calendar createdAt) {
		this.createdAt = createdAt;
	}


	public List<Blog> getBlog() {
		return blog;
	}


	public void setBlog(List<Blog> blog) {
		this.blog = blog;
	}


	@Override
	public String toString() {
		return "User [id=" + id + ", username=" + username + ", password=" + password + ", active=" + active
				+ ", createdAt=" + createdAt + ", blog=" + blog + ", roles=" + roles + "]";
	}

	
}