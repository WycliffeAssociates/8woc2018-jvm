package org.wycliffeassociates.otter.jvm.app.ui.projectwizard.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import com.github.thomasnield.rxkotlinfx.toObservable
import io.reactivex.subjects.PublishSubject
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.common.data.model.Language
import org.wycliffeassociates.otter.common.domain.collections.CreateProject
import org.wycliffeassociates.otter.jvm.app.ui.inject.Injector
import org.wycliffeassociates.otter.jvm.app.ui.projecthome.viewmodel.ProjectHomeViewModel
import tornadofx.ViewModel
import tornadofx.Wizard
import tornadofx.booleanBinding

class ProjectWizardViewModel : ViewModel() {
    private val languageRepo = Injector.languageRepo
    private val collectionRepo = Injector.collectionRepo

    val clearLanguages: PublishSubject<Boolean> = PublishSubject.create()
    val sourceLanguageProperty = bind(true) { SimpleObjectProperty<Language>() }
    val targetLanguageProperty = bind(true) { SimpleObjectProperty<Language>() }
    val collections: ObservableList<Collection> = FXCollections.observableArrayList()
    val languages: ObservableList<Language> = FXCollections.observableArrayList()

    private val collectionHierarchy: ArrayList<List<Collection>> = ArrayList()

    private val projects = FXCollections.observableArrayList<Collection>()

    private val existingProjects: ObservableList<Collection> = FXCollections.observableArrayList()
    val showOverlayProperty = SimpleBooleanProperty(false)
    val creationCompletedProperty = SimpleBooleanProperty(false)

    private val creationUseCase = CreateProject(collectionRepo)

    init {
        languageRepo
                .getAll()
                .observeOnFx()
                .subscribe { retrieved ->
                    languages.setAll(retrieved)
                }

        loadProjects()

        targetLanguageProperty.toObservable().subscribe { language ->
            existingProjects.setAll(projects.filter { it.resourceContainer?.language == language })
        }
    }

    private fun loadProjects() {
        collectionRepo
                .getRootProjects()
                .subscribe { retrieved ->
                    projects.setAll(retrieved)
                }
    }

    fun getRootSources() {
        collectionRepo
                .getRootSources()
                .observeOnFx()
                .subscribe { retrieved ->
                    collectionHierarchy.add(retrieved.filter {
                        it.resourceContainer?.language == sourceLanguageProperty.value
                    })
                    collections.setAll(collectionHierarchy.last())
                }
    }

    fun doOnUserSelection(selectedCollection: Collection) {
        if (selectedCollection.labelKey == "project") {
            createProject(selectedCollection)
        } else {
            showSubcollections(selectedCollection)
        }
    }

    private fun showSubcollections(collection: Collection) {
        collectionRepo
                .getChildren(collection)
                .observeOnFx()
                .doOnSuccess { subcollections ->
                    collectionHierarchy.add(subcollections)
                    collections.setAll(collectionHierarchy.last().sortedBy { it.sort })
                }
                .subscribe()
    }

    private fun createProject(selectedCollection: Collection) {
        targetLanguageProperty.value?.let { language ->
            showOverlayProperty.value = true
            creationUseCase
                    .create(selectedCollection, language)
                    .subscribe {
                        tornadofx.find(ProjectHomeViewModel::class).loadProjects()
                        showOverlayProperty.value = false
                        creationCompletedProperty.value = true
                    }
        }
    }

    fun goBack(projectWizard: Wizard) {
        when {
            collectionHierarchy.size > 1 -> {
                collectionHierarchy.removeAt(collectionHierarchy.lastIndex)
                collections.setAll(collectionHierarchy.last().sortedBy { it.sort })
            }
            collectionHierarchy.size == 1 -> {
                collectionHierarchy.removeAt(0)
                projectWizard.back()
            }
            else -> projectWizard.back()
        }
    }

    fun doesProjectExist(project: Collection): Boolean {
        return existingProjects.map { it.titleKey }.contains(project.titleKey)
    }

    fun reset() {
        clearLanguages.onNext(true)
        collections.setAll()
        collectionHierarchy.clear()
        existingProjects.clear()
        creationCompletedProperty.value = false
        loadProjects()
    }

    fun filterLanguages(query: String): ObservableList<Language> =
        languages.filtered {
            it.name.contains(query, true)
                    || it.anglicizedName.contains(query, true)
                    || it.slug.contains(query, true)
        }.sorted { lang1, lang2 ->
            when {
                lang1.slug.startsWith(query, true) -> -1
                lang2.slug.startsWith(query, true) -> 1
                lang1.name.startsWith(query, true) -> -1
                lang2.name.startsWith(query, true) -> 1
                lang1.anglicizedName.startsWith(query, true) -> -1
                lang2.anglicizedName.startsWith(query, true) -> 1
                else -> 0
            }
        }

    fun languagesValid() = sourceLanguageProperty.booleanBinding(targetLanguageProperty) {
        sourceLanguageProperty.value != null && targetLanguageProperty.value != null
    }
}