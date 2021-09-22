package com.crystal.walkin.condo.models

class ObjectiveTypeModel(val id: String, val name: String, val description: String){
    override fun toString(): String {
        return name
    }
    fun getID(): String {
        return id
    }
}