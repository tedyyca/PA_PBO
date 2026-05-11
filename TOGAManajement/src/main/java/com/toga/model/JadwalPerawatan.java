package com.toga.model;

import org.apache.commons.lang3.StringUtils;
import java.time.LocalDate;

public class JadwalPerawatan {
    private int id;
    private int tanamanId;
    private String namaTanaman;
    private String jenisPerawatan;
    private LocalDate tanggal;
    private boolean sudahDilakukan;

    public JadwalPerawatan(int tanamanId, String namaTanaman, String jenisPerawatan,
                           LocalDate tanggal, boolean sudahDilakukan) {
        this.tanamanId      = tanamanId;
        this.namaTanaman    = namaTanaman;
        this.jenisPerawatan = jenisPerawatan;
        this.tanggal        = tanggal;
        this.sudahDilakukan = sudahDilakukan;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getTanamanId() { return tanamanId; }
    public String getNamaTanaman() { return namaTanaman; }

    public String getJenisPerawatan() { return jenisPerawatan; }
    public boolean setJenisPerawatan(String jenisPerawatan) {
        if (StringUtils.isBlank(jenisPerawatan)) return false;
        this.jenisPerawatan = jenisPerawatan;
        return true;
    }

    public LocalDate getTanggal() { return tanggal; }
    public boolean setTanggal(LocalDate tanggal) {
        if (tanggal == null) return false;
        this.tanggal = tanggal;
        return true;
    }

    public boolean isSudahDilakukan() { return sudahDilakukan; }
    public void setSudahDilakukan(boolean sudahDilakukan) {
        this.sudahDilakukan = sudahDilakukan;
    }

    public String getStatusLabel() { return sudahDilakukan ? "Selesai" : "Belum"; }
}