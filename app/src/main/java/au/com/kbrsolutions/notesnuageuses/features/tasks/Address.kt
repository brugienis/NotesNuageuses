package au.com.kbrsolutions.notesnuageuses.features.tasks

data class Address(
        val addressLine1: String,
        val addressLine2: String = "",
        val city: String,
        val state: String = "",
        val province: String = "",
        val zip: String,
        val country: String = "USA") {

    class Builder {
        private lateinit var addressLine1: String
        private var addressLine2: String = ""
        private var city: String = ""
        private var state: String = ""
        private var province: String = ""
        private var zip: String = ""
        private var country: String = "USA"

        fun addressLine1(addressLine1: String) = apply { this.addressLine1 = addressLine1 }
        fun addressLine2(addressLine2: String) = apply { this.addressLine2 = addressLine2 }
        fun city(city: String) = apply { this.city = city }
        fun state(state: String) = apply { this.state = state }
        fun province(province: String) = apply { this.province = province }
        fun zip(zip: String) = apply { this.zip = zip }
        fun country(country: String) = apply { this.country = country }

        fun build() = Address(
                addressLine1,
                addressLine2,
                city,
                state,
                province,
                zip,
                country
        )
    }
}