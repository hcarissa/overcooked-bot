# Strategi Algoritma: Algoritma Greedy
**Tugas Besar 1 IF2211 Strategi Algoritma** 

**Pemanfaatan Algoritma Greedy dalam Aplikasi Permainan *Overdrive***

## Spesifikasi dan Permasalahan
```
* Memanfaatkan Algoritma Greedy dalam Permainan *Overdrive Entelect Challenge 2020* untuk memenangkan permainan.

* Algoritma dibuat dengan melanjutkan dan melengkapi implementasi Bot permainan dalam Bahasa Java dengan menggunakan Intellij IDEA.

* Overdrive adalah permainan balapan bot player dengan algoritma masing - masing player. Permainan balapan ini selesai ketika salah satu player selesai di garis finish dan jika kedua bot finish bersama score terbesar akan menjadi penentu kemenangan.

* Peta akan memiliki 1500 blocks dan 4 lane. Player akan memulai dengan speed 5 yang berarti bisa maju 5 block ke depan. Ada banyak powerups dan obstacle di peta permainan yang dapat digunakan.
```

## Implementasi Strategi-Strategi Greedy
```
Program utama akan berjalan dan mereturn command dengan menghitung optimum sesuai prioritas dalam run(). Di dalam itu, terdapat beberapa strategi greedy.

1. Strategy FindClearLane, yaitu strategi yang digunakan untuk navigasi mencari lane yang paling kosong dan paling sedikit obstacle.

2. Strategy FindPowerUps, yaitu strategi yang digunakan untuk navigasi mencari letak powerups terdekat yang bisa dicapai oleh bot.
```

## Requirement
* Java (Minimal Java 8), dapat diunduh di https://www.oracle.com/java/technologies/javase/javase-jdk8-downloads.html.
* Intellij IDEA, dapat diunduh di https://www.jetbrains.com/idea/.
* NodeJS, dapat diunduh di https://nodejs.org/en/download/.
* Official Overdrive 2020 Entelect Challenge Entelect, dapat diunduh di https://github.com/EntelectChallenge/2020-Overdrive
* (Optional) Overdrive 2020 Entelect Challenge Visualizer, dapat diunduh di https://github.com/Affuta/overdrive-round-runner

## Setup
* Pastikan semua requirement di atas sudah terinstall pada perangkat keras yang akan digunakan.
* Perhatikan bahwa Game Engine yang diunduh dari link di atas merupakan `starter pack` yang digunakan oleh pemain untuk memulai membuat bot.
* Struktur folder starter pack tersebut dapat dilihat di https://github.com/EntelectChallenge/2020-Overdrive.
* Lakukan pengimplementasian kode program menggunakan Intellij IDEA (dapat dilakukan dengan menjalankan file `pom.xml`).
* Setelah diimplementasikan, lakukan instalasi program dengan menggunakan `Maven Toolbox` pada bagian `Lifecycle` yang terletak di bagian kanan Intellij IDEA.
* Instalasi ini menghasilkan sebuah folder bernama target yang akan berisi sebuah file bernama `java-starter-bot-jar-with-dependencies.jar`.
* Pindahkan file ini ke dalam folder `starter-pack\java\target`.
* Pastikan konfigurasi program yang ada di `game-runner-config.json` sudah benar, meliputi direktori bot yang digunakan.
* Jika menggunakan file yang terdapat dalam repo ini, maka yang perlu dilakukan adalah menggantikan file jar yang terdapat pada folder target dengan file yang ada di folder `bin`.
* Selain itu, jangan lupa untuk tetap mengubah source code program dengan mengganti folder `starter-bots` dengan folder `src` yang ada di repositori ini.

## Run Permainan
```
1. Program dijalankan dengan `run.bat` di Windows atau `make run` di Linux yang ada di starter-pack.

2. Akan ditampilkan permainan dalam Terminal yang digunakan dalam perangkat lunak. Arsip dari permainan ini akan dimasukkan ke dalam folder `match-logs`.
```

## Sources
```
Official Entelect Challenge 2020 Overdrive Starter Pack: https://github.com/EntelectChallenge/2020-Overdrive.

Entelect Forum: https://forum.entelect.co.za/.
```

## Author
```
Kristo Abdi Wiguna / 13520058
Yakobus Iryanto Prasethio / 13520104
Hilda Carissa Widelia / 13520164
```