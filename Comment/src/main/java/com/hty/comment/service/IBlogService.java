package com.hty.comment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hty.comment.dto.Result;
import com.hty.comment.entity.Blog;


public interface IBlogService extends IService<Blog> {

    Result queryBlogById(Long id);

    Result queryHotBlog(Integer current);
}
