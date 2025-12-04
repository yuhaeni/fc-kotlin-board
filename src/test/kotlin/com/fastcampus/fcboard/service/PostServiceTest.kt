package com.fastcampus.fcboard.service

import com.fastcampus.fcboard.domain.Comment
import com.fastcampus.fcboard.domain.Post
import com.fastcampus.fcboard.exception.PostNotDeletableException
import com.fastcampus.fcboard.exception.PostNotFoundException
import com.fastcampus.fcboard.exception.PostNotUpdatableException
import com.fastcampus.fcboard.repository.CommentRepository
import com.fastcampus.fcboard.repository.PostRepository
import com.fastcampus.fcboard.service.dto.PostCreateRequestDto
import com.fastcampus.fcboard.service.dto.PostSearchRequestDto
import com.fastcampus.fcboard.service.dto.PostUpdateRequestDto
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull

@SpringBootTest
class PostServiceTest(
    private val postService: PostService,
    private val postRepository: PostRepository,
    private val commentRepository: CommentRepository,
) : BehaviorSpec({

        beforeSpec {
            postRepository.saveAll(
                listOf(
                    Post(title = "title1", content = "content", createdBy = "haeni"),
                    Post(title = "title2", content = "content2", createdBy = "kou"),
                    Post(title = "title3", content = "content3", createdBy = "haeni"),
                    Post(title = "title4", content = "content3", createdBy = "haeni"),
                    Post(title = "title5", content = "content3", createdBy = "kou"),
                    Post(title = "title6", content = "content3", createdBy = "haeni"),
                    Post(title = "title7", content = "content3", createdBy = "kou"),
                    Post(title = "title8", content = "content3", createdBy = "haeni"),
                    Post(title = "title9", content = "content3", createdBy = "haeni"),
                    Post(title = "title10", content = "content3", createdBy = "haeni"),
                    Post(title = "title11", content = "content3", createdBy = "haeni"),
                    Post(title = "title12", content = "content3", createdBy = "kou"),
                    Post(title = "title13", content = "content3", createdBy = "kou"),
                ),
            )
        }

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

        given("게시글 상세조회 시") {
            val post = postRepository.save(Post(title = "title", content = "content", createdBy = "haeni"))

            When("정상 조회 시") {
                val postDetail = postService.getPost(post.id)
                then("게시글의 내용이 정상적으로 반환됨을 확인한다.") {
                    postDetail.title shouldBe "title"
                    postDetail.content shouldBe "content"
                    postDetail.createdBy shouldBe "haeni"
                }
            }

            When("게시물이 없을 때") {
                then("게시물을 찾을 수 없다는 예외가 발생한다.") {
                    shouldThrow<PostNotFoundException> { postService.getPost(99999L) }
                }
            }

            When("댓글 추가 시") {
                commentRepository.save(Comment(content = "댓글@", post, createdBy = "Gou"))
                commentRepository.save(Comment(content = "댓글*", post, createdBy = "Kou"))
                commentRepository.save(Comment(content = "댓글#", post, createdBy = "Kou"))
                then("게시물과 댓글이 함께 조회됨을 확인한다.") {
                    val postDetail = postService.getPost(post.id)
                    postDetail.comments.size shouldBe 3
                    postDetail.comments[0].content shouldBe "댓글@"
                    postDetail.comments[1].content shouldBe "댓글*"
                    postDetail.comments[2].content shouldBe "댓글#"
                    postDetail.comments[0].createdBy shouldBe "Gou"
                    postDetail.comments[1].createdBy shouldBe "Kou"
                    postDetail.comments[2].createdBy shouldBe "Kou"
                }
            }
        }

        given("게시물 목록 조회 시") {
            When("정상 조회 시") {
                val postPage = postService.findPageBy(PageRequest.of(0, 10), PostSearchRequestDto())
                then("게시물 페이지가 반환된다.") {
                    postPage.pageable.pageNumber shouldBe 0
                    postPage.pageable.pageSize shouldBe 10
                    postPage.content.size shouldBe 10
                    postPage.content[0].title shouldContain "title"
                    postPage.content[0].createdBy shouldContain "haeni"
                }
            }
            When("타이틀로 검색할 시") {
                val postPage = postService.findPageBy(PageRequest.of(0, 10), PostSearchRequestDto(title = "title1"))
                then("타이틀에 해당하는 게시글이 반환된다.") {
                    postPage.pageable.pageNumber shouldBe 0
                    postPage.pageable.pageSize shouldBe 10
                    postPage.content.size shouldBe 5
                    postPage.content[0].title shouldContain "title"
                    postPage.content[0].createdBy shouldContain "kou"
                }
            }
            When("작성자로 검색할 시") {
                val postPage = postService.findPageBy(PageRequest.of(0, 10), PostSearchRequestDto(createdBy = "haeni"))
                then("작성자에 해당하는 게시글이 반환된다.") {
                    postPage.pageable.pageNumber shouldBe 0
                    postPage.pageable.pageSize shouldBe 10
                    postPage.content.size shouldBe 10
                    postPage.content[0].title shouldContain "title"
                    postPage.content[0].createdBy shouldBe "haeni"
                }
            }
        }
    })
