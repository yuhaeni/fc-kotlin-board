package com.fastcampus.fcboard.exception

open class PostException(
    message: String,
) : RuntimeException(message)

class PostNotFoundException : PostException("게시글을 찾을 수 없습니다.")

class PostNotUpdatableException : PostException("수정할 수 없는 게시물 입니다.")

class PostNotDeletableException : PostException("삭제할 수 없는 게시물 입니다.")
