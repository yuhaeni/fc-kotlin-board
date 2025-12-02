package com.fastcampus.fcboard.domain

import com.fastcampus.fcboard.exception.PostNotUpdatableException
import com.fastcampus.fcboard.service.dto.PostUpdateRequestDto
import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany

@Entity
class Post(
    createdBy: String,
    title: String,
    content: String,
) : BaseEntity(createdBy) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    var title = title
        protected set

    var content = content
        protected set

    @OneToMany(mappedBy = "post", orphanRemoval = true, cascade = [CascadeType.ALL])
    var comments = mutableListOf<Comment>()
        protected set

    fun update(postUpdateRequestDto: PostUpdateRequestDto) {
        if (this.createdBy != postUpdateRequestDto.updatedBy) {
            throw PostNotUpdatableException()
        }

        this.title = postUpdateRequestDto.title
        this.content = postUpdateRequestDto.content
        super.updatedBy(postUpdateRequestDto.updatedBy)
    }
}
