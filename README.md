# makeYour Jurney

makeYour Jurney adalah aplikasi AI travel planner yang membantu user membuat rencana perjalanan berdasarkan asal, tujuan, budget, durasi, jumlah orang, dan preferensi perjalanan.

Aplikasi ini fokus untuk membantu user menyusun trip secara cepat tanpa harus membuka banyak platform manual.

---

## Problem yang Diselesaikan

Perencanaan perjalanan sering ribet karena user harus mencari banyak hal secara terpisah:

- Mencari hotel yang sesuai budget.
- Mencari aktivitas wisata di kota tujuan.
- Membandingkan harga, rating, dan review.
- Menghitung estimasi total budget.
- Mengecek jarak dan durasi perjalanan.
- Menyusun itinerary harian.
- Menentukan apakah budget cukup atau tidak.

makeYour Jurney menyelesaikan masalah tersebut dengan menggabungkan data travel, maps, budget calculation, dan AI recommendation dalam satu flow.

---

## Deskripsi Aplikasi

makeYour Jurney adalah aplikasi AI travel assistant untuk membuat rencana perjalanan otomatis.

User cukup memasukkan informasi seperti:

```txt
Aku dari Jakarta mau ke Surabaya.
Budget 3 juta.
Untuk 2 orang.
3 hari 2 malam.
Mau yang hemat tapi nyaman.
````

Sistem akan memproses:

```txt
- Asal perjalanan
- Tujuan perjalanan
- Budget
- Jumlah orang
- Durasi perjalanan
- Preferensi trip
- Data hotel
- Data aktivitas
- Estimasi rute
- Estimasi total biaya
```

Lalu aplikasi menghasilkan:

```txt
- Rekomendasi hotel
- Rekomendasi aktivitas
- Estimasi budget
- Jarak dan durasi perjalanan
- Itinerary harian
- Paket hemat, balanced, dan nyaman
```

---

## Tujuan Aplikasi

Tujuan utama makeYour Jurney adalah membuat proses travel planning menjadi lebih mudah, cepat, dan terarah.

Aplikasi ini tidak difokuskan untuk booking pada MVP awal, tetapi difokuskan sebagai AI travel planner yang membantu user mengambil keputusan.

---

## Core Concept

makeYour Jurney hanya menggunakan 2 sumber utama:

```txt
1. Apify
   Untuk mengambil data travel seperti hotel, aktivitas, harga, rating, dan review.

2. Maps Module
   Untuk menghitung rute, jarak, durasi, dan menampilkan visual map.
```

Semua fitur lain seperti budget calculation, ranking, itinerary, dan rekomendasi dilakukan oleh backend dan AI layer.

---

## External Source

### 1. Apify

Apify digunakan untuk mengambil data travel dari sumber seperti Traveloka.

Data yang digunakan:

```txt
- Nama hotel
- Harga hotel
- Rating hotel
- Review hotel
- Lokasi hotel
- Nama aktivitas
- Harga aktivitas
- Rating aktivitas
- Gambar aktivitas
```

Data dari Apify harus dicache agar tidak terlalu sering melakukan request ulang.

---

### 2. Maps Module

Maps Module digunakan untuk menghitung rute perjalanan.

Data yang digunakan:

```txt
- Origin
- Destination
- Distance
- Duration
- Travel mode
- Route polyline
- Alternative route
```

Untuk MVP gratis, gunakan:

```txt
- Leaflet.js
- OpenStreetMap
- OpenRouteService atau routing provider berbasis OSM
```

Google Maps API tidak digunakan pada MVP agar biaya tetap aman.

---

## Fitur Utama

### 1. AI Travel Planner

User dapat mengetik rencana perjalanan menggunakan bahasa natural.

Contoh:

```txt
Aku mau liburan ke Bandung dari Jakarta, budget 2 juta, 2 hari 1 malam.
```

AI akan memahami kebutuhan user dan mengubahnya menjadi struktur data perjalanan.

---

### 2. Trip Input

Aplikasi mendukung input:

```txt
- Origin
- Destination
- Start date
- End date
- Durasi perjalanan
- Jumlah orang
- Budget
- Trip style
```

Trip style dapat berupa:

```txt
- Hemat
- Balanced
- Nyaman
- Family trip
- Couple trip
- Solo trip
- Healing trip
- Adventure trip
```

---

### 3. Hotel Recommendation

Sistem menampilkan rekomendasi hotel berdasarkan:

```txt
- Harga
- Rating
- Review
- Lokasi
- Kesesuaian dengan budget
- Kesesuaian dengan itinerary
```

Output hotel:

```txt
- Nama hotel
- Harga per malam
- Rating
- Review count
- Lokasi
- Alasan rekomendasi
```

---

### 4. Activity Recommendation

Sistem menampilkan rekomendasi aktivitas di kota tujuan.

Rekomendasi aktivitas mempertimbangkan:

```txt
- Harga
- Rating
- Lokasi
- Durasi aktivitas
- Preferensi user
- Sisa budget
```

Output aktivitas:

```txt
- Nama aktivitas
- Harga per orang
- Rating
- Lokasi
- Estimasi durasi
- Alasan rekomendasi
```

---

### 5. Budget Planner

Sistem menghitung estimasi total biaya perjalanan.

Komponen budget:

```txt
- Hotel
- Aktivitas
- Makan
- Transport lokal
- Transport antar kota
- Buffer tambahan
```

Contoh output:

```txt
Budget user: Rp3.000.000

Estimasi:
- Hotel: Rp900.000
- Aktivitas: Rp600.000
- Makan: Rp600.000
- Transport: Rp500.000
- Buffer: Rp300.000

Total estimasi: Rp2.900.000
Sisa budget: Rp100.000
Status: Within budget
```

---

### 6. Route & Distance Map

Maps Module menampilkan rute dari asal ke tujuan.

Fitur maps:

```txt
- Input asal
- Input tujuan
- Mode perjalanan
- Estimasi jarak
- Estimasi durasi
- Route line di map
- Alternatif rute
```

Contoh:

```txt
From: Jakarta
To: Surabaya
Mode: Motor

Jarak: 791 km
Durasi: 17 jam 51 menit
```

---

### 7. Itinerary Generator

AI menyusun itinerary berdasarkan durasi perjalanan, lokasi, budget, dan aktivitas yang tersedia.

Contoh output:

```txt
Day 1:
- Berangkat dari Jakarta
- Check-in hotel
- Aktivitas ringan
- Makan malam

Day 2:
- Wisata utama
- Kuliner lokal
- Aktivitas sore

Day 3:
- Checkout hotel
- Beli oleh-oleh
- Pulang
```

---

### 8. Trip Package Recommendation

Sistem memberikan 3 jenis paket perjalanan:

```txt
1. Hemat
   Fokus pada harga paling murah namun tetap layak.

2. Balanced
   Kombinasi antara harga, rating, kenyamanan, dan lokasi.

3. Nyaman
   Fokus pada pengalaman terbaik yang masih mendekati budget.
```

Setiap paket berisi:

```txt
- Hotel pilihan
- Aktivitas pilihan
- Estimasi total biaya
- Status budget
- Alasan rekomendasi
```

---

### 9. Smart Budget Status

Sistem memberikan status budget berdasarkan estimasi total biaya.

Status:

```txt
- Under budget
- Within budget
- Almost over budget
- Over budget
```

Rule:

```txt
Jika total estimasi <= 85% dari budget:
Status = Under budget

Jika total estimasi <= 100% dari budget:
Status = Within budget

Jika total estimasi <= 115% dari budget:
Status = Almost over budget

Jika total estimasi > 115% dari budget:
Status = Over budget
```

---

## Scope MVP

Fitur yang masuk MVP:

```txt
- AI chat untuk travel planning
- Input asal dan tujuan
- Input budget
- Input jumlah orang
- Input tanggal atau durasi
- Rekomendasi hotel dari Apify
- Rekomendasi aktivitas dari Apify
- Estimasi budget perjalanan
- Itinerary otomatis
- Maps route sederhana
- Jarak dan durasi perjalanan
- Paket hemat, balanced, dan nyaman
- Cache hasil pencarian
```

---

## Out of Scope MVP

Fitur yang tidak masuk MVP awal:

```txt
- Booking hotel langsung dari aplikasi
- Payment gateway
- Tiket pesawat real-time
- Refund management
- Cancellation management
- Loyalty point
- Promo management
- Admin panel kompleks
- Live chat dengan travel agent
- AI voice assistant
- Offline map
- Integrasi banyak OTA
- Google Maps API berbayar
```

---

## Future Scope

Fitur yang bisa dikembangkan setelah MVP:

```txt
- Booking hotel
- Booking aktivitas
- Flight recommendation
- Multi-city trip planner
- Group trip planner
- Shared itinerary
- PDF export itinerary
- Calendar integration
- Expense tracker
- Real-time price monitoring
- Travel reminder
- AI packing list
- Weather-based recommendation
- Personalized travel history
```

---

## Main Flow

```txt
1. User membuka aplikasi.
2. User memasukkan rencana perjalanan.
3. AI membaca kebutuhan user.
4. Sistem mengecek data yang masih kurang.
5. Sistem mengambil data hotel dan aktivitas dari Apify.
6. Sistem mengambil data jarak dan durasi dari Maps Module.
7. Sistem menghitung estimasi budget.
8. Sistem membuat beberapa opsi paket perjalanan.
9. Sistem menyusun itinerary.
10. User melihat hasil rekomendasi.
```

---

## Data Flow

```txt
User Input
  ↓
AI Intent Parser
  ↓
Travel Data Fetcher
  ├── Apify Module
  └── Maps Module
  ↓
Data Normalizer
  ↓
Budget Engine
  ↓
Recommendation Engine
  ↓
Itinerary Generator
  ↓
User Interface
```

---

## Basic Data Model

### Trip Request

```json
{
  "origin": "Jakarta",
  "destination": "Surabaya",
  "start_date": "2026-06-25",
  "end_date": "2026-06-28",
  "days": 3,
  "nights": 2,
  "people": 2,
  "budget": 3000000,
  "trip_style": "hemat"
}
```

---

### Hotel Item

```json
{
  "name": "Hotel Example",
  "location": "Surabaya",
  "price_per_night": 450000,
  "rating": 8.6,
  "review_count": 1200,
  "source": "Traveloka via Apify"
}
```

---

### Activity Item

```json
{
  "name": "City Tour Surabaya",
  "location": "Surabaya",
  "price_per_person": 150000,
  "rating": 4.7,
  "duration_hours": 3,
  "source": "Traveloka via Apify"
}
```

---

### Route Item

```json
{
  "origin": "Jakarta",
  "destination": "Surabaya",
  "mode": "motor",
  "distance_km": 791,
  "duration_minutes": 1071,
  "duration_label": "17 jam 51 menit"
}
```

---

### Budget Result

```json
{
  "budget": 3000000,
  "hotel_total": 900000,
  "activity_total": 600000,
  "food_total": 600000,
  "transport_total": 500000,
  "buffer": 300000,
  "estimated_total": 2900000,
  "remaining_budget": 100000,
  "status": "within_budget"
}
```

---

## Recommendation Logic

Ranking rekomendasi dihitung berdasarkan:

```txt
- Budget fit
- Rating
- Review count
- Distance efficiency
- Price value
- Trip style match
```

Formula sederhana:

```txt
final_score =
  budget_score * 0.35 +
  rating_score * 0.25 +
  location_score * 0.20 +
  review_score * 0.10 +
  preference_score * 0.10
```

---

## Budget Rule

```txt
- Sistem harus menghitung semua komponen biaya.
- Sistem harus memberi status budget.
- Sistem harus memberi saran jika budget kurang.
- Sistem tidak boleh memaksa rekomendasi yang melebihi budget terlalu jauh.
- Sistem harus tetap memberi opsi hemat jika budget terbatas.
```

---

## Maps Rule

```txt
- Maps digunakan untuk menghitung route dari origin ke destination.
- Maps harus menampilkan jarak dan estimasi durasi.
- Maps boleh menampilkan beberapa alternatif route.
- Maps UI boleh dibuat mirip Google Maps.
- Untuk MVP, gunakan map provider gratis.
- Jangan gunakan Google Maps API pada MVP.
- Cache route berdasarkan origin, destination, dan travel mode.
```

---

## Apify Rule

```txt
- Apify digunakan untuk mengambil data Traveloka.
- Data Apify harus dicache.
- Jangan panggil Apify berulang untuk query yang sama.
- Jika Apify gagal, gunakan fallback cached data.
- Jika tidak ada cached data, tampilkan pesan bahwa data belum tersedia.
```

---

## UI Pages

### 1. Home Page

Berisi:

```txt
- Search destination
- Budget input
- Trip duration input
- CTA start planning
```

---

### 2. AI Chat Page

Berisi:

```txt
- Chat input
- AI response
- Suggested prompt
- Trip summary card
```

---

### 3. Recommendation Page

Berisi:

```txt
- Paket hemat
- Paket balanced
- Paket nyaman
- Hotel recommendation
- Activity recommendation
- Budget breakdown
```

---

### 4. Maps Page

Berisi:

```txt
- Origin input
- Destination input
- Travel mode
- Route map
- Distance
- Duration
- Alternative routes
```

---

### 5. Itinerary Page

Berisi:

```txt
- Day by day itinerary
- Activity time
- Estimated cost
- Route between places
- Notes from AI
```

---

## Tech Stack Recommendation

### Mobile

```txt
- React Native
- Tailwind CSS, Shacdn Ui
- Framer Motion atau GSAP
- Leaflet.js
```

### Backend

```txt
- Spring Boot + Spring Boot Ai
- PostgreSQL
- Redis Cache
- Apify Client
- Maps Routing Client
```

### Moonrepo

```txt
- Spring Boot + Spring Boot Ai
- React Native
- PostgreSQL
- Redis
```

### Docker

```txt
- Spring Boot + Spring Boot Ai
- React Native
- PostgreSQL
- Redis
```

### AI Layer

```txt
- Intent parser
- Budget reasoning
- Recommendation generator
- Itinerary generator
```

---

## Environment Variables

```env
APIFY_TOKEN=
MAPS_API_KEY=
DATABASE_URL=
REDIS_URL=
AI_API_KEY=
```
---

## MVP Success Criteria

MVP dianggap berhasil jika user bisa:

```txt
- Membuat rencana perjalanan lewat chat.
- Melihat estimasi budget.
- Melihat rekomendasi hotel.
- Melihat rekomendasi aktivitas.
- Melihat jarak dan durasi perjalanan.
- Mendapat itinerary otomatis.
- Memilih paket hemat, balanced, atau nyaman.
```

---

## Important Notes

```txt
- makeYour Jurney bukan aplikasi booking pada MVP.
- makeYour Jurney adalah AI travel planner.
- Booking dan pembayaran tidak masuk scope awal.
- Data hotel dan aktivitas berasal dari Apify.
- Data rute dan jarak berasal dari Maps Module.
- Semua rekomendasi harus transparan dan memiliki alasan.
- Sistem harus menjaga penggunaan API agar tetap hemat.
```

