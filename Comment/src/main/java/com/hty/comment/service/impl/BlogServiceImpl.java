package com.hty.comment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hty.comment.entity.Blog;
import com.hty.comment.mapper.BlogMapper;
import com.hty.comment.service.IBlogService;
import org.springframework.stereotype.Service;


@Service("blogService")
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements IBlogService {

}
