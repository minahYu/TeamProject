package com.example.teamproject.navigation.model

data class ContentDTO (var explain : String? = null,// contents의 설명을 관리
                       var imageUrl : String? = null,  // 이미지 주소를 관리
                       var uid: String? = null, // 어느 user가 올렸는지 uid를 관리해주는 변수
                       var userId : String? = null,
                       var timeStamp : Long? = null,
                       var favoriteCount : Int = 0,
                       var favorites : MutableMap<String,Boolean> = HashMap()) {
    data class Comment(var uid: String? = null,
                       var userId: String? = null,
                       var comment: String? = null,
                       var timeStamp: Long? = null)
}