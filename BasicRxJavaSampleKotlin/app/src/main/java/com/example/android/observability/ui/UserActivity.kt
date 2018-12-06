package com.example.android.observability.ui

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.example.android.observability.Injection
import com.example.android.observability.R
import com.example.android.observability.persistence.User
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_user.*

class UserActivity : AppCompatActivity() {

    private lateinit var viewModelFactory: ViewModelFactory

    private lateinit var viewModel: UserViewModel

    private val disposable = CompositeDisposable()

    private lateinit var usersList: List<User>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        viewModelFactory = Injection.provideViewModelFactory(this)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(UserViewModel::class.java)
        update_button.setOnClickListener { updateUserName() }
        auth_button.setOnClickListener { auth() }
    }

    override fun onStart() {
        super.onStart()

        disposable.add(viewModel.userName()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    this.usersList = it
                    fillList(it) },
                        { error -> Log.e(TAG, "Unable to get username", error) }))
    }

    private fun fillList(it: List<User>) {
        var items = ""
        for (value in it.map { user -> user.userName }) {
            items += "\n".plus(value)
        }
        this.users_list.text = items
    }

    override fun onStop() {
        super.onStop()
        disposable.clear()
    }

    private fun updateUserName() {
        val userName = user_name_input.text.toString()
        val password = user_password_input.text.toString()

        update_button.isEnabled = false

        disposable.add(viewModel.updateUserName(userName, password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    update_button.isEnabled = true
                    showToast("Usuário salvo com sucesso!")
                },
                        { error ->
                            Log.e(TAG, "Unable to update username", error)
                            showToast("Erro ao salvar usuário!")
                        }))
    }


    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun auth() {
        if (viewModel.isValid(usersList, user_name_input.text.toString(), user_password_input.text.toString())) {
            showToast("Usuário autenticado com sucesso!")
        } else {
            showToast("Usuário ou senha inválidos!")
        }
    }

    companion object {
        private val TAG = UserActivity::class.java.simpleName
    }
}
