package com.cni.tutspringboot2.image;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ImageService {

    private static String UPLOAD_ROOT = "C:\\Users\\Calin\\Documents\\TEST\\upload-dir";

    private final ResourceLoader resourceLoader;
    private final ImageRepository imageRepository;

    public ImageService(final ResourceLoader resourceLoader, final ImageRepository imageRepository) {
        this.resourceLoader = Objects.requireNonNull(resourceLoader, "resourceLoader must not be null");
        this.imageRepository = Objects.requireNonNull(imageRepository, "imageRepository must not be null");
    }

    public Flux<Image> findAllImages() {
        return imageRepository.findAll();
    }

    public Mono<Resource> findOneImage(String filename) {
        return Mono.fromSupplier(() -> resourceLoader.getResource("file:" + UPLOAD_ROOT + "/" + filename));
    }

    public Mono<Void> createImage(Flux<FilePart> files) {
        return files.flatMap(file -> {
            Mono<Image> saveDatabaseImage = imageRepository.save(new Image(UUID.randomUUID().toString(), file.filename()));
            Mono<Void> copyFile = copyImageFile(file);

            return Mono.when(saveDatabaseImage, copyFile);
        }).then();
    }

    private Mono<Void> copyImageFile(final FilePart file) {
        return Mono.just(Paths.get(UPLOAD_ROOT, file.filename()).toFile())
                        .log("createImage-picktarget")
                        .map(destFile -> {
                            try {
                                destFile.createNewFile();
                                return destFile;
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .log("createImage-newfile")
                        .flatMap(file::transferTo)
                        .log("createImage-copy");
    }

    public Mono<Void> deleteImage(String filename) {
        Mono<Void> deleteDatabaseImage = imageRepository
                .findByName(filename)
                .flatMap(imageRepository::delete);

        Mono<Void> deleteFile = Mono.fromRunnable(() -> {
            try {
                Files.deleteIfExists(Paths.get(UPLOAD_ROOT, filename));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        return Mono.when(deleteDatabaseImage, deleteFile).then();
    }

    /**
     * Pre-load some test images     *     * @return Spring Boot {@link CommandLineRunner} automatically     *         run after app context is loaded.
     */
    @Bean
    CommandLineRunner setUp() throws IOException {
        return (args) -> {
            FileSystemUtils.deleteRecursively(new File(UPLOAD_ROOT));
            Files.createDirectory(Paths.get(UPLOAD_ROOT));
            FileCopyUtils.copy("Test file", new FileWriter(UPLOAD_ROOT + "/learning-spring-boot-cover.jpg"));
            FileCopyUtils.copy("Test file2", new FileWriter(UPLOAD_ROOT + "/learning-spring-boot-2nd-edition-cover.jpg"));
            FileCopyUtils.copy("Test file3", new FileWriter(UPLOAD_ROOT + "/bazinga.png"));
        };
    }
}
