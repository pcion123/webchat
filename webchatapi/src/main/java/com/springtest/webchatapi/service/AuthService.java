package com.springtest.webchatapi.service;

import java.math.BigDecimal;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.github.f4b6a3.uuid.UuidCreator;
import com.springtest.webchatapi.exception.AccountAlreadyExistsException;
import com.springtest.webchatapi.exception.InvalidCredentialsException;
import com.springtest.webchatapi.model.dto.api.LoginRequest;
import com.springtest.webchatapi.model.dto.api.LoginResponse;
import com.springtest.webchatapi.model.dto.api.RegisterRequest;
import com.springtest.webchatapi.model.dto.api.RegisterResponse;
import com.springtest.webchatapi.model.entity.Account;
import com.springtest.webchatapi.model.entity.UserModel;
import com.springtest.webchatapi.repository.AccountMapper;
import com.springtest.webchatapi.repository.UserMapper;
import com.springtest.webchatapi.security.JwsTokenService;
import com.springtest.webchatapi.security.JwsTokenService.AccessToken;

@Service
public class AuthService {

    private static final String ACTIVE_STATUS = "ACTIVE";
    private static final Integer DEFAULT_LEVEL = 0;
    private static final BigDecimal DEFAULT_MONEY = new BigDecimal("0.000");

    private final UserMapper userMapper;
    private final AccountMapper accountMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwsTokenService jwsTokenService;

    public AuthService(UserMapper userMapper, AccountMapper accountMapper,
            PasswordEncoder passwordEncoder, JwsTokenService jwsTokenService) {
        this.userMapper = userMapper;
        this.accountMapper = accountMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwsTokenService = jwsTokenService;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String username = normalizeUsername(request.getUsername());
        if (accountMapper.countByUsername(username) > 0) {
            throw new AccountAlreadyExistsException();
        }

        String userId = newId();
        UserModel userModel = new UserModel();
        userModel.setUserId(userId);
        userModel.setUsername(username);
        userModel.setLevel(DEFAULT_LEVEL);
        userModel.setMoney(DEFAULT_MONEY);

        Account account = new Account();
        account.setAccountId(newId());
        account.setUserId(userId);
        account.setUsername(username);
        account.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        account.setStatus(ACTIVE_STATUS);

        try {
            userMapper.insert(userModel);
            accountMapper.insert(account);
        } catch (DuplicateKeyException exception) {
            throw new AccountAlreadyExistsException();
        }

        return new RegisterResponse(userId, username);
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        String username = normalizeUsername(request.getUsername());
        Account account = accountMapper.findByUsername(username)
                .orElseThrow(InvalidCredentialsException::new);
        if (!ACTIVE_STATUS.equals(account.getStatus())
                || !passwordEncoder.matches(request.getPassword(), account.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        accountMapper.updateLastLoginTime(account.getAccountId());
        Account updatedAccount = accountMapper.findByUsername(username)
                .orElseThrow(InvalidCredentialsException::new);
        AccessToken accessToken = jwsTokenService.issueAccessToken(updatedAccount.getUserId(),
                updatedAccount.getUsername());
        return new LoginResponse(updatedAccount.getUserId(), updatedAccount.getUsername(),
                updatedAccount.getLastLoginTime(), accessToken.value(), JwsTokenService.TOKEN_TYPE,
                accessToken.expiresAt());
    }

    private String normalizeUsername(String username) {
        return username.trim();
    }

    private String newId() {
        return UuidCreator.getTimeOrderedEpoch().toString().replace("-", "");
    }
}
