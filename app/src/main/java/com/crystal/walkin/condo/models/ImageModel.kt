package com.crystal.walkin.condo.models

class ImageModel(val type: String, val type_name: String, val url: String){
    fun type(): String {
        return type
    }
    fun url(): String {
        return url
    }
}