package org.wycliffeassociates.otter.jvm.persistence.repo

import jooq.tables.daos.DublinCoreEntityDao
import jooq.tables.daos.LanguageEntityDao
import jooq.tables.daos.RcLinkEntityDao
import org.jooq.Configuration
import org.junit.*
import org.wycliffeassociates.otter.common.data.dao.Dao
import org.wycliffeassociates.otter.common.data.model.Language
import org.wycliffeassociates.otter.jvm.persistence.JooqTestConfiguration
import org.wycliffeassociates.otter.jvm.persistence.TestDataStore
import org.wycliffeassociates.otter.jvm.persistence.mapping.LanguageMapper
import org.wycliffeassociates.otter.jvm.persistence.mapping.ResourceContainerMapper
import java.io.File
import java.util.*

class ResourceContainerDaoTest {
    companion object {
        val config: Configuration = JooqTestConfiguration.setup("test_content.sqlite")
        val languageDao: Dao<Language> = LanguageDao(LanguageEntityDao(config), LanguageMapper())

        @BeforeClass
        @JvmStatic
        fun setupAll() {
            // Put all the languages in the database
            TestDataStore.languages.forEach { language ->
                language.id = languageDao
                        .insert(language)
                        .blockingFirst()
            }
            // set all the ids
            TestDataStore.resourceContainers.forEach { rc ->
                rc.language = TestDataStore.languages.filter { rc.language.slug == it.slug }.first()
            }
        }

        @AfterClass
        @JvmStatic
        fun tearDownAll() {
            JooqTestConfiguration.tearDown("test_content.sqlite")
        }
    }

    @Test
    fun testSingleResourceContainerCRUD() {
        val testRc = TestDataStore.resourceContainers.first()
        testRc.language = TestDataStore.languages.first()
        val dao = ResourceContainerDao(
                DublinCoreEntityDao(config),
                RcLinkEntityDao(config),
                ResourceContainerMapper(languageDao)
        )
        DaoTestCases.assertInsertAndRetrieveSingle(dao, testRc)
        // Update the test rc
        testRc.conformsTo = "new spec"
        testRc.creator = "Matthew Russell"
        testRc.description = "A book by Matthew Russell, written completely in YAML."
        testRc.format = "application/yaml"
        testRc.identifier = "mbr"
        val newTime = Calendar.getInstance()
        newTime.time = Date(1000 * (Date().time / 1000))
        testRc.modified = newTime
        testRc.issued = newTime
        testRc.language = TestDataStore.languages.last()
        testRc.publisher = "Russell House"
        testRc.type = "electronic"
        testRc.title = "My Amazing Title"
        testRc.version = 22
        testRc.path = File("/updated/path/to/my/container")
        DaoTestCases.assertUpdate(dao, testRc)
        DaoTestCases.assertDelete(dao, testRc)
    }

    @Test
    fun testAllResourceContainersInsertAndRetrieve() {
        val dao = ResourceContainerDao(
                DublinCoreEntityDao(config),
                RcLinkEntityDao(config),
                ResourceContainerMapper(languageDao)
        )
        DaoTestCases.assertInsertAndRetrieveAll(dao, TestDataStore.resourceContainers)
        TestDataStore.resourceContainers.forEach {
            dao.delete(it).blockingAwait()
        }
    }

    @Test
    fun testAddAndRemoveLinks() {
        val dao = ResourceContainerDao(
                DublinCoreEntityDao(config),
                RcLinkEntityDao(config),
                ResourceContainerMapper(languageDao)
        )
        val testRc1 = TestDataStore.resourceContainers[0]
        val testRc2 = TestDataStore.resourceContainers[1]
        testRc1.id = dao.insert(testRc1).blockingFirst()
        testRc2.id = dao.insert(testRc2).blockingFirst()

        // Add the link
        dao.addLink(testRc1, testRc2).blockingAwait()

        // Check to make sure the link is really there
        var rc1Links = dao.getLinks(testRc1).blockingFirst()
        var rc2Links = dao.getLinks(testRc2).blockingFirst()
        Assert.assertEquals(1, rc1Links.size)
        Assert.assertTrue(rc1Links.contains(testRc2))
        Assert.assertEquals(1, rc2Links.size)
        Assert.assertTrue(rc2Links.contains(testRc1))

        // Remove the link
        dao.removeLink(testRc2, testRc1).blockingAwait()
        // Check if the link is really gone
        rc1Links = dao.getLinks(testRc1).blockingFirst()
        rc2Links = dao.getLinks(testRc2).blockingFirst()
        Assert.assertTrue(rc1Links.isEmpty())
        Assert.assertTrue(rc2Links.isEmpty())

        DaoTestCases.assertDelete(dao, testRc1)
        DaoTestCases.assertDelete(dao, testRc2)
    }
}