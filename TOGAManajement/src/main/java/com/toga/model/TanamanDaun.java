package com.toga.model;

import org.apache.commons.lang3.StringUtils;
import java.time.LocalDate;

public class TanamanDaun extends Tanaman implements ITanamanObat, IPerawatan {
    private String bentukDaun;

    public TanamanDaun(String nama, String namaLatin, String manfaat, String bentukDaun, StatusTanaman status, LocalDate tanggalTanam) {
        super(nama, namaLatin, manfaat, status, tanggalTanam);
        this.bentukDaun = bentukDaun;
    }

    public String getBentukDaun() { return bentukDaun; }
    public boolean setBentukDaun(String bentukDaun) {
        if (StringUtils.isBlank(bentukDaun)) return false;
        this.bentukDaun = bentukDaun;
        return true;
    }

    @Override
    public String getJenis() { return "Tanaman Daun"; }

    @Override
    public String getPropertiTambahan() { return bentukDaun; }

    @Override
    String getInfoSingkat() { return nama + " (" + getNamaLatin() + ") - Daun"; }

    @Override
    public int estimasiHariPanen() { return 60; }

    @Override
    public void tampilInfo() {
        super.tampilInfo();
        System.out.println("Jenis       : Tanaman Daun");
        System.out.println("Bentuk Daun : " + bentukDaun);
    }

    @Override
    public String getDeskripsiObat() {
        return "Daun " + nama + " dengan bentuk " + bentukDaun +
                " digunakan sebagai bahan obat tradisional.";
    }

    @Override
    public String getCaraPenggunaan() {
        return "Daun " + nama + " dapat direbus untuk diminum air rebusannya, " +
                "atau ditumbuk halus dan ditempelkan langsung pada kulit.";
    }

    @Override
    public String getJadwalSiram() { return "Setiap hari"; }

    @Override
    public String getCahayaYangDibutuhkan() { return "Teduh hingga semi teduh"; }
}
