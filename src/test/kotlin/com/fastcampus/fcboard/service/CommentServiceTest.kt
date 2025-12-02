package com.fastcampus.fcboard.service

import com.fastcampus.fcboard.domain.Comment
import com.fastcampus.fcboard.domain.Post
import com.fastcampus.fcboard.exception.CommentNotDeletableException
import com.fastcampus.fcboard.exception.CommentNotUpdatableException
import com.fastcampus.fcboard.exception.PostNotFoundException
import com.fastcampus.fcboard.repository.CommentRepository
import com.fastcampus.fcboard.repository.PostRepository
import com.fastcampus.fcboard.service.dto.CommentCreateRequestDto
import com.fastcampus.fcboard.service.dto.CommentUpdateRequestDto
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull

@SpringBootTest
class CommentServiceTest(
    private val commentService: CommentService,
    private val commentRepository: CommentRepository,
    private val postRepository: PostRepository,
) : BehaviorSpec({

        given("댓글 생성 시") {
            val post =
                postRepository.save(
                    Post(
                        title = "제목~",
                        content = "내용~",
                        createdBy = "haeni",
                    ),
                )

            When("인풋이 정상적으로 들어오면") {
                val commentId =
                    commentService.createComment(
                        post.id,
                        CommentCreateRequestDto(
                            content = "댓글!",
                            createdBy = "미도리야",
                        ),
                    )
                then("정상 생성됨을 확인한다.") {
                    commentId shouldBeGreaterThan 0L
                    val comment = commentRepository.findByIdOrNull(commentId)
                    comment shouldNotBe null
                    comment?.content shouldBe "댓글!"
                    comment?.createdBy shouldBe "미도리야"
                }
            }

            When("게시글이 존재하지 않으면") {
                then("게시글이 존재하지 않는다는 예외가 발생한다.") {
                    shouldThrow<PostNotFoundException> {
                        commentService.createComment(
                            99999L,
                            CommentCreateRequestDto(
                                content = "댓글!",
                                createdBy = "미도리야",
                            ),
                        )
                    }
                }
            }
        }

        given("댓글 수정 시") {

            val post =
                postRepository.save(
                    Post(
                        title = "제목~~",
                        content = "내용~~",
                        createdBy = "haeni",
                    ),
                )
            val saved =
                commentRepository.save(
                    Comment(
                        post = post,
                        content = "댓글!!",
                        createdBy = "미도리야",
                    ),
                )

            When("인풋이 정상적으로 들어오면") {
                val updatedId =
                    commentService.updateComment(
                        saved.id,
                        CommentUpdateRequestDto(
                            content = "댓글@@",
                            updatedBy = "미도리야",
                        ),
                    )
                then("정상 수정됨을 확인한다.") {
                    updatedId shouldBe saved.id
                    val updated = commentRepository.findByIdOrNull(updatedId)
                    updated shouldNotBe null
                    updated?.content shouldBe "댓글@@"
                    updated?.updatedBy shouldBe "미도리야"
                }
            }

            When("댓글 작성자와 수정자가 다르면") {
                then("수정할 수 없는 댓글이라는 예외가 발생한다.") {
                    shouldThrow<CommentNotUpdatableException> {
                        commentService.updateComment(
                            saved.id,
                            CommentUpdateRequestDto(
                                content = "댓글@@",
                                updatedBy = "test",
                            ),
                        )
                    }
                }
            }
        }

        given("댓글 삭제 시") {

            val post =
                postRepository.save(
                    Post(
                        title = "제목~~",
                        content = "내용~~",
                        createdBy = "haeni",
                    ),
                )

            val saved1 =
                commentRepository.save(
                    Comment(
                        post = post,
                        content = "댓글!!",
                        createdBy = "미도리야",
                    ),
                )

            val saved2 =
                commentRepository.save(
                    Comment(
                        post = post,
                        content = "댓글!!",
                        createdBy = "미도리야",
                    ),
                )

            When("인풋이 정상적으로 들어오면") {
                then("정상 삭제 됨을 확익한다.") {
                    val commentId =
                        commentService.deleteComment(
                            saved1.id,
                            saved1.createdBy,
                        )
                    commentId shouldBe saved1.id
                    commentRepository.findByIdOrNull(commentId) shouldBe null
                }
            }

            When("댓글 작성자와 수정자가 다르면") {
                then("삭제할 수 없는 댓글이라는 예외가 발생한다.") {
                    shouldThrow<CommentNotDeletableException> {
                        commentService.deleteComment(
                            saved2.id,
                            "test11",
                        )
                    }
                }
            }
        }
    })
