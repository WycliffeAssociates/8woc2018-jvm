package org.wycliffeassociates.otter.jvm.persistence.repo

import org.wycliffeassociates.otter.common.data.model.Language
import org.wycliffeassociates.otter.common.data.dao.LanguageDao
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.jooq.Configuration
import org.wycliffeassociates.otter.jvm.persistence.mapping.LanguageMapper
import jooq.tables.daos.LanguageEntityDao

class DefaultLanguageDao(config: Configuration, private val languageMapper: LanguageMapper) : LanguageDao {
    // uses generated repo to access database
    private val languagesDao = LanguageEntityDao(config)

    override fun delete(obj: Language): Completable {
        return Completable.fromAction {
            languagesDao.delete(languageMapper.mapToEntity(obj))
        }.subscribeOn(Schedulers.io())
    }

    override fun getAll(): Observable<List<Language>> {
        return Observable.fromCallable {
            languagesDao
                .findAll()
                .toList()
                .map { languageMapper.mapFromEntity(it) }
        }.subscribeOn(Schedulers.io())
    }

    override fun getById(id: Int): Observable<Language> {
        return Observable.fromCallable {
            languageMapper.mapFromEntity(
                languagesDao
                    .fetchById(id)
                    .first()
            )
        }.subscribeOn(Schedulers.io())
    }

    override fun insert(obj: Language): Observable<Int> {
        return Observable.fromCallable {
            val entity = languageMapper.mapToEntity(obj)
            if (entity.id == 0) entity.id = null
            languagesDao.insert(entity)
            // fetches by slug to get inserted value since generated insert returns nothing
            languagesDao
                .fetchBySlug(obj.slug)
                .first()
                .id
        }.subscribeOn(Schedulers.io())
    }

    override fun update(obj: Language): Completable {
        return Completable.fromAction {
            languagesDao.update(languageMapper.mapToEntity(obj))
        }.subscribeOn(Schedulers.io())
    }

    override fun getGatewayLanguages(): Observable<List<Language>> {
        return Observable.fromCallable {
            languagesDao
                    .fetchByGateway(1)
                    .map {
                        languageMapper.mapFromEntity(it)
                    }
        }.subscribeOn(Schedulers.io())
    }

}