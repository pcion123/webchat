package com.springtest.webchatapi.repository;

import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.springtest.webchatapi.model.entity.UserModel;

@Mapper
public interface UserMapper {

    int insert(UserModel userModel);

    Optional<UserModel> findById(@Param("userId") String userId);

    List<UserModel> findAll(@Param("username") String username, @Param("limit") int limit,
            @Param("offset") int offset);

    long count(@Param("username") String username);

    int update(UserModel userModel);

    int deleteById(@Param("userId") String userId);
}