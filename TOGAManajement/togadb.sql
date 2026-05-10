CREATE DATABASE IF NOT EXISTS toga_db;
USE toga_db;

CREATE TABLE IF NOT EXISTS tanaman (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nama VARCHAR(100) NOT NULL,
    nama_latin VARCHAR(100) NOT NULL,
    manfaat TEXT NOT NULL,
    jenis VARCHAR(20) NOT NULL,
    properti_tambahan VARCHAR(100) NOT NULL,
    status VARCHAR(20) DEFAULT 'BIBIT',
    tanggal_tanam DATE NOT NULL
);

CREATE TABLE IF NOT EXISTS pengguna (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nama VARCHAR(100) NOT NULL,
    alamat TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS jadwal_perawatan (
    id INT AUTO_INCREMENT PRIMARY KEY,
    tanaman_id INT NOT NULL,
    jenis_perawatan VARCHAR(50) NOT NULL,
    tanggal DATE NOT NULL,
    sudah_dilakukan BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (tanaman_id) REFERENCES tanaman(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS catatan_perawatan (
    id INT AUTO_INCREMENT PRIMARY KEY,
    tanaman_id INT NOT NULL,
    pengguna_id INT NOT NULL,
    keterangan TEXT NOT NULL,
    tanggal DATE NOT NULL,
    FOREIGN KEY (tanaman_id) REFERENCES tanaman(id) ON DELETE CASCADE,
    FOREIGN KEY (pengguna_id) REFERENCES pengguna(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS catatan_panen (
    id INT AUTO_INCREMENT PRIMARY KEY,
    tanaman_id INT NOT NULL,
    pengguna_id INT NOT NULL,
    keterangan TEXT NOT NULL,
    tanggal_panen DATE NOT NULL,
    hasil_panen VARCHAR(100) NOT NULL,
    FOREIGN KEY (tanaman_id) REFERENCES tanaman(id) ON DELETE CASCADE,
    FOREIGN KEY (pengguna_id) REFERENCES pengguna(id) ON DELETE CASCADE
);

SET SQL_SAFE_UPDATES = 0;

UPDATE tanaman
SET status = CASE
    WHEN DATEDIFF(CURDATE(), tanggal_tanam) < 30
        THEN 'BIBIT'
    WHEN jenis = 'Tanaman Daun'
         AND DATEDIFF(CURDATE(), tanggal_tanam) >= 30
         AND DATEDIFF(CURDATE(), tanggal_tanam) < (60 - 14)
        THEN 'TUMBUH'
    WHEN jenis = 'Tanaman Daun'
         AND DATEDIFF(CURDATE(), tanggal_tanam) >= (60 - 14)
        THEN 'SIAP_PANEN'
    WHEN jenis = 'Tanaman Buah'
         AND DATEDIFF(CURDATE(), tanggal_tanam) >= 30
         AND DATEDIFF(CURDATE(), tanggal_tanam) < (180 - 14)
        THEN 'TUMBUH'
    WHEN jenis = 'Tanaman Buah'
         AND DATEDIFF(CURDATE(), tanggal_tanam) >= (180 - 14)
        THEN 'SIAP_PANEN'
    WHEN jenis = 'Tanaman Rempah'
         AND DATEDIFF(CURDATE(), tanggal_tanam) >= 30
         AND DATEDIFF(CURDATE(), tanggal_tanam) < (240 - 14)
        THEN 'TUMBUH'
    WHEN jenis = 'Tanaman Rempah'
         AND DATEDIFF(CURDATE(), tanggal_tanam) >= (240 - 14)
        THEN 'SIAP_PANEN'
    ELSE 'BIBIT'
END
WHERE status != 'SUDAH_DIPANEN';

SELECT nama, jenis, tanggal_tanam,
       DATEDIFF(CURDATE(), tanggal_tanam) AS hari_sejak_tanam,
       status
FROM tanaman
ORDER BY jenis, tanggal_tanam;
