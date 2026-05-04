package com.toga.model;

import org.apache.commons.lang3.StringUtils;
import java.time.LocalDate;

public class TanamanRempah extends Tanaman implements ITanamanObat, IPerawatan {
    private String aroma;

    public TanamanRempah(String nama, String namaLatin, String manfaat, String aroma, StatusTanaman status, LocalDate tanggalTanam) {
        super(nama, namaLatin, manfaat, status, tanggalTanam);
        this.aroma = aroma;
    }

    public String getAroma() { return aroma; }
    public boolean setAroma(String aroma) {
        if (StringUtils.isBlank(aroma)) return false;
        this.aroma = aroma;
        return true;
    }

    @Override
    public String getJenis() { return "Tanaman Rempah"; }

    @Override
    public String getPropertiTambahan() { return aroma; }

    @Override
    String getInfoSingkat() { return nama + " (" + getNamaLatin() + ") - Rempah"; }

    @Override
    public int estimasiHariPanen() { return 240; }

    @Override
    public void tampilInfo() {
        super.tampilInfo();
        System.out.println("Jenis      : Tanaman Rempah");
        System.out.println("Aroma      : " + aroma);
    }

    @Override
    public String getDeskripsiObat() {
        return "Bagian rimpang " + nama + " dengan aroma " + aroma +
                " digunakan sebagai bahan obat tradisional.";
    }

    @Override
    public String getCaraPenggunaan() {
        return "Rimpang " + nama + " dapat direbus dan diminum air rebusannya, " +
                "atau ditumbuk dan dioleskan pada bagian yang sakit.";
    }

    @Override
    public String getJadwalSiram() { return "2 hari sekali"; }

    @Override
    public String getCahayaYangDibutuhkan() { return "Sinar matahari penuh"; }
}
