package com.karataspartners.legal_analysis.controller;

import com.karataspartners.legal_analysis.service.WebsiteCrawlerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import java.net.URL;
import java.util.*;

@RestController
@RequestMapping("/api/crawler")  // Base path olarak api/crawler kullanıyoruz
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")  // Frontend portu
public class WebsiteCrawlerController {

    private final WebsiteCrawlerService websiteCrawlerService;

    // URL geçerliliğini kontrol eden fonksiyon
    private boolean isValidURL(String url) {
        try {
            new URL(url).toURI();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Web sitesini tarayan endpoint
    // URL: /api/crawler/crawl?url={your-url}
    @GetMapping("/crawl")
    public ResponseEntity<String> crawlWebsite(@RequestParam String url) {
        if (!isValidURL(url)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Geçersiz URL formatı.");
        }

        try {
            String content = websiteCrawlerService.crawlWebsite(url);
            // İçeriğin JSON olup olmadığını kontrol ediyoruz
            if (content.trim().startsWith("{") || content.trim().startsWith("[")) {
                // JSON verisi ise, content-type ayarlıyoruz
                return ResponseEntity.ok()
                        .header("Content-Type", "application/json")
                        .body(content);
            } else {
                // Değilse, düz metin olarak döndürüyoruz
                return ResponseEntity.ok(content);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Bir hata oluştu: " + e.getMessage());
        }
    }

    // Hukuki terimlerin olup olmadığını kontrol eden endpoint
    @GetMapping("/check-legal-terms")
    public ResponseEntity<String> checkLegalTerms(@RequestParam String url) {
        List<String> legalTerms = Arrays.asList("Gizlilik Politikası", "Çerez Politikası", "Kullanım Koşulları", "Kişisel Veri", "Veri Sorumlusu");

        if (!isValidURL(url)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Geçersiz URL formatı.");
        }

        try {
            boolean hasLegalTerms = websiteCrawlerService.checkForLegalTerms(url, legalTerms);
            if (hasLegalTerms) {
                return ResponseEntity.ok("Web sayfası gerekli hukuki terimleri içeriyor.");
            } else {
                return ResponseEntity.ok("Web sayfasında gerekli hukuki terimler bulunamadı.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Bir hata oluştu: " + e.getMessage());
        }
    }



    @GetMapping("/report-missing-terms")
    public ResponseEntity<?> reportMissingTerms(@RequestParam String url) {
        List<String> legalTerms = Arrays.asList("Gizlilik Politikası", "Çerez Politikası", "Kullanım Koşulları", "Kişisel Veri", "Veri Sorumlusu");

        if (!isValidURL(url)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Geçersiz URL formatı.");
        }

        try {
            // Web sitesini tarıyoruz
            String content = websiteCrawlerService.crawlWebsite(url);

            // Eksik terimleri buluyoruz
            List<String> missingTerms = websiteCrawlerService.findMissingLegalTerms(url, legalTerms);
            if (missingTerms.isEmpty()) {
                return ResponseEntity.ok(Collections.singletonMap("message", "Web sayfası tüm gerekli hukuki terimleri içeriyor."));
            } else {
                // Eksik terimleri JSON formatında döndürüyoruz
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Eksik terimler bulundu.");
                response.put("missingTerms", missingTerms);
                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            // Hata mesajını JSON formatında döndürüyoruz
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "Bir hata oluştu: " + e.getMessage()));
        }
    }


    // Eksik terimleri düzelten öneriler sunan endpoint
    @GetMapping("/fix-missing-terms")
    public ResponseEntity<String> fixMissingTerms(@RequestParam String url) {
        List<String> legalTerms = Arrays.asList("Gizlilik Politikası", "Çerez Politikası", "Kullanım Koşulları", "Kişisel Veri", "Veri Sorumlusu");

        if (!isValidURL(url)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Geçersiz URL formatı.");
        }

        try {
            List<String> missingTerms = websiteCrawlerService.findMissingLegalTerms(url, legalTerms);
            if (missingTerms.isEmpty()) {
                return ResponseEntity.ok("Web sayfası tüm gerekli hukuki terimleri içeriyor.");
            } else {
                StringBuilder suggestions = new StringBuilder("Eksik terimler ve öneriler:\n");
                for (String term : missingTerms) {
                    if (term.equals("Gizlilik Politikası")) {
                        suggestions.append(term).append(": Gizlilik politikanızı eklemek için şu rehberi takip edin: [URL]\n");
                    } else if (term.equals("Çerez Politikası")) {
                        suggestions.append(term).append(": Çerez politikanızı oluşturmak için buradan destek alın: [URL]\n");
                    } else {
                        suggestions.append(term).append(": Bu belgenin eksikliğini gidermek için bir hukuk danışmanına başvurun.\n");
                    }
                }
                return ResponseEntity.ok(suggestions.toString());
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Bir hata oluştu: " + e.getMessage());
        }
    }
}
