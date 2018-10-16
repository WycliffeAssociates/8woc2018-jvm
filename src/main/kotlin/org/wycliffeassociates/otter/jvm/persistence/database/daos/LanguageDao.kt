package org.wycliffeassociates.otter.jvm.persistence.database.daos

import jooq.Tables.LANGUAGE_ENTITY
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.impl.DSL
import org.jooq.impl.DSL.max
import org.wycliffeassociates.otter.jvm.persistence.database.InsertionException
import org.wycliffeassociates.otter.jvm.persistence.entities.LanguageEntity

class LanguageDao(
        private val dsl: DSLContext
) : ILanguageDao {

    override fun fetchGateway(): List<LanguageEntity> {
        return dsl
                .select()
                .from(LANGUAGE_ENTITY)
                .where(LANGUAGE_ENTITY.GATEWAY.eq(1))
                .fetch {
                    RecordMappers.mapToLanguageEntity(it)
                }
    }

    override fun fetchTargets(): List<LanguageEntity> {
        return dsl
                .select()
                .from(LANGUAGE_ENTITY)
                .where(LANGUAGE_ENTITY.GATEWAY.eq(0))
                .fetch {
                    RecordMappers.mapToLanguageEntity(it)
                }
    }

    override fun fetchBySlug(slug: String): LanguageEntity {
        return dsl
                .select()
                .from(LANGUAGE_ENTITY)
                .where(LANGUAGE_ENTITY.SLUG.eq(slug))
                .fetchOne {
                    RecordMappers.mapToLanguageEntity(it)
                }
    }

    @Synchronized
    override fun insert(entity: LanguageEntity): Int {
        if (entity.id != 0) throw InsertionException("Entity ID is not 0")

        // Insert the language entity
        dsl
                .insertInto(
                        LANGUAGE_ENTITY,
                        LANGUAGE_ENTITY.SLUG,
                        LANGUAGE_ENTITY.NAME,
                        LANGUAGE_ENTITY.ANGLICIZED,
                        LANGUAGE_ENTITY.DIRECTION,
                        LANGUAGE_ENTITY.GATEWAY
                )
                .values(
                        entity.slug,
                        entity.name,
                        entity.anglicizedName,
                        entity.direction,
                        entity.gateway
                )
                .execute()

        // Fetch and return the resulting ID
        return dsl
                .select(max(LANGUAGE_ENTITY.ID))
                .from(LANGUAGE_ENTITY)
                .fetchOne {
                    it.getValue(max(LANGUAGE_ENTITY.ID))
                }
    }

    @Synchronized
    override fun insertAll(entities: List<LanguageEntity>): List<Int> {
        val initialLargest = dsl
                .select(max(LANGUAGE_ENTITY.ID))
                .from(LANGUAGE_ENTITY)
                .fetchOne {
                    it.getValue(max(LANGUAGE_ENTITY.ID))
                } ?: 0
        dsl.transaction { config ->
            val transactionDsl = DSL.using(config)
            entities.forEach { entity ->
                // Insert the language entity
                transactionDsl
                        .insertInto(
                                LANGUAGE_ENTITY,
                                LANGUAGE_ENTITY.SLUG,
                                LANGUAGE_ENTITY.NAME,
                                LANGUAGE_ENTITY.ANGLICIZED,
                                LANGUAGE_ENTITY.DIRECTION,
                                LANGUAGE_ENTITY.GATEWAY
                        )
                        .values(
                                entity.slug,
                                entity.name,
                                entity.anglicizedName,
                                entity.direction,
                                entity.gateway
                        )
                        .execute()
            }
            // Implicit commit
        }
        val finalLargest = dsl
                .select(max(LANGUAGE_ENTITY.ID))
                .from(LANGUAGE_ENTITY)
                .fetchOne {
                    it.getValue(max(LANGUAGE_ENTITY.ID))
                }

        // Return the ids
        return ((initialLargest + 1)..finalLargest).toList()
    }

    override fun fetchById(id: Int): LanguageEntity {
        return dsl
                .select()
                .from(LANGUAGE_ENTITY)
                .where(LANGUAGE_ENTITY.ID.eq(id))
                .fetchOne {
                    RecordMappers.mapToLanguageEntity(it)
                }
    }

    override fun fetchAll(): List<LanguageEntity> {
        return dsl
                .select()
                .from(LANGUAGE_ENTITY)
                .fetch {
                    RecordMappers.mapToLanguageEntity(it)
                }
    }

    override fun update(entity: LanguageEntity) {
        dsl
                .update(LANGUAGE_ENTITY)
                .set(LANGUAGE_ENTITY.SLUG, entity.slug)
                .set(LANGUAGE_ENTITY.NAME, entity.name)
                .set(LANGUAGE_ENTITY.ANGLICIZED, entity.anglicizedName)
                .set(LANGUAGE_ENTITY.DIRECTION, entity.direction)
                .set(LANGUAGE_ENTITY.GATEWAY, entity.gateway)
                .where(LANGUAGE_ENTITY.ID.eq(entity.id))
                .execute()
    }

    override fun delete(entity: LanguageEntity) {
        dsl
                .deleteFrom(LANGUAGE_ENTITY)
                .where(LANGUAGE_ENTITY.ID.eq(entity.id))
                .execute()
    }
}