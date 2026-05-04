package com.toga.model;

import org.apache.commons.lang3.StringUtils;
import java.time.LocalDate;

public class CatatanPanen extends Catatan {
    private LocalDate tanggalPanen;
    private String hasilPanen;

    public CatatanPanen(int tanamanId, int penggunaId, String namaTanaman, String namaPengguna, String keterangan, LocalDate tanggalPanen, String hasilPanen) {
        super(tanamanId, penggunaId, namaTanaman, namaPengguna, keterangan);
        this.tanggalPanen = tanggalPanen;
        this.hasilPanen = hasilPanen;
    }

    public LocalDate getTanggalPanen() { return tanggalPanen; }
    public void setTanggalPanen(LocalDate tanggalPanen) { this.tanggalPanen = tanggalPanen; }

    public String getHasilPanen() { return hasilPanen; }
    public boolean setHasilPanen(String hasilPanen) {
        if (StringUtils.isBlank(hasilPanen)) return false;
        this.hasilPanen = hasilPanen;
        return true;
    }

    @Override
    public void tampilInfo() {
        super.tampilInfo();
        System.out.println("Tgl Panen  : " + tanggalPanen);
        System.out.println("Hasil Panen: " + hasilPanen);
    }
}
