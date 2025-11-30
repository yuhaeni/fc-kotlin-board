package com.fastcampus.fcboard.controller

import com.fastcampus.fcboard.domain.Post
import com.fastcampus.fcboard.exception.PostNotDeletableException
import com.fastcampus.fcboard.exception.PostNotFoundException
import com.fastcampus.fcboard.exception.PostNotUpdatableException
import com.fastcampus.fcboard.repository.PostRepository
import com.fastcampus.fcboard.service.PostService
import com.fastcampus.fcboard.service.dto.PostCreateRequestDto
import com.fastcampus.fcboard.service.dto.PostUpdateRequestDto
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull

@SpringBootTest
class PostServiceTest(
    private val postService: PostService,
    private val postRepository: PostRepository,
) : BehaviorSpec({

        given("게시글 생성 시") {
            When("게시글 인풋이 정상적으로 들어오면") {
                val postId =
                    postService.createPost(
                        PostCreateRequestDto(
                            title = "제목",
                            content = "내용",
                            createdBy = "haeni",
                        ),
                    )
                then("게시글이 정상적으로 생성됨을 확인한다.") {
                    postId shouldBeGreaterThan 0L
                    val post = postRepository.findByIdOrNull(postId)
                    post shouldNotBe null
                    post?.title shouldBe "제목"
                    post?.content shouldBe "내용"
                    post?.createdBy shouldBe "haeni"
                }
            }
        }

        given("게시글 수정") {
            val post = postRepository.save(Post(title = "title", content = "content", createdBy = "haeni"))

            When("게시글 정상 수정 시") {
                val updatedId =
                    postService.updatePost(
                        post.id,
                        PostUpdateRequestDto(
                            title = "수정된 제목",
                            content = "수정된 내용",
                            updatedBy = "haeni",
                        ),
                    )
                then("게시글이 정상적으로 수정됨을 확인한다.") {
                    post.id shouldBe updatedId
                    val post = postRepository.findByIdOrNull(post.id)
                    post shouldNotBe null
                    post?.title shouldBe "수정된 제목"
                    post?.content shouldBe "수정된 내용"
                }
            }

            When("수정하려는 게시글이 없을 때") {
                then("게시글을 찾을 수 없다는 예외가 발생한다.") {
                    shouldThrow<PostNotFoundException> {
                        postService.updatePost(
                            9999L,
                            PostUpdateRequestDto(
                                title = "수정된 제목",
                                content = "수정된 내용",
                                updatedBy = "kou",
                            ),
                        )
                    }
                }
            }

            When("작성자가 동일하지 않을 때") {
                then("수정할 수 없는 게시물이라는 예외가 발생한다.") {
                    shouldThrow<PostNotUpdatableException> {
                        postService.updatePost(
                            1L,
                            PostUpdateRequestDto(
                                title = "수정된 제목",
                                content = "수정된 내용",
                                updatedBy = "kou",
                            ),
                        )
                    }
                }
            }
        }

        given("게시글 삭제 시") {
            val post = postRepository.save(Post(title = "title", content = "content", createdBy = "haeni"))

            When("정상 삭제 시") {
                val postId = postService.deletePost(post.id, "haeni")
                then("게시글이 정상적으로 삭제됨을 확인한다.") {
                    postId shouldBe post.id
                    postRepository.findByIdOrNull(postId) shouldBe null
                }
            }

            When("작성자가 동일하지 않을 시") {
                val post = postRepository.save(Post(title = "title", content = "content", createdBy = "haeni"))
                then("식제할 수 없는 게시물이라는 예외가 발생한다.") {
                    shouldThrow<PostNotDeletableException> { postService.deletePost(post.id, "kou") }
                }
            }
        }
    })
