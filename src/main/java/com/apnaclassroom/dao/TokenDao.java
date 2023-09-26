package com.apnaclassroom.dao;

import com.apnaclassroom.enums.TokenType;
import com.apnaclassroom.model.management.Course;
import com.apnaclassroom.model.user.Token;
import com.apnaclassroom.model.user.User;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

@Repository
public class TokenDao {

    private static final Logger LOG = LoggerFactory.getLogger(TokenDao.class);

    private static final String INSERT_INTO_TOKENS="INSERT INTO tokens (token_id, expired, revoked, token, token_type, create_ts, update_ts, user_id) VALUES(?,?,?,?,?,?,?,?)";

    private static final String SELECT_TOKENS_BY_USERID="SELECT * FROM tokens where expired=0 and revoked=0 and user_id = ?";

    private static final String SELECT_TOKENS_BY_TOKEN_ID="SELECT * FROM tokens where token_id = ?";

    private static final String UPDATE_TOKEN_TO_EXPIRE="UPDATE tokens SET expired= ?, revoked = ? where token_id = ?";

    private static final String DELETE_EXPIRED_TOKEN_="DELETE FROM tokens where expired=1 and revoked=1";

    private final DataSource dataSource;

    public TokenDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Token> findAllValidTokenByUser(String username){
        List<Token> tokenList = new ArrayList<>();

        LOG.info("Fetching all valid tokens for userId: {}", username);
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_TOKENS_BY_USERID, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1,username);
            ResultSet resultSet = preparedStatement.executeQuery();

            if(Objects.nonNull(resultSet)){
                while (resultSet.next()){
                    Token token = new Token();
                    populateUserData(token, resultSet);
                    tokenList.add(token);
                }
            }
        } catch (Exception ex) {
            LOG.error("Unable to fetch all valid tokens for userId: {}, Exception: {}", username, ExceptionUtils.getStackTrace(ex));
        }

        return tokenList;
    }

    private void populateUserData(Token token, ResultSet resultSet) throws SQLException {
        User user = new User();
        user.setUsername(resultSet.getString("user_id"));
        token.setUser(user);

        token.setTokenId(resultSet.getString("token_id"));
        token.setTokenValue(resultSet.getString("token"));
        token.setExpired(resultSet.getBoolean("expired"));
        token.setRevoked(resultSet.getBoolean("revoked"));
        token.setTokenType(TokenType.valueOf(resultSet.getString("token_type")));
        token.setCreateTs(resultSet.getString("create_ts"));
        token.setUpdateTs(resultSet.getString("update_ts"));
    }

    public Optional<Token> findByToken(String tokenId){
        Token token = new Token();
        LOG.info("Fetching token for tokenId: {}", tokenId);
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_TOKENS_BY_TOKEN_ID, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1,tokenId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if(Objects.nonNull(resultSet)){
                while (resultSet.next()){
                    populateUserData(token, resultSet);
                }
            }
        } catch (Exception ex) {
            LOG.error("Unable to fetch token for tokenId: {}, Exception: {}", tokenId, ExceptionUtils.getStackTrace(ex));
        }

        return Optional.of(token);
    }

    public void saveToken(Token token) {
        boolean insertStatus;
        LOG.info("Saving new token data into DB for username: {}", token.getUser().getUsername());
        try (Connection connection = dataSource.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(INSERT_INTO_TOKENS, Statement.RETURN_GENERATED_KEYS)) {
            insertUserStatement(token, preparedStatement);

            insertStatus = preparedStatement.execute();
            if(insertStatus){
                LOG.info("Token data saved into DB successfully for username: {}", token.getUser().getUsername());
            }
        } catch (Exception ex) {
            LOG.error("Unable to save token data into DB for username: {}, Exception: {}", token.getUser().getUsername(), ExceptionUtils.getStackTrace(ex));
        }
    }

    private void insertUserStatement(Token token, PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.setString(1, token.getTokenId());
        preparedStatement.setBoolean(2, token.isExpired());
        preparedStatement.setBoolean(3, token.isRevoked());
        preparedStatement.setString(4, token.getTokenValue());
        preparedStatement.setString(5, token.getTokenType().toString());
        preparedStatement.setString(6, String.valueOf(LocalDateTime.now()));
        preparedStatement.setString(7, String.valueOf(LocalDateTime.now()));
        preparedStatement.setString(8, token.getUser().getUsername());
    }

    public void updateAllValidTokensToExpire(List<Token> validUserTokens) {
        if(!CollectionUtils.isEmpty(validUserTokens)){
            validUserTokens.stream().filter(Objects::nonNull)
                    .forEach(this::updateTokenToExpire);
        }
    }

    public void updateTokenToExpire(Token token){
        String tokenId = token.getTokenId();
        LOG.info("Updating token data into DB for tokenId: {}, to make it expire", tokenId);
        try (Connection connection = dataSource.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_TOKEN_TO_EXPIRE, Statement.RETURN_GENERATED_KEYS)) {
            updateTokenStatement(token, preparedStatement, tokenId);

            int updateStatus = preparedStatement.executeUpdate();
            if(updateStatus > 0){
                LOG.info("Token data updated into DB for tokenId: {}", tokenId);
            }
        } catch (Exception ex) {
            LOG.error("Unable to update token data into DB for tokenId: {}, Exception: {}", tokenId, ExceptionUtils.getStackTrace(ex));
        }
    }

    private void updateTokenStatement(Token token, PreparedStatement preparedStatement, String tokenId) throws SQLException {
        preparedStatement.setBoolean(1, token.isExpired());
        preparedStatement.setBoolean(2, token.isRevoked());
        preparedStatement.setString(3, tokenId);
    }

    public int deleteExpiredTokens() {
        int deleteCount = 0;
        LOG.info("Deleting expired tokens from tokens table...");

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement
                     = connection.prepareStatement(DELETE_EXPIRED_TOKEN_, Statement.RETURN_GENERATED_KEYS)) {
            deleteCount = preparedStatement.executeUpdate();

            if(deleteCount == 0){
                LOG.info("There is no expired token in table to delete");
            }
        } catch (Exception ex) {
            LOG.error("Unable to delete expired tokens from DB, Exception: {}", ExceptionUtils.getStackTrace(ex));
        }

        return deleteCount;
    }
}
