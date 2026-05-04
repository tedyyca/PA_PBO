package com.toga.model;

import java.time.LocalDate;

public class CatatanPerawatan extends Catatan {
    private LocalDate tanggal;

    public CatatanPerawatan(int tanamanId, int penggunaId, String namaTanaman, String namaPengguna, String keterangan, LocalDate tanggal) {
        super(tanamanId, penggunaId, namaTanaman, namaPengguna, keterangan);
        this.tanggal = tanggal;
    }

    public LocalDate getTanggal() { return tanggal; }
    public void setTanggal(LocalDate tanggal) { this.tanggal = tanggal; }

    @Override
    public void tampilInfo() {
        super.tampilInfo();
        System.out.println("Tanggal   : " + tanggal);
    }
}
