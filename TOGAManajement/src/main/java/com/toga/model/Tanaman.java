package com.toga.model;

import org.apache.commons.lang3.StringUtils;
import java.time.LocalDate;
import java.util.ArrayList;

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
        if (StringUtils.isBlank(nama)) {
            return false;
        }
        this.nama = nama;
        return true;
    }

    public String getNamaLatin() { return namaLatin; }
    public boolean setNamaLatin(String namaLatin) {
        if (StringUtils.isBlank(namaLatin)) {
            return false;
        }
        this.namaLatin = namaLatin;
        return true;
    }

    public String getManfaat() { return manfaat; }
    public boolean setManfaat(String manfaat) {
        if (StringUtils.isBlank(manfaat)) {
            return false;
        }
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
        long sisa = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), tanggalPanen);
        return (int) sisa;
    }

    public static int hitungTanaman(ArrayList<Tanaman> daftar) {
        return daftar.size();
    }

    public static int hitungTanaman(ArrayList<Tanaman> daftar, String jenis) {
        int count = 0;
        for (Tanaman t : daftar) {
            if (t.getJenis().equalsIgnoreCase(jenis)) count++;
        }
        return count;
    }

    public void tampilInfo() {
        System.out.println("Nama       : " + getNama());
        System.out.println("Nama Latin : " + getNamaLatin());
        System.out.println("Manfaat    : " + getManfaat());
        System.out.println("Status     : " + getStatus());
    }
}
