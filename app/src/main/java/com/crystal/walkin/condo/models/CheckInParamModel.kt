package com.crystal.walkin.condo.models

class CheckInParamModel private constructor(val idcard: String?, val name: String, val vehicle_id: String, val temperature: String,
                                            val departmentId: String, val objectiveId: String, val images: String, val gender: String, val address: String, val birthDate: String, val from: String, val objectiveNote: String, val personContact: String) {
    data class Builder(val name: String, val departmentId: String, val objectiveId: String, val images: String) {
        var idcard: String? = null
            private set
        var vehicle_id: String = ""
            private set
        var temperature: String = ""
            private set
        var gender: String = ""
            private set
        var address: String = ""
            private set
        var birthDate: String = ""
            private set
        var from: String = ""
            private set
        var objective_note: String = ""
            private set
        var person_contact: String = ""
            private set

        fun idcard(idcard: String) = apply { this.idcard = idcard }
        fun vehicle_id(vehicle_id: String) = apply { this.vehicle_id = vehicle_id }
        fun temperature(temperature: String) = apply { this.temperature = temperature }
        fun gender(gender: String) = apply { this.gender = gender }
        fun address(address: String) = apply { this.address = address }
        fun birthDate(birthDate: String) = apply { this.birthDate = birthDate }
        fun from(from: String) = apply { this.from = from }
        fun objectiveNote(objectiveNote: String) = apply { this.objective_note = objectiveNote }
        fun personContact(personContact: String) = apply { this.person_contact = personContact }

        fun build() = CheckInParamModel(idcard, name, vehicle_id, temperature, departmentId, objectiveId, images, gender, address, birthDate, from, objective_note, person_contact)
    }
}