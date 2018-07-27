/*
 * This file is generated by jOOQ.
 */
package persistence;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Generated;

import org.jooq.Catalog;
import org.jooq.Table;
import org.jooq.impl.SchemaImpl;

import persistence.tables.LanguageEntity;
import persistence.tables.SqliteSequence;
import persistence.tables.UserEntity;
import persistence.tables.UserLanguagesEntity;
import persistence.tables.UserPreferencesEntity;


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
public class DefaultSchema extends SchemaImpl {

    private static final long serialVersionUID = 564916347;

    /**
     * The reference instance of <code></code>
     */
    public static final DefaultSchema DEFAULT_SCHEMA = new DefaultSchema();

    /**
     * The table <code>LANGUAGE ENTITY</code>.
     */
    public final LanguageEntity LANGUAGE_ENTITY = persistence.tables.LanguageEntity.LANGUAGE_ENTITY;

    /**
     * The table <code>USER ENTITY</code>.
     */
    public final UserEntity USER_ENTITY = persistence.tables.UserEntity.USER_ENTITY;

    /**
     * The table <code>USER LANGUAGES ENTITY</code>.
     */
    public final UserLanguagesEntity USER_LANGUAGES_ENTITY = persistence.tables.UserLanguagesEntity.USER_LANGUAGES_ENTITY;

    /**
     * The table <code>USER PREFERENCES ENTITY</code>.
     */
    public final UserPreferencesEntity USER_PREFERENCES_ENTITY = persistence.tables.UserPreferencesEntity.USER_PREFERENCES_ENTITY;

    /**
     * The table <code>sqlite_sequence</code>.
     */
    public final SqliteSequence SQLITE_SEQUENCE = persistence.tables.SqliteSequence.SQLITE_SEQUENCE;

    /**
     * No further instances allowed
     */
    private DefaultSchema() {
        super("", null);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Catalog getCatalog() {
        return DefaultCatalog.DEFAULT_CATALOG;
    }

    @Override
    public final List<Table<?>> getTables() {
        List result = new ArrayList();
        result.addAll(getTables0());
        return result;
    }

    private final List<Table<?>> getTables0() {
        return Arrays.<Table<?>>asList(
            LanguageEntity.LANGUAGE_ENTITY,
            UserEntity.USER_ENTITY,
            UserLanguagesEntity.USER_LANGUAGES_ENTITY,
            UserPreferencesEntity.USER_PREFERENCES_ENTITY,
            SqliteSequence.SQLITE_SEQUENCE);
    }
}
