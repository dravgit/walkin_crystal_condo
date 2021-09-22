package com.crystal.walkin.condo.models

class PartialVisitorResponseModel(val contact_code: String, val name: String, val checkin_time: String, val status: String, val status_name: String, val checkout_time: String, val department: String, val images: List<ImageModel>): BaseResponseModel()