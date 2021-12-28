package com.neo.fbrules.main.presenter.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neo.fbrules.core.Constants
import com.neo.fbrules.core.Message
import com.neo.fbrules.core.Result
import com.neo.fbrules.main.domain.model.DomainCredential
import com.neo.fbrules.main.domain.useCase.*
import com.neo.fbrules.core.MutableSingleLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getRules: GetRules,
    private val setRules: SetRules
) : ViewModel() {

    var credential: DomainCredential? = null

    //OBSERVABLES

    //default
    val rules = MutableLiveData<String>()

    val error = MutableSingleLiveData<Result.Error>()
    val alert = MutableSingleLiveData<Message>()
    val message = MutableSingleLiveData<Message>()

    val loading = MutableLiveData(false)

    //especial
    val configBottomSheet = MutableSingleLiveData<() -> Unit>()
    val decryptBottomSheet = MutableSingleLiveData<Unit>()

    init {
        decryptBottomSheet.setValue(Unit)
    }

    //actions
    fun pullRules() {
        viewModelScope.launch {
            when (
                val response = verifyCredential { credential ->
                    loading.postValue(true)
                    getRules(credential)
                }
            ) {
                is Result.Success -> {
                    val data = response.data
                    rules.postValue(data)
                }

                is Result.Error -> {

                    when (response.type) {
                        Constants.ERROR.CREDENTIAL_NOT_FOUND -> {
                            configBottomSheet.postValue {
                                pullRules()
                            }
                        }

                        else -> {
                            error.postValue(
                                Result.Error(
                                    response.type,
                                    response.title,
                                    response.message
                                )
                            )
                        }
                    }
                }
            }

            loading.postValue(false)
        }
    }

    fun pushRules(rules: String?) {

        if (rules == null) {
            error.setValue(Result.Error(message = "rules is null"))
            return
        }

        viewModelScope.launch {
            when (
                val response = verifyCredential { credential ->
                    loading.postValue(true)
                    setRules(rules, credential)
                }
            ) {
                is Result.Success -> {
                    message.postValue(
                        Message(
                            title = "Success",
                            message = "Regras atualizadas!"
                        )
                    )
                }

                is Result.Error -> {

                    when (response.type) {
                        Constants.ERROR.CREDENTIAL_NOT_FOUND -> {
                            configBottomSheet.postValue {
                                pushRules(rules)
                            }
                        }

                        else -> {
                            error.postValue(response)
                        }
                    }
                }
            }

            loading.postValue(false)
        }
    }

    private suspend fun <T> verifyCredential(
        function: suspend (DomainCredential) -> Result<T>
    ): Result<T> {
        return if (credential == null) {
            Result.Error(Constants.ERROR.CREDENTIAL_NOT_FOUND)
        } else {
            function.invoke(credential!!)
        }
    }

    fun openConfig() {
        configBottomSheet.setValue{}
    }
}