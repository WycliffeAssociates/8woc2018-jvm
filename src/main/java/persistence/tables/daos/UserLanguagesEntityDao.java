/*
 * This file is generated by jOOQ.
 */
package persistence.tables.daos;


import java.util.List;

import javax.annotation.Generated;

import org.jooq.Configuration;
import org.jooq.Record3;
import org.jooq.impl.DAOImpl;

import persistence.tables.UserLanguagesEntity;
import persistence.tables.records.UserLanguagesEntityRecord;


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
public class UserLanguagesEntityDao extends DAOImpl<UserLanguagesEntityRecord, persistence.tables.pojos.UserLanguagesEntity, Record3<Integer, Integer, Integer>> {

    /**
     * Create a new UserLanguagesEntityDao without any configuration
     */
    public UserLanguagesEntityDao() {
        super(UserLanguagesEntity.USER_LANGUAGES_ENTITY, persistence.tables.pojos.UserLanguagesEntity.class);
    }

    /**
     * Create a new UserLanguagesEntityDao with an attached configuration
     */
    public UserLanguagesEntityDao(Configuration configuration) {
        super(UserLanguagesEntity.USER_LANGUAGES_ENTITY, persistence.tables.pojos.UserLanguagesEntity.class, configuration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Record3<Integer, Integer, Integer> getId(persistence.tables.pojos.UserLanguagesEntity object) {
        return compositeKeyRecord(object.getUserfk(), object.getLanguagefk(), object.getIssource());
    }

    /**
     * Fetch records that have <code>userfk IN (values)</code>
     */
    public List<persistence.tables.pojos.UserLanguagesEntity> fetchByUserfk(Integer... values) {
        return fetch(UserLanguagesEntity.USER_LANGUAGES_ENTITY.USERFK, values);
    }

    /**
     * Fetch records that have <code>languagefk IN (values)</code>
     */
    public List<persistence.tables.pojos.UserLanguagesEntity> fetchByLanguagefk(Integer... values) {
        return fetch(UserLanguagesEntity.USER_LANGUAGES_ENTITY.LANGUAGEFK, values);
    }

    /**
     * Fetch records that have <code>isSource IN (values)</code>
     */
    public List<persistence.tables.pojos.UserLanguagesEntity> fetchByIssource(Integer... values) {
        return fetch(UserLanguagesEntity.USER_LANGUAGES_ENTITY.ISSOURCE, values);
    }
}