package com.fastcampus.fcboard.service

import com.fastcampus.fcboard.exception.PostNotDeletableException
import com.fastcampus.fcboard.exception.PostNotFoundException
import com.fastcampus.fcboard.repository.PostRepository
import com.fastcampus.fcboard.service.dto.PostCreateRequestDto
import com.fastcampus.fcboard.service.dto.PostDetailResponseDto
import com.fastcampus.fcboard.service.dto.PostSearchRequestDto
import com.fastcampus.fcboard.service.dto.PostSummaryResponseDto
import com.fastcampus.fcboard.service.dto.PostUpdateRequestDto
import com.fastcampus.fcboard.service.dto.toDetailResponseDto
import com.fastcampus.fcboard.service.dto.toEntity
import com.fastcampus.fcboard.service.dto.toSummaryResponseDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class PostService(
    private val postRepository: PostRepository,
    private val likeService: LikeService,
) {
    @Transactional // Transactional이 클래스 단위 함수 단위 둘 다 있는 경우, 함수 단위가 더 구체적이라 우선적으로 적용됨.
    fun createPost(requestDto: PostCreateRequestDto): Long = postRepository.save(requestDto.toEntity()).id

    @Transactional
    fun updatePost(
        id: Long,
        requestDto: PostUpdateRequestDto,
    ): Long {
        val post = postRepository.findByIdOrNull(id) ?: throw PostNotFoundException()
        post.update(requestDto)
        return id
    }

    @Transactional
    fun deletePost(
        id: Long,
        deletedBy: String,
    ): Long {
        val post = postRepository.findByIdOrNull(id)
        if (post?.createdBy != deletedBy) throw PostNotDeletableException()
        postRepository.deleteById(id)
        return id
    }

    fun getPost(id: Long): PostDetailResponseDto {
        val likeCount = likeService.countLike(id)
        return postRepository.findByIdOrNull(id)?.toDetailResponseDto(likeCount) ?: throw PostNotFoundException()
    }

    fun findPageBy(
        pageRequest: Pageable,
        postSearchRequestDto: PostSearchRequestDto,
    ): Page<PostSummaryResponseDto> =
        postRepository.findPageBy(pageRequest, postSearchRequestDto).toSummaryResponseDto(likeService::countLike)
}
