/*
 * This file is generated by jOOQ.
 */
package persistence.tables.daos;


import java.util.List;

import javax.annotation.Generated;

import org.jooq.Configuration;
import org.jooq.impl.DAOImpl;

import persistence.tables.UserEntity;
import persistence.tables.records.UserEntityRecord;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.11.2"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class UserEntityDao extends DAOImpl<UserEntityRecord, persistence.tables.pojos.UserEntity, Integer> {

    /**
     * Create a new UserEntityDao without any configuration
     */
    public UserEntityDao() {
        super(UserEntity.USER_ENTITY, persistence.tables.pojos.UserEntity.class);
    }

    /**
     * Create a new UserEntityDao with an attached configuration
     */
    public UserEntityDao(Configuration configuration) {
        super(UserEntity.USER_ENTITY, persistence.tables.pojos.UserEntity.class, configuration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Integer getId(persistence.tables.pojos.UserEntity object) {
        return object.getId();
    }

    /**
     * Fetch records that have <code>id IN (values)</code>
     */
    public List<persistence.tables.pojos.UserEntity> fetchById(Integer... values) {
        return fetch(UserEntity.USER_ENTITY.ID, values);
    }

    /**
     * Fetch a unique record that has <code>id = value</code>
     */
    public persistence.tables.pojos.UserEntity fetchOneById(Integer value) {
        return fetchOne(UserEntity.USER_ENTITY.ID, value);
    }

    /**
     * Fetch records that have <code>audioHash IN (values)</code>
     */
    public List<persistence.tables.pojos.UserEntity> fetchByAudiohash(String... values) {
        return fetch(UserEntity.USER_ENTITY.AUDIOHASH, values);
    }

    /**
     * Fetch records that have <code>audioPath IN (values)</code>
     */
    public List<persistence.tables.pojos.UserEntity> fetchByAudiopath(String... values) {
        return fetch(UserEntity.USER_ENTITY.AUDIOPATH, values);
    }

    /**
     * Fetch records that have <code>imgPath IN (values)</code>
     */
    public List<persistence.tables.pojos.UserEntity> fetchByImgpath(String... values) {
        return fetch(UserEntity.USER_ENTITY.IMGPATH, values);
    }
}