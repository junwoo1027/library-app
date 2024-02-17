package com.group.libraryapp.service.user;

import com.group.libraryapp.domain.user.User
import com.group.libraryapp.domain.user.UserRepository
import com.group.libraryapp.dto.user.request.UserCreateRequest
import com.group.libraryapp.dto.user.request.UserUpdateRequest
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest


@SpringBootTest
class UserServiceTest @Autowired constructor(
    private val userRepository: UserRepository,
    private val userService: UserService
) {

    @AfterEach
    fun clean() {
        userRepository.deleteAll()
    }

    @Test
    @DisplayName("유저 저장이 정상 동작한다")
    fun saveUser() {
        //given
        val request = UserCreateRequest("junwoo", null)

        //when
        userService.saveUser(request)

        //then
        val results = userRepository.findAll()
        assertThat(results).hasSize(1)
        assertThat(results[0].name).isEqualTo("junwoo")
        assertThat(results[0].age).isNull()
    }

    @Test
    @DisplayName("유저 조회가 정상 동작한다")
    fun getUsersTest() {
        // given
        userRepository.saveAll(listOf(
            User("A", 20),
            User("B", 21),
            User("C", null),
        ))

        // when
        val results = userService.users

        // then
        assertThat(results).hasSize(3)
        assertThat(results).extracting("name").containsExactlyInAnyOrder("A", "B", "C")
        assertThat(results).extracting("age").containsExactly(20, 21, null)
    }

    @Test
    @DisplayName("유저 업데이트가 정상 동작한다")
    fun updateUserNameTest() {
        // given
        val user = userRepository.save(User("A", null))
        val request = UserUpdateRequest(user.id!!, "B")

        // when
        userService.updateUserName(request)

        // then
        val findUser = userRepository.findAll()[0]
        assertThat(findUser.name).isEqualTo("B")
    }

    @Test
    @DisplayName("유저 삭제가 정상 동작한다")
    fun deleteUserTest() {
        // given
        val user = userRepository.save(User("A", null))

        // when
        userService.deleteUser(user.name)

        //then
        val results = userRepository.findAll()
        assertThat(results).isEmpty()
    }
}