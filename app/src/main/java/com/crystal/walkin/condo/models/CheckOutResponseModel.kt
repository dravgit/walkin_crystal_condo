package com.crystal.walkin.condo.models

class CheckOutResponseModel(val from: String,
                            val contact_code: String,
                            val idcard: String,
                            val fullname: String,
                            val vehicle_registration: String,
                            val temperature: String,
                            val department_id: String,
                            val objective_id: String,
                            val checkout_time: String,
                            val checkin_time: String,
                            val objective_note: String,
                            val show_time: String,
                            val gender: String,
                            val birth_date: String,
                            val address: String,
                            val person_contact: String): BaseResponseModel()