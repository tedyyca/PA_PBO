package com.toga.model;

import org.apache.commons.lang3.StringUtils;

public class Catatan {
    private int id;
    private int tanamanId;
    private int penggunaId;
    private String namaTanaman;
    private String namaPengguna;
    private String keterangan;

    public Catatan(int tanamanId, int penggunaId, String namaTanaman, String namaPengguna, String keterangan) {
        this.tanamanId = tanamanId;
        this.penggunaId = penggunaId;
        this.namaTanaman = namaTanaman;
        this.namaPengguna = namaPengguna;
        this.keterangan = keterangan;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getTanamanId() { return tanamanId; }
    public int getPenggunaId() { return penggunaId; }
    public String getNamaTanaman() { return namaTanaman; }
    public String getNamaPengguna() { return namaPengguna; }

    public String getKeterangan() { return keterangan; }
    public boolean setKeterangan(String keterangan) {
        if (StringUtils.isBlank(keterangan)) return false;
        this.keterangan = keterangan;
        return true;
    }

    public void tampilInfo() {
        System.out.println("Tanaman   : " + namaTanaman);
        System.out.println("Pengguna  : " + namaPengguna);
        System.out.println("Keterangan: " + keterangan);
    }
}