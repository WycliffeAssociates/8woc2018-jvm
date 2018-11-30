package org.wycliffeassociates.otter.jvm.persistence.repositories.test

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.jooq.DSLContext
import org.wycliffeassociates.otter.jvm.persistence.database.AppDatabase
import org.wycliffeassociates.otter.jvm.persistence.database.daos.*
import org.wycliffeassociates.otter.jvm.persistence.entities.*
import java.lang.Integer.max
import java.lang.Integer.min

class MockDatabase {
    companion object {
        private fun metadataDao(): ResourceMetadataDao = mock {
            val dao = InMemoryDao<ResourceMetadataEntity>()
            val linkDao = InMemoryDao<Pair<Int, Int>>()
            on { insert(any(), anyOrNull()) }.then { input ->
                dao.insert(input.getArgument(0))
            }
            on { update(any(), anyOrNull()) }.then { input ->
                dao.update(input.getArgument(0), input.getArgument<ResourceMetadataEntity>(0).id)
            }
            on { delete(any(), anyOrNull()) }.then { input ->
                dao.delete(input.getArgument<ResourceMetadataEntity>(0).id)
            }
            on { fetchById(any(), anyOrNull()) }.then { call ->
                dao.fetchById(call.getArgument(0))
            }
            on { fetchAll(anyOrNull()) }.then { call ->
                dao.fetchAll()
            }
            on { addLink(any(), any(), anyOrNull()) }.then { call ->
                val id1: Int = call.getArgument(0)
                val id2: Int = call.getArgument(1)
                val smaller = min(id1, id2)
                val larger = max(id1, id2)
                if (linkDao.fetchAll().filter { it.first == smaller && it.second == larger }.isEmpty()) {
                    linkDao.insert(Pair(smaller, larger))
                }
            }
            on { fetchLinks(any(), anyOrNull()) }.then { call ->
                val id: Int = call.getArgument(0)
                return@then linkDao
                        .fetchByProperty("first", id)
                        .map { it.second }
                        .plus(
                                linkDao
                                        .fetchByProperty("second", id)
                                        .map { it.first }
                        )
                        .map { dao.fetchById(it) }
            }
            on { removeLink(any(), any(), anyOrNull()) }.then { call ->
                val id1: Int = call.getArgument(0)
                val id2: Int = call.getArgument(1)
                val smaller = min(id1, id2)
                val larger = max(id1, id2)
                linkDao
                        .fetchAll()
                        .filter { it.first == smaller && it.second == larger }
                        .forEach { linkDao.delete(it) }
            }
        }
        private fun pluginDao(): AudioPluginDao = mock {
            val dao = InMemoryDao<AudioPluginEntity>()
            on { insert(any(), anyOrNull()) }.then { input ->
                dao.insert(input.getArgument(0))
            }
            on { update(any(), anyOrNull()) }.then { input ->
                dao.update(input.getArgument(0), input.getArgument<AudioPluginEntity>(0).id)
            }
            on { delete(any(), anyOrNull()) }.then { input ->
                dao.delete(input.getArgument<AudioPluginEntity>(0).id)
            }
            on { fetchById(any(), anyOrNull()) }.then { call ->
                dao.fetchById(call.getArgument(0))
            }
            on { fetchAll(anyOrNull()) }.then { call ->
                dao.fetchAll()
            }
        }
        private fun languageDao(): LanguageDao = mock {
            val dao = InMemoryDao<LanguageEntity>()
            on { insert(any(), anyOrNull()) }.then { input ->
                dao.insert(input.getArgument(0))
            }
            on { insertAll(any(), anyOrNull()) }.then { input ->
                input.getArgument<List<LanguageEntity>>(0).map { entity -> dao.insert(entity) }
            }
            on { update(any(), anyOrNull()) }.then { input ->
                dao.update(input.getArgument(0), input.getArgument<LanguageEntity>(0).id)
            }
            on { delete(any(), anyOrNull()) }.then { input ->
                dao.delete(input.getArgument<LanguageEntity>(0).id)
            }
            on { fetchAll(anyOrNull()) }.then { call ->
                dao.fetchAll()
            }
            on { fetchById(any(), anyOrNull()) }.then { call ->
                dao.fetchById(call.getArgument(0))
            }
            on { fetchBySlug(any(), anyOrNull()) }.then { call ->
                dao.fetchByProperty("slug", call.getArgument(0)).first()
            }
            on { fetchGateway(anyOrNull()) }.then { call ->
                dao.fetchByProperty("gateway", 1)
            }
            on { fetchTargets(anyOrNull()) }.then { call ->
                dao.fetchByProperty("gateway", 0)
            }
        }
        private fun contentDao(): ContentDao = mock {
            val dao = InMemoryDao<ContentEntity>()
            val linkDao = InMemoryDao<Pair<Int, Int>>()
            on { insert(any(), anyOrNull()) }.then { input ->
                dao.insert(input.getArgument(0))
            }
            on { fetchAll(anyOrNull()) }.then {
                dao.fetchAll()
            }
            on { fetchById(any(), anyOrNull()) }.then { call ->
                dao.fetchById(call.getArgument(0))
            }
            on { fetchByCollectionId(any(), anyOrNull()) }.then { call ->
                dao.fetchByProperty("collectionFk", call.getArgument(0))
            }
            on { fetchSources(any(), anyOrNull()) }.then { call ->
                linkDao
                        .fetchByProperty("first", call.getArgument<ContentEntity>(0).id)
                        .map {
                            dao.fetchById(it.second)
                        }
            }
            on { updateSources(any(), any(), anyOrNull()) }.then { call ->
                linkDao.fetchAll().filter {
                    it.first == call.getArgument<ContentEntity>(0).id
                }.forEach { linkDao.delete(it) }
                call.getArgument<List<ContentEntity>>(1).forEach {
                    linkDao.insert(Pair(call.getArgument<ContentEntity>(0).id, it.id))
                }
            }
            on { update(any(), anyOrNull()) }.then { input ->
                dao.update(input.getArgument(0), input.getArgument<ContentEntity>(0).id)
            }
            on { delete(any(), anyOrNull()) }.then { input ->
                dao.delete(input.getArgument<ContentEntity>(0).id)
            }
        }
        private fun collectionDao(): CollectionDao = mock {
            val dao = InMemoryDao<CollectionEntity>()
            on { insert(any(), anyOrNull()) }.then { input ->
                dao.insert(input.getArgument(0))
            }
            on { fetchAll(anyOrNull()) }.then {
                dao.fetchAll()
            }
            on { fetchById(any(), anyOrNull()) }.then { call ->
                dao.fetchById(call.getArgument(0))
            }
            on { fetchChildren(any(), anyOrNull()) }.then { call ->
                dao.fetchAll().filter { it.parentFk == call.getArgument<CollectionEntity>(0).id }
            }
            on { fetchSource(any(), anyOrNull()) }.then { call ->
                dao.fetchById(call.getArgument<CollectionEntity>(0).sourceFk ?: throw NullPointerException())
            }
            on { fetchBySlugAndContainerId(any(), any(), anyOrNull()) }.then { call ->
                val slug: String = call.getArgument(0)
                val metadataId: Int = call.getArgument(1)
                dao.fetchAll().filter { it.slug == slug && it.metadataFk == metadataId }.first()
            }
            on { update(any(), anyOrNull()) }.then { input ->
                dao.update(input.getArgument(0), input.getArgument<CollectionEntity>(0).id)
            }
            on { delete(any(), anyOrNull()) }.then { input ->
                dao.delete(input.getArgument<CollectionEntity>(0).id)
            }
        }
        private fun takeDao(): TakeDao = mock {
            val dao = InMemoryDao<TakeEntity>()
            on { insert(any(), anyOrNull()) }.then { input ->
                dao.insert(input.getArgument(0))
            }
            on { update(any(), anyOrNull()) }.then { input ->
                dao.update(input.getArgument(0), input.getArgument<TakeEntity>(0).id)
            }
            on { delete(any(), anyOrNull()) }.then { input ->
                dao.delete(input.getArgument<TakeEntity>(0).id)
            }
            on { fetchAll(anyOrNull()) }.then { call ->
                dao.fetchAll()
            }
            on { fetchByContentId(any(), anyOrNull()) }.then { call ->
                dao.fetchByProperty("contentFk", call.getArgument(0))
            }
            on { fetchById(any(), anyOrNull()) }.then { call ->
                dao.fetchById(call.getArgument(0))
            }
        }
        private fun markerDao(): MarkerDao = mock {
            val dao = InMemoryDao<MarkerEntity>()
            on { insert(any(), anyOrNull()) }.then { input ->
                dao.insert(input.getArgument(0))
            }
            on { delete(any(), anyOrNull()) }.then { input ->
                dao.delete(input.getArgument<MarkerEntity>(0).id)
            }
            on { fetchByTakeId(any(), anyOrNull()) }.then { input ->
                dao.fetchByProperty<Int>("takeFk", input.getArgument(0))
            }
            on { fetchAll(anyOrNull()) }.then { input ->
                dao.fetchAll()
            }
        }
        fun database(): AppDatabase {
            val takeDao = takeDao()
            val markerDao = markerDao()
            val chunkDao = contentDao()
            val languageDao = languageDao()
            val pluginDao = pluginDao()
            val metadataDao = metadataDao()
            val collectionDao = collectionDao()
            return mock {
                on { getTakeDao() } doReturn takeDao
                on { getMarkerDao() } doReturn markerDao
                on { getContentDao() } doReturn chunkDao
                on { getLanguageDao() } doReturn languageDao
                on { getAudioPluginDao() } doReturn pluginDao
                on { getResourceMetadataDao() } doReturn metadataDao
                on { getCollectionDao() } doReturn collectionDao
                on { transaction(any()) }.then { input ->
                    input.getArgument<(DSLContext) -> Unit>(0)(mock())
                }
            }
        }
    }
}