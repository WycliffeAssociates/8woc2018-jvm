package org.wycliffeassociates.otter.jvm.persistence.repositories

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import jooq.tables.CollectionEntity.COLLECTION_ENTITY
import jooq.tables.ContentDerivative.CONTENT_DERIVATIVE
import jooq.tables.ContentEntity.CONTENT_ENTITY
import org.jooq.DSLContext
import org.jooq.impl.DSL.field
import org.jooq.impl.DSL.value
import org.wycliffeassociates.otter.common.collections.tree.Tree
import org.wycliffeassociates.otter.common.collections.tree.TreeNode
import org.wycliffeassociates.otter.common.data.model.Chunk
import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.common.data.model.ResourceMetadata
import org.wycliffeassociates.otter.common.domain.mapper.mapToMetadata
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.repositories.ICollectionRepository
import org.wycliffeassociates.otter.jvm.persistence.database.AppDatabase
import org.wycliffeassociates.otter.jvm.persistence.entities.CollectionEntity
import org.wycliffeassociates.otter.jvm.persistence.repositories.mapping.ChunkMapper
import org.wycliffeassociates.otter.jvm.persistence.repositories.mapping.CollectionMapper
import org.wycliffeassociates.otter.jvm.persistence.repositories.mapping.LanguageMapper
import org.wycliffeassociates.otter.jvm.persistence.repositories.mapping.ResourceMetadataMapper
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import org.wycliffeassociates.resourcecontainer.entity.Checking
import org.wycliffeassociates.resourcecontainer.entity.Manifest
import org.wycliffeassociates.resourcecontainer.entity.dublincore
import org.wycliffeassociates.resourcecontainer.entity.project
import java.io.File
import java.lang.NullPointerException
import java.time.LocalDate
import org.wycliffeassociates.otter.jvm.persistence.repositories.mapping.ChunkMapper
import kotlin.system.measureTimeMillis


class CollectionRepository(
        private val database: AppDatabase,
        private val directoryProvider: IDirectoryProvider,
        private val collectionMapper: CollectionMapper = CollectionMapper(),
        private val chunkMapper: ChunkMapper = ChunkMapper(),
        private val metadataMapper: ResourceMetadataMapper = ResourceMetadataMapper(),
        private val languageMapper: LanguageMapper = LanguageMapper(),
        private val chunkMapper: ChunkMapper = ChunkMapper()
) : ICollectionRepository {
    private val collectionDao = database.getCollectionDao()
    private val chunkDao = database.getChunkDao()
    private val metadataDao = database.getResourceMetadataDao()
    private val languageDao = database.getLanguageDao()

    override fun delete(obj: Collection): Completable {
        return Completable
                .fromAction {
                    collectionDao.delete(collectionMapper.mapToEntity(obj))
                }
                .subscribeOn(Schedulers.io())
    }

    override fun getAll(): Single<List<Collection>> {
        return Single
                .fromCallable {
                    collectionDao
                            .fetchAll()
                            .map(this::buildCollection)
                }
                .subscribeOn(Schedulers.io())
    }

    override fun getBySlugAndContainer(slug: String, container: ResourceMetadata): Maybe<Collection> {
        return Maybe
                .fromCallable {
                    buildCollection(collectionDao.fetchBySlugAndContainerId(slug, container.id))
                }
                .onErrorComplete()
                .subscribeOn(Schedulers.io())
    }

    override fun getChildren(collection: Collection): Single<List<Collection>> {
        return Single
                .fromCallable {
                    collectionDao
                            .fetchChildren(collectionMapper.mapToEntity(collection))
                            .map(this::buildCollection)
                }
                .subscribeOn(Schedulers.io())
    }

    override fun updateSource(collection: Collection, newSource: Collection): Completable {
        return Completable
                .fromAction {
                    val entity = collectionDao.fetchById(collection.id)
                    entity.sourceFk = newSource.id
                    collectionDao.update(entity)
                }
                .subscribeOn(Schedulers.io())
    }

    override fun updateParent(collection: Collection, newParent: Collection): Completable {
        return Completable
                .fromAction {
                    val entity = collectionDao.fetchById(collection.id)
                    entity.parentFk = newParent.id
                    collectionDao.update(entity)
                }
                .subscribeOn(Schedulers.io())
    }

    override fun insert(collection: Collection): Single<Int> {
        return Single
                .fromCallable {
                    collectionDao.insert(collectionMapper.mapToEntity(collection))
                }
                .subscribeOn(Schedulers.io())
    }

    override fun update(obj: Collection): Completable {
        return Completable
                .fromAction {
                    val entity = collectionDao.fetchById(obj.id)
                    val newEntity = collectionMapper.mapToEntity(obj, entity.parentFk, entity.sourceFk)
                    collectionDao.update(newEntity)
                }
                .subscribeOn(Schedulers.io())
    }

    private fun createResourceContainer(source: Collection, targetLanguage: Language): ResourceContainer {
        val metadata = source.resourceContainer
        metadata ?: throw NullPointerException("Source has no resource metadata")

        val slug = "${targetLanguage.slug}_${metadata.identifier}"
        val directory = directoryProvider.resourceContainerDirectory.resolve(slug)
        val container = ResourceContainer.create(directory) {
            // Set up the manifest
            manifest = Manifest(
                    dublincore {
                        identifier = metadata.identifier
                        issued = LocalDate.now().toString()
                        modified = LocalDate.now().toString()
                        language = org.wycliffeassociates.resourcecontainer.entity.language {
                            identifier = targetLanguage.slug
                            direction = targetLanguage.direction
                            title = targetLanguage.name
                        }
                        format = "text/usfm"
                        subject = metadata.subject
                        type = "book"
                        title = metadata.title
                    },
                    listOf(),
                    Checking()
            )
        }
        container.write()
        return container
    }

    override fun deriveProject(source: Collection, language: Language): Completable {
        return Completable
                .fromAction {
                    database.transaction { dsl ->
                        // Check for existing resource containers
                        val existingMetadata = metadataDao.fetchAll(dsl)
                        val matches = existingMetadata.filter {
                            it.identifier == source.resourceContainer?.identifier
                                    && it.languageFk == language.id
                        }

                        val metadataEntity = if (matches.isEmpty()) {
                            // This combination of identifier and language does not already exist; create it
                            val container = createResourceContainer(source, language)
                            // Convert DublinCore to ResourceMetadata
                            val metadata = container.manifest.dublinCore
                                    .mapToMetadata(container.dir, language)

                            // Insert ResourceMetadata into database
                            val entity = metadataMapper.mapToEntity(metadata)
                            entity.id = metadataDao.insert(entity, dsl)
                            /* return@if */ entity
                        } else {
                            // Use the existing metadata
                            /* return@if */ matches.first()
                        }

                        // Insert the derived project
                        val sourceEntity = collectionDao.fetchById(source.id, dsl)
                        val projectEntity = sourceEntity
                                // parentFk null for now. May be non-null if derivative categories added
                                .copy(id = 0, metadataFk = metadataEntity.id, parentFk = null, sourceFk = sourceEntity.id)
                        projectEntity.id = collectionDao.insert(projectEntity, dsl)

                        // Copy the chapters
                        copyChapters(dsl, sourceEntity.id, projectEntity.id, metadataEntity.id)

                        // Copy the content
                        copyContent(dsl, sourceEntity.id, metadataEntity.id)

                        // Link the derivative content
                        linkContent(dsl, sourceEntity.id, projectEntity.id)

                        // Add a project to the container if necessary
                        // Load the existing resource container and see if we need to add another project
                        val container = ResourceContainer.load(File(metadataEntity.path))
                        if (container.manifest.projects.none { it.identifier == source.slug }) {
                            container.manifest.projects = container.manifest.projects.plus(
                                    project {
                                        sort = if (metadataEntity.subject.toLowerCase() == "bible"
                                                && projectEntity.sort > 39) {
                                            projectEntity.sort + 1
                                        } else {
                                            projectEntity.sort
                                        }
                                        identifier = projectEntity.slug
                                        path = "./${projectEntity.slug}"
                                        // This title will not be localized into the target language
                                        title = projectEntity.title
                                        // Unable to get these fields from the source collection
                                        categories = listOf()
                                        versification = ""
                                    }
                            )
                            // Update the container
                            container.write()
                        }
                    }
                }
                .subscribeOn(Schedulers.io())
    }

    private fun copyChapters(dsl: DSLContext, sourceId: Int, projectId: Int, metadataId: Int) {
        // Copy all the chapter collections
        dsl.insertInto(
                COLLECTION_ENTITY,
                COLLECTION_ENTITY.PARENT_FK,
                COLLECTION_ENTITY.SOURCE_FK,
                COLLECTION_ENTITY.LABEL,
                COLLECTION_ENTITY.TITLE,
                COLLECTION_ENTITY.SLUG,
                COLLECTION_ENTITY.SORT,
                COLLECTION_ENTITY.RC_FK
        ).select(
                dsl.select(
                        value(projectId),
                        COLLECTION_ENTITY.ID,
                        COLLECTION_ENTITY.LABEL,
                        COLLECTION_ENTITY.TITLE,
                        COLLECTION_ENTITY.SLUG,
                        COLLECTION_ENTITY.SORT,
                        value(metadataId)
                )
                        .from(COLLECTION_ENTITY)
                        .where(COLLECTION_ENTITY.PARENT_FK.eq(sourceId))
        ).execute()
    }

    private fun copyContent(dsl: DSLContext, sourceId: Int, metadataId: Int) {
        dsl.insertInto(
                CONTENT_ENTITY,
                CONTENT_ENTITY.COLLECTION_FK,
                CONTENT_ENTITY.LABEL,
                CONTENT_ENTITY.START,
                CONTENT_ENTITY.SORT
        )
                .select(
                        dsl.select(
                                COLLECTION_ENTITY.ID,
                                field("verselabel", String::class.java),
                                field("versestart", Int::class.java),
                                field("versesort", Int::class.java)
                        )
                                .from(
                                        dsl.select(
                                                CONTENT_ENTITY.ID.`as`("verseid"),
                                                CONTENT_ENTITY.COLLECTION_FK.`as`("chapterid"),
                                                CONTENT_ENTITY.LABEL.`as`("verselabel"),
                                                CONTENT_ENTITY.START.`as`("versestart"),
                                                CONTENT_ENTITY.SORT.`as`("versesort")
                                        )
                                                .from(CONTENT_ENTITY)
                                                .where(CONTENT_ENTITY.COLLECTION_FK.`in`(
                                                        dsl
                                                                .select(COLLECTION_ENTITY.ID)
                                                                .from(COLLECTION_ENTITY)
                                                                .where(COLLECTION_ENTITY.PARENT_FK.eq(sourceId))
                                                ))
                                )
                                .leftJoin(COLLECTION_ENTITY)
                                .on(COLLECTION_ENTITY.SOURCE_FK.eq(field("chapterid", Int::class.java))
                                        .and(COLLECTION_ENTITY.RC_FK.eq(metadataId)))
                ).execute()
    }

    private fun linkContent(dsl: DSLContext, sourceId: Int, projectId: Int) {
        dsl.insertInto(
                CONTENT_DERIVATIVE,
                CONTENT_DERIVATIVE.CONTENT_FK,
                CONTENT_DERIVATIVE.SOURCE_FK
        ).select(
                dsl.select(
                        field("derivedid", Int::class.java),
                        field("sourceid", Int::class.java)
                )
                        .from(
                                dsl.select(
                                        field("sourceid", Int::class.java),
                                        field("sourcesort", Int::class.java),
                                        COLLECTION_ENTITY.SLUG.`as`("sourcechapter")
                                )
                                        .from(
                                                dsl.select(
                                                        CONTENT_ENTITY.ID.`as`("sourceid"),
                                                        CONTENT_ENTITY.SORT.`as`("sourcesort"),
                                                        CONTENT_ENTITY.COLLECTION_FK.`as`("chapterid")
                                                ).from(CONTENT_ENTITY).where(
                                                        CONTENT_ENTITY.COLLECTION_FK.`in`(
                                                                dsl
                                                                        .select(COLLECTION_ENTITY.ID)
                                                                        .from(COLLECTION_ENTITY)
                                                                        .where(COLLECTION_ENTITY.PARENT_FK.eq(sourceId))
                                                        )
                                                )
                                        )
                                        .leftJoin(COLLECTION_ENTITY)
                                        .on(COLLECTION_ENTITY.ID.eq(field("chapterid", Int::class.java)))
                        )
                        .leftJoin(
                                dsl
                                        .select(
                                                field("derivedid", Int::class.java),
                                                field("derivedsort", Int::class.java),
                                                COLLECTION_ENTITY.SLUG.`as`("derivedchapter")
                                        )
                                        .from(
                                                dsl
                                                        .select(
                                                                CONTENT_ENTITY.ID.`as`("derivedid"),
                                                                CONTENT_ENTITY.SORT.`as`("derivedsort"),
                                                                CONTENT_ENTITY.COLLECTION_FK.`as`("chapterid")
                                                        )
                                                        .from(CONTENT_ENTITY)
                                                        .where(CONTENT_ENTITY.COLLECTION_FK.`in`(
                                                                dsl
                                                                        .select(COLLECTION_ENTITY.ID)
                                                                        .from(COLLECTION_ENTITY)
                                                                        .where(COLLECTION_ENTITY.PARENT_FK.eq(projectId))
                                                        ))
                                        )
                                        .leftJoin(COLLECTION_ENTITY)
                                        .on(COLLECTION_ENTITY.ID.eq(field("chapterid", Int::class.java)))
                        )
                        .on(
                                field("sourcesort", Int::class.java)
                                        .eq(field("derivedsort", Int::class.java))
                                        .and(field("sourcechapter", Int::class.java)
                                                .eq(field("derivedchapter", Int::class.java)))
                        )
        ).execute()
    }

    private fun buildCollection(entity: CollectionEntity): Collection {
        var metadata: ResourceMetadata? = null
        entity.metadataFk?.let {
            val metadataEntity = metadataDao.fetchById(it)
            val language = languageMapper.mapFromEntity(languageDao.fetchById(metadataEntity.languageFk))
            metadata = metadataMapper.mapFromEntity(metadataEntity, language)
        }

        return collectionMapper.mapFromEntity(entity, metadata)
    }
  
    override fun importResourceContainer(rc: ResourceContainer, rcTree: Tree, languageSlug: String): Completable {
        return Completable.fromAction {
            database.transaction { dsl ->
                val language = languageMapper.mapFromEntity(languageDao.fetchBySlug(languageSlug, dsl))
                val metadata = rc.manifest.dublinCore.mapToMetadata(rc.dir, language)
                val metadataId = metadataDao.insert(metadataMapper.mapToEntity(metadata), dsl)

                importCollection(null, metadataId, rcTree, dsl)
            }
        }.subscribeOn(Schedulers.io())
    }

    private fun importNode(parentId: Int, metadataId: Int, node: TreeNode, dsl: DSLContext) {
        when(node) {
            is Tree -> {
                importCollection(parentId, metadataId, node, dsl)
            }
            is TreeNode -> {
                importChunk(parentId, node, dsl)
            }
        }
    }

    private fun importCollection(parentId: Int?, metadataId: Int, node: Tree, dsl: DSLContext){
        val collection = node.value
        if (collection is Collection) {
            val entity = collectionMapper.mapToEntity(collection)
            entity.parentFk = parentId
            entity.metadataFk = metadataId
            val id = collectionDao.insert(entity, dsl)
            for (node in node.children) {
                importNode(id, metadataId, node, dsl)
            }
        }
    }

    private fun importChunk(parentId: Int, node: TreeNode, dsl: DSLContext) {
        val chunk = node.value
        if (chunk is Chunk) {
            val entity = chunkMapper.mapToEntity(chunk)
            entity.collectionFk = parentId
            chunkDao.insert(entity, dsl)
        }
    }
}