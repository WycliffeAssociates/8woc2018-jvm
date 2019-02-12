package org.wycliffeassociates.otter.jvm.persistence

import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import org.jooq.DSLContext
import org.wycliffeassociates.otter.common.collections.tree.Tree
import org.wycliffeassociates.otter.common.collections.tree.TreeNode
import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.common.data.model.Content
import org.wycliffeassociates.otter.common.domain.mapper.mapToMetadata
import org.wycliffeassociates.otter.common.persistence.IResourceContainerTreeImporter
import org.wycliffeassociates.otter.jvm.persistence.database.AppDatabase
import org.wycliffeassociates.otter.jvm.persistence.entities.ResourceLinkEntity
import org.wycliffeassociates.otter.jvm.persistence.repositories.mapping.CollectionMapper
import org.wycliffeassociates.otter.jvm.persistence.repositories.mapping.ContentMapper
import org.wycliffeassociates.otter.jvm.persistence.repositories.mapping.LanguageMapper
import org.wycliffeassociates.otter.jvm.persistence.repositories.mapping.ResourceMetadataMapper
import org.wycliffeassociates.resourcecontainer.ResourceContainer

class ResourceContainerTreeImporter(
        private val database: AppDatabase
) : IResourceContainerTreeImporter {
    private val collectionDao = database.getCollectionDao()
    private val contentDao = database.getContentDao()
    private val resourceMetadataDao = database.getResourceMetadataDao()
    private val languageDao = database.getLanguageDao()
    private val resourceLinkDao = database.getResourceLinkDao()

    override fun importResourceContainer(rc: ResourceContainer, rcTree: Tree, languageSlug: String): Completable {
        return Completable.fromAction {
            database.transaction { dsl ->
                val language = LanguageMapper().mapFromEntity(languageDao.fetchBySlug(languageSlug, dsl))
                val metadata = rc.manifest.dublinCore.mapToMetadata(rc.dir, language)
                val dublinCoreFk = resourceMetadataDao.insert(ResourceMetadataMapper().mapToEntity(metadata), dsl)

                val relatedDublinCoreIds: List<Int> =
                        linkRelatedResourceContainers(dublinCoreFk, rc.manifest.dublinCore.relation, dsl)

                // TODO: Probably shouldn't hardcode "help"
                if (rc.type() == "help") {
                    relatedDublinCoreIds.forEach { relatedId ->
                        val ih = ImportHelper(dublinCoreFk, relatedId, dsl)
                        ih.importCollection(null, rcTree)
                    }
                } else {
                    val ih = ImportHelper(dublinCoreFk, null, dsl)
                    ih.importCollection(null, rcTree)
                }
            }
        }.subscribeOn(Schedulers.io())
    }

    private fun linkRelatedResourceContainers(
            dublinCoreFk: Int,
            relations: List<String>,
            dsl: DSLContext)
            : List<Int> {
        val relatedIds = mutableListOf<Int>()
        relations.forEach { relation ->
            val parts = relation.split('/')
            val relatedDublinCore = resourceMetadataDao.fetchByLanguageAndIdentifier(parts[0], parts[1], dsl)
            resourceMetadataDao.addLink(dublinCoreFk, relatedDublinCore.id, dsl)
            relatedIds.add(relatedDublinCore.id)
        }
        return relatedIds
    }

    inner class ImportHelper(val dublinCoreId: Int, val relatedBundleDublinCoreId: Int?, val dsl: DSLContext) {

        fun importCollection(parentId: Int?, node: Tree) {
            (node.value as? Collection)?.let { collection ->
                when (relatedBundleDublinCoreId) {
                    null -> addCollection(collection, parentId)
                    else -> findCollectionId(collection, relatedBundleDublinCoreId)
                }
            }?.let { collectionId ->
                for (childNode in node.children) {
                    importNode(collectionId, childNode)
                }
            }
        }

        private fun findCollectionId(collection: Collection, containerId: Int): Int =
                collectionDao.fetchBySlugAndContainerId(collection.slug, containerId)
                        .id

        private fun addCollection(collection: Collection, parentId: Int?): Int {
            val entity = CollectionMapper().mapToEntity(collection).apply {
                parentFk = parentId
                dublinCoreFk = dublinCoreId
            }
            return collectionDao.insert(entity, dsl)
        }

        private fun importNode(parentId: Int, node: TreeNode) {
            when (node) {
                is Tree -> {
                    importCollection(parentId, node)
                }
                is TreeNode -> {
                    importContent(parentId, node)
                }
            }
        }

        private fun importContent(parentId: Int, node: TreeNode) {
            val content = node.value
            if (content is Content) {
                val entity = ContentMapper().mapToEntity(content).apply { collectionFk = parentId }
                val contentId = contentDao.insert(entity, dsl)
                if (relatedBundleDublinCoreId != null) {
                    linkResource(parentId, contentId, content.start)
                }
            }
        }

        private fun linkResource(parentId: Int, contentId: Int, start: Int) {
            when (start) {
                0 -> addResource(contentId, null, parentId) // chapter resource
                else -> {
                    val contentEntity = contentDao.fetchByCollectionIdAndStart(parentId, start, dsl)
                    addResource(contentId, contentEntity.id, null)
                }
            }
        }

        private fun addResource(contentId: Int, contentFk: Int?, collectionFk: Int?) {
            val resourceEntity = ResourceLinkEntity(
                    0,
                    contentId,
                    contentFk,
                    collectionFk,
                    dublinCoreId
            )
            resourceLinkDao.insert(resourceEntity, dsl)
        }
    }
}