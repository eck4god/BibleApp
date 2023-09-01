package main.java.Data;

import main.java.Service.ProgramDirectoryService;

import java.io.File;
import java.util.ArrayList;

public enum References {
    Placeholder,
    Strongs;

    public String toString() {
        switch (this) {
            case Placeholder -> {
                return "-- Select a Reference --";
            }
            case Strongs -> {
                return "Strongs Exhaustive Concordance";
            }
            default -> {
                return "Error";
            }
        }
    }

    public ArrayList<File> toFiles() {
        switch (this) {
            case Strongs -> {
                ProgramDirectoryService programDirectoryService = new ProgramDirectoryService();
                String path = programDirectoryService.getProgramDirectory();
                ArrayList<File> files = new ArrayList<>();
                files.add(new File(path + "/Resources/Reference/concordance_a.json"));
                files.add(new File(path + "/Resources/Reference/concordance_b.json"));
                files.add(new File(path + "/Resources/Reference/concordance_c.json"));
                files.add(new File(path + "/Resources/Reference/concordance_d.json"));
                files.add(new File(path + "/Resources/Reference/concordance_e.json"));
                files.add(new File(path + "/Resources/Reference/concordance_f.json"));
                files.add(new File(path + "/Resources/Reference/concordance_g.json"));
                files.add(new File(path + "/Resources/Reference/concordance_h.json"));
                files.add(new File(path + "/Resources/Reference/concordance_i.json"));
                files.add(new File(path + "/Resources/Reference/concordance_j.json"));
                files.add(new File(path + "/Resources/Reference/concordance_k.json"));
                files.add(new File(path + "/Resources/Reference/concordance_l.json"));
                files.add(new File(path + "/Resources/Reference/concordance_m.json"));
                files.add(new File(path + "/Resources/Reference/concordance_n.json"));
                files.add(new File(path + "/Resources/Reference/concordance_o.json"));
                files.add(new File(path + "/Resources/Reference/concordance_p.json"));
                files.add(new File(path + "/Resources/Reference/concordance_q.json"));
                files.add(new File(path + "/Resources/Reference/concordance_r.json"));
                files.add(new File(path + "/Resources/Reference/concordance_s.json"));
                files.add(new File(path + "/Resources/Reference/concordance_t.json"));
                files.add(new File(path + "/Resources/Reference/concordance_u.json"));
                files.add(new File(path + "/Resources/Reference/concordance_v.json"));
                files.add(new File(path + "/Resources/Reference/concordance_w.json"));
                files.add(new File(path + "/Resources/Reference/concordance_y.json"));
                files.add(new File(path + "/Resources/Reference/concordance_z.json"));

                return files;
            }
            default -> {
                return new ArrayList<>();
            }
        }
    }
}
