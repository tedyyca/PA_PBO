package com.toga.service.impl;

import com.toga.dto.TanamanDTO;
import com.toga.model.ITanamanObat;
import com.toga.model.StatusTanaman;
import com.toga.model.Tanaman;
import com.toga.model.TanamanBuah;
import com.toga.model.TanamanDaun;
import com.toga.model.TanamanRempah;
import com.toga.repository.TanamanRepository;
import com.toga.service.TanamanService;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TanamanServiceImpl implements TanamanService {
    private final TanamanRepository tanamanRepository;
    private static final String REGEX_HURUF_SPASI = "[a-zA-Z ]+";

    public TanamanServiceImpl(TanamanRepository tanamanRepository) {
        this.tanamanRepository = tanamanRepository;
    }

    @Override
    public List<TanamanDTO> getAllTanaman() {
        List<Tanaman> list = tanamanRepository.findAll();
        List<TanamanDTO> result = new ArrayList<>();
        for (Tanaman t : list) result.add(toDTO(t));
        return result;
    }

    @Override
    public void tambahTanaman(TanamanDTO dto) {
        validasiDTO(dto);
        if (tanamanRepository.isDuplikat(-1, dto.getNama(), dto.getNamaLatin(), dto.getManfaat(), dto.getJenis(), dto.getPropertiTambahan(), dto.getTanggalTanam(), dto.getEstimasiHari())) {
            throw new IllegalArgumentException("Data tanaman sudah ada! Tidak dapat menambahkan duplikat.");
        }
        StatusTanaman status = Tanaman.hitungStatus(dto.getTanggalTanam(), dto.getEstimasiHari());
        dto.setStatus(status.name());
        tanamanRepository.save(buildTanaman(dto));
    }

    @Override
    public void ubahTanaman(TanamanDTO dto) {
        validasiDTO(dto);
        Tanaman existing = tanamanRepository.findById(dto.getId());
        if (existing == null) {
            throw new IllegalArgumentException("Tanaman tidak ditemukan!");
        }

        boolean isTidakAdaPerubahan = existing.getNama().equals(dto.getNama()) &&
                existing.getNamaLatin().equals(dto.getNamaLatin()) &&
                existing.getManfaat().equals(dto.getManfaat()) &&
                existing.getJenis().equals(dto.getJenis()) &&
                existing.getPropertiTambahan().equals(dto.getPropertiTambahan()) &&
                existing.getTanggalTanam().equals(dto.getTanggalTanam()) &&
                existing.getEstimasiHari() == dto.getEstimasiHari();

        if (isTidakAdaPerubahan) {
            throw new IllegalArgumentException("Tidak ada perubahan data.");
        }

        if (tanamanRepository.isDuplikat(dto.getId(), dto.getNama(), dto.getNamaLatin(), dto.getManfaat(), dto.getJenis(), dto.getPropertiTambahan(), dto.getTanggalTanam(), dto.getEstimasiHari())) {
            throw new IllegalArgumentException("Data tanaman sudah ada! Tidak dapat menyimpan duplikat.");
        }

        existing.setNama(dto.getNama());
        existing.setNamaLatin(dto.getNamaLatin());
        existing.setManfaat(dto.getManfaat());
        existing.setTanggalTanam(dto.getTanggalTanam());
        existing.setEstimasiHari(dto.getEstimasiHari());

        String propertiBaru = dto.getPropertiTambahan();
        if (existing instanceof TanamanRempah && propertiBaru != null) {
            ((TanamanRempah) existing).setAroma(propertiBaru);
        } else if (existing instanceof TanamanDaun && propertiBaru != null) {
            ((TanamanDaun) existing).setBentukDaun(propertiBaru);
        } else if (existing instanceof TanamanBuah && propertiBaru != null) {
            ((TanamanBuah) existing).setMusimBerbuah(propertiBaru);
        }

        String statusSaatIni = tanamanRepository.findStatusById(dto.getId());
        StatusTanaman statusBaru = "SUDAH_DIPANEN".equals(statusSaatIni) ? StatusTanaman.SUDAH_DIPANEN : Tanaman.hitungStatus(existing.getTanggalTanam(), existing.getEstimasiHari());
        existing.setStatus(statusBaru);

        tanamanRepository.update(existing);
    }

    @Override
    public void hapusTanaman(int id) {
        tanamanRepository.delete(id);
    }

    @Override
    public String getInfoObat(TanamanDTO dto) {
        Tanaman t = buildTanaman(dto);
        if (t instanceof ITanamanObat obat) {
            return "Deskripsi  : " + obat.getDeskripsiObat() + "\n\nCara Pakai : " + obat.getCaraPenggunaan();
        }
        return "Tanaman ini tidak memiliki info obat.";
    }

    @Override
    public String getEstimasiPanen(TanamanDTO dto) {
        if (dto.getTanggalTanam() == null) throw new IllegalArgumentException("Tanggal tanam harus diisi!");
        if (dto.getTanggalTanam().isAfter(LocalDate.now())) throw new IllegalArgumentException("Tanggal tanam tidak boleh lebih dari hari ini!");
        Tanaman t = buildTanaman(dto);
        int sisa = t.getSisaHariPanen();
        int bln = sisa / 30;
        int hr = sisa % 30;
        if (sisa == 0) return dto.getNama() + " sudah melewati estimasi waktu panen!";
        return "Estimasi sisa waktu panen " + dto.getNama() + ":\n" + sisa + " hari lagi (" + bln + " bulan " + hr + " hari)";
    }

    private void validasiDTO(TanamanDTO dto) {
        if (dto.getNama() == null || dto.getNama().isBlank()) throw new IllegalArgumentException("Nama tanaman harus diisi!");
        if (dto.getNamaLatin() == null || dto.getNamaLatin().isBlank()) throw new IllegalArgumentException("Nama Latin harus diisi!");
        if (dto.getManfaat() == null || dto.getManfaat().isBlank()) throw new IllegalArgumentException("Manfaat harus diisi!");
        if (dto.getPropertiTambahan() == null || dto.getPropertiTambahan().isBlank()) throw new IllegalArgumentException("Properti tambahan harus diisi!");
        if (dto.getTanggalTanam() == null) throw new IllegalArgumentException("Tanggal tanam harus diisi!");
        if (dto.getEstimasiHari() <= 0) throw new IllegalArgumentException("Estimasi hari panen harus lebih dari 0!");
        if (!dto.getNama().matches(REGEX_HURUF_SPASI)) throw new IllegalArgumentException("Nama tanaman hanya boleh berisi huruf dan spasi!");
        if (!dto.getNamaLatin().matches(REGEX_HURUF_SPASI)) throw new IllegalArgumentException("Nama Latin hanya boleh berisi huruf dan spasi!");
        if (!dto.getPropertiTambahan().matches(REGEX_HURUF_SPASI)) throw new IllegalArgumentException("Properti tambahan hanya boleh berisi huruf dan spasi!");
    }

    private Tanaman buildTanaman(TanamanDTO dto) {
        StatusTanaman st = dto.getStatus() != null ? StatusTanaman.valueOf(dto.getStatus()) : StatusTanaman.BIBIT;
        Tanaman t = switch (dto.getJenis()) {
            case "Tanaman Rempah" -> new TanamanRempah(dto.getNama(), dto.getNamaLatin(), dto.getManfaat(), dto.getPropertiTambahan(), st, dto.getTanggalTanam(), dto.getEstimasiHari());
            case "Tanaman Daun" -> new TanamanDaun(dto.getNama(), dto.getNamaLatin(), dto.getManfaat(), dto.getPropertiTambahan(), st, dto.getTanggalTanam(), dto.getEstimasiHari());
            default -> new TanamanBuah(dto.getNama(), dto.getNamaLatin(), dto.getManfaat(), dto.getPropertiTambahan(), st, dto.getTanggalTanam(), dto.getEstimasiHari());
        };
        t.setId(dto.getId());
        return t;
    }

    private TanamanDTO toDTO(Tanaman t) {
        return new TanamanDTO(t.getId(), t.getNama(), t.getNamaLatin(), t.getManfaat(), t.getJenis(), t.getPropertiTambahan(), t.getStatus().name(), t.getTanggalTanam(), t.getEstimasiHari());
    }
}