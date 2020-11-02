package com.akm.blog.demo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.akm.blog.demo.model.Blog;
import com.akm.blog.demo.model.User;

@Repository
public interface BlogRepository extends JpaRepository<Blog, Long> {
    
    Page<Blog> findAllByUser(User user, Pageable pageable);


}