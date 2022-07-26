package com.example.goddiary2

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Diary : RealmObject(){
    @PrimaryKey
    var date :String = ""
    var dateColor : Int = 1
    var year : Int = 0
    var month : Int = 0
    var weather : Int = 0
    var tag : Int = 0
    var title : String = ""
    var bodyText : String = ""
    var byteImage : ByteArray? = null
    var condition : Int = 0
}

open class Config : RealmObject(){
    @PrimaryKey
    var id : Int = 0
    var diaryName : String = "ばぁば日記"
    var diaryTitleColor : Int = 0
    var tag : String = "タグなし,1,2,3,4,5,6,7,8,9,10"
    var byteMainImage : ByteArray? = null
}
