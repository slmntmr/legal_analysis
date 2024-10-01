package com.karataspartners.legal_analysis.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WebsiteCrawlerService {

    private final RestTemplate restTemplate = new RestTemplate();

    // JSON verisi çeken servis
    public String crawlWebsite(String url) {
        // Verilen URL'e GET isteği yapıyoruz ve JSON verisini alıyoruz
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        return response.getBody();  // JSON verisini String olarak döndürüyoruz
    }

    // Hukuki terimlerin olup olmadığını kontrol eden fonksiyon
    public boolean checkForLegalTerms(String url, List<String> legalTerms) {
        String content = crawlWebsite(url).toLowerCase();
        for (String term : legalTerms) {
            if (content.contains(term.toLowerCase())) {
                return true; // Terim bulundu
            }
        }
        return false; // Terim bulunamadı
    }

    // Eksik terimleri bulan fonksiyon
    // Eksik hukuki terimleri bulan fonksiyon
    public List<String> findMissingLegalTerms(String url, List<String> legalTerms) throws IOException {
        String content = crawlWebsite(url).toLowerCase();  // Sayfanın içeriğini alıyoruz ve küçük harfe çeviriyoruz
        // İçerikte bulunmayan terimleri filtreliyoruz
        return legalTerms.stream()
                .filter(term -> !content.contains(term.toLowerCase()))
                .collect(Collectors.toList());  // Eksik terimleri döndürüyoruz
    }
}
