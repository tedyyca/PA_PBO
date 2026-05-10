package com.toga.model;

import org.apache.commons.lang3.StringUtils;
import java.time.LocalDate;

public class TanamanBuah extends Tanaman implements ITanamanObat {
    private String musimBerbuah;

    public TanamanBuah(String nama, String namaLatin, String manfaat, String musimBerbuah, StatusTanaman status, LocalDate tanggalTanam) {
        super(nama, namaLatin, manfaat, status, tanggalTanam);
        this.musimBerbuah = musimBerbuah;
    }

    public String getMusimBerbuah() { return musimBerbuah; }
    public boolean setMusimBerbuah(String musimBerbuah) {
        if (StringUtils.isBlank(musimBerbuah)) return false;
        this.musimBerbuah = musimBerbuah;
        return true;
    }

    @Override public String getJenis() { return "Tanaman Buah"; }
    @Override public String getPropertiTambahan() { return musimBerbuah; }
    @Override String getInfoSingkat() { return nama + " (" + getNamaLatin() + ") - Buah"; }
    @Override public int estimasiHariPanen() { return 180; }

    @Override
    public void tampilInfo() {
        super.tampilInfo();
        System.out.println("Jenis         : Tanaman Buah");
        System.out.println("Musim Berbuah : " + musimBerbuah);
    }

    @Override
    public String getDeskripsiObat() {
        return "Buah " + nama + " yang dipanen saat musim " + musimBerbuah + " digunakan " + getManfaat();
    }

    @Override
    public String getCaraPenggunaan() {
        return "Buah " + nama + " dapat dikonsumsi langsung saat matang, " +
                "atau diolah menjadi jus dan ekstrak untuk penggunaan obat.";
    }
}