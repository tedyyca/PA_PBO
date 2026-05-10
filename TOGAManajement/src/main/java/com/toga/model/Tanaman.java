package com.toga.model;

import org.apache.commons.lang3.StringUtils;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public abstract class Tanaman {
    private int id;
    protected String nama;
    private String namaLatin;
    private String manfaat;
    private StatusTanaman status;
    private LocalDate tanggalTanam;

    public Tanaman(String nama, String namaLatin, String manfaat, StatusTanaman status, LocalDate tanggalTanam) {
        this.nama = nama;
        this.namaLatin = namaLatin;
        this.manfaat = manfaat;
        this.status = status;
        this.tanggalTanam = tanggalTanam;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNama() { return nama; }
    public boolean setNama(String nama) {
        if (StringUtils.isBlank(nama)) return false;
        this.nama = nama;
        return true;
    }

    public String getNamaLatin() { return namaLatin; }
    public boolean setNamaLatin(String namaLatin) {
        if (StringUtils.isBlank(namaLatin)) return false;
        this.namaLatin = namaLatin;
        return true;
    }

    public String getManfaat() { return manfaat; }
    public boolean setManfaat(String manfaat) {
        if (StringUtils.isBlank(manfaat)) return false;
        this.manfaat = manfaat;
        return true;
    }

    public StatusTanaman getStatus() { return status; }
    public void setStatus(StatusTanaman status) { this.status = status; }

    public LocalDate getTanggalTanam() { return tanggalTanam; }
    public void setTanggalTanam(LocalDate tanggalTanam) { this.tanggalTanam = tanggalTanam; }

    String getInfoSingkat() {
        return nama + " (" + namaLatin + ")";
    }

    public abstract String getJenis();
    public abstract String getPropertiTambahan();
    public abstract int estimasiHariPanen();

    public int estimasiHariPanen(String tujuan) {
        int dasar = estimasiHariPanen();
        if (tujuan.equalsIgnoreCase("bibit")) return dasar + 30;
        else if (tujuan.equalsIgnoreCase("obat")) return dasar + 45;
        return dasar;
    }

    public int sisaHariPanen() {
        if (tanggalTanam == null) return -1;
        int totalHari = estimasiHariPanen();
        LocalDate tanggalPanen = tanggalTanam.plusDays(totalHari);
        long sisa = ChronoUnit.DAYS.between(LocalDate.now(), tanggalPanen);
        return (int) sisa;
    }

    /**
     * Menghitung status otomatis berdasarkan tanggal tanam dan estimasi hari panen.
     * Dipanggil saat menyimpan data tanaman (Tambah/Ubah), KECUALI jika sudah dipanen.
     *
     * Logika:
     *   - Hari ke-0 s.d. hari ke-29              → BIBIT
     *   - Hari ke-30 s.d. (estimasi - 15)        → TUMBUH
     *   - (estimasi - 14) s.d. estimasi ke atas  → SIAP_PANEN
     *
     * Status SUDAH_DIPANEN hanya di-set oleh PanenController, tidak lewat sini.
     */
    public static StatusTanaman hitungStatus(LocalDate tanggalTanam, int estimasiHari) {
        if (tanggalTanam == null) return StatusTanaman.BIBIT;
        long hariSejaktanam = ChronoUnit.DAYS.between(tanggalTanam, LocalDate.now());

        if (hariSejaktanam < 30) {
            return StatusTanaman.BIBIT;
        } else if (hariSejaktanam < estimasiHari - 14) {
            return StatusTanaman.TUMBUH;
        } else {
            return StatusTanaman.SIAP_PANEN;
        }
    }

    public void tampilInfo() {
        System.out.println("Nama       : " + getNama());
        System.out.println("Nama Latin : " + getNamaLatin());
        System.out.println("Manfaat    : " + getManfaat());
        System.out.println("Status     : " + getStatus());
    }
}