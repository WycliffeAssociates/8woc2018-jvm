package persistence.repo

import data.model.Language
import data.dao.Dao
import data.dao.LanguageDao
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.jooq.Configuration
import persistence.mapping.LanguageMapper
import persistence.tables.LanguageEntity
import persistence.tables.daos.LanguageEntityDao

class LanguageRepo(val config: Configuration): LanguageDao {
    private val languagesDao = LanguageEntityDao(config)
    private val languageMapper = LanguageMapper()

    override fun delete(obj: Language): Completable {
        return Completable.fromAction {
            languagesDao.delete(languageMapper.mapToEntity(obj))
        }.subscribeOn(Schedulers.io())
    }

    override fun getAll(): Observable<List<Language>> {
        return Observable.create<List<Language>> {
            it.onNext(languagesDao.findAll().toList().map { languageMapper.mapFromEntity(it) })
        }.subscribeOn(Schedulers.io())
    }

    override fun getById(id: Int): Observable<Language> {
        return Observable.create<Language> {
            it.onNext(languageMapper.mapFromEntity(languagesDao.fetchById(id).first()))
        }.subscribeOn(Schedulers.io())
    }

    override fun insert(obj: Language): Observable<Int> {
        return Observable.create<Int>{
            val sql = "SELECT MAX(id) FROM LANGUAGE ENTITY"
            languagesDao.insert(languageMapper.mapToEntity(obj))
            it.onNext(languagesDao.fetchBySlug(obj.slug).first().id)
        }.subscribeOn(Schedulers.io())
    }

    override fun update(obj: Language): Completable {
        return Completable.fromAction {
            languagesDao.update(languageMapper.mapToEntity(obj))
        }.subscribeOn(Schedulers.io())
    }

    override fun getGatewayLanguages(): Observable<List<Language>> {
        return Observable.create<List<Language>> {
            it.onNext(
                    languagesDao.fetchByIsgateway(1).map {
                        languageMapper.mapFromEntity(it)
                    }
            )
        }.subscribeOn(Schedulers.io())
    }

}