package com.example.ruiji.controller;


import com.example.ruiji.common.Res;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

@RestController
@RequestMapping("/common")
public class CommonController {

    @Value("${ruiji.path}")
    private String basePath;

    @PostMapping("/upload")
    public Res<String> upload(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        UUID uuid = UUID.randomUUID();
        File dir = new File(basePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try {
            if (originalFilename != null) {
                file.transferTo(new File(basePath + uuid + originalFilename.substring(originalFilename.lastIndexOf("."))));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Res.success(file.getOriginalFilename());
    }

    @GetMapping("/download")
    public void download(String name, HttpServletResponse response) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        File file = new File(basePath + name);
        if (!file.exists() || !file.isFile()) {
            throw new IllegalArgumentException("文件不存在");
        }

        response.setContentType("image/jpeg");
        try (InputStream inputStream = new FileInputStream(file); OutputStream outputStream = response.getOutputStream()) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
            outputStream.flush();
        } catch (IOException e) {
            throw new RuntimeException("文件下载失败", e);
        }
    }


}
