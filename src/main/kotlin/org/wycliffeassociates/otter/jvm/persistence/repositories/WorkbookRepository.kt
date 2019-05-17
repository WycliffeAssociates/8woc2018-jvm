package org.wycliffeassociates.otter.jvm.persistence.repositories

import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.ReplayRelay
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import org.wycliffeassociates.otter.common.data.model.*
import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.common.data.model.Take
import org.wycliffeassociates.otter.common.data.workbook.*
import org.wycliffeassociates.otter.common.persistence.repositories.*
import java.util.*
import java.util.Collections.synchronizedMap

private typealias ModelTake = org.wycliffeassociates.otter.common.data.model.Take
private typealias WorkbookTake = org.wycliffeassociates.otter.common.data.workbook.Take

class WorkbookRepository(private val db: IDatabaseAccessors) : IWorkbookRepository {
    constructor(
        collectionRepository: ICollectionRepository,
        contentRepository: IContentRepository,
        resourceRepository: IResourceRepository,
        takeRepository: ITakeRepository
    ) : this(DefaultDatabaseAccessors(collectionRepository, contentRepository, resourceRepository, takeRepository))

    /** Disposers for Relays in the current workbook. */
    private val connections = CompositeDisposable()

    override fun get(source: Collection, target: Collection): Workbook {
        // Clear database connections and dispose observables for the
        // previous Workbook if a new one was requested.
        connections.clear()
        return Workbook(book(source), book(target))
    }

    private fun book(bookCollection: Collection): Book {
        return Book(
            title = bookCollection.titleKey,
            sort = bookCollection.sort,
            chapters = constructBookChapters(bookCollection),
            subtreeResources = db.getSubtreeResourceInfo(bookCollection)
        )
    }

    private fun constructBookChapters(bookCollection: Collection): Observable<Chapter> {
        val collections = db.getChildren(bookCollection)
            .flattenAsObservable { list -> list.sortedBy { it.sort } }

        val chapters = collections
            .concatMapEager { constructChapter(it).toObservable() }

        return chapters.cache()
    }

    private fun constructChapter(chapterCollection: Collection): Single<Chapter> {
        return db.getCollectionMetaContent(chapterCollection)
            .map { metaContent ->
                Chapter(
                    title = chapterCollection.titleKey,
                    sort = chapterCollection.sort,
                    resources = constructResourceGroups(chapterCollection),
                    audio = constructAssociatedAudio(metaContent),
                    chunks = constructChunks(chapterCollection),
                    subtreeResources = db.getSubtreeResourceInfo(chapterCollection)
                )
            }
    }

    private fun constructChunks(chapterCollection: Collection): Observable<Chunk> {
        val contents = db.getContentByCollection(chapterCollection)
            .flattenAsObservable { list -> list.sortedBy { it.sort } }
            .filter { it.labelKey != "chapter" } // TODO: filter by something better

        val chunks = contents
            .map {
                Chunk(
                    title = it.start.toString(),
                    sort = it.sort,
                    audio = constructAssociatedAudio(it),
                    resources = constructResourceGroups(it),
                    text = textItem(it)
                )
            }

        return chunks.cache()
    }

    private fun textItem(content: Content?): TextItem? {
        return content
            ?.format
            ?.let { MimeType.of(it) }
            ?.let { mimeType ->
                content.text?.let {
                    TextItem(it, mimeType)
                }
            }
    }

    private fun constructResource(title: Content, body: Content?): Resource? {
        val titleTextItem = textItem(title)
            ?: return null

        return Resource(
            sort = title.sort,
            title = titleTextItem,
            body = textItem(body),
            titleAudio = constructAssociatedAudio(title),
            bodyAudio = body?.let { constructAssociatedAudio(body) }
        )
    }

    private fun constructResourceGroups(content: Content) = constructResourceGroups(
        resourceInfoList = db.getResourceInfo(content),
        getResourceContents = { db.getResources(content, it) }
    )

    private fun constructResourceGroups(collection: Collection) = constructResourceGroups(
        resourceInfoList = db.getResourceInfo(collection),
        getResourceContents = { db.getResources(collection, it) }
    )

    private fun constructResourceGroups(
        resourceInfoList: List<ResourceInfo>,
        getResourceContents: (ResourceInfo) -> Observable<Content>
    ): List<ResourceGroup> {
        return resourceInfoList.map {
            ResourceGroup(
                it,
                getResourceContents(it)
                    .contentsToResources()
                    .cache()
            )
        }
    }

    private fun Observable<Content>.contentsToResources(): Observable<Resource> {
        return this
            .buffer(2, 1)
            .concatMapMaybe { (a, b) ->
                Maybe.fromCallable {
                    when {
                        a.labelKey != "title" -> null
                        b.labelKey != "body" -> constructResource(a, null)
                        else -> constructResource(a, b)
                    }
                }
            }
    }

    /** Build a relay primed with the current deletion state, that responds to updates by writing to the DB. */
    private fun deletionRelay(modelTake: ModelTake): BehaviorRelay<DateHolder> {
        val relay = BehaviorRelay.createDefault(DateHolder(modelTake.deleted))

        val subscription = relay
            .skip(1) // ignore the initial value
            .subscribe {
                db.updateTake(modelTake, it)
            }

        connections += subscription
        return relay
    }


    private fun deselectUponDelete(take: WorkbookTake, selectedTakeRelay: BehaviorRelay<TakeHolder>) {
        val subscription = take.deletedTimestamp
            .filter { localDate -> localDate.value != null }
            .filter { take == selectedTakeRelay.value?.value }
            .map { TakeHolder(null) }
            .subscribe(selectedTakeRelay)
        connections += subscription
    }

    private fun workbookTake(modelTake: ModelTake): WorkbookTake {
        return WorkbookTake(
            name = modelTake.filename,
            file = modelTake.path,
            number = modelTake.number,
            format = MimeType.WAV, // TODO
            createdTimestamp = modelTake.created,
            deletedTimestamp = deletionRelay(modelTake)
        )
    }

    private fun modelTake(workbookTake: WorkbookTake, markers: List<Marker> = listOf()): ModelTake {
        return ModelTake(
            filename = workbookTake.file.name,
            path = workbookTake.file,
            number = workbookTake.number,
            created = workbookTake.createdTimestamp,
            deleted = null,
            played = false,
            markers = markers
        )
    }

    private fun constructAssociatedAudio(content: Content): AssociatedAudio {
        /** Map to recover model.Take objects from workbook.Take objects. */
        val takeMap = synchronizedMap(WeakHashMap<WorkbookTake, ModelTake>())

        /** The initial selected take, from the DB. */
        val initialSelectedTake = TakeHolder(content.selectedTake?.let { workbookTake(it) })

        /** Relay to send selected-take updates out to consumers, but also receive updates from UI. */
        val selectedTakeRelay = BehaviorRelay.createDefault(initialSelectedTake)

        // When we receive an update, write it to the DB.
        val selectedTakeRelaySubscription = selectedTakeRelay
            .distinctUntilChanged() // Don't write unless changed
            .skip(1) // Don't write the value we just loaded from the DB
            .subscribe {
                content.selectedTake = it.value?.let { wbTake -> takeMap[wbTake] }
                db.updateContent(content)
            }

        /** Initial Takes read from the DB. */
        val takesFromDb = db.getTakeByContent(content)
            .flattenAsObservable { list -> list.sortedBy { it.number } }
            .map { workbookTake(it) to it }

        /** Relay to send Takes out to consumers, but also receive new Takes from UI. */
        val takesRelay = ReplayRelay.create<WorkbookTake>()
        takesFromDb
            // Record the mapping between data types.
            .doOnNext { (wbTake, modelTake) -> takeMap[wbTake] = modelTake }
            // Feed the initial list to takesRelay
            .map { (wbTake, _) -> wbTake }
            .subscribe(takesRelay)

        val takesRelaySubscription = takesRelay
            // When the selected take becomes deleted, deselect it.
            .doOnNext { deselectUponDelete(it, selectedTakeRelay) }

            // Keep the takeMap current.
            .filter { !takeMap.contains(it) } // don't duplicate takes
            .map { it to modelTake(it) }
            .doOnNext { (wbTake, modelTake) -> takeMap[wbTake] = modelTake }

            // Insert the new take into the DB.
            .subscribe { (_, modelTake) ->
                db.insertTakeForContent(modelTake, content)
                    .subscribe { insertionId -> modelTake.id = insertionId }
            }

        connections += takesRelaySubscription
        connections += selectedTakeRelaySubscription
        return AssociatedAudio(takesRelay, selectedTakeRelay)
    }

    interface IDatabaseAccessors {
        fun getChildren(collection: Collection): Single<List<Collection>>
        fun getCollectionMetaContent(collection: Collection): Single<Content>
        fun getContentByCollection(collection: Collection): Single<List<Content>>
        fun updateContent(content: Content): Completable
        fun getResources(content: Content, info: ResourceInfo): Observable<Content>
        fun getResources(collection: Collection, info: ResourceInfo): Observable<Content>
        fun getResourceInfo(content: Content): List<ResourceInfo>
        fun getResourceInfo(collection: Collection): List<ResourceInfo>
        fun getSubtreeResourceInfo(collection: Collection): List<ResourceInfo>
        fun insertTakeForContent(take: ModelTake, content: Content): Single<Int>
        fun getTakeByContent(content: Content): Single<List<Take>>
        fun updateTake(take: ModelTake, date: DateHolder): Completable
    }
}

private class DefaultDatabaseAccessors(
    private val collectionRepo: ICollectionRepository,
    private val contentRepo: IContentRepository,
    private val resourceRepo: IResourceRepository,
    private val takeRepo: ITakeRepository
) : WorkbookRepository.IDatabaseAccessors {
    override fun getChildren(collection: Collection) = collectionRepo.getChildren(collection)

    override fun getCollectionMetaContent(collection: Collection) = contentRepo.getCollectionMetaContent(collection)
    override fun getContentByCollection(collection: Collection) = contentRepo.getByCollection(collection)
    override fun updateContent(content: Content) = contentRepo.update(content)

    override fun getResources(content: Content, info: ResourceInfo) = resourceRepo.getResources(content, info)
    override fun getResources(collection: Collection, info: ResourceInfo) = resourceRepo.getResources(collection, info)
    override fun getResourceInfo(content: Content) = resourceRepo.getResourceInfo(content)
    override fun getResourceInfo(collection: Collection) = resourceRepo.getResourceInfo(collection)
    override fun getSubtreeResourceInfo(collection: Collection) = resourceRepo.getSubtreeResourceInfo(collection)

    override fun insertTakeForContent(take: ModelTake, content: Content) = takeRepo.insertForContent(take, content)
    override fun getTakeByContent(content: Content) = takeRepo.getByContent(content)
    override fun updateTake(take: ModelTake, date: DateHolder) = takeRepo.update(take.copy(deleted = date.value))
}
