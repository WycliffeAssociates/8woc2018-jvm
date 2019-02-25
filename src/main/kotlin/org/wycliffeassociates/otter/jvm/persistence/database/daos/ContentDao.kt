package org.wycliffeassociates.otter.jvm.persistence.database.daos

import jooq.Tables.CONTENT_DERIVATIVE
import jooq.Tables.CONTENT_ENTITY
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.Select
import org.jooq.SelectFieldOrAsterisk
import org.jooq.impl.DSL.max
import org.wycliffeassociates.otter.jvm.persistence.database.InsertionException
import org.wycliffeassociates.otter.jvm.persistence.entities.ContentEntity

private const val VERSE_LABEL_VALUE = "verse"

class ContentDao(
        private val instanceDsl: DSLContext
) {
    fun fetchByCollectionId(collectionId: Int, dsl: DSLContext = instanceDsl): List<ContentEntity> {
        return dsl
                .select()
                .from(CONTENT_ENTITY)
                .where(CONTENT_ENTITY.COLLECTION_FK.eq(collectionId))
                .fetch {
                    RecordMappers.mapToContentEntity(it)
                }
    }

    fun fetchVerseByCollectionIdAndStart(collectionId: Int, start: Int, dsl: DSLContext = instanceDsl): ContentEntity? {
        return dsl
                .select()
                .from(CONTENT_ENTITY)
                .where(CONTENT_ENTITY.COLLECTION_FK.eq(collectionId))
                .and(CONTENT_ENTITY.START.eq(start))
                .and(CONTENT_ENTITY.LABEL.eq(VERSE_LABEL_VALUE))
                .fetchOne()
                ?.let { RecordMappers.mapToContentEntity(it) }
    }

    fun selectVerseByCollectionIdAndStart(
            collectionId: Int,
            start: Int,
            extraFields: List<SelectFieldOrAsterisk>,
            dsl: DSLContext = instanceDsl
    ): Select<Record>
    {
        return dsl
                .select(CONTENT_ENTITY.ID, *extraFields.toTypedArray())
                .from(CONTENT_ENTITY)
                .where(CONTENT_ENTITY.COLLECTION_FK.eq(collectionId))
                .and(CONTENT_ENTITY.START.eq(start))
                .and(CONTENT_ENTITY.LABEL.eq(VERSE_LABEL_VALUE))
                .limit(1)
    }

    fun fetchSources(entity: ContentEntity, dsl: DSLContext = instanceDsl): List<ContentEntity> {
        val sourceIds = dsl
                .select(CONTENT_DERIVATIVE.SOURCE_FK)
                .from(CONTENT_DERIVATIVE)
                .where(CONTENT_DERIVATIVE.CONTENT_FK.eq(entity.id))
                .fetch {
                    it.getValue(CONTENT_DERIVATIVE.SOURCE_FK)
                }

        return dsl
                .select()
                .from(CONTENT_ENTITY)
                .where(CONTENT_ENTITY.ID.`in`(sourceIds))
                .fetch {
                    RecordMappers.mapToContentEntity(it)
                }
    }

    fun updateSources(entity: ContentEntity, sources: List<ContentEntity>, dsl: DSLContext = instanceDsl) {
        // Delete the existing sources
        dsl
                .deleteFrom(CONTENT_DERIVATIVE)
                .where(CONTENT_DERIVATIVE.CONTENT_FK.eq(entity.id))
                .execute()

        // Add the sources
        if (sources.isNotEmpty()) {
            sources.forEach {
                val insertStatement = dsl
                        .insertInto(CONTENT_DERIVATIVE, CONTENT_DERIVATIVE.CONTENT_FK, CONTENT_DERIVATIVE.SOURCE_FK)
                        .values(entity.id, it.id)
                        .execute()
            }
        }
    }

    @Synchronized
    fun insert(entity: ContentEntity, dsl: DSLContext = instanceDsl): Int {
        if (entity.id != 0) throw InsertionException("Entity ID was not 0")

        // Insert the new content entity
        dsl
                .insertInto(
                        CONTENT_ENTITY,
                        CONTENT_ENTITY.COLLECTION_FK,
                        CONTENT_ENTITY.SORT,
                        CONTENT_ENTITY.START,
                        CONTENT_ENTITY.LABEL,
                        CONTENT_ENTITY.SELECTED_TAKE_FK,
                        CONTENT_ENTITY.TEXT,
                        CONTENT_ENTITY.FORMAT
                )
                .values(
                        entity.collectionFk,
                        entity.sort,
                        entity.start,
                        entity.labelKey,
                        entity.selectedTakeFk,
                        entity.text,
                        entity.format
                )
                .execute()

        // Get the ID
        return dsl
                .select(max(CONTENT_ENTITY.ID))
                .from(CONTENT_ENTITY)
                .fetchOne {
                    it.getValue(max(CONTENT_ENTITY.ID))
                }
    }

    fun fetchById(id: Int, dsl: DSLContext = instanceDsl): ContentEntity {
        return dsl
                .select()
                .from(CONTENT_ENTITY)
                .where(CONTENT_ENTITY.ID.eq(id))
                .fetchOne {
                    RecordMappers.mapToContentEntity(it)
                }
    }

    fun fetchAll(dsl: DSLContext = instanceDsl): List<ContentEntity> {
        return dsl
                .select()
                .from(CONTENT_ENTITY)
                .fetch {
                    RecordMappers.mapToContentEntity(it)
                }
    }

    fun update(entity: ContentEntity, dsl: DSLContext = instanceDsl) {
        dsl
                .update(CONTENT_ENTITY)
                .set(CONTENT_ENTITY.SORT, entity.sort)
                .set(CONTENT_ENTITY.LABEL, entity.labelKey)
                .set(CONTENT_ENTITY.START, entity.start)
                .set(CONTENT_ENTITY.COLLECTION_FK, entity.collectionFk)
                .set(CONTENT_ENTITY.SELECTED_TAKE_FK, entity.selectedTakeFk)
                .set(CONTENT_ENTITY.TEXT, entity.text)
                .set(CONTENT_ENTITY.FORMAT, entity.format)
                .where(CONTENT_ENTITY.ID.eq(entity.id))
                .execute()
    }

    fun delete(entity: ContentEntity, dsl: DSLContext = instanceDsl) {
        dsl
                .deleteFrom(CONTENT_ENTITY)
                .where(CONTENT_ENTITY.ID.eq(entity.id))
                .execute()
    }

}