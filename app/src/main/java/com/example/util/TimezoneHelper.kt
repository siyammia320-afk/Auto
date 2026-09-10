package com.example.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class CountryProfile(
    val iso: String,
    val flag: String,
    val countryName: String,
    val timeZoneId: String,
    val acceptLanguage: String,
    val firstNames: List<String>,
    val lastNames: List<String>
)

object TimezoneHelper {

    private val defaultProfile = CountryProfile(
        iso = "US",
        flag = "🇺🇸",
        countryName = "USA",
        timeZoneId = "America/New_York",
        acceptLanguage = "en-US,en;q=0.9",
        firstNames = listOf("James", "John", "Robert", "Michael", "William", "David", "Richard", "Joseph", "Thomas", "Charles", "Daniel", "Matthew", "Anthony"),
        lastNames = listOf("Smith", "Johnson", "Williams", "Brown", "Jones", "Miller", "Davis", "Wilson", "Anderson", "Taylor", "Thomas", "Moore")
    )

    private val profilesByDialCode = mapOf(
        "880" to CountryProfile(
            iso = "BD",
            flag = "🇧🇩",
            countryName = "Bangladesh",
            timeZoneId = "Asia/Dhaka",
            acceptLanguage = "bn-BD,bn;q=0.9,en-US;q=0.8,en;q=0.7",
            firstNames = listOf("Tanvir", "Araft", "Siam", "Rahim", "Karim", "Rakib", "Hasan", "Nayeem", "Shakil", "Mahmud", "Foysal", "Sakib", "Tamim", "Riad", "Emon", "Alamin", "Zubair", "Fahim"),
            lastNames = listOf("Bhai", "Khan", "Ahmed", "Chowdhury", "Hossain", "Islam", "Sheikh", "Rahman", "Miah", "Uddin", "Ali", "Hasan", "Sarker")
        ),
        "88" to CountryProfile(
            iso = "BD",
            flag = "🇧🇩",
            countryName = "Bangladesh",
            timeZoneId = "Asia/Dhaka",
            acceptLanguage = "bn-BD,bn;q=0.9,en-US;q=0.8,en;q=0.7",
            firstNames = listOf("Tanvir", "Araft", "Siam", "Rahim", "Karim", "Rakib", "Hasan", "Nayeem", "Shakil", "Mahmud", "Foysal", "Sakib", "Tamim", "Riad", "Emon", "Alamin", "Zubair", "Fahim"),
            lastNames = listOf("Bhai", "Khan", "Ahmed", "Chowdhury", "Hossain", "Islam", "Sheikh", "Rahman", "Miah", "Uddin", "Ali", "Hasan", "Sarker")
        ),
        "225" to CountryProfile(
            iso = "CI",
            flag = "🇨🇮",
            countryName = "Ivory Coast",
            timeZoneId = "Africa/Abidjan",
            acceptLanguage = "fr-CI,fr-FR;q=0.9,fr;q=0.8,en-US;q=0.7,en;q=0.6",
            firstNames = listOf("Kouame", "Koffi", "Konan", "Jean", "Yao", "Mamadou", "Ibrahim", "Abdoulaye", "Bakary", "Seydou", "Adama", "Oumar", "Amadou", "Stephane", "Patrick"),
            lastNames = listOf("Traore", "Kouassi", "Diallo", "Bamba", "Ouattara", "Coulibaly", "Bakayoko", "Toure", "Kone", "Cisse", "Diarra", "Fofana")
        ),
        "1" to CountryProfile(
            iso = "US",
            flag = "🇺🇸",
            countryName = "USA",
            timeZoneId = "America/New_York",
            acceptLanguage = "en-US,en;q=0.9",
            firstNames = listOf("James", "John", "Robert", "Michael", "William", "David", "Richard", "Joseph", "Thomas", "Charles", "Daniel", "Matthew", "Anthony"),
            lastNames = listOf("Smith", "Johnson", "Williams", "Brown", "Jones", "Miller", "Davis", "Wilson", "Anderson", "Taylor", "Thomas", "Moore")
        ),
        "44" to CountryProfile(
            iso = "GB",
            flag = "🇬🇧",
            countryName = "United Kingdom",
            timeZoneId = "Europe/London",
            acceptLanguage = "en-GB,en-US;q=0.9,en;q=0.8",
            firstNames = listOf("Oliver", "George", "Arthur", "Noah", "Leo", "Oscar", "Harry", "Jack", "Henry", "Charlie", "Freddie"),
            lastNames = listOf("Smith", "Jones", "Taylor", "Brown", "Williams", "Wilson", "Johnson", "Davies", "Robinson", "Wright")
        ),
        "91" to CountryProfile(
            iso = "IN",
            flag = "🇮🇳",
            countryName = "India",
            timeZoneId = "Asia/Kolkata",
            acceptLanguage = "en-IN,en-GB;q=0.9,hi-IN;q=0.8,hi;q=0.7",
            firstNames = listOf("Rahul", "Amit", "Rohit", "Vikram", "Ajay", "Vijay", "Sanjay", "Rajesh", "Deepak", "Sunil", "Anil", "Manoj"),
            lastNames = listOf("Sharma", "Verma", "Gupta", "Patel", "Singh", "Kumar", "Mishra", "Yadav", "Joshi", "Das")
        ),
        "92" to CountryProfile(
            iso = "PK",
            flag = "🇵🇰",
            countryName = "Pakistan",
            timeZoneId = "Asia/Karachi",
            acceptLanguage = "ur-PK,ur;q=0.9,en-US;q=0.8,en;q=0.7",
            firstNames = listOf("Muhammad", "Ali", "Ahmed", "Usman", "Bilal", "Hamza", "Hassan", "Hussain", "Zain", "Omer", "Farhan"),
            lastNames = listOf("Khan", "Malik", "Shah", "Chaudhry", "Butt", "Bhatti", "Qureshi", "Abbasi", "Mirza", "Sheikh")
        ),
        "234" to CountryProfile(
            iso = "NG",
            flag = "🇳🇬",
            countryName = "Nigeria",
            timeZoneId = "Africa/Lagos",
            acceptLanguage = "en-NG,en-GB;q=0.9,en;q=0.8",
            firstNames = listOf("Chinedu", "Emeka", "Oluwaseun", "Adebayo", "Ibrahim", "Musa", "Chukwuma", "Femi", "Tunde", "Babatunde"),
            lastNames = listOf("Okafor", "Adeyemi", "Balogun", "Eze", "Nwosu", "Okonkwo", "Bello", "Abubakar", "Danjuma", "Lawal")
        ),
        "33" to CountryProfile(
            iso = "FR",
            flag = "🇫🇷",
            countryName = "France",
            timeZoneId = "Europe/Paris",
            acceptLanguage = "fr-FR,fr;q=0.9,en-US;q=0.8,en;q=0.7",
            firstNames = listOf("Gabriel", "Leo", "Raphael", "Louis", "Lucas", "Arthur", "Hugo", "Jules", "Noah", "Paul", "Pierre"),
            lastNames = listOf("Martin", "Bernard", "Thomas", "Petit", "Robert", "Richard", "Durand", "Dubois", "Moreau", "Laurent")
        ),
        "49" to CountryProfile(
            iso = "DE",
            flag = "🇩🇪",
            countryName = "Germany",
            timeZoneId = "Europe/Berlin",
            acceptLanguage = "de-DE,de;q=0.9,en-US;q=0.8,en;q=0.7",
            firstNames = listOf("Lukas", "Leon", "Finn", "Elias", "Jonas", "Ben", "Noah", "Paul", "Felix", "Maximilian"),
            lastNames = listOf("Müller", "Schmidt", "Schneider", "Fischer", "Weber", "Meyer", "Wagner", "Becker", "Schulz")
        ),
        "93" to CountryProfile(
            iso = "AF",
            flag = "🇦🇫",
            countryName = "Afghanistan",
            timeZoneId = "Asia/Kabul",
            acceptLanguage = "fa-AF,ps;q=0.9,en-US;q=0.8,en;q=0.7",
            firstNames = listOf("Ahmad", "Mohammad", "Bilal", "Omid", "Sardar", "Farhad", "Zia", "Nawid"),
            lastNames = listOf("Popal", "Wardak", "Shinwari", "Stanikzai", "Karimi", "Mohammadi", "Ahmadi")
        ),
        "212" to CountryProfile(
            iso = "MA",
            flag = "🇲🇦",
            countryName = "Morocco",
            timeZoneId = "Africa/Casablanca",
            acceptLanguage = "ar-MA,fr-MA;q=0.9,ar;q=0.8,fr;q=0.7,en;q=0.6",
            firstNames = listOf("Youssef", "Amine", "Mehdi", "Hamza", "Anas", "Omar", "Ayoub", "Walid", "Reda", "Taha"),
            lastNames = listOf("Alaoui", "Idrissi", "Benjelloun", "Berrada", "El Amrani", "Chraibi", "El Fassi", "Mansouri")
        ),
        "213" to CountryProfile(
            iso = "DZ",
            flag = "🇩🇿",
            countryName = "Algeria",
            timeZoneId = "Africa/Algiers",
            acceptLanguage = "ar-DZ,fr-DZ;q=0.9,ar;q=0.8,fr;q=0.7,en;q=0.6",
            firstNames = listOf("Mohamed", "Islam", "Abderrahmane", "Ayoub", "Khaled", "Sofiane", "Riyad", "Youcef"),
            lastNames = listOf("Saadi", "Benali", "Bouzid", "Khelifi", "Mebarki", "Mansouri", "Brahimi", "Haddad")
        ),
        "971" to CountryProfile(
            iso = "AE",
            flag = "🇦🇪",
            countryName = "UAE",
            timeZoneId = "Asia/Dubai",
            acceptLanguage = "ar-AE,ar;q=0.9,en-US;q=0.8,en;q=0.7",
            firstNames = listOf("Abdullah", "Mohammed", "Ahmed", "Sultan", "Rashid", "Khalid", "Mansoor", "Fahad"),
            lastNames = listOf("Al-Maktoum", "Al-Nuaimi", "Al-Marzooqi", "Al-Zaabi", "Al-Qasimi", "Al-Suwaidi")
        ),
        "966" to CountryProfile(
            iso = "SA",
            flag = "🇸🇦",
            countryName = "Saudi Arabia",
            timeZoneId = "Asia/Riyadh",
            acceptLanguage = "ar-SA,ar;q=0.9,en-US;q=0.8,en;q=0.7",
            firstNames = listOf("Saud", "Fahad", "Abdullah", "Mohammed", "Khalid", "Abdulaziz", "Bandar", "Turki"),
            lastNames = listOf("Al-Ghamdi", "Al-Harbi", "Al-Shehri", "Al-Qahtani", "Al-Otaibi", "Al-Dossari")
        ),
        "62" to CountryProfile(
            iso = "ID",
            flag = "🇮🇩",
            countryName = "Indonesia",
            timeZoneId = "Asia/Jakarta",
            acceptLanguage = "id-ID,id;q=0.9,en-US;q=0.8,en;q=0.7",
            firstNames = listOf("Budi", "Agus", "Bambang", "Eko", "Dwi", "Hadi", "Rudi", "Joko", "Indra", "Doni"),
            lastNames = listOf("Santoso", "Wijaya", "Kusuma", "Pratama", "Saputra", "Setiawan", "Hidayat", "Wibowo")
        ),
        "60" to CountryProfile(
            iso = "MY",
            flag = "🇲🇾",
            countryName = "Malaysia",
            timeZoneId = "Asia/Kuala_Lumpur",
            acceptLanguage = "ms-MY,ms;q=0.9,en-US;q=0.8,en;q=0.7",
            firstNames = listOf("Muhammad", "Ahmad", "Adam", "Amir", "Danial", "Farhan", "Haziq", "Irfan"),
            lastNames = listOf("Abdullah", "Ismail", "Ibrahim", "Othman", "Yusof", "Razak", "Kassim", "Zainal")
        ),
        "63" to CountryProfile(
            iso = "PH",
            flag = "🇵🇭",
            countryName = "Philippines",
            timeZoneId = "Asia/Manila",
            acceptLanguage = "en-PH,tl-PH;q=0.9,tl;q=0.8,en;q=0.7",
            firstNames = listOf("John", "Mark", "Angelo", "Joshua", "Christian", "Daniel", "Michael", "Gabriel"),
            lastNames = listOf("Santos", "Reyes", "Cruz", "Bautista", "Ocampo", "Garcia", "Mendoza", "Torres")
        ),
        "84" to CountryProfile(
            iso = "VN",
            flag = "🇻🇳",
            countryName = "Vietnam",
            timeZoneId = "Asia/Ho_Chi_Minh",
            acceptLanguage = "vi-VN,vi;q=0.9,en-US;q=0.8,en;q=0.7",
            firstNames = listOf("Duc", "Huy", "Nam", "Phong", "Quan", "Sang", "Thanh", "Tuan", "Vinh", "Hai"),
            lastNames = listOf("Nguyen", "Tran", "Le", "Pham", "Hoang", "Huynh", "Phan", "Vu", "Dang", "Bui")
        ),
        "66" to CountryProfile(
            iso = "TH",
            flag = "🇹🇭",
            countryName = "Thailand",
            timeZoneId = "Asia/Bangkok",
            acceptLanguage = "th-TH,th;q=0.9,en-US;q=0.8,en;q=0.7",
            firstNames = listOf("Somchai", "Somsak", "Arthit", "Kittisak", "Narong", "Pornchai", "Wichai", "Chaiwat"),
            lastNames = listOf("Suksom", "Wongsuwan", "Rattana", "Saetang", "Chaiyaphum", "Phromma", "Boonma")
        ),
        "95" to CountryProfile(
            iso = "MM",
            flag = "🇲🇲",
            countryName = "Myanmar",
            timeZoneId = "Asia/Yangon",
            acceptLanguage = "my-MM,my;q=0.9,en-US;q=0.8,en;q=0.7",
            firstNames = listOf("Aung", "Min", "Kyaw", "Zaw", "Ko", "Tun", "Soe", "Win", "Myo", "Naing"),
            lastNames = listOf("Lwin", "Oo", "Thu", "San", "Lin", "Hlaing", "Zin", "Shein", "Aung", "Htun")
        ),
        "855" to CountryProfile(
            iso = "KH",
            flag = "🇰🇭",
            countryName = "Cambodia",
            timeZoneId = "Asia/Phnom_Penh",
            acceptLanguage = "km-KH,km;q=0.9,en-US;q=0.8,en;q=0.7",
            firstNames = listOf("Sokha", "Chann", "Piseth", "Vireak", "Dara", "Chenda", "Bora", "Rith"),
            lastNames = listOf("Heng", "Chan", "Seng", "Kim", "Meng", "Chea", "Keo", "Sam")
        ),
        "254" to CountryProfile(
            iso = "KE",
            flag = "🇰🇪",
            countryName = "Kenya",
            timeZoneId = "Africa/Nairobi",
            acceptLanguage = "en-KE,sw-KE;q=0.9,en;q=0.8",
            firstNames = listOf("Brian", "Kevin", "Dennis", "Victor", "John", "Peter", "Collins", "Evans"),
            lastNames = listOf("Mwangi", "Kamau", "Otieno", "Ochieng", "Kipchumba", "Kipkorir", "Maina")
        ),
        "27" to CountryProfile(
            iso = "ZA",
            flag = "🇿🇦",
            countryName = "South Africa",
            timeZoneId = "Africa/Johannesburg",
            acceptLanguage = "en-ZA,en-GB;q=0.9,en;q=0.8",
            firstNames = listOf("Sipho", "Thabo", "Bongani", "Kagiso", "Lethabo", "Bandile", "Junior", "Lungelo"),
            lastNames = listOf("Dlamini", "Nkosi", "Ndlovu", "Khumalo", "Sithole", "Zulu", "Mthembu", "Cele")
        ),
        "233" to CountryProfile(
            iso = "GH",
            flag = "🇬🇭",
            countryName = "Ghana",
            timeZoneId = "Africa/Accra",
            acceptLanguage = "en-GH,en-GB;q=0.9,en;q=0.8",
            firstNames = listOf("Kwame", "Kofi", "Kwaku", "Yaw", "Kwadwo", "Emmanuel", "Samuel", "Joseph"),
            lastNames = listOf("Mensah", "Osei", "Appiah", "Boateng", "Asante", "Agyemang", "Amoah", "Owusu")
        ),
        "20" to CountryProfile(
            iso = "EG",
            flag = "🇪🇬",
            countryName = "Egypt",
            timeZoneId = "Africa/Cairo",
            acceptLanguage = "ar-EG,ar;q=0.9,en-US;q=0.8,en;q=0.7",
            firstNames = listOf("Ahmed", "Mohamed", "Mahmoud", "Mostafa", "Youssef", "Ali", "Hassan", "Ibrahim"),
            lastNames = listOf("El-Sayed", "Hassan", "Ali", "Ibrahim", "Abdel-Rahman", "Khalil", "Salem", "Nasser")
        ),
        "977" to CountryProfile(
            iso = "NP",
            flag = "🇳🇵",
            countryName = "Nepal",
            timeZoneId = "Asia/Kathmandu",
            acceptLanguage = "ne-NP,ne;q=0.9,en-US;q=0.8,en;q=0.7",
            firstNames = listOf("Bikash", "Ramesh", "Suresh", "Santosh", "Ashok", "Dipendra", "Prakash", "Binod"),
            lastNames = listOf("Sharma", "Shrestha", "Adhikari", "Thapa", "Karki", "Tamang", "Magar", "Gurung")
        ),
        "94" to CountryProfile(
            iso = "LK",
            flag = "🇱🇰",
            countryName = "Sri Lanka",
            timeZoneId = "Asia/Colombo",
            acceptLanguage = "si-LK,ta;q=0.9,en-US;q=0.8,en;q=0.7",
            firstNames = listOf("Kasun", "Nuwan", "Dinesh", "Chaminda", "Roshan", "Sanjeewa", "Pradeep", "Asanka"),
            lastNames = listOf("Perera", "Fernando", "Silva", "Bandara", "Jayasinghe", "Dissanayake", "Wickramasinghe")
        ),
        "90" to CountryProfile(
            iso = "TR",
            flag = "🇹🇷",
            countryName = "Turkey",
            timeZoneId = "Europe/Istanbul",
            acceptLanguage = "tr-TR,tr;q=0.9,en-US;q=0.8,en;q=0.7",
            firstNames = listOf("Yusuf", "Mustafa", "Mehmet", "Ahmet", "Omer", "Ali", "Murat", "Emre", "Burak"),
            lastNames = listOf("Yilmaz", "Kaya", "Demir", "Celik", "Sahin", "Yildiz", "Yildirim", "Ozturk", "Aydin")
        ),
        "55" to CountryProfile(
            iso = "BR",
            flag = "🇧🇷",
            countryName = "Brazil",
            timeZoneId = "America/Sao_Paulo",
            acceptLanguage = "pt-BR,pt;q=0.9,en-US;q=0.8,en;q=0.7",
            firstNames = listOf("Lucas", "Gabriel", "Matheus", "Felipe", "Gustavo", "Guilherme", "Rafael", "Thiago"),
            lastNames = listOf("Silva", "Santos", "Oliveira", "Souza", "Rodrigues", "Ferreira", "Alves", "Pereira")
        ),
        "52" to CountryProfile(
            iso = "MX",
            flag = "🇲🇽",
            countryName = "Mexico",
            timeZoneId = "America/Mexico_City",
            acceptLanguage = "es-MX,es;q=0.9,en-US;q=0.8,en;q=0.7",
            firstNames = listOf("Santiago", "Mateo", "Sebastian", "Leonardo", "Matias", "Emiliano", "Diego", "Daniel"),
            lastNames = listOf("Hernandez", "Garcia", "Martinez", "Lopez", "Gonzalez", "Perez", "Rodriguez", "Sanchez")
        ),
        "57" to CountryProfile(
            iso = "CO",
            flag = "🇨🇴",
            countryName = "Colombia",
            timeZoneId = "America/Bogota",
            acceptLanguage = "es-CO,es;q=0.9,en-US;q=0.8,en;q=0.7",
            firstNames = listOf("Juan", "David", "Carlos", "Andres", "Alejandro", "Daniel", "Mateo", "Nicolas"),
            lastNames = listOf("Rodriguez", "Gomez", "Gonzalez", "Martinez", "Garcia", "Lopez", "Hernandez", "Sanchez")
        ),
        "7" to CountryProfile(
            iso = "RU",
            flag = "🇷🇺",
            countryName = "Russia",
            timeZoneId = "Europe/Moscow",
            acceptLanguage = "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7",
            firstNames = listOf("Aleksandr", "Dmitry", "Maksim", "Sergey", "Andrey", "Aleksey", "Artyom", "Ilya"),
            lastNames = listOf("Ivanov", "Smirnov", "Kuznetsov", "Popov", "Vasiliev", "Petrov", "Sokolov", "Mikhailov")
        ),
        "34" to CountryProfile(
            iso = "ES",
            flag = "🇪🇸",
            countryName = "Spain",
            timeZoneId = "Europe/Madrid",
            acceptLanguage = "es-ES,es;q=0.9,en-US;q=0.8,en;q=0.7",
            firstNames = listOf("Hugo", "Martin", "Lucas", "Mateo", "Leo", "Daniel", "Alejandro", "Pablo", "Manuel"),
            lastNames = listOf("Garcia", "Rodriguez", "Gonzalez", "Fernandez", "Lopez", "Martinez", "Sanchez", "Perez")
        ),
        "39" to CountryProfile(
            iso = "IT",
            flag = "🇮🇹",
            countryName = "Italy",
            timeZoneId = "Europe/Rome",
            acceptLanguage = "it-IT,it;q=0.9,en-US;q=0.8,en;q=0.7",
            firstNames = listOf("Leonardo", "Francesco", "Alessandro", "Lorenzo", "Mattia", "Andrea", "Gabriele", "Riccardo"),
            lastNames = listOf("Rossi", "Russo", "Ferrari", "Esposito", "Bianchi", "Romano", "Colombo", "Ricci")
        ),
        "31" to CountryProfile(
            iso = "NL",
            flag = "🇳🇱",
            countryName = "Netherlands",
            timeZoneId = "Europe/Amsterdam",
            acceptLanguage = "nl-NL,nl;q=0.9,en-US;q=0.8,en;q=0.7",
            firstNames = listOf("Noah", "Sem", "Lucas", "Liam", "Levi", "Finn", "Milan", "Daan", "Bram", "Luuk"),
            lastNames = listOf("De Jong", "Jansen", "De Vries", "Van de Berg", "Van Dijk", "Bakker", "Janssen", "Visser")
        ),
        "48" to CountryProfile(
            iso = "PL",
            flag = "🇵🇱",
            countryName = "Poland",
            timeZoneId = "Europe/Warsaw",
            acceptLanguage = "pl-PL,pl;q=0.9,en-US;q=0.8,en;q=0.7",
            firstNames = listOf("Antoni", "Jan", "Aleksander", "Franciszek", "Jakub", "Szymon", "Mikolaj", "Filip"),
            lastNames = listOf("Nowak", "Kowalski", "Wisniewski", "Wojcik", "Kowalczyk", "Kaminski", "Lewandowski")
        ),
        "380" to CountryProfile(
            iso = "UA",
            flag = "🇺🇦",
            countryName = "Ukraine",
            timeZoneId = "Europe/Kyiv",
            acceptLanguage = "uk-UA,uk;q=0.9,en-US;q=0.8,en;q=0.7",
            firstNames = listOf("Artem", "Oleksandr", "Maksym", "Dmytro", "Matvii", "Nazar", "Bohdan", "Vladyslav"),
            lastNames = listOf("Melnyk", "Shevchenko", "Boyko", "Kovalenko", "Bondarenko", "Tkachenko", "Kravchenko")
        )
    )

    fun cleanDigits(number: String): String {
        return number.replace(Regex("\\D"), "")
    }

    fun getProfile(phoneNumberOrRange: String): CountryProfile {
        val digits = cleanDigits(phoneNumberOrRange)
        if (digits.isEmpty()) return defaultProfile

        // Bangladesh special case: local format "01xxxxxxxxx"
        if (digits.startsWith("01") && digits.length == 11) {
            return profilesByDialCode["880"] ?: defaultProfile
        }
        if (digits.startsWith("88")) {
            return profilesByDialCode["880"] ?: defaultProfile
        }

        // Try matching dial prefixes from 4 digits down to 1 digit
        for (len in 4 downTo 1) {
            if (digits.length >= len) {
                val prefix = digits.substring(0, len)
                profilesByDialCode[prefix]?.let { return it }
            }
        }

        return defaultProfile
    }

    fun detectCountryIso(phoneNumber: String): String {
        return getProfile(phoneNumber).iso
    }

    fun getCountryInfo(rangeCode: String): Pair<String, String> {
        val profile = getProfile(rangeCode)
        return Pair(profile.flag, profile.countryName)
    }

    fun getCountryTime(phoneNumber: String): Pair<String, String> {
        val profile = getProfile(phoneNumber)
        return try {
            val tz = TimeZone.getTimeZone(profile.timeZoneId)
            val sdf = SimpleDateFormat("hh:mm:ss a", Locale.US).apply {
                timeZone = tz
            }
            Pair(sdf.format(Date()), profile.timeZoneId)
        } catch (e: Exception) {
            val sdf = SimpleDateFormat("hh:mm:ss a", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            Pair(sdf.format(Date()), "UTC")
        }
    }

    fun extractOtpFromText(text: String): String {
        val cleanText = text.replace("-", "").replace(" ", "")
        val patterns = listOf(
            Regex("\\b(\\d{8})\\b"),
            Regex("\\b(\\d{7})\\b"),
            Regex("\\b(\\d{6})\\b"),
            Regex("\\b(\\d{5})\\b"),
            Regex("\\b(\\d{4})\\b"),
            Regex("\\b(\\d{3})\\b"),
            Regex("code[:\\s]*(\\d+)", RegexOption.IGNORE_CASE),
            Regex("OTP[:\\s]*(\\d+)", RegexOption.IGNORE_CASE),
            Regex("(\\d+)")
        )
        for (p in patterns) {
            val match = p.find(cleanText)
            if (match != null) {
                val grp = match.groupValues.getOrNull(1) ?: match.value
                if (grp.length >= 3) {
                    return grp
                }
            }
        }
        return "N/A"
    }
}
