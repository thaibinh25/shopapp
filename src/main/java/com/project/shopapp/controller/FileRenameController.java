package com.project.shopapp.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class FileRenameController {

    @GetMapping("/rename-files")
    public ResponseEntity<?> renameImageFiles() {
       // String folderPath = "uploads"; // hoặc đường dẫn tuyệt đối nếu cần
        String folderPath = System.getProperty("user.dir") + File.separator + "uploads";
        File folder = new File(folderPath);
        if (!folder.exists() || !folder.isDirectory()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("❌ Folder not found");
        }

        File[] files = folder.listFiles();
        List<String> logs = new ArrayList<>();

        for (File file : files) {
            if (file.isFile() && file.getName().contains(" ")) {
                String oldName = file.getName();
                String newName = oldName.replace(" ", "_");
                File newFile = new File(folder, newName);

                if (newFile.exists()) {
                    logs.add("⚠️ Bỏ qua: " + newName + " đã tồn tại");
                    continue;
                }

                boolean renamed = file.renameTo(newFile);
                if (renamed) {
                    logs.add("✅ Đổi tên: " + oldName + " → " + newName);
                } else {
                    logs.add("❌ Lỗi khi đổi tên: " + oldName);
                }
            }
        }

        return ResponseEntity.ok(logs);
    }
}