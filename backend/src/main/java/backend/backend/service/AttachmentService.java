package backend.backend.service;

import backend.backend.dto.response.AttachmentResponse;
import backend.backend.entity.TicketAttachment;
import backend.backend.exception.BadRequestException;
import backend.backend.repository.TicketAttachmentRepository;
import backend.backend.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.util.Objects;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttachmentService {

    @Value("${app.upload.dir}")
    private String uploadDir;

    private final TicketAttachmentRepository attachmentRepository;
    private final TicketRepository ticketRepository;

    private static final int MAX_ATTACHMENTS = 3;
    private static final List<String> ALLOWED_TYPES = List.of("image/jpeg", "image/png", "image/webp");

    @Transactional
    public AttachmentResponse uploadAttachment(Long ticketId, MultipartFile file, Long uploaderId) {
        Objects.requireNonNull(ticketId, "Ticket ID must not be null");
        Objects.requireNonNull(uploaderId, "Uploader ID must not be null");

        if (!ticketRepository.existsById(ticketId)) {
            throw new BadRequestException("Ticket not found");
        }

        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new BadRequestException("Only JPEG, PNG, and WEBP images are allowed");
        }

        long existingCount = attachmentRepository.countByTicketId(ticketId);
        if (existingCount >= MAX_ATTACHMENTS) {
            throw new BadRequestException("Maximum 3 attachments allowed per ticket");
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null) {
            throw new BadRequestException("Invalid file: original filename is missing");
        }
        String originalFileName = StringUtils.cleanPath(originalName);
        String extension = originalFileName.substring(originalFileName.lastIndexOf(".") + 1);
        String uniqueName = UUID.randomUUID().toString() + "." + extension;
        Path targetPath = Paths.get(uploadDir, ticketId.toString(), uniqueName);

        try {
            Files.createDirectories(targetPath.getParent());
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save file: " + e.getMessage());
        }

        TicketAttachment attachment = new TicketAttachment();
        attachment.setTicket(ticketRepository.getReferenceById(ticketId));
        attachment.setFileName(originalFileName);
        attachment.setFileType(file.getContentType());
        attachment.setFilePath(targetPath.toString());
        attachment.setUploadedBy(uploaderId);

        return mapToResponse(attachmentRepository.save(attachment));
    }

    private AttachmentResponse mapToResponse(TicketAttachment attachment) {
        AttachmentResponse response = new AttachmentResponse();
        response.setId(attachment.getId());
        response.setFileName(attachment.getFileName());
        response.setFileType(attachment.getFileType());
        response.setFilePath(attachment.getFilePath());
        response.setUploadedAt(attachment.getUploadedAt());
        return response;
    }
}
