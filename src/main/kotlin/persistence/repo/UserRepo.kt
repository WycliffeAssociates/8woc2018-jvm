package persistence.repo

import data.User
import data.dao.Dao
import io.reactivex.Completable
import io.reactivex.Observable
import io.requery.Persistable
import io.requery.kotlin.eq
import io.requery.kotlin.set
import io.requery.sql.KotlinEntityDataStore
import persistence.mapping.UserMapper
import persistence.model.IUserEntity

//todo implement DAO
class UserRepo(private val dataStore: KotlinEntityDataStore<Persistable>): Dao<User> {

    private val userMapper = UserMapper()
    /**
     * function to create and insert a user into the database
     * takes in a audioHash and a path to a recording to creaete
     */
    override fun insert(user: User): Observable<Int> {
        // returns created user
        return Observable.just(dataStore.insert(userMapper.mapToEntity(user)).id)
    }

    /**
     * gets user by Id
     */
    override fun getById(id:Int): Observable<User>{
        val tmp = dataStore {
            val result = dataStore.select(IUserEntity::class).where(IUserEntity::id eq id)
            result.get().first()
        }
        return Observable.just(userMapper.mapFromEntity(tmp))
    }

    /**
     * given a audioHash gets the user
     */
    fun getByHash(hash: String): Observable<User> {
        val tmp = dataStore {
            val result = dataStore.select(IUserEntity::class).where(IUserEntity::audioHash eq hash)
            result.get().first()
        }
        return Observable.just(userMapper.mapFromEntity(tmp))
    }

    /**
     * gets all the users currently stored in db
     */
    override fun getAll(): Observable<List<User>>{
        val tmp = dataStore {
            val result = dataStore.select(IUserEntity::class)
            result.get().asIterable()
        }
        return Observable.just(tmp.map { userMapper.mapFromEntity(it) })

    }

    //todo fix
    override fun update(user: User): Completable{
        return Completable.fromAction{
            dataStore.update(IUserEntity::class).set(IUserEntity::audioHash,user.audioHash)
                    .where(IUserEntity::id eq  user)
        }
    }

    /**
     * deletes user by id
     */
    override fun delete(user: User): Completable{
        return Completable.fromAction{
            dataStore.delete(IUserEntity::class).where(IUserEntity::id eq user.id).get().value()
        }
    }
}