package com.oop.traveloop.ui.auth

import com.oop.traveloop.domain.model.AuthSession
import com.oop.traveloop.domain.usecase.LoginUseCase
import com.oop.traveloop.domain.usecase.LogoutUseCase
import com.oop.traveloop.domain.usecase.ObserveAuthSessionUseCase
import com.oop.traveloop.domain.usecase.ObserveUserProfileUseCase
import com.oop.traveloop.domain.usecase.RefreshUserProfileUseCase
import com.oop.traveloop.domain.usecase.RegisterUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun viewModel(repo: FakeAuthRepository) = AuthViewModel(
        LoginUseCase(repo), RegisterUseCase(repo), LogoutUseCase(repo), RefreshUserProfileUseCase(repo), ObserveAuthSessionUseCase(repo), ObserveUserProfileUseCase(repo)
    )

    @Test
    fun loginSuccess_clearsLoadingAndError() = runTest {
        val repo = FakeAuthRepository()
        val vm = viewModel(repo)
        vm.onAction(AuthAction.EmailChanged("user@example.com"))
        vm.onAction(AuthAction.PasswordChanged("password123"))
        vm.onAction(AuthAction.SubmitLogin)
        advanceUntilIdle()
        assertFalse(vm.uiState.value.isLoading)
        assertNull(vm.uiState.value.error)
    }

    @Test
    fun loginFailure_surfacesErrorMessage() = runTest {
        val repo = FakeAuthRepository(loginError = IllegalStateException("Backend tidak terhubung"))
        val vm = viewModel(repo)
        vm.onAction(AuthAction.EmailChanged("user@example.com"))
        vm.onAction(AuthAction.PasswordChanged("password123"))
        vm.onAction(AuthAction.SubmitLogin)
        advanceUntilIdle()
        assertEquals("Backend tidak terhubung", vm.uiState.value.error)
    }

    @Test
    fun loginHttpError_surfacesFriendlyMessage() = runTest {
        val repo = FakeAuthRepository(loginError = IllegalStateException("HTTP 400"))
        val vm = viewModel(repo)
        vm.onAction(AuthAction.EmailChanged("user@example.com"))
        vm.onAction(AuthAction.PasswordChanged("password123"))
        vm.onAction(AuthAction.SubmitLogin)
        advanceUntilIdle()
        assertEquals("Email atau password salah", vm.uiState.value.error)
    }

    @Test
    fun loginValidationError_surfacesErrorMessage() = runTest {
        val repo = FakeAuthRepository()
        val vm = viewModel(repo)
        vm.onAction(AuthAction.EmailChanged("user@example.com"))
        vm.onAction(AuthAction.SubmitLogin)
        advanceUntilIdle()
        assertFalse(vm.uiState.value.isLoading)
        assertEquals("Password wajib diisi", vm.uiState.value.error)
    }

    @Test
    fun registerValidationError_surfacesErrorMessage() = runTest {
        val repo = FakeAuthRepository()
        val vm = viewModel(repo)
        vm.onAction(AuthAction.EmailChanged("user@example.com"))
        vm.onAction(AuthAction.PasswordChanged("password123"))
        vm.onAction(AuthAction.SubmitRegister)
        advanceUntilIdle()
        assertFalse(vm.uiState.value.isLoading)
        assertEquals("Nama wajib diisi", vm.uiState.value.error)
    }

    @Test
    fun registerHttp400_surfacesRegisterMessage() = runTest {
        val repo = FakeAuthRepository(registerError = IllegalStateException("HTTP 400"))
        val vm = viewModel(repo)
        vm.onAction(AuthAction.NameChanged("User"))
        vm.onAction(AuthAction.EmailChanged("user@example.com"))
        vm.onAction(AuthAction.PasswordChanged("password123"))
        vm.onAction(AuthAction.SubmitRegister)
        advanceUntilIdle()
        assertEquals("Nama, email, atau password belum valid", vm.uiState.value.error)
    }

    @Test
    fun registerConflict_surfacesDuplicateEmailMessage() = runTest {
        val repo = FakeAuthRepository(registerError = IllegalStateException("HTTP 409"))
        val vm = viewModel(repo)
        vm.onAction(AuthAction.NameChanged("User"))
        vm.onAction(AuthAction.EmailChanged("user@example.com"))
        vm.onAction(AuthAction.PasswordChanged("password123"))
        vm.onAction(AuthAction.SubmitRegister)
        advanceUntilIdle()
        assertEquals("Email sudah terdaftar", vm.uiState.value.error)
    }

    @Test
    fun logout_callsRepositoryAndClearsSession() = runTest {
        val repo = FakeAuthRepository()
        repo.setSession(AuthSession("access", "refresh"))
        val vm = viewModel(repo)
        vm.logout()
        advanceUntilIdle()
        assertTrue(repo.logoutCalled)
    }

    @Test
    fun session_reflectsClearedTokenAfterFailedRefresh() = runTest {
        val repo = FakeAuthRepository()
        repo.setSession(AuthSession("access", "refresh"))
        val vm = viewModel(repo)
        val collected = mutableListOf<AuthSession?>()
        val job = launch { vm.session.collect { collected.add(it) } }
        advanceUntilIdle()
        repo.clearSession()
        advanceUntilIdle()
        job.cancel()
        assertEquals(AuthSession("access", "refresh"), collected.first())
        assertNull(collected.last())
    }
}
