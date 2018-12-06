package com.example.android.observability.ui

import androidx.lifecycle.ViewModel
import com.example.android.observability.HashUtil
import com.example.android.observability.nextInt
import com.example.android.observability.persistence.User
import com.example.android.observability.persistence.UserDao
import io.reactivex.Completable
import io.reactivex.Flowable
import java.util.*

class UserViewModel(private val dataSource: UserDao) : ViewModel() {

    fun userName(): Flowable<List<User>> {
        return dataSource.getAllUsers()
                .map { user -> user }
    }

    fun updateUserName(userName: String, password: String): Completable {
        return Completable.fromAction {
            val randomPasswordKey = getRandomPasswordKey()
            val user = User(userName = userName, passwordKey = randomPasswordKey, passwordValue = getObfuscatedPassword(password, randomPasswordKey))
            dataSource.insertUser(user)
        }
    }

    fun isValid(listUser: List<User>, userName: String, userPassword: String) : Boolean {
        for (user in listUser) {
            if (user.userName == userName &&
                    user.passwordValue == getObfuscatedPassword(userPassword, user.passwordKey)) {
                return true
            }
        }
        return false
    }

    private fun getRandomPasswordKey() : Int {
        return Random().nextInt(0..999999)
    }

    private fun getObfuscatedPassword(inputPassword: String, randomPasswordKey: Int): String {
        val inputPasswordHashed = HashUtil.hashString("SHA-256", inputPassword)
        return HashUtil.hashString("SHA-256", randomPasswordKey.toString().plus(inputPasswordHashed))
    }
}
