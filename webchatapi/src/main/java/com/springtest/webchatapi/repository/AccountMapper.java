package com.springtest.webchatapi.repository;

import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.springtest.webchatapi.model.entity.Account;

@Mapper
public interface AccountMapper {

    int insert(Account account);

    Optional<Account> findByUsername(@Param("username") String username);

    Optional<Account> findByUserId(@Param("userId") String userId);

    long countByUsername(@Param("username") String username);

    int updateLastLoginTime(@Param("accountId") String accountId);
}