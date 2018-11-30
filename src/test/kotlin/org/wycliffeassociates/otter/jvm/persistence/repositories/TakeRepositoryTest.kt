package org.wycliffeassociates.otter.jvm.persistence.repositories

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.wycliffeassociates.otter.common.data.model.Content
import org.wycliffeassociates.otter.common.data.model.Marker
import org.wycliffeassociates.otter.common.data.model.Take
import org.wycliffeassociates.otter.jvm.persistence.entities.ContentEntity
import org.wycliffeassociates.otter.jvm.persistence.repositories.test.MockDatabase
import java.io.File
import java.time.LocalDate

class TakeRepositoryTest {
    val mockDatabase = MockDatabase.database()

    // UUT
    private val takeRepository = TakeRepository(mockDatabase)

    private val mockContent: Content = mock { on { id } doReturn 1 }
    @Before
    fun setup() {
        // insert the mock content
        mockDatabase
                .getContentDao()
                .insert(ContentEntity(1, 0, "", 0, 0, null))
    }

    @Test
    fun shouldCRUDTake() {
        val take = create()

        val retrieved = retrieveByContent()
        updateMarkerIds(retrieved.first().markers, take.markers)
        Assert.assertEquals(listOf(take), retrieved)

        update(take)
        val retrievedUpdated = retrieveByContent()
        updateMarkerIds(retrievedUpdated.first().markers, take.markers)
        Assert.assertEquals(listOf(take), retrievedUpdated)

        delete(take)
        val retrievedDeleted = retrieveByContent()
        Assert.assertEquals(emptyList<Take>(), retrievedDeleted)
    }

    @Test
    fun shouldGetAllTakes() {
        val takes = listOf(create(), create())
        val retrieved = takeRepository.getAll().blockingGet()
        for (i in 0 until takes.size) {
            updateMarkerIds(retrieved[i].markers, takes[i].markers)
        }
        Assert.assertEquals(takes, retrieved)
    }

    @Test
    fun shouldNotRemoveExistingTake() {
        // Create a dummy existing take
        val take = create(path = File("./take.wav").absoluteFile)
        take.path.createNewFile()

        takeRepository.removeNonExistentTakes().blockingAwait()

        val retrieved = retrieveByContent()
        updateMarkerIds(retrieved.first().markers, take.markers)
        Assert.assertEquals(listOf(take), retrieved)

        // Clean up
        take.path.delete()
    }

    @Test
    fun shouldRemoveNonExistentTake() {
        // Create take without take file
        create(path = File("./take.wav").absoluteFile)

        takeRepository.removeNonExistentTakes().blockingAwait()

        val retrieved = retrieveByContent()
        Assert.assertEquals(emptyList<Take>(), retrieved)
    }

    @Test
    fun shouldRemoveNonExistentSelectedTake() {
        // Create take without take file
        val take = create(path = File("./take.wav").absoluteFile)

        // set take as selected
        mockDatabase
                .getContentDao()
                .update(ContentEntity(1, 0, "", 0, 0, take.id))

        takeRepository.removeNonExistentTakes().blockingAwait()

        val retrieved = retrieveByContent()
        Assert.assertEquals(emptyList<Take>(), retrieved)

        // Check if the selected take was nulled
        val selectedTakeFk = mockDatabase
                .getContentDao()
                .fetchById(1)
                .selectedTakeFk
        Assert.assertEquals(null, selectedTakeFk)
    }

    // CRUD methods
    private fun create(path: File = File("/path/to/take.wav")): Take {
        val take = Take(
                "take.wav",
                path,
                1,
                LocalDate.now(),
                true,
                listOf(Marker(1, 2000, "verse"))
        )
        take.id = takeRepository.insertForContent(take, mockContent).blockingGet()
        return take
    }

    private fun retrieveByContent(): List<Take> = takeRepository.getByContent(mockContent).blockingGet()

    private fun updateMarkerIds(retrievedMarkers: List<Marker>, originalMarkers: List<Marker>) {
        // Update the marker ids since only the take id is returned by insert
        originalMarkers.forEach { marker ->
            marker.id = retrievedMarkers.filter { it.number == marker.number }.firstOrNull()?.id ?: 0
        }
    }

    private fun update(take: Take) {
        take.filename = "newfile.wav"
        take.path = File("/new/take/path/newfile.wav")
        take.played = false
        take.number = 34
        take.timestamp = LocalDate.of(2018, 11, 1)
        take.markers = listOf(Marker(2, 9402959, "marker"))
        takeRepository.update(take).blockingAwait()
    }

    private fun delete(take: Take) {
        takeRepository.delete(take).blockingAwait()
    }
}