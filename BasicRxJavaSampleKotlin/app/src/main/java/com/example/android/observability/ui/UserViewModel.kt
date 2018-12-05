/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.observability.ui

import androidx.lifecycle.ViewModel
import com.example.android.observability.HashUtil
import com.example.android.observability.persistence.User
import com.example.android.observability.persistence.UserDao
import io.reactivex.Completable
import io.reactivex.Flowable

class UserViewModel(private val dataSource: UserDao) : ViewModel() {

    fun userName(): Flowable<String> {
        return dataSource.getUserById(USER_ID)
                .map { user -> user.userName }
    }

    fun updateUserName(userName: String, password: String): Completable {
        return Completable.fromAction {
            val user = User(USER_ID, userName, PASSWORD_KEY, getObfuscatedPassword(password))
            dataSource.insertUser(user)
        }
    }

    private fun getObfuscatedPassword(inputPassword: String): String {
        val inputPasswordHashed = HashUtil.hashString("SHA-256", inputPassword)
        return HashUtil.hashString("SHA-256", PASSWORD_KEY.plus(inputPasswordHashed))
    }

    companion object {
        const val USER_ID = "1"
        const val PASSWORD_KEY = "SALT"
    }
}
